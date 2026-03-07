import time
import threading
import cv2
import math
from ultralytics import YOLO

# IMPORTA FUNCOES DO SEU RIO2WPILIB
from limelight.RIO2WPILIB import (
    init_nt,
    rio2wpi_tx,
    rio2wpi_ta,
    rio2wpi_has_target,
    rio2wpi_bbox,
    rio2wpi_distance,
)

# =========================
# CONFIGURACOES
# =========================

LIME2_STREAM_URL = "http://10.91.63.2:1181/?action=stream"

MODEL_PATH = "gamepiece2026.pt"
CONF_THRESHOLD = 0.40

INFER_DT = 0.05
STALE_TIMEOUT_S = 2.0

REOPEN_COOLDOWN_S = 0.5
FAILS_BEFORE_REOPEN = 8

# HFOV padrao Limelight
LIMELIGHT_HFOV_DEG = 59.6

running = True

# =========================
# ESTADO COMPARTILHADO
# =========================
state_lock = threading.Lock()
latest = {"tx": None, "ta": None, "bbox": None, "_ts": 0.0}

frame_lock = threading.Lock()
latest_frame = None

cap_status = {"opened": False, "fails": 0, "reopens": 0, "last_ok": 0.0}
last_infer = 0.0

# =========================
# NETWORKTABLES
# =========================
init_nt(server="10.91.63.2", table_name="limelight-back")

# =========================
# CAPTURE
# =========================
def open_capture(url: str) -> cv2.VideoCapture:
    cap = cv2.VideoCapture(url, cv2.CAP_FFMPEG)
    try:
        cap.set(cv2.CAP_PROP_BUFFERSIZE, 1)
    except Exception:
        pass
    return cap


def capture_worker():
    global running, latest_frame

    cap = None
    last_open_try = 0.0

    def reopen():
        nonlocal cap, last_open_try
        now = time.time()
        if now - last_open_try < REOPEN_COOLDOWN_S:
            return
        last_open_try = now

        if cap is not None:
            try:
                cap.release()
            except Exception:
                pass

        cap = open_capture(LIME2_STREAM_URL)
        cap_status["opened"] = cap.isOpened() if cap else False
        cap_status["fails"] = 0
        cap_status["reopens"] += 1

    reopen()

    while running:
        if cap is None or not cap.isOpened():
            reopen()
            time.sleep(0.02)
            continue

        ret, fr = cap.read()
        if not ret or fr is None:
            cap_status["fails"] += 1
            if cap_status["fails"] >= FAILS_BEFORE_REOPEN:
                reopen()
            time.sleep(0.01)
            continue

        cap_status["fails"] = 0
        cap_status["last_ok"] = time.time()

        with frame_lock:
            latest_frame = fr

        time.sleep(0.001)

    if cap:
        cap.release()

# =========================
# INFERENCIA (SMART TARGET GROUP)
# =========================
def infer_one_frame(model: YOLO, frame):
    if frame is None:
        return None, None, None

    h, w = frame.shape[:2]
    cx = w / 2.0

    focal_px = (w / 2.0) / math.tan(math.radians(LIMELIGHT_HFOV_DEG / 2.0))
    frame_area = w * h

    results = model(frame, verbose=False)

    centers_x = []
    areas = []
    boxes = []

    for r in results:
        for box in r.boxes:
            conf = float(box.conf[0])
            if conf < CONF_THRESHOLD:
                continue

            x1, y1, x2, y2 = map(int, box.xyxy[0])

            bx = (x1 + x2) / 2.0
            centers_x.append(bx)

            area = max(0, (x2 - x1)) * max(0, (y2 - y1))
            areas.append(area)

            boxes.append([x1, y1, x2, y2])

    if len(centers_x) == 0:
        return None, None, None

    # ===== CENTRO MEDIO DO GRUPO =====
    avg_x = sum(centers_x) / len(centers_x)

    dx = avg_x - cx
    tx_deg = math.degrees(math.atan(dx / focal_px))

    # ===== AREA TOTAL DO GRUPO =====
    ta = (sum(areas) / frame_area) * 100.0

    # ===== BBOX VIRTUAL DO GRUPO =====
    x1 = int(min(b[0] for b in boxes))
    y1 = int(min(b[1] for b in boxes))
    x2 = int(max(b[2] for b in boxes))
    y2 = int(max(b[3] for b in boxes))

    bbox = [x1, y1, x2, y2]

    return tx_deg, ta, bbox


def update_latest(tx, ta, bbox):
    now = time.time()
    with state_lock:
        latest["tx"] = tx
        latest["ta"] = ta
        latest["bbox"] = bbox
        latest["_ts"] = now

    has_target = tx is not None and bbox is not None
    rio2wpi_has_target(has_target)

    if has_target:
        rio2wpi_tx(tx)
        rio2wpi_ta(ta)
        rio2wpi_bbox(bbox)

        # Estimate distance from bbox height (tune PIECE_REAL_HEIGHT_M and FOCAL_PX for your setup)
        PIECE_REAL_HEIGHT_M = 0.30  # approximate game piece height in meters — tune this!
        x1, y1, x2, y2 = bbox
        bbox_height_px = max(1, y2 - y1)
        # focal length in pixels estimated from HFOV and frame height
        # using same focal_px as infer_one_frame but for vertical axis
        distance = (PIECE_REAL_HEIGHT_M * 480) / bbox_height_px  # assumes 480px frame height
        rio2wpi_distance(distance)


def publish_stale_if_needed():
    now = time.time()
    with state_lock:
        ts = latest["_ts"]

    if ts != 0.0 and (now - ts) > STALE_TIMEOUT_S:
        rio2wpi_has_target(False)


def main_loop():
    global running, last_infer

    model = YOLO(MODEL_PATH)
    threading.Thread(target=capture_worker, daemon=True).start()

    while running:
        now = time.time()
        publish_stale_if_needed()

        if now - last_infer >= INFER_DT:
            img = None
            with frame_lock:
                if latest_frame is not None:
                    img = latest_frame.copy()

            if img is not None:
                last_infer = now
                tx, ta, bbox = infer_one_frame(model, img)
                update_latest(tx, ta, bbox)

        time.sleep(0.001)

# =========================
# START
# =========================
if __name__ == "__main__":
    try:
        main_loop()
    except KeyboardInterrupt:
        running = False