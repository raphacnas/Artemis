package frc.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.CommandPS5Controller;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Dashboards.Drive.DriveModePublisher;
import frc.robot.Dashboards.RobotStress.DashboardPublisherStress;
import frc.robot.Dashboards.RobotStress.RobotStressController;
import frc.robot.Dashboards.RobotStress.RobotStressMonitor;
import frc.robot.adl.ADLExecutor;
import frc.robot.adl.ADLManager;
import frc.robot.adl.HumanIntentSource;
import frc.robot.adl.RobotContextProvider;
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

@SuppressWarnings("unused")
public class RobotContainer {

  private final CommandPS5Controller controller;
  @SuppressWarnings("unused")
  private final CommandPS5Controller logitech;

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
  @SuppressWarnings("unused")
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

  private final SendableChooser<String> autoChooser;

  private final ADLManager adlManager;

  public RobotContainer() {

    controller = new CommandPS5Controller(Constants.PS5_ID);
    logitech   = new CommandPS5Controller(Constants.LOGITECH_ID);

    xSupplier = () -> controller.getLeftX();
    ySupplier = () -> controller.getLeftY();

    drivebase           = new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve/neo"));
    vision              = new ViewSubsystem();
    shooterSubsystem    = new ShooterSubsystem();
    intake              = new IntakeAngleManager();
    rollerSubsystem     = new IntakeRollerSubsystem();
    spindexerSubsystem  = new SpindexerSubsystem();
    preShooterSubsystem = new PreShooterSubsystem();

    shooterManager    = new ShooterManager(shooterSubsystem);
    rollerManager     = new IntakeRollerManager(rollerSubsystem);
    spindexerManager  = new SpindexerManager(spindexerSubsystem);
    preShooterManager = new PreShooterManager(preShooterSubsystem, vision, shooterManager);
    climberManager    = new ClimberManager();

    stressMonitor    = new RobotStressMonitor();
    stressController = new RobotStressController();
    stressPublisher  = new DashboardPublisherStress();
    modePublisher    = new DriveModePublisher();

    // Build auto chooser — commands are built lazily when auto starts,
    // not at robot init, to avoid blocking the loop on PathPlanner file loading
    autoChooser = new SendableChooser<>();
    autoChooser.setDefaultOption("AutoRobotRight", "AutoRobotRight");
    autoChooser.addOption("AutoTaxiHUB",           "AutoTaxiHUB");
    autoChooser.addOption("Do Nothing",            "");
    SmartDashboard.putData("Auto Chooser", autoChooser);

    aimLockFront   = new AimLockCommand(drivebase, vision, AimLockCommand.CameraSide.FRONT, xSupplier, ySupplier);
    aimLockBack    = new AimLockCommand(drivebase, vision, AimLockCommand.CameraSide.BACK,  xSupplier, ySupplier);
    alignWithPiece = new AlignWithPieceCommand(drivebase, vision);

    configureBindings();
    DriverStation.silenceJoystickConnectionWarning(true);

    adlManager = new ADLManager(
    new HumanIntentSource(),
    new RobotContextProvider(),
    new ADLExecutor(
        intake,
        rollerManager,
        climberManager,
        preShooterManager,
        shooterManager,
        spindexerManager
    )
);
  }

  private void configureBindings() {

    drivebase.setDefaultCommand(
        new DriveCommand(
            drivebase,
            () -> ySupplier.getAsDouble(),
            () -> xSupplier.getAsDouble(),
            () -> controller.getRightX()
        )
    );

    // --- Main controller (PS5) ---

    controller.L1().onTrue(new InstantCommand(() -> intake.setManualOutput(0.3),  intake))
                   .onFalse(new InstantCommand(() -> intake.stop(), intake));

    controller.R1().onTrue(new InstantCommand(() -> intake.setManualOutput(-0.3), intake))
                   .onFalse(new InstantCommand(() -> intake.stop(), intake));

    // Intake angle - L2/R2 on main controller (no conflict now)
    // new Trigger(() -> controller.getL2Axis() > 0.04)
    //     .onTrue(new InstantCommand(() -> intake.setManual(), intake))
    //     .whileTrue(new RunCommand(() -> intake.setManualOutput(0.3), intake))
    //     .onFalse(new InstantCommand(() -> intake.stop(), intake));

    // new Trigger(() -> controller.getR2Axis() > 0.04)
    //     .onTrue(new InstantCommand(() -> intake.setManual(), intake))
    //     .whileTrue(new RunCommand(() -> intake.setManualOutput(-0.3), intake))
    //     .onFalse(new InstantCommand(() -> intake.stop(), intake));

    controller.povUp().onTrue(new InstantCommand(() -> spindexerManager.toggleSpin(),          spindexerManager));
    controller.povUp().onTrue(new InstantCommand(() -> preShooterManager.toggleManualFeed(),    preShooterManager));
    controller.povRight().onTrue(new InstantCommand(() -> preShooterManager.toggleReverseFeed(),  preShooterManager));
    controller.povUp().onTrue(new InstantCommand(  () -> shooterManager.toggleShooter(),          shooterManager));

    // --- Logitech controller - Climber (no axis conflict) ---

    controller.R2()
        .onTrue(new InstantCommand(() -> climberManager.setManual(), climberManager))
        .whileTrue(new RunCommand(() -> climberManager.setClimbManual(-0.3), climberManager))
        .onFalse(new InstantCommand(() -> climberManager.setStopManualClimb()));

    controller.L2() 
        .onTrue(new InstantCommand(() -> climberManager.setManual(), climberManager))
        .whileTrue(new RunCommand(() -> climberManager.setClimbManual(0.3), climberManager))
        .onFalse(new InstantCommand(() -> climberManager.setStopManualClimb()));

    // --- Vision commands ---
    // Triangle: aim lock front camera (tower alignment)
    controller.triangle().whileTrue(aimLockFront);

    // Cross: aim lock back camera (hub alignment)
    controller.cross().whileTrue(aimLockBack);

    // Square: align with game piece using AI pipeline
    controller.square().whileTrue(alignWithPiece);
  }

  public Command getAutonomousCommand() {
    String selected = autoChooser.getSelected();
    if (selected == null || selected.isEmpty()) return Commands.none();
    return buildAutoCommand(selected);
  }

  /** Wraps a PathPlanner auto name with the standard shooter/spindexer sequence. */
  private Command buildAutoCommand(String autoName) {
    return Commands.sequence(
        Commands.runOnce(() -> shooterManager.start()),
        new PathPlannerAuto(autoName),
        Commands.runOnce(() -> preShooterManager.enableAuto()),
        Commands.runOnce(() -> spindexerManager.start()),
        Commands.waitSeconds(3.0),
        Commands.runOnce(() -> shooterManager.stop()),
        Commands.runOnce(() -> preShooterManager.stop()),
        Commands.runOnce(() -> spindexerManager.stop())
    );
  }

  public void periodic() {

    adlManager.periodic();

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