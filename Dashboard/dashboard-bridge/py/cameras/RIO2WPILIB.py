import time
import json
import asyncio
import argparse
from typing import Optional, List

from networktables import NetworkTables

# WebSocket é opcional: só é usado se você rodar este arquivo como script
import websockets


# =========================
# CONFIG PADRAO
# =========================
DEFAULT_ROBORIO = "10.91.63.2"

# Tabela NT onde você quer publicar
DEFAULT_TABLE = "limelight-back"

# Chaves NT
KEY_TX = "piece_tx"
KEY_TA = "ta" 
KEY_DISTANCE = "piece_distance"
KEY_HAS_TARGET = "has_target"
KEY_BBOX = "bbox"  # number array [x1,y1,x2,y2]

# WS (ponte opcional pro browser)
WS_HOST = "0.0.0.0"
DEFAULT_WS_PORT = 5811
WS_PATH = "/nt/limelight"

POLL_INTERVAL = 0.05  # 50ms


# =========================
# ESTADO GLOBAL DO MODULO
# =========================
_initialized = False
_table = None
_roborio_server = DEFAULT_ROBORIO
_table_name = DEFAULT_TABLE

_clients = set()


# =========================
# NT INIT
# =========================
def init_nt(server: str = DEFAULT_ROBORIO, table_name: str = DEFAULT_TABLE, timeout_s: float = 5.0):
    """
    Inicializa NetworkTables uma unica vez e guarda a referencia da tabela.
    Chame isso UMA vez no inicio do seu programa (ou deixe a primeira funcao chamar).
    """
    global _initialized, _table, _roborio_server, _table_name

    if _initialized:
        return _table

    _roborio_server = server
    _table_name = table_name

    print(f"[RIO2WPILIB] Inicializando NetworkTables -> server={server}")
    NetworkTables.initialize(server=server)

    waited = 0.0
    while not NetworkTables.isConnected() and waited < timeout_s:
        time.sleep(0.1)
        waited += 0.1

    if NetworkTables.isConnected():
        print("[RIO2WPILIB] ✅ Conectado ao NetworkTables")
    else:
        print("[RIO2WPILIB] ⚠️ Nao confirmou conexao ainda (vai tentar continuar mesmo assim)")

    _table = NetworkTables.getTable(table_name)
    _initialized = True
    return _table


def _ensure():
    if not _initialized or _table is None:
        init_nt(_roborio_server, _table_name)


# =========================
# PUBLISH HELPERS
# =========================
def rio2wpi_tx(tx: float):
    _ensure()
    _table.putNumber(KEY_TX, float(tx))

def rio2wpi_ta(ta: float):
    _ensure()
    _table.putNumber(KEY_TA, float(ta))


def rio2wpi_distance(distancia: float):
    _ensure()
    _table.putNumber(KEY_DISTANCE, float(distancia))


def rio2wpi_has_target(has_target: bool):
    _ensure()
    _table.putBoolean(KEY_HAS_TARGET, bool(has_target))


def rio2wpi_bbox(bbox: List[float]):
    """
    bbox esperado: [x1, y1, x2, y2]
    """
    _ensure()
    # if bbox is None or len(bbox) != 4:
    #     return
    _table.putNumberArray(KEY_BBOX, [float(v) for v in bbox])


# =========================
# WS BRIDGE (OPCIONAL)
# =========================
def _read_any(table, key):
    """
    Le numero, booleano, string ou number array.
    Retorna valor python (float/bool/str/list) ou None.
    """
    # number
    try:
        v = table.getNumber(key, None)
        if v is not None:
            return v
    except:
        pass

    # boolean
    try:
        vb = table.getBoolean(key, None)
        if vb is not None:
            return bool(vb)
    except:
        pass

    # string
    try:
        vs = table.getString(key, None)
        if vs is not None:
            return vs
    except:
        pass

    # number array
    try:
        arr = table.getNumberArray(key, None)
        if arr is not None:
            return list(arr)
    except:
        pass

    return None


async def _poll_and_broadcast(keys):
    last_values = {}
    while True:
        _ensure()
        for key in keys:
            topic = f"/{_table_name}/{key}"
            val = _read_any(_table, key)

            if topic not in last_values or last_values[topic] != val:
                last_values[topic] = val
                msg = json.dumps({"topic": topic, "value": val})

                dead = []
                for ws in _clients:
                    try:
                        await ws.send(msg)
                    except:
                        dead.append(ws)
                for ws in dead:
                    _clients.discard(ws)

        await asyncio.sleep(POLL_INTERVAL)


async def _handle_ws(ws):
    # opcional: travar path
    if getattr(ws, "path", None) is not None and ws.path != WS_PATH:
        await ws.close()
        return

    print("[RIO2WPILIB] 🟢 Browser conectado:", ws.remote_address)
    _clients.add(ws)

    try:
        # snapshot inicial
        _ensure()
        for key in [KEY_TX, KEY_DISTANCE, KEY_HAS_TARGET, KEY_BBOX]:
            topic = f"/{_table_name}/{key}"
            val = _read_any(_table, key)
            await ws.send(json.dumps({"topic": topic, "value": val}))

        # loop: aceita PUT opcional (se você quiser controlar algo do browser)
        async for message in ws:
            obj = json.loads(message)
            if obj.get("action") == "put":
                _ensure()
                key = obj["key"]
                value = obj["value"]

                if isinstance(value, bool):
                    _table.putBoolean(key, value)
                elif isinstance(value, (int, float)):
                    _table.putNumber(key, float(value))
                elif isinstance(value, list):
                    # assume number array
                    _table.putNumberArray(key, [float(v) for v in value])
                else:
                    _table.putString(key, str(value))

                print("[RIO2WPILIB] 📡 PUT:", obj)

    except Exception as e:
        print("[RIO2WPILIB] WS erro:", e)
    finally:
        _clients.discard(ws)
        print("[RIO2WPILIB] 🔴 Browser desconectou")


async def run_ws_bridge(roborio: str, port: int):
    init_nt(roborio, DEFAULT_TABLE)

    keys = [KEY_TX, KEY_DISTANCE, KEY_HAS_TARGET, KEY_BBOX]

    server = await websockets.serve(_handle_ws, WS_HOST, port)
    print(f"[RIO2WPILIB] WS ouvindo em ws://localhost:{port}{WS_PATH}")

    poll_task = asyncio.create_task(_poll_and_broadcast(keys))
    await server.wait_closed()
    await poll_task


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--roborio", default=DEFAULT_ROBORIO, help="IP do roboRIO")
    parser.add_argument("--port", type=int, default=DEFAULT_WS_PORT, help="Porta WebSocket")
    args = parser.parse_args()

    try:
        asyncio.run(run_ws_bridge(args.roborio, args.port))
    except KeyboardInterrupt:
        print("[RIO2WPILIB] Encerrando...")


if __name__ == "__main__":
    main()