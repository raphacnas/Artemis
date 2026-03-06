package frc.robot.Dashboards.RobotStress;

import edu.wpi.first.wpilibj.RobotController;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;

public class RobotStressMonitor {

    public double getBatteryVoltage() {
        return RobotController.getBatteryVoltage();
    }

    private double batteryStress(double voltage) {
        if (voltage >= 12.0) return 0;
        if (voltage >= 11.0) return 20;
        if (voltage >= 10.0) return 45;
        if (voltage >= 9.0)  return 70;
        return 100;
    }

    public double calculateStressScore(double drivetrainCurrent, double totalCurrent) {
        double voltage = getBatteryVoltage();

        double voltageStress = batteryStress(voltage);
        double drivetrainStress = drivetrainCurrent * 0.15;
        double systemStress = totalCurrent * 0.05;

        double score = voltageStress + drivetrainStress + systemStress;
        return Math.min(score, 100);
    }

    public String getStressLevel(double score) {
        if (score < 20) return "LOW";
        if (score < 45) return "MEDIUM";
        if (score < 70) return "HIGH";
        return "CRITICAL";
    }

    public RobotStressData generateData(SwerveSubsystem drivebase) {

        double voltage = getBatteryVoltage();

        double drivetrainCurrent = drivebase.getTotalRobotCurrent();
        double totalCurrent = drivebase.getTotalRobotCurrent();

        double score = calculateStressScore(drivetrainCurrent, totalCurrent);
        String level = getStressLevel(score);

        return new RobotStressData(
            voltage,
            totalCurrent,
            drivetrainCurrent,
            score,
            level
        );
    }
}
