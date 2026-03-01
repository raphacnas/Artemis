package frc.robot;

import edu.wpi.first.math.geometry.Translation2d;
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
import frc.robot.commands.Poses.AutoGoAndAlignOutpost;
// import frc.robot.autos.NamedCommandsRegistry;
import frc.robot.commands.auto_blocks.AutoTaxiCommand;
import frc.robot.commands.teleopDrive.DriveCommand;
// import frc.robot.commands.vision.AimAtTagCommand;
import frc.robot.commands.vision.AlignWithPieceCommand;
import frc.robot.commands.vision.AutoShootAssistCommand;
// import frc.robot.commands.vision.AimAtTagCommand.CameraSide;
import frc.robot.subsystems.Score.Angular.IntakeAngleManager;
import frc.robot.subsystems.Score.Angular.StreamDeckIntakeAngleController;
import frc.robot.subsystems.Score.PreShooter.PreShooterManager;
import frc.robot.subsystems.Score.PreShooter.PreShooterSubsystem;
import frc.robot.subsystems.Score.Rollers.IntakeManager;
import frc.robot.subsystems.Score.Rollers.IntakeRollerSubsystem;
import frc.robot.subsystems.Score.Rollers.StreamDeckIntakeRollerController;
import frc.robot.subsystems.Score.Shooter.ShooterManager;
import frc.robot.subsystems.Score.Shooter.ShooterSubsystem;
import frc.robot.subsystems.Score.Spindexer.SpindexerManager;
import frc.robot.subsystems.Score.Spindexer.SpindexerSubsystem;
import frc.robot.subsystems.Sensors.ViewSubsystem;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;

import java.io.File;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.commands.PathPlannerAuto;

public class RobotContainer{

private final CommandPS5Controller controller = new CommandPS5Controller(0);
private final CommandJoystick logitech = new CommandJoystick(1);

private final SwerveSubsystem drivebase = new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve/neo"));

private final IntakeAngleManager intake = new IntakeAngleManager();
private final StreamDeckIntakeAngleController streamDeck = new StreamDeckIntakeAngleController(intake);    

private final IntakeRollerSubsystem rollerSubsystem = new IntakeRollerSubsystem();  
private final IntakeManager rollerManager =  new IntakeManager(rollerSubsystem);
private final StreamDeckIntakeRollerController rollerStreamDeck = new StreamDeckIntakeRollerController(rollerManager);

private final SpindexerSubsystem spindexerSubsystem = new SpindexerSubsystem();
private final SpindexerManager spindexerManager = new SpindexerManager(spindexerSubsystem);

private final ShooterSubsystem shooterSubsystem = new ShooterSubsystem();
private final ViewSubsystem vision = new ViewSubsystem();
private final ShooterManager shooterManager = new ShooterManager(shooterSubsystem, vision);

private final PreShooterSubsystem preShooterSubsystem = new PreShooterSubsystem(); 
private final PreShooterManager preShooterManager = new PreShooterManager(preShooterSubsystem);

private final AutoShootAssistCommand autoShootAssist = new AutoShootAssistCommand(drivebase, vision, shooterManager);

//private final AimAtTagCommand aimAtTag = new AimAtTagCommand(drivebase, vision, AimAtTagCommand.CameraSide.FRONT);


public RobotContainer(){
  configureBindings();
  DriverStation.silenceJoystickConnectionWarning(true);

//   NamedCommandsRegistry.registerAll(
//     drivebase,
//     vision,
//     shooterManager,
//     preShooterManager,
//     spindexerManager,
//     null // se ainda não tiver climb
// );

  vision.selectAllHubTags();
  vision.selectAllTowerTags();
}

private void configureBindings(){

  /* ==================== =================== ====================
   * ==================== PILOTO DE LOCOMOÇÃO ====================
     =================== ==================== ==================== */
  
     controller.triangle().onTrue(
    new InstantCommand(() -> autoShootAssist.toggle())
);

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

//   controller.square().toggleOnTrue(
//     new AimAtTagCommand(
//         drivebase,
//         vision,
//         AimAtTagCommand.CameraSide.BACK
//     )
// );;


  /* ==================== =================== ====================
    * ===================== PILOTO DE SISTEMA =====================
      =================== ==================== ==================== */

 // ================= ANGLE =================  (REFAZER ESSA PARTE DEPOIS)

  // SQUARE : Alternar entre posição ZERO e TARGET
  logitech.button(4).onTrue(
    new InstantCommand(() -> {
      if (intake.getCurrentState() != IntakeAngleManager.ControlState.AUTOMATIC) {
        intake.moveToTargetPosition();
      } else {
        intake.moveToZeroPosition();
      }
    })
  );

  //================= CALIBRAÇÕES INTAKE =================

  //  X : Calibrar ZERO
  logitech.button(3).onTrue(
    new InstantCommand(() -> intake.calibrateZero())
  );

  // CIRCLE : Calibrar TARGET
  logitech.button(2).onTrue(
    new InstantCommand(() -> intake.calibrateTargetAngle())
  );

  // ================= ROLLERS =================

  //LB
  logitech.button(5).onTrue(
    new InstantCommand(() -> rollerManager.toggleIntake())
  );

  //RB 
  logitech.button(6).onTrue(
    new InstantCommand(() -> rollerManager.toggleOuttake())
  );

  // ================= MANUAL ANGLE =================

//lt
   new Trigger(() -> logitech.getRawAxis(2) > 0.04)
    .onTrue(new InstantCommand(() -> intake.setManual()))
    .whileTrue(new RunCommand(() -> intake.setManualOutput(0.3)))
    .onFalse(new InstantCommand(() -> intake.stop()));

    //rt
  new Trigger(() -> logitech.getRawAxis(3) > 0.04)
    .onTrue(new InstantCommand(() -> intake.setManual()))
    .whileTrue(new RunCommand(() -> intake.setManualOutput(-0.3)))
    .onFalse(new InstantCommand(() -> intake.stop()));


  // ================= PRESHOOTERS =================
  
  //button back 
  logitech.povLeft().onTrue(
    new InstantCommand(() -> spindexerManager.toggleSpin())
  );

  logitech.povDown().onTrue(
    new InstantCommand(() -> preShooterManager.toggleManualFeed())
  );

  // ================= SHOOTER =================

  logitech.povUp().onTrue(
    new InstantCommand(() -> shooterManager.toggleShooter())
  );
  
}

public Command getAutonomousCommand() {
 return new AutoGoAndAlignOutpost(drivebase, vision);
}

public void setMotorBrake(boolean brake)
{
  drivebase.setMotorBrake(brake);
}
}