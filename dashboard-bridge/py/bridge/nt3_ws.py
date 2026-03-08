import asyncio
import json
import time
import websockets
from networktables import NetworkTables

clients = set()
PULSE_TIME = 0.2

# =========================
# SUAS TABLES
# =========================

TABLES_AND_KEYS = {
    "RobotStress": [
        "batteryVoltage",
        "totalCurrent",
        "drivetrainCurrent",
        "stressScore",
        "stressLevel",
        "speedScale",
        "chassisSpeed"
    ],

    "ADL": [
        "state",
        "decision",
        "intent"
    ],

    "Vision": [
        "HasTarget",
        "Aligned",
        "Confidence"
    ],

    "Mechanisms": [
        "ShooterReady",
        "HasGamePiece",
        "IntakeActive",
        "ClimbAvailable"
    ],

    "Game": [
        "Endgame"
    ],

    "Drive": [
        "Moving"
    ],

    "Robot": [
        "BatteryVoltage"
    ],

    "Shooter": [
        "CurrentRPM",
        "TargetRPM"
    ],

    "StreamDeck/IntakeAngle": [
        "toggleCount",
        "calibrateZero",
        "calibrateTarget"
    ],
    "StreamDeck/IntakeRoller": [
        "intakeToggle",
        "outtakeToggle"
    ],
    "limelight-back": [
        "piece_tx",
        "ta",
        "piece_distance",
        "has_target",
        "bbox",
        "hw"
    ],
    "limelight-front": [
        "tx",
        "tv",
        "ta",
        "hw"
    ],
    "limelight-lime2plus": [
        "hw"
    ],
    "Modes": [
        "AimLockLime4",
        "AimLockLime2",
        "AlignLime2",
        "AimLockLime2Plus",
        "ShooterLime2Plus",
        "AlignPiece"
    ]
}

# =========================
# NT3 INIT
# =========================

def init_nt(server_ip: str):
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


def ensure_entry_exists(table, key):
    if key not in table.getKeys():
        table.putNumber(key, 0.0)


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

        for table_name, keys in TABLES_AND_KEYS.items():
            table = get_table(table_name)

            for key in keys:
                ensure_entry_exists(table, key)
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

# FIX: path=None aceita o path "/nt/dashboard" que o cliente JS envia.
# Sem isso, versões antigas do websockets passam o path como segundo argumento
# posicional e o handler lança TypeError silencioso — causando o loop de
# conectar/desconectar imediatamente.
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