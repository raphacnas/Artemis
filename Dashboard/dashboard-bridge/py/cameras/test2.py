from ultralytics import YOLO

model = YOLO("gamepiece2026.pt")

model.predict(source=1, conf=0.40, show=True)