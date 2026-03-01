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
import frc.robot.commands.teleopDrive.DriveCommand;
import frc.robot.commands.vision.AimAtTagCommand;
import frc.robot.commands.vision.AlignWithPieceCommand;
import frc.robot.commands.vision.AimAtTagCommand.CameraSide;
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
import java.lang.ModuleLayer.Controller;

public class RobotContainer{

  // 0. Váriaveis
  public final double xSupplier, ySupplier;

  // 1. Controles
  private final CommandPS5Controller controller;
  private final CommandJoystick logitech;

  // 2. Subsystems (Hardware e Base)
  private final SwerveSubsystem drivebase;
  private final ViewSubsystem vision;
  private final ShooterSubsystem shooterSubsystem;
  private final IntakeAngleManager intake;
  private final IntakeRollerSubsystem rollerSubsystem;
  private final SpindexerSubsystem spindexerSubsystem;
  private final PreShooterSubsystem preShooterSubsystem;

  // 3. Managers (Lógica de Controle)
  private final ShooterManager shooterManager;
  private final IntakeManager rollerManager;
  private final SpindexerManager spindexerManager;
  private final PreShooterManager preShooterManager;

  // 4. Periféricos e Comandos Complexos
  private final StreamDeckIntakeAngleController streamDeck;
  private final StreamDeckIntakeRollerController rollerStreamDeck;
  private final AimAtTagCommand aimAtTag;

  // 5. Autônomo
  private final SendableChooser<Command> autoChooser;

  

  public RobotContainer() {

      // --- Instanciação dos Controles ---
      controller = new CommandPS5Controller(Constants.PS5_ID);
      logitech = new CommandJoystick(Constants.LOGITECH_ID);

      // --- Váriaveis ---
      xSupplier = -controller.getLeftX();
      ySupplier = -controller.getLeftY();

      // --- Instanciação dos Subsystems ---
      drivebase = new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve/neo"));
      vision = new ViewSubsystem();
      shooterSubsystem = new ShooterSubsystem();
      intake = new IntakeAngleManager();
      rollerSubsystem = new IntakeRollerSubsystem();
      spindexerSubsystem = new SpindexerSubsystem();
      preShooterSubsystem = new PreShooterSubsystem();

      // --- Instanciação dos Managers (Injeção de Dependência) ---
      shooterManager = new ShooterManager(shooterSubsystem, vision);
      rollerManager = new IntakeManager(rollerSubsystem);
      spindexerManager = new SpindexerManager(spindexerSubsystem);
      preShooterManager = new PreShooterManager(preShooterSubsystem);

      // --- Instanciação dos Periféricos/Comandos ---
      streamDeck = new StreamDeckIntakeAngleController(intake);
      rollerStreamDeck = new StreamDeckIntakeRollerController(rollerManager);
      aimAtTag = new AimAtTagCommand(drivebase, vision, AimAtTagCommand.CameraSide.FRONT, xSupplier, ySupplier);

      // --- Configurações Finais ---
      autoChooser = new SendableChooser<>();
      
      DriverStation.silenceJoystickConnectionWarning(true);
      configureBindings();

      // Configuração do Seletor de Auto
      autoChooser.setDefaultOption("Do Nothing", Commands.runOnce(drivebase::zeroGyroWithAlliance));
      SmartDashboard.putData("Auto Chooser", autoChooser);
  }


private void configureBindings(){

  /* ==================== =================== ====================
   * ==================== PILOTO DE LOCOMOÇÃO ====================
     =================== ==================== ==================== */
  
 drivebase.setDefaultCommand(
    new DriveCommand(
        drivebase,
        () -> {
            if (controller.povUp().getAsBoolean()) return 0.6;
            if (controller.povDown().getAsBoolean()) return -0.6;
            return ySupplier;
        },

        () -> {
            if (controller.povRight().getAsBoolean()) return -0.6;
            if (controller.povLeft().getAsBoolean()) return 0.6;
            return xSupplier;
        },

        () -> controller.getRightX()
    )
);
  controller.options().onTrue(
  Commands.runOnce(drivebase::zeroGyroWithAlliance)
);


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
  
  logitech.povUp().onTrue(
    new InstantCommand(() -> aimAtTag.toggle())
  );

}

public Command getAutonomousCommand(){
  return autoChooser.getSelected();
}

public void setMotorBrake(boolean brake)
{
  drivebase.setMotorBrake(brake);
}
}