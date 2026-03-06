// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.Swervedrive;

import static edu.wpi.first.units.Units.Meter;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Config;
import frc.robot.Constants;
import java.io.File;
import java.util.Arrays;
import java.util.function.Supplier;



import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.commands.PathfindingCommand;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.pathplanner.lib.path.PathConstraints;
import com.revrobotics.spark.SparkMax;

import swervelib.SwerveController;
import swervelib.SwerveDrive;
import swervelib.SwerveDriveTest;
import swervelib.parser.SwerveControllerConfiguration;
import swervelib.parser.SwerveDriveConfiguration;
import swervelib.parser.SwerveParser;
import swervelib.telemetry.SwerveDriveTelemetry;
import swervelib.telemetry.SwerveDriveTelemetry.TelemetryVerbosity;

public class SwerveSubsystem extends SubsystemBase{
 
  private final SwerveDrive swerveDrive;

   public SwerveSubsystem(File directory){ 
    boolean blueAlliance = DriverStation.getAlliance().isPresent() && DriverStation.getAlliance().get() == Alliance.Blue;
        Pose2d startingPose = new Pose2d();

    SwerveDriveTelemetry.verbosity = TelemetryVerbosity.HIGH;
    try{
      swerveDrive = new SwerveParser(directory).createSwerveDrive(Constants.MAX_SPEED, startingPose);
      
    } catch (Exception e)
    {
      throw new RuntimeException(e);
    }
    swerveDrive.setCosineCompensator(true);
    swerveDrive.setAngularVelocityCompensation(false,false, 0);
    swerveDrive.setModuleEncoderAutoSynchronize(false,1); 
    setupPathPlanner();
  }

  public ChassisSpeeds getRobotRelativeSpeeds() {
    return swerveDrive.getRobotVelocity();
}

  private void setupPathPlanner() {

    try {

        AutoBuilder.configure(
            this::getPose,
            this::resetOdometry,
            this::getRobotRelativeSpeeds,

            (speeds, feedforwards) -> {
                swerveDrive.setChassisSpeeds(speeds);
            },

            new PPHolonomicDriveController(
                new PIDConstants(5.0, 0.0, 0.0),
                new PIDConstants(5.0, 0.0, 0.0)
            ),

            RobotConfig.fromGUISettings(),

            () -> DriverStation.getAlliance().isPresent()
                    && DriverStation.getAlliance().get() == DriverStation.Alliance.Red,

            this
        );

    } catch (Exception e) {
        e.printStackTrace();
    }

    PathfindingCommand.warmupCommand().schedule();
}
  // ================= HEADING PID =================

private final ProfiledPIDController headingPID =
    new ProfiledPIDController(
        2.0,
        0.0,
        0.2,
        new TrapezoidProfile.Constraints(
            Units.degreesToRadians(60),
            Units.degreesToRadians(100)));

{
    headingPID.enableContinuousInput(-Math.PI, Math.PI);
}

public ProfiledPIDController getHeadingPID() {
    return headingPID;
}

public void stop() {
    drive(new Translation2d(), 0.0, false);
}
 
  public SwerveSubsystem(SwerveDriveConfiguration driveCfg, SwerveControllerConfiguration controllerCfg)
  {
    swerveDrive = new SwerveDrive(driveCfg,
                                  controllerCfg,
                                  Constants.MAX_SPEED,
                                  new Pose2d(new Translation2d(Meter.of(2), Meter.of(0)),
                                             Rotation2d.fromDegrees(0)));
  }

  @Override
  public void periodic(){
  }

  @Override
  public void simulationPeriodic(){
  }

  public Command sysIdDriveMotorCommand()
  {
    return SwerveDriveTest.generateSysIdCommand(
        SwerveDriveTest.setDriveSysIdRoutine(
            new Config(),
            this, swerveDrive, 12, true),
        3.0, 5.0, 3.0);
  }

  public Command sysIdAngleMotorCommand()
  {
    return SwerveDriveTest.generateSysIdCommand(
        SwerveDriveTest.setAngleSysIdRoutine(
            new Config(),
            this, swerveDrive),
        3.0, 5.0, 3.0);
  }

  public Command centerModulesCommand(){
    return run(() -> Arrays.asList(swerveDrive.getModules())
                           .forEach(it -> it.setAngle(0.0)));
  }

