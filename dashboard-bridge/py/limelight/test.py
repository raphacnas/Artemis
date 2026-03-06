# test_server.py
import time
from networktables import NetworkTables

NetworkTables.startServer()  # runs as server instead of client

table = NetworkTables.getTable("RobotStress")

voltage = 12.5
while True:
    table.putNumber("batteryVoltage", voltage)
    table.putNumber("totalCurrent", 45.0)
    table.putNumber("drivetrainCurrent", 30.0)
    table.putNumber("stressScore", 25.0)
    table.putString("stressLevel", "LOW")
    table.putNumber("speedScale", 1.0)
    table.putNumber("chassisSpeed", 1.5)

    voltage -= 0.05  # slowly drain so you can see it update
    if voltage < 9.0:
        voltage = 12.5

    time.sleep(0.5)