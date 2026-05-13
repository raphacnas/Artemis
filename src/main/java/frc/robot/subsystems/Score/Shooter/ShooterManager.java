package frc.robot.subsystems.Score.Shooter;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ShooterManager extends SubsystemBase {

    public enum ShooterState {
        IDLE,
        SPINNING,
        SPINNING_BACK,
        DISABLED
    }

    private final ShooterSubsystem shooter;
    private ShooterState state = ShooterState.IDLE;

    public ShooterManager(ShooterSubsystem shooter) {
        this.shooter = shooter;
        SmartDashboard.putString("Shooter/State", state.name());
    }

    // ================= CONTROLE =================

    public void toggleShooter() {
        if (state == ShooterState.SPINNING) {
            setState(ShooterState.IDLE);
        } else {
            setState(ShooterState.SPINNING);
        }
    }

    // public void toggleReverseShooter() {
    //     if (state == ShooterState.SPINNING_BACK) {
    //         setState(ShooterState.IDLE);
    //     } else {
    //         setState(ShooterState.SPINNING_BACK);
    //     }
    // }

    public void start() {
        if (state != ShooterState.DISABLED) {
            setState(ShooterState.SPINNING);
        }
    }

    public void stop() {
        setState(ShooterState.IDLE);
    }

    public void disable(String reason) {
        setState(ShooterState.DISABLED);
        SmartDashboard.putString("Shooter/DisabledReason", reason);
    }

    public void reenable() {
        if (state == ShooterState.DISABLED) {
            setState(ShooterState.IDLE);
        }
    }

    private void setState(ShooterState newState) {
        state = newState;
        SmartDashboard.putString("Shooter/State", state.name());
    }

    // ================= GETTERS =================

    public boolean isSpinning() {
        return state == ShooterState.SPINNING;
    }

    public ShooterState getState() {
        return state;
    }

    // ================= PERIODIC =================

    @Override
    public void periodic() {
        switch (state) {
            case SPINNING:
                shooter.setpower(0.4);
                break;
            // case SPINNING_BACK:
            //     shooter.setpower(-0.6);
            //     break;
            case DISABLED:
            case IDLE:
            default:
                shooter.stop();
                break;
        }
    }
}