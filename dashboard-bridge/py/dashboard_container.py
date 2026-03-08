import threading
import time

import limelight.AI_Data
import bridge.nt3_ws 

def run_horizontal():
    limelight.AI_Data.main_loop()

def run_ws():
    import asyncio
    asyncio.run(bridge.nt3_ws.main_async("10.91.63.2", 5900))

if __name__ == "__main__":
    t1 = threading.Thread(target=run_horizontal, daemon=True)
    t2 = threading.Thread(target=run_ws, daemon=True)

    t1.start()
    t2.start()

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        pass