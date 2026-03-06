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
import frc.robot.commands.auto_blocks.NamedCommandsRegistry;
import frc.robot.commands.teleopDrive.DriveCommand;
import frc.robot.commands.vision.AimAtTagCommand;
import frc.robot.commands.vision.AlignWithPieceCommand;
import frc.robot.subsystems.Score.Angular.IntakeAngleManager;
import frc.robot.subsystems.Score.Angular.StreamDeckIntakeAngleController;
import frc.robot.subsystems.Score.Climb.ClimberManager;
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
import java.util.function.DoubleSupplier;

import com.pathplanner.lib.commands.PathPlannerAuto;

public class RobotContainer {

  /* ================= CONTROLLERS ================= */
  private final CommandPS5Controller controller;
  private final CommandJoystick logitech;

  /* ================= INPUT SUPPLIERS ================= */
  // FIX: Use lambdas so these return the current axis value each call
  private final DoubleSupplier xSupplier;
  private final DoubleSupplier ySupplier;

  /* ================= SUBSYSTEMS ================= */
  private final SwerveSubsystem drivebase;
  private final ViewSubsystem vision;
  private final ShooterSubsystem shooterSubsystem;
  private final IntakeAngleManager intake;
  private final IntakeRollerSubsystem rollerSubsystem;
  private final SpindexerSubsystem spindexerSubsystem;
  private final PreShooterSubsystem preShooterSubsystem;

  /* ================= MANAGERS ================= */
  private final ShooterManager shooterManager;
  private final IntakeManager rollerManager;
  private final SpindexerManager spindexerManager;
  private final PreShooterManager preShooterManager;

  /* ================= STREAM DECK ================= */
  private final StreamDeckIntakeAngleController streamDeck;
  private final StreamDeckIntakeRollerController rollerStreamDeck;

  public RobotContainer() {

    /* ========= CONTROLLERS ========= */
    controller = new CommandPS5Controller(Constants.PS5_ID);
    logitech = new CommandJoystick(Constants.LOGITECH_ID);

    // FIX: Wrap in lambdas — getLeftX()/getLeftY() return double, not DoubleSupplier
    xSupplier = () -> controller.getLeftX();
    ySupplier = () -> controller.getLeftY();

    /* ========= SUBSYSTEMS ========= */
    drivebase = new SwerveSubsystem(
        new File(Filesystem.getDeployDirectory(), "swerve/neo"));
    vision = new ViewSubsystem();
    shooterSubsystem = new ShooterSubsystem();
    intake = new IntakeAngleManager();
    rollerSubsystem = new IntakeRollerSubsystem();
    spindexerSubsystem = new SpindexerSubsystem();
    preShooterSubsystem = new PreShooterSubsystem();

    /* ========= MANAGERS ========= */
    shooterManager = new ShooterManager(shooterSubsystem, vision);
    rollerManager = new IntakeManager(rollerSubsystem);
    spindexerManager = new SpindexerManager(spindexerSubsystem);
    preShooterManager = new PreShooterManager(preShooterSubsystem);

    /* ========= STREAM DECK ========= */
    streamDeck = new StreamDeckIntakeAngleController(intake);
    rollerStreamDeck = new StreamDeckIntakeRollerController(rollerManager);

    configureBindings();
    DriverStation.silenceJoystickConnectionWarning(true);

    NamedCommandsRegistry.registerAll(
        drivebase,
        vision,
        shooterManager,
        preShooterManager,
        spindexerManager
    );

    vision.selectAllHubTags();
    vision.selectAllTowerTags();
  }

  private void configureBindings() {

    /* ==================== =================== ====================
     * ==================== PILOTO DE LOCOMOÇÃO ====================
       =================== ==================== ==================== */

    // Default drive command using fixed DoubleSuppliers
    drivebase.setDefaultCommand(
        new DriveCommand(
            drivebase,
            () -> {
                if (controller.povUp().getAsBoolean()) return 0.6;
                if (controller.povDown().getAsBoolean()) return -0.6;
                return ySupplier.getAsDouble(); // FIX: call getAsDouble()
            },
            () -> {
                if (controller.povRight().getAsBoolean()) return -0.6;
                if (controller.povLeft().getAsBoolean()) return 0.6;
                return xSupplier.getAsDouble(); // FIX: call getAsDouble()
            },
            () -> controller.getRightX()
        )
    );

    // Zero gyro relative to alliance
    controller.options().onTrue(
        Commands.runOnce(drivebase::zeroGyroWithAlliance)
    );

    // Aim at tag with back camera (toggle)
    controller.square().toggleOnTrue(
        new AimAtTagCommand(
            drivebase,
            vision,
            AimAtTagCommand.CameraSide.BACK,
            xSupplier,
            ySupplier
        )
    );

    /* ================= ANGLE ================= */

    logitech.button(4).onTrue(
        new InstantCommand(() -> {
          if (intake.getCurrentState() != IntakeAngleManager.ControlState.AUTOMATIC) {
            intake.moveToTargetPosition();
          } else {
            intake.moveToZeroPosition();
          }
        })
    );

    logitech.button(3).onTrue(
        new InstantCommand(intake::calibrateZero)
    );

    logitech.button(2).onTrue(
        new InstantCommand(intake::calibrateTargetAngle)
    );

    /* ================= ROLLERS ================= */

    logitech.button(5).onTrue(
        new InstantCommand(rollerManager::toggleIntake)
    );

    logitech.button(6).onTrue(
        new InstantCommand(rollerManager::toggleOuttake)
    );

    /* ================= MANUAL ANGLE ================= */

    new Trigger(() -> logitech.getRawAxis(2) > 0.04)
        .onTrue(new InstantCommand(intake::setManual))
        .whileTrue(new RunCommand(() -> intake.setManualOutput(0.3)))
        .onFalse(new InstantCommand(intake::stop));

    new Trigger(() -> logitech.getRawAxis(3) > 0.04)
        .onTrue(new InstantCommand(intake::setManual))
        .whileTrue(new RunCommand(() -> intake.setManualOutput(-0.3)))
        .onFalse(new InstantCommand(intake::stop));

    /* ================= PRESHOOTER ================= */

    logitech.povLeft().onTrue(
        new InstantCommand(spindexerManager::toggleSpin)
    );

    logitech.povDown().onTrue(
        new InstantCommand(preShooterManager::toggleManualFeed)
    );

    /* ================= SHOOTER ================= */

    logitech.povUp().onTrue(
        new InstantCommand(shooterManager::toggleShooter)
    );
  }

  /* ================= AUTO ================= */

  public Command getAutonomousCommand() {
    return new PathPlannerAuto("AutoRobotCenter");
  }

  /* ================= GETTERS ================= */

  public SwerveSubsystem getSwerveSubsystem() {
    return drivebase;
  }

  public void setMotorBrake(boolean brake) {
    drivebase.setMotorBrake(brake);
  }
}
