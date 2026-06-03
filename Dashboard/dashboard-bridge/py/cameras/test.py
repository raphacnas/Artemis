import cv2
import numpy as np
import time

# ── CONFIG ─────────────────────────────────────────────────────────────
SQUARES_X      = 11
SQUARES_Y      = 8
SQUARE_SIZE_M  = 0.038
MARKER_SIZE_M  = 0.028

CAMERA_INDEX   = 1

MIN_FRAMES     = 25
MIN_CORNERS    = 30

OUTPUT_FILE    = "calibration.npz"

# ── ARUCO ──────────────────────────────────────────────────────────────
aruco_dict = cv2.aruco.getPredefinedDictionary(cv2.aruco.DICT_4X4_50)
board = cv2.aruco.CharucoBoard(
    (SQUARES_X, SQUARES_Y),
    SQUARE_SIZE_M,
    MARKER_SIZE_M,
    aruco_dict
)
detector = cv2.aruco.CharucoDetector(board)

# ── CAMERA ─────────────────────────────────────────────────────────────
cap = cv2.VideoCapture(CAMERA_INDEX)

if not cap.isOpened():
    print("ERROR: camera")
    exit(1)

# ── HEATMAP ────────────────────────────────────────────────────────────
heatmap = None

def update_heatmap(pts):
    pts = pts.reshape(-1, 2)
    for x, y in pts:
        x = int(x)
        y = int(y)
        if 0 <= x < heatmap.shape[1] and 0 <= y < heatmap.shape[0]:
            heatmap[y, x] += 1

def draw_heatmap(frame):
    hm = cv2.GaussianBlur(heatmap, (0,0), 15)
    hm_norm = cv2.normalize(hm, None, 0, 255, cv2.NORM_MINMAX)
    hm_color = cv2.applyColorMap(hm_norm.astype(np.uint8), cv2.COLORMAP_JET)

    hm_color = cv2.resize(hm_color, (frame.shape[1], frame.shape[0]))

    return cv2.addWeighted(frame, 0.7, hm_color, 0.3, 0)

def coverage_score():
    return np.count_nonzero(heatmap > 0) / heatmap.size

# ── SPREAD CHECK ───────────────────────────────────────────────────────
def good_spread(pts, w, h):
    pts = pts.reshape(-1, 2)
    xs, ys = pts[:,0], pts[:,1]

    return (
        xs.min() < w * 0.3 and
        xs.max() > w * 0.7 and
        ys.min() < h * 0.3 and
        ys.max() > h * 0.7
    )

# ── CAPTURE ────────────────────────────────────────────────────────────
all_corners, all_ids = [], []
captured = 0
last = 0

print("SPACE = capture | Q = calibrate")

while True:
    ret, frame = cap.read()
    if not ret:
        continue

    h, w = frame.shape[:2]

    if heatmap is None:
        heatmap = np.zeros((h, w), dtype=np.float32)

    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    corners, ids, _, _ = detector.detectBoard(gray)

    display = frame.copy()

    if corners is not None:
        print("corners:", corners.shape)

    if corners is not None and len(corners) >= MIN_CORNERS:
        cv2.aruco.drawDetectedCornersCharuco(display, corners, ids, (0,255,0))
        cv2.putText(display, f"{len(corners)} corners",
                    (10,30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0,255,0), 2)
    else:
        n = len(corners) if corners is not None else 0
        cv2.putText(display, f"{n} corners",
                    (10,30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0,100,255), 2)

    # ── HEATMAP SEMPRE ATUALIZA ──
    if corners is not None:
        update_heatmap(corners)

    display = draw_heatmap(display)

    cv2.putText(display, f"Frames: {captured}/{MIN_FRAMES}",
                (10,60), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255,255,255), 2)

    cov = coverage_score()
    cv2.putText(display, f"Coverage: {cov:.2f}",
                (10,90), cv2.FONT_HERSHEY_SIMPLEX, 0.6,
                (0,255,0) if cov > 0.25 else (0,100,255), 2)

    print("coverage:", cov)

    cv2.imshow("Calibration", display)
    key = cv2.waitKey(1) & 0xFF

    if key == ord(' '):
        now = time.time()
        if corners is not None and len(corners) >= MIN_CORNERS and (now-last)>0.5:

            if good_spread(corners, w, h):
                all_corners.append(corners)
                all_ids.append(ids)
                captured += 1
                last = now
                print(f"Frame {captured} OK ({len(corners)})")
            else:
                print("Frame rejeitado (baixa cobertura espacial)")

    elif key in (ord('q'), ord('Q')):
        break

cap.release()
cv2.destroyAllWindows()

# ── CHECK FINAL ────────────────────────────────────────────────────────
if captured < MIN_FRAMES or coverage_score() < 0.2:
    print("Dados insuficientes")
    exit(1)

# ── BUILD POINTS ───────────────────────────────────────────────────────
objpoints, imgpoints = [], []

for i in range(len(all_corners)):
    obj, img = board.matchImagePoints(all_corners[i], all_ids[i])
    objpoints.append(obj.reshape(1,-1,3).astype(np.float32))
    imgpoints.append(img.reshape(1,-1,2).astype(np.float32))

# ── INIT ───────────────────────────────────────────────────────────────
K = np.zeros((3,3))
K[0,0] = w * 0.5
K[1,1] = w * 0.5
K[0,2] = w / 2
K[1,2] = h / 2
K[2,2] = 1

D = np.zeros((4,1))

rvecs = [np.zeros((1,1,3), np.float64) for _ in objpoints]
tvecs = [np.zeros((1,1,3), np.float64) for _ in objpoints]

flags = cv2.fisheye.CALIB_RECOMPUTE_EXTRINSIC | cv2.fisheye.CALIB_FIX_SKEW

criteria = (cv2.TERM_CRITERIA_EPS+cv2.TERM_CRITERIA_MAX_ITER, 200, 1e-6)

# ── CALIBRATE ─────────────────────────────────────────────────────────
rms, K, D, rvecs, tvecs = cv2.fisheye.calibrate(
    objpoints, imgpoints,
    (w, h),
    K, D,
    rvecs, tvecs,
    flags,
    criteria
)

print("\nRMS:", rms)
print("K:\n", K)
print("D:\n", D.T)

np.savez(OUTPUT_FILE, K=K, D=D)
print("Salvo em", OUTPUT_FILE)