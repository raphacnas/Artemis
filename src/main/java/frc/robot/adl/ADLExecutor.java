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
           case SCORING:
    intakeRoller.stop();
    shooter.start();
    spindexer.start();
    preShooter.enableAuto();
    break;
case CLIMBING:
    intakeRoller.stop();
    preShooter.stop();
    shooter.stop();
    climb.goToMax();
    break;
case ACQUIRING:
    shooter.stop();
    preShooter.stop();
    intakeRoller.toggleIntake();
    break;
case EMERGENCY:
    intakeRoller.stop();
    preShooter.stop();
    shooter.stop();
    climb.setStopManualClimb();
    intakeAngle.stop();
    break;
default:
    break;
        }
    }
}