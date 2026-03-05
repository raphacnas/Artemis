from networktables import NetworkTables
import time

ROBOT_IP = "10.91.63.2"  # ajuste se necessário

# =========================
# SUAS TABLES DEFINIDAS
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
# INIT NT3
# =========================

NetworkTables.initialize(server=ROBOT_IP)

print("🔗 Conectando...")
while not NetworkTables.isConnected():
    time.sleep(0.5)

print("✅ Conectado!\n")

# =========================
# PRINTAR TUDO QUE EXISTE NO ROBÔ
# =========================

print("📡 ===== TABLES EXISTENTES NO ROBÔ =====")

# Root table
root = NetworkTables.getTable("")

subtables = root.getSubTables()

for table_name in subtables:
    table = NetworkTables.getTable(table_name)
    print(f"\n📂 Table: {table_name}")
    keys = table.getKeys()

    if not keys:
        print("   (sem keys)")
    else:
        for key in keys:
            value = table.getValue(key, None)
            print(f"   - {key} = {value}")

# =========================
# PRINTAR APENAS AS SUAS TABLES
# =========================

print("\n\n🎯 ===== TABLES DO SEU CÓDIGO =====")

for table_name, keys in TABLES_AND_KEYS.items():
    table = NetworkTables.getTable(table_name)

    print(f"\n📂 Table: {table_name}")

    existing_keys = table.getKeys()

    if not existing_keys:
        print("   (nenhuma key publicada ainda)")
        continue

    for key in existing_keys:
        value = table.getValue(key, None)
        print(f"   - {key} = {value}")