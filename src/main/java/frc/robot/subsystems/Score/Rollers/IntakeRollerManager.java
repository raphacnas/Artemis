package frc.robot.subsystems.Score.Rollers;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class IntakeRollerManager extends SubsystemBase {

    public enum IntakeState {
        IDLE,
        INTAKING,
        OUTTAKING,
        DISABLED
    }

    private final IntakeRollerSubsystem rollers;
    private IntakeState state = IntakeState.IDLE;

    public IntakeRollerManager(IntakeRollerSubsystem rollers) {
        this.rollers = rollers;
        SmartDashboard.putString("Intake/State", state.name());
    }

   public void toggleIntake() {
    if (state == IntakeState.INTAKING) {
        setState(IntakeState.IDLE);
    } else {
        setState(IntakeState.INTAKING);
    }
}

public void toggleOuttake() {
    if (state == IntakeState.OUTTAKING) {
        setState(IntakeState.IDLE);
    } else {
        setState(IntakeState.OUTTAKING);
    }
}

    public void stop() {
        setState(IntakeState.IDLE);
    }

    public void disable(String reason) {
        setState(IntakeState.DISABLED);
        SmartDashboard.putString("Intake/DisabledReason", reason);
    }

    private void setState(IntakeState newState) {
        state = newState;
        SmartDashboard.putString("Intake/State", state.name());
    }

    public void reenable() {
        if (state == IntakeState.DISABLED) {
            state = IntakeState.IDLE;
            SmartDashboard.putString("Intake/State", state.name());
        }
    }

    @Override
    public void periodic() {

        switch (state) {

            case INTAKING:
                rollers.intake();
                break;

            case OUTTAKING:
                rollers.outtake();
                break;

            case DISABLED:
            case IDLE:
            default:
                rollers.stop();
                break;
        }
    }

    public IntakeState getState() {
        return state;
    }
}