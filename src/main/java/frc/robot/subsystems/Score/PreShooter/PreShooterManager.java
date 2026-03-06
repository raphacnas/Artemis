package frc.robot.subsystems.Score.PreShooter;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import frc.robot.subsystems.Score.Shooter.ShooterManager;
import frc.robot.subsystems.Sensors.ViewSubsystem;

public class PreShooterManager extends SubsystemBase {

    public enum PreShooterState {
        IDLE,
        ARMED,
        AUTO_FEEDING,
        DISABLED
    }

    public enum ControlMode {
        MANUAL,
        AUTO_DISTANCE
    }

    private PreShooterState state = PreShooterState.IDLE;
    private ControlMode mode = ControlMode.MANUAL;

    private final PreShooterSubsystem preShooter;
    private final ViewSubsystem vision;
    private final ShooterManager shooterManager;

    public PreShooterManager(PreShooterSubsystem preShooter, ViewSubsystem vision, ShooterManager shooterManager) {
        this.preShooter = preShooter;
        this.vision = vision;
        this.shooterManager = shooterManager;
    }

    // ================= MODE =================

    public void toggleMode() {
        mode = (mode == ControlMode.MANUAL)
                ? ControlMode.AUTO_DISTANCE
                : ControlMode.MANUAL;

        state = PreShooterState.IDLE;
        SmartDashboard.putString("PreShooter/Mode", mode.name());
    }

    public ControlMode getMode() {
        return mode;
    }

    // ================= TELEOP =================

    public void toggleManualFeed() {

        if (mode != ControlMode.MANUAL) return;

        if (state == PreShooterState.ARMED) {
            state = PreShooterState.IDLE;
        } else {
            state = PreShooterState.ARMED;
        }
    }

    // ================= AUTO =================

    public void enableAuto() {
        mode = ControlMode.AUTO_DISTANCE;
        state = PreShooterState.IDLE;
    }

    public void stop() {
        state = PreShooterState.IDLE;
        preShooter.stop();
    }

    public PreShooterState getState() {
        return state;
    }

    @Override
    public void periodic() {

        if (state == PreShooterState.DISABLED) {
            preShooter.stop();
            return;
        }

        // ===== AUTO MODE =====
        if (mode == ControlMode.AUTO_DISTANCE) {

            boolean tagValid = vision.hasValidFrontTarget();

            boolean aligned =
                Math.abs(vision.getFrontTxRad())
                < Math.toRadians(1.2);

            double distance = vision.getFrontDistanceToTag();

            boolean validDistance = distance != Double.MAX_VALUE;

            boolean shooterReady = shooterManager.isAtSpeed();

            if (tagValid && aligned && shooterReady && validDistance) {
                state = PreShooterState.AUTO_FEEDING;
            } else {
                state = PreShooterState.IDLE;
            }
        }

        // ===== EXECUTION =====
        switch (state) {

            case ARMED:
            case AUTO_FEEDING:
                preShooter.feed();
                break;

            case DISABLED:
            case IDLE:
            default:
                preShooter.stop();
                break;
        }

        SmartDashboard.putString("PreShooter/State", state.name());
        SmartDashboard.putString("PreShooter/Mode", mode.name());
    }
}