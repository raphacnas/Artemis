package frc.robot;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.UsbCamera;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

public class Robot extends TimedRobot {

  private Command m_autonomousCommand;
  private RobotContainer m_robotContainer;

  @Override
  public void robotInit() {
    m_robotContainer = new RobotContainer();

    UsbCamera camera = CameraServer.startAutomaticCapture(0);
    camera.setResolution(640, 480);
    camera.setFPS(30);
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
    m_robotContainer.periodic();
  }

  @Override
  public void disabledInit() {
    if (m_robotContainer.getSwerveSubsystem() != null) {
      m_robotContainer.getSwerveSubsystem().setMotorBrake(true);
    }
  }

  @Override
  public void disabledPeriodic() {}

  @Override
  public void autonomousInit() {
    m_robotContainer.refreshTagSelection();
    m_autonomousCommand = m_robotContainer.getAutonomousCommand();

    if (m_robotContainer.getSwerveSubsystem() != null) {
      m_robotContainer.getSwerveSubsystem().zeroGyroWithAlliance();
      m_robotContainer.getSwerveSubsystem().setMotorBrake(true);
    }

    if (m_autonomousCommand != null) {
      m_autonomousCommand.schedule();
    }
  }

  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {
    m_robotContainer.refreshTagSelection();

    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }

    if (m_robotContainer.getSwerveSubsystem() != null) {
      m_robotContainer.getSwerveSubsystem().setMotorBrake(true);
    }
  }

  @Override
  public void teleopPeriodic() {}

  @Override
  public void testInit() {
    CommandScheduler.getInstance().cancelAll();
  }

  @Override
  public void testPeriodic() {}
}