  public Command driveForward()
  {
    return run(() -> {
      swerveDrive.drive(new Translation2d(1, 0), 0, false, false);
    }).finallyDo(() -> swerveDrive.drive(new Translation2d(0, 0), 0, false, false));
  }

  public void replaceSwerveModuleFeedforward(double kS, double kV, double kA){
    swerveDrive.replaceSwerveModuleFeedforward(new SimpleMotorFeedforward(kS, kV, kA));
  }


  public void drive(Translation2d translation, double rotation, boolean fieldRelative){
    swerveDrive.drive(translation,
                      rotation,
                      fieldRelative,
                      false); 
  }

  public void driveFieldOriented(ChassisSpeeds velocity){
    swerveDrive.driveFieldOriented(velocity);
  }

  public Command driveFieldOriented(Supplier<ChassisSpeeds> velocity){
    return run(() -> {
      swerveDrive.driveFieldOriented(velocity.get());
    });
  }

  public void drive(ChassisSpeeds velocity){
    swerveDrive.drive(velocity);
  }

  public SwerveDriveKinematics getKinematics(){
    return swerveDrive.kinematics;
  }

  public void resetOdometry(Pose2d initialHolonomicPose){
    swerveDrive.resetOdometry(initialHolonomicPose);
  }

 
  public Pose2d getPose(){
    return swerveDrive.getPose();
  }

  public void setChassisSpeeds(ChassisSpeeds chassisSpeeds){
    swerveDrive.setChassisSpeeds(chassisSpeeds);
  }

  
  public void postTrajectory(Trajectory trajectory){
    swerveDrive.postTrajectory(trajectory);
  }

  public void zeroGyro(){
    swerveDrive.zeroGyro();
  }

  private boolean isRedAlliance(){
    var alliance = DriverStation.getAlliance();
    return alliance.isPresent() ? alliance.get() == DriverStation.Alliance.Red : false;
  }

  public void zeroGyroWithAlliance(){
    if (isRedAlliance())
    {
      zeroGyro();
      resetOdometry(new Pose2d(getPose().getTranslation(), Rotation2d.fromDegrees(180)));
    } else
    {
      zeroGyro();
    }
  }

  public void setMotorBrake(boolean brake){
    swerveDrive.setMotorIdleMode(brake);
  }
  
  public Rotation2d getHeading(){
    return getPose().getRotation();
  }

  public ChassisSpeeds getTargetSpeeds(double xInput, double yInput, double headingX, double headingY){
    return swerveDrive.swerveController.getTargetSpeeds(xInput,
                                                        yInput,
                                                        headingX,
                                                        headingY,
                                                        getHeading().getRadians(),
                                                        Constants.MAX_SPEED);
  }

  public ChassisSpeeds getTargetSpeeds(double xInput, double yInput, Rotation2d angle){
    return swerveDrive.swerveController.getTargetSpeeds(xInput,
                                                        yInput,
                                                        angle.getRadians(),
                                                        getHeading().getRadians(),
                                                        Constants.MAX_SPEED);
  }

public double getTotalRobotCurrent() {
    double sum = 0.0;
    for (var module : swerveDrive.getModules()) {
      Object drive = module.getDriveMotor().getMotor();
      Object angle = module.getAngleMotor().getMotor();
      if (RobotBase.isReal()) {
        if (drive instanceof SparkMax d) sum += d.getOutputCurrent();
        if (angle instanceof SparkMax a) sum += a.getOutputCurrent();
      }
    }
    return sum;
  }

  public ChassisSpeeds getFieldVelocity(){
    return swerveDrive.getFieldVelocity();
  }

  public ChassisSpeeds getRobotVelocity(){
    return swerveDrive.getRobotVelocity();
  }

  public SwerveController getSwerveController(){
    return swerveDrive.swerveController;
  }

  public SwerveDriveConfiguration getSwerveDriveConfiguration(){
    return swerveDrive.swerveDriveConfiguration;
  }

  public void lock(){
    swerveDrive.lockPose();
  }

  
  public Rotation2d getPitch(){
    return swerveDrive.getPitch();
  }

  public SwerveDrive getSwerveDrive(){
    return swerveDrive;
  }
  
  public PathConstraints getPathConstraints() {
  return new PathConstraints(
      3.0,
      2.5,
      Math.PI,
      Math.PI * 2
  );
}


}