package frc.robot.subsystems.Score.PreShooter;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import frc.robot.subsystems.Score.Shooter.ShooterManager;
import frc.robot.subsystems.Sensors.ViewSubsystem;

public class PreShooterManager extends SubsystemBase {

    public enum PreShooterState {
    IDLE,
    ARMED,
    AUTO_FEEDING,
    REVERSE_FEEDING,  
    DISABLED
}

    public enum ControlMode {
        MANUAL,
        AUTO_DISTANCE
    }

    private PreShooterState state = PreShooterState.IDLE;
    private ControlMode mode = ControlMode.MANUAL;
    private boolean autoMode = false;

    private final PreShooterSubsystem preShooter;
    private final ViewSubsystem vision;
    private final ShooterManager shooterManager;

    private final Timer shooterWarmupTimer = new Timer();
    private boolean timerStarted = false;

    private static final double SHOOTER_WARMUP_SECONDS = 1.0;

    public PreShooterManager(PreShooterSubsystem preShooter, ViewSubsystem vision, ShooterManager shooterManager) {
        this.preShooter     = preShooter;
        this.vision         = vision;
        this.shooterManager = shooterManager;
    }

    public void toggleMode() {
        mode  = (mode == ControlMode.MANUAL) ? ControlMode.AUTO_DISTANCE : ControlMode.MANUAL;
        state = PreShooterState.IDLE;
        resetWarmup();
        SmartDashboard.putString("PreShooter/Mode", mode.name());
    }

    public void toggleReverseFeed() {
    if (mode != ControlMode.MANUAL) return;
    state = (state == PreShooterState.REVERSE_FEEDING) ? PreShooterState.IDLE : PreShooterState.REVERSE_FEEDING;
}

    public ControlMode getMode() { return mode; }

    public void toggleManualFeed() {
        if (mode != ControlMode.MANUAL) return;
        state = (state == PreShooterState.ARMED) ? PreShooterState.IDLE : PreShooterState.ARMED;
    }

    public void enableAuto() {
        mode     = ControlMode.AUTO_DISTANCE;
        autoMode = true;
        state    = PreShooterState.ARMED;
        resetWarmup();
    }

    public void stop() {
        state    = PreShooterState.IDLE;
        autoMode = false;
        resetWarmup();
        preShooter.stop();
    }

    public PreShooterState getState() { return state; }

    private void resetWarmup() {
        shooterWarmupTimer.stop();
        shooterWarmupTimer.reset();
        timerStarted = false;
    }

    private boolean isShooterReady() {
        if (!shooterManager.isSpinning()) {
            resetWarmup();
            return false;
        }

        if (!timerStarted) {
            shooterWarmupTimer.start();
            timerStarted = true;
        }

        return shooterWarmupTimer.hasElapsed(SHOOTER_WARMUP_SECONDS);
    }

    @Override
    public void periodic() {

        if (state == PreShooterState.DISABLED) {
            preShooter.stop();
            return;
        }

        if (mode == ControlMode.AUTO_DISTANCE) {

            boolean shooterReady = isShooterReady();

            if (autoMode) {
                if (shooterReady) {
                    state = PreShooterState.AUTO_FEEDING;
                } else {
                    state = PreShooterState.ARMED;
                }
            } else {
                boolean tagValid      = vision.hasValidFrontTarget();
                boolean aligned       = Math.abs(vision.getFrontTxRad()) < Math.toRadians(1.2);
                boolean validDistance = vision.getFrontDistanceToTag() != Double.MAX_VALUE;

                if (tagValid && aligned && shooterReady && validDistance) {
                    state = PreShooterState.AUTO_FEEDING;
                } else {
                    state = PreShooterState.IDLE;
                }
            }
        }

        switch (state) {
    case ARMED:
    case AUTO_FEEDING:
        preShooter.feed();
        break;
    case REVERSE_FEEDING:
        preShooter.reverseFeed();
        break;
    case DISABLED:
    case IDLE:
    default:
        preShooter.stop();
        break;
}

        SmartDashboard.putString("PreShooter/State", state.name());
        SmartDashboard.putString("PreShooter/Mode",  mode.name());
    }
}