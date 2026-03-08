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
import frc.robot.commands.teleopDrive.DriveCommand;
import frc.robot.commands.vision.AimLockCommand;
import frc.robot.commands.vision.AlignWithPieceCommand;
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

  private final CommandPS5Controller controller;
  private final CommandJoystick logitech;

  private final DoubleSupplier xSupplier;
  private final DoubleSupplier ySupplier;

  private final SwerveSubsystem drivebase;
  private final ViewSubsystem vision;
  private final ShooterSubsystem shooterSubsystem;
  private final IntakeAngleManager intake;
  private final IntakeRollerSubsystem rollerSubsystem;
  private final SpindexerSubsystem spindexerSubsystem;
  private final PreShooterSubsystem preShooterSubsystem;

  private final ShooterManager shooterManager;
  private final IntakeRollerManager rollerManager;
  private final SpindexerManager spindexerManager;
  private final PreShooterManager preShooterManager;
  private final ClimberManager climberManager;

  private final AimLockCommand aimLockFront;
  private final AimLockCommand aimLockBack;
  private final AlignWithPieceCommand alignWithPiece;

  private final RobotStressMonitor stressMonitor;
  private final RobotStressController stressController;
  private final DashboardPublisherStress stressPublisher;
  private final DriveModePublisher modePublisher;

  public RobotContainer() {

    controller = new CommandPS5Controller(Constants.PS5_ID);
    logitech   = new CommandJoystick(Constants.LOGITECH_ID);

    xSupplier = () -> controller.getLeftX();
    ySupplier = () -> controller.getLeftY();

    drivebase           = new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve/neo"));
    vision              = new ViewSubsystem();
    shooterSubsystem    = new ShooterSubsystem();
    intake              = new IntakeAngleManager();
    rollerSubsystem     = new IntakeRollerSubsystem();
    spindexerSubsystem  = new SpindexerSubsystem();
    preShooterSubsystem = new PreShooterSubsystem();

    shooterManager   = new ShooterManager(shooterSubsystem);
    rollerManager    = new IntakeRollerManager(rollerSubsystem);
    spindexerManager = new SpindexerManager(spindexerSubsystem);
    preShooterManager = new PreShooterManager(preShooterSubsystem, vision, shooterManager);
    climberManager   = new ClimberManager();

    stressMonitor   = new RobotStressMonitor();
    stressController = new RobotStressController();
    stressPublisher = new DashboardPublisherStress();
    modePublisher   = new DriveModePublisher();

    aimLockFront   = new AimLockCommand(drivebase, vision, AimLockCommand.CameraSide.FRONT, xSupplier, ySupplier);
    aimLockBack    = new AimLockCommand(drivebase, vision, AimLockCommand.CameraSide.BACK,  xSupplier, ySupplier);
    alignWithPiece = new AlignWithPieceCommand(drivebase, vision);

    configureBindings();
    DriverStation.silenceJoystickConnectionWarning(true);
  }

  private void configureBindings() {

    drivebase.setDefaultCommand(
        new DriveCommand(
            drivebase,
            () -> {
                if (controller.povUp().getAsBoolean())    return  0.6;
                if (controller.povDown().getAsBoolean())  return -0.6;
                return ySupplier.getAsDouble();
            },
            () -> {
                if (controller.povRight().getAsBoolean()) return -0.6;
                if (controller.povLeft().getAsBoolean())  return  0.6;
                return xSupplier.getAsDouble();
            },
            () -> controller.getRightX()
        )
    );

    controller.options().onTrue(Commands.runOnce(drivebase::zeroGyroWithAlliance));
    controller.square().toggleOnTrue(aimLockBack);
    controller.circle().toggleOnTrue(aimLockFront);
    controller.cross().toggleOnTrue(alignWithPiece);

    logitech.button(5).onTrue(new InstantCommand(() -> rollerManager.toggleIntake(),   rollerManager));
    logitech.button(6).onTrue(new InstantCommand(() -> rollerManager.toggleOuttake(),  rollerManager));

    new Trigger(() -> logitech.getRawAxis(2) > 0.04)
        .onTrue(new InstantCommand(() -> intake.setManual(), intake))
        .whileTrue(new RunCommand(() -> intake.setManualOutput(0.3), intake))
        .onFalse(new InstantCommand(() -> intake.stop(), intake));

    new Trigger(() -> logitech.getRawAxis(3) > 0.04)
        .onTrue(new InstantCommand(() -> intake.setManual(), intake))
        .whileTrue(new RunCommand(() -> intake.setManualOutput(-0.3), intake))
        .onFalse(new InstantCommand(() -> intake.stop(), intake));

    logitech.povLeft().onTrue(new InstantCommand(() -> spindexerManager.toggleSpin(),       spindexerManager));
    logitech.povDown().onTrue(new InstantCommand(() -> preShooterManager.toggleManualFeed(), preShooterManager));
    logitech.povRight().onTrue(new InstantCommand(() -> preShooterManager.toggleReverseFeed(), preShooterManager));
    logitech.povUp().onTrue(new InstantCommand(  () -> shooterManager.toggleShooter(),       shooterManager));

    logitech.button(4)
        .onTrue(new InstantCommand(() -> climberManager.setManual(), climberManager))
        .whileTrue(new InstantCommand(() -> climberManager.setClimbManual(-0.3), climberManager))
        .onFalse(new InstantCommand(() -> climberManager.setStopManualClimb()));

    logitech.button(1)
        .whileTrue(new InstantCommand(() -> climberManager.setManual(), climberManager))
        .whileTrue(new InstantCommand(() -> climberManager.setClimbManual(0.3), climberManager))
        .onFalse(new InstantCommand(() -> climberManager.setStopManualClimb()));
  }

  public Command getAutonomousCommand() {
    return Commands.sequence(
        Commands.runOnce(() -> shooterManager.start()),
        new PathPlannerAuto("AutoRobotRight"),
        Commands.runOnce(() -> preShooterManager.enableAuto()),
        Commands.waitSeconds(0.01),
        Commands.runOnce(() -> spindexerManager.start()),
        Commands.waitSeconds(3.0),
        Commands.runOnce(() -> shooterManager.stop()),
        Commands.runOnce(() -> preShooterManager.stop()),
        Commands.runOnce(() -> spindexerManager.stop())
    );
}

  public void periodic() {
    var stressData   = stressMonitor.generateData(drivebase);
    stressController.update(stressData);

    double speedScale   = stressController.getSpeedScale();
    double chassisSpeed = drivebase.getRobotVelocity().vxMetersPerSecond;

    stressPublisher.publish(stressData, speedScale, chassisSpeed);

    modePublisher.publishAim(aimLockBack.isActive() ? 1 : 0);
    modePublisher.publishAlign(
    preShooterManager.getMode() == PreShooterManager.ControlMode.AUTO_DISTANCE ? 1 : 0);
    modePublisher.publishShooterLime2Plus(shooterManager.isSpinning() ? 1 : 0);
    modePublisher.publishAimLime4(aimLockFront.isActive() ? 1 : 0);
    modePublisher.publishAlignPiece(alignWithPiece.isActive() ? 1 : 0);
  }

  public SwerveSubsystem getSwerveSubsystem() { return drivebase; }

  public void refreshTagSelection() {
    vision.selectAllHubTags();
    vision.selectAllTowerTags();
  }

  public void setMotorBrake(boolean brake) {
    drivebase.setMotorBrake(brake);
  }
}