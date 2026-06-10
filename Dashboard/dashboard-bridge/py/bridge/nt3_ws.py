import asyncio
import json
from pathlib import Path
import time
import websockets
from networktables import NetworkTables

clients = set()
PULSE_TIME = 0.2
CONFIG_PATH = Path(__file__).resolve().parents[3] / "dashboard-web" / "config" / "dashboard.json"

def load_config():
    with CONFIG_PATH.open("r", encoding="utf-8") as f:
        return json.load(f)


CONFIG = load_config()
NT_CONFIG = CONFIG["networkTables"]

# =========================
# NT3 INIT
# =========================

def init_nt(server_ip: str):
    # CORRECAO: so inicializa se ainda nao estiver conectado,
    # evitando conflito com a inicializacao do AI_Data.py
    if NetworkTables.isConnected():
        print(f"🔗 NT3 -> {server_ip} (ja conectado, reaproveitando)")
        return

    NetworkTables.initialize(server=server_ip)
    print(f"🔗 NT3 -> {server_ip} (aguardando...)")

def get_table(name):
    return NetworkTables.getTable(name)


def read_value(table, key):
    keys = table.getKeys()
    if key not in keys:
        return None

    val = table.getValue(key, None)
    return val


def ensure_entry_exists(table, key, spec):
    if key not in table.getKeys():
        value_type = spec.get("type", "number")
        default = spec.get("default")

        if value_type == "numberArray":
            table.putNumberArray(key, default or [])
        elif value_type == "boolean":
            table.putBoolean(key, bool(default))
        elif value_type == "string":
            table.putString(key, "" if default is None else str(default))
        else:
            table.putNumber(key, float(default or 0.0))

def write_value(table, key, value):
    if isinstance(value, bool):
        table.putBoolean(key, value)
    elif isinstance(value, (int, float)):
        table.putNumber(key, value)
    elif isinstance(value, list):
        table.putNumberArray(key, value)
    else:
        table.putString(key, str(value))


async def pulse_button(table, key):
    table.putBoolean(key, True)
    await asyncio.sleep(PULSE_TIME)
    table.putBoolean(key, False)


# =========================
# NT MONITOR
# =========================

async def nt_monitor():
    print("📡 Monitor NT3 iniciado")

    while True:
        if not NetworkTables.isConnected():
            print("⚠️ NT desconectado — aguardando reconexão")
            await asyncio.sleep(2)
            continue

        for table_name, keys in NT_CONFIG.items():
            table = get_table(table_name)

            for key, spec in keys.items():
                ensure_entry_exists(table, key, spec)
                value = read_value(table, key)

                if value is not None:
                    message = json.dumps({
                        "topic": f"/{table_name}/{key}",
                        "value": value
                    })

                    dead = []
                    for ws in clients:
                        try:
                            await ws.send(message)
                        except Exception as e:
                            print(f"⚠️ Erro ao enviar para cliente: {type(e).__name__}: {e}")
                            dead.append(ws)

                    for ws in dead:
                        clients.discard(ws)

        await asyncio.sleep(0.1)

# =========================
# WEBSOCKET
# =========================

async def handle_ws(ws, path=None):
    clients.add(ws)
    print(f"✅ WS conectado ({len(clients)})")

    try:
        async for message in ws:
            obj = json.loads(message)

            action = obj.get("action")
            table_name = obj.get("table")
            key = obj.get("key")
            value = obj.get("value")

            table = get_table(table_name)

            if action == "press":
                asyncio.create_task(pulse_button(table, key))

            elif action == "put":
                write_value(table, key, value)

    except Exception as e:
        print(f"⚠️ WS handler erro: {type(e).__name__}: {e}")
    finally:
        clients.discard(ws)
        print(f"❌ WS desconectado ({len(clients)})")


# =========================
# ENTRY POINT
# =========================

async def main_async(server_ip: str, port: int):
    init_nt(server_ip)

    asyncio.create_task(nt_monitor())

    async with websockets.serve(
        handle_ws,
        "0.0.0.0",
        port,
        max_size=None,
        ping_interval=20,
        ping_timeout=30,
    ):
        print(f"🚀 WebSocket em ws://0.0.0.0:{port}")
        await asyncio.Future()
