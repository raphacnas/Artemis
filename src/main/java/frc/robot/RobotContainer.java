package frc.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.CommandJoystick;
import edu.wpi.first.wpilibj2.command.button.CommandPS5Controller;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Dashboards.Drive.DriveModePublisher;
import frc.robot.Dashboards.RobotStress.DashboardPublisherStress;
import frc.robot.Dashboards.RobotStress.RobotStressController;
import frc.robot.Dashboards.RobotStress.RobotStressMonitor;
import frc.robot.commands.auto_blocks.NamedCommandsRegistry;
import frc.robot.commands.teleopDrive.DriveCommand;
import frc.robot.commands.vision.AimLockCommand;
import frc.robot.subsystems.Score.Climb.ClimberManager;
import frc.robot.subsystems.Score.Angular.IntakeAngleManager;
import frc.robot.subsystems.Score.PreShooter.PreShooterManager;
import frc.robot.subsystems.Score.PreShooter.PreShooterSubsystem;
import frc.robot.subsystems.Score.Rollers.IntakeRollerManager;
import frc.robot.subsystems.Score.Rollers.IntakeRollerSubsystem;
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
  private final IntakeRollerManager rollerManager;
  private final SpindexerManager spindexerManager;
  private final PreShooterManager preShooterManager;
  private final ClimberManager climberManager;

  /* ================= COMMANDS ================= */
  private final AimLockCommand aimLockFront;
  private final AimLockCommand aimLockBack;

  /* ================= DASHBOARD ================= */
  private final RobotStressMonitor stressMonitor;
  private final RobotStressController stressController;
  private final DashboardPublisherStress stressPublisher;
  private final DriveModePublisher modePublisher;

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
    rollerManager = new IntakeRollerManager(rollerSubsystem);
    spindexerManager = new SpindexerManager(spindexerSubsystem);
    preShooterManager = new PreShooterManager(preShooterSubsystem, vision, shooterManager);
    climberManager = new ClimberManager();

    /* ========= DASHBOARD ========= */
    stressMonitor = new RobotStressMonitor();
    stressController = new RobotStressController();
    stressPublisher = new DashboardPublisherStress();
    modePublisher = new DriveModePublisher();

    /* ========= COMMANDS ========= */
    aimLockFront = new AimLockCommand(drivebase, vision, AimLockCommand.CameraSide.FRONT, xSupplier, ySupplier);
    aimLockBack  = new AimLockCommand(drivebase, vision, AimLockCommand.CameraSide.BACK,  xSupplier, ySupplier);

    configureBindings();
    DriverStation.silenceJoystickConnectionWarning(true);

    NamedCommandsRegistry.registerAll(
        drivebase,
        vision,
        shooterManager,
        preShooterManager,
        spindexerManager,
        climberManager
    );

    // Tag selection is deferred to autonomousInit/teleopInit via refreshTagSelection()
    // so that DriverStation.getAlliance() is guaranteed to be available
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
    controller.square().toggleOnTrue(aimLockBack);

    // Aim at tag with front camera / Lime 4 (toggle)
    controller.circle().toggleOnTrue(aimLockFront);

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
        new InstantCommand(() -> intake.calibrateZero(), intake)
    );

    logitech.button(2).onTrue(
        new InstantCommand(() -> intake.calibrateTargetAngle(), intake)
    );

    /* ================= ROLLERS ================= */

    logitech.button(5).onTrue(
        new InstantCommand(() -> rollerManager.toggleIntake(), rollerManager)
    );

    logitech.button(6).onTrue(
        new InstantCommand(() -> rollerManager.toggleOuttake(), rollerManager)
    );

    /* ================= MANUAL ANGLE ================= */

    new Trigger(() -> logitech.getRawAxis(2) > 0.04)
        .onTrue(new InstantCommand(() -> intake.setManual(), intake))
        .whileTrue(new RunCommand(() -> intake.setManualOutput(0.3), intake))
        .onFalse(new InstantCommand(() -> intake.stop(), intake));

    new Trigger(() -> logitech.getRawAxis(3) > 0.04)
        .onTrue(new InstantCommand(() -> intake.setManual(), intake))
        .whileTrue(new RunCommand(() -> intake.setManualOutput(-0.3), intake))
        .onFalse(new InstantCommand(() -> intake.stop(), intake));

    /* ================= PRESHOOTER ================= */

    logitech.povLeft().onTrue(
        new InstantCommand(() -> spindexerManager.toggleSpin(), spindexerManager)
    );

    logitech.povDown().onTrue(
        new InstantCommand(() -> preShooterManager.toggleManualFeed(), preShooterManager)
    );

    /* ================= SHOOTER ================= */

    logitech.povUp().onTrue(
        new InstantCommand(() -> shooterManager.toggleShooter(), shooterManager)
    );

    /* ================= CLIMB ================= */

    logitech.povUpRight().onTrue(
      new InstantCommand(() -> climberManager.setClimbManual(0.8), climberManager)
    );

    logitech.povDownLeft().onTrue(
      new InstantCommand(() -> climberManager.setClimbManual(-0.8), climberManager)
    );
  }

  /* ================= AUTO ================= */

  public Command getAutonomousCommand() {
    return new PathPlannerAuto("AutoTaxiLeft");
  }

  public void periodic() {
    var stressData = stressMonitor.generateData(drivebase);
    stressController.update(stressData);

    double speedScale = stressController.getSpeedScale();
    double chassisSpeed = drivebase.getRobotVelocity().vxMetersPerSecond;

    stressPublisher.publish(stressData, speedScale, chassisSpeed);

    // Publish mode indicators to dashboard
    modePublisher.publishAim(aimLockBack.isActive() ? 1 : 0);
    modePublisher.publishAlign(
        preShooterManager.getMode() == PreShooterManager.ControlMode.AUTO_DISTANCE ? 1 : 0);
    modePublisher.publishShooterLime2Plus(shooterManager.isEnabled() ? 1 : 0);
    modePublisher.publishAimLime4(aimLockFront.isActive() ? 1 : 0);
  }

  /* ================= GETTERS ================= */

  public SwerveSubsystem getSwerveSubsystem() {
    return drivebase;
  }

  public void refreshTagSelection() {
    vision.selectAllHubTags();
    vision.selectAllTowerTags();
  }

  public void setMotorBrake(boolean brake) {
    drivebase.setMotorBrake(brake);
  }
}