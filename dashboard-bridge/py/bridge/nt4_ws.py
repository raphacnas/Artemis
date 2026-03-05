import asyncio
import json
import time
import websockets
from ntcore import NetworkTableInstance

clients = set()
ntinst = NetworkTableInstance.getDefault()
PULSE_TIME = 0.2

# =========================
# SUAS TABLES[]
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
    "Modes": [
        "AimLockLime4",
        "AimLockLime2",
        "AlignLime2"
    ]
}

# =========================
# NT4 INIT
# =========================

def init_nt(server_ip: str):
    ntinst.startClient4("dashboard-bridge")
    ntinst.setServer(server_ip)

    print(f"🔗 Conectando NT4 -> {server_ip}")

    for _ in range(20):
        if ntinst.isConnected():
            print("✅ NT4 conectado!")
            return
        time.sleep(0.5)

    print("❌ NT4 não conectou")


def get_table(name):
    return ntinst.getTable(name)


def read_value(table, key):
    entry = table.getEntry(key)
    if entry.exists():
        v = entry.getValue()
        if v:
            return v.value()
    return None


def ensure_entry_exists(table, key):
    entry = table.getEntry(key)
    if not entry.exists():
        # força criação com tipo double padrão
        entry.setDouble(0.0)


def write_value(table, key, value):
    entry = table.getEntry(key)

    if isinstance(value, bool):
        entry.setBoolean(value)
    elif isinstance(value, (int, float)):
        entry.setDouble(float(value))
    elif isinstance(value, list):
        entry.setDoubleArray(value)
    else:
        entry.setString(str(value))


async def pulse_button(table, key):
    entry = table.getEntry(key)
    entry.setBoolean(True)
    await asyncio.sleep(PULSE_TIME)
    entry.setBoolean(False)


# =========================
# NT MONITOR
# =========================

async def nt_monitor():
    print("📡 Monitor NT iniciado")

    while True:
        if not ntinst.isConnected():
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
                        except:
                            dead.append(ws)

                    for ws in dead:
                        clients.discard(ws)

        await asyncio.sleep(0.1)


# =========================
# WEBSOCKET
# =========================

async def handle_ws(ws):
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

    except:
        pass
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
        max_size=None
    ):
        print(f"🚀 WebSocket em ws://0.0.0.0:{port}")
        await asyncio.Future()

    print("RobotStress:", ntinst.getTable("RobotStress").getKeys())
    print("SmartDashboard:", ntinst.getTable("SmartDashboard").getKeys())
