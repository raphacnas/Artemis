import threading
import time
import asyncio

import limelight.AI_Data
import bridge.nt3_ws

def run_limelight():
    try:
        limelight.AI_Data.main_loop()
    except Exception as e:
        print(f"❌ Limelight erro: {e}")

def run_ws():
    try:
        asyncio.run(bridge.nt3_ws.main_async("10.91.63.2", 5810))
    except Exception as e:
        print(f"❌ WS erro: {e}")

if __name__ == "__main__":
    print("🚀 HYDRA #9163 — iniciando bridge...")

    t1 = threading.Thread(target=run_limelight, daemon=True)
    t2 = threading.Thread(target=run_ws, daemon=True)

    t1.start()
    t2.start()

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("⏹ Encerrando...")