package frc.robot.adl;

import frc.robot.subsystems.Score.Angular.IntakeAngleManager;
import frc.robot.subsystems.Score.Rollers.IntakeRollerManager;
import frc.robot.subsystems.Score.Climb.ClimberManager;
import frc.robot.subsystems.Score.PreShooter.PreShooterManager;
import frc.robot.subsystems.Score.Shooter.ShooterManager;
import frc.robot.subsystems.Score.Spindexer.SpindexerManager;

public class ADLExecutor {

    private final IntakeAngleManager intakeAngle;
    private final IntakeRollerManager intakeRoller;
    private final ClimberManager climb;
    private final PreShooterManager preShooter;
    private final ShooterManager shooter;
    private final SpindexerManager spindexer;

    public ADLExecutor(
        IntakeAngleManager intakeAngle,
        IntakeRollerManager intakeRoller,
        ClimberManager climb,
        PreShooterManager preShooter,
        ShooterManager shooter,
        SpindexerManager spindexer
    ) {
        this.intakeAngle  = intakeAngle;
        this.intakeRoller = intakeRoller;
        this.climb        = climb;
        this.preShooter   = preShooter;
        this.shooter      = shooter;
        this.spindexer    = spindexer;
    }

    public void execute(ADLState state) {
        switch (state) {
            case ACQUIRING:
                intakeAngle.moveToTargetPosition();
                intakeRoller.toggleIntake();
                break;
            case SCORING:
                intakeRoller.stop();
                shooter.enable();
                spindexer.start();
                preShooter.enableAuto();
                break;
            case CLIMBING:
                intakeRoller.stop();
                preShooter.stop();
                shooter.disable();
                climb.goToMax();
                break;
            case EMERGENCY:
                intakeRoller.stop();
                preShooter.stop();
                shooter.disable();
                climb.setStopManualClimb();
                intakeAngle.stop();
                break;
            case IDLE:
            default:
                intakeRoller.stop();
                preShooter.stop();
                break;
        }
    }
}