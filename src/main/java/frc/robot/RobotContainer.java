package frc.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.CommandJoystick;
import edu.wpi.first.wpilibj2.command.button.CommandPS5Controller;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.commands.DriveCommand;
import frc.robot.subsystems.Score.Angular.IntakeAngleManager;
import frc.robot.subsystems.Score.Angular.StreamDeckIntakeAngleController;
import frc.robot.subsystems.Score.Rollers.IntakeManager;
import frc.robot.subsystems.Score.Rollers.IntakeRollerSubsystem;
import frc.robot.subsystems.Score.Rollers.StreamDeckIntakeRollerController;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;

import java.io.File;

public class RobotContainer
{

private final CommandPS5Controller controller = new CommandPS5Controller(0);
private final CommandJoystick logitech = new CommandJoystick(1);
private final SwerveSubsystem drivebase = new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve/neo"));

private final IntakeAngleManager intake = new IntakeAngleManager();
private final StreamDeckIntakeAngleController streamDeck = new StreamDeckIntakeAngleController(intake);    

private final IntakeRollerSubsystem rollerSubsystem =
  new IntakeRollerSubsystem();  

private final IntakeManager rollerManager =
  new IntakeManager(rollerSubsystem);

private final StreamDeckIntakeRollerController rollerStreamDeck =
  new StreamDeckIntakeRollerController(rollerManager);


private final SendableChooser<Command> autoChooser = new SendableChooser<>();

public RobotContainer()
{
  configureBindings();
  DriverStation.silenceJoystickConnectionWarning(true);

  
  autoChooser.setDefaultOption("Do Nothing",
      Commands.runOnce(drivebase::zeroGyroWithAlliance));

  SmartDashboard.putData("Auto Chooser", autoChooser);
}

private void configureBindings(){
  
 drivebase.setDefaultCommand(
    new DriveCommand(
        drivebase,
        () -> {
            if (controller.povUp().getAsBoolean()) return 0.6;
            if (controller.povDown().getAsBoolean()) return -0.6;
            return -controller.getLeftY();
        },

        () -> {
            if (controller.povRight().getAsBoolean()) return -0.6;
            if (controller.povLeft().getAsBoolean()) return 0.6;
            return -controller.getLeftX();
        },

        () -> controller.getRightX()
    )
);
  controller.options().onTrue(
  Commands.runOnce(drivebase::zeroGyroWithAlliance)
);

 // ================= ANGLE =================

  // 🔺 TRIANGLE → Toggle posição (Zero ↔ Target)
  logitech.button(4).onTrue(
    new InstantCommand(() -> {
      if (intake.getCurrentState() != IntakeAngleManager.ControlState.AUTOMATIC) {
        intake.moveToTargetPosition();
      } else {
        intake.moveToZeroPosition();
      }
    })
  );

  //  X : Calibrar ZERO
  logitech.button(3).onTrue(
    new InstantCommand(() -> intake.calibrateZero())
  );

  // CIRCLE : Calibrar TARGET
  logitech.button(2).onTrue(
    new InstantCommand(() -> intake.calibrateTargetAngle())
  );

  // ================= ROLLERS =================

  // L1 : Toggle Intake
  logitech.button(5).onTrue(
    new InstantCommand(() -> rollerManager.toggleIntake())
  );

  // R1 : Toggle Outtake
  logitech.button(6).onTrue(
    new InstantCommand(() -> rollerManager.toggleOuttake())
  );

  // ================= MANUAL ANGLE =================

  new Trigger(() -> logitech.getRawAxis(7) > 0.04)
    .onTrue(new InstantCommand(() -> intake.setManual()))
    .whileTrue(new RunCommand(() -> intake.setManualOutput(0.25)))
    .onFalse(new InstantCommand(() -> intake.stop()));

  new Trigger(() -> logitech.getRawAxis(8) > 0.04)
    .onTrue(new InstantCommand(() -> intake.setManual()))
    .whileTrue(new RunCommand(() -> intake.setManualOutput(-0.25)))
    .onFalse(new InstantCommand(() -> intake.stop()));
}



public Command getAutonomousCommand(){
  return autoChooser.getSelected();
}

public void setMotorBrake(boolean brake)
{
  drivebase.setMotorBrake(brake);
}
}