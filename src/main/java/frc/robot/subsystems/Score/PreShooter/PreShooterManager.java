package frc.robot.subsystems.Score.PreShooter;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import frc.robot.Constants;
import frc.robot.subsystems.Score.Shooter.ShooterManager;

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

    public PreShooterManager(PreShooterSubsystem preShooter) {
        this.preShooter = preShooter;
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

            // boolean tagValid = vision.hasValidFrontTarget();
            boolean tagValid = true;

            // boolean aligned =
            //     Math.abs(vision.getFrontTxRad())
            //     < Math.toRadians(1.2);
            boolean aligned = true;

            // double distance = vision.getFrontDistanceToTag();
            double distance = 2;

            // boolean validDistance = distance != Double.MAX_VALUE && Math.abs
            //         (distance - Constants.LimelightConstants.distance4Shoot) < 0.20;
            boolean validDistance = true;

            boolean shooterReady = true;

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