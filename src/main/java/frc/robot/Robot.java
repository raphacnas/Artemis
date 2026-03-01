// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;

import java.io.File;

import org.littletonrobotics.junction.LoggedRobot;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.NT4Publisher;
import org.littletonrobotics.junction.wpilog.WPILOGWriter;

public class Robot extends LoggedRobot {
  private Command m_autonomousCommand;
  private final RobotContainer robotContainer;
  

  public Robot() {
   
    robotContainer = new RobotContainer();
 
  }

   @Override
public void robotInit() {

  // Logger.recordMetadata("ProjectName", "SwerveBinga"); 
  // Logger.recordMetadata("RuntimeType", RobotBase.getRuntimeType().toString());

  // if (RobotBase.isSimulation()) {
  //   Logger.addDataReceiver(new WPILOGWriter("logs"));
  //   Logger.addDataReceiver(new NT4Publisher());
  // } else {
  //   Logger.addDataReceiver(new WPILOGWriter("/U/logs"));
  //   Logger.addDataReceiver(new NT4Publisher());
  // }

  // Logger.start();  
}
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
    Logger.recordOutput("Robot/LoopTimeSec", edu.wpi.first.wpilibj.Timer.getFPGATimestamp() );
    SmartDashboard.putNumber("teste", 123);
    
    //robotContainer.updateDashboards();
   
  }
  

  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {}

  
@Override
public void autonomousInit() {

    m_autonomousCommand = robotContainer.getAutonomousCommand();

    if (m_autonomousCommand != null) {
        m_autonomousCommand.schedule();
    }
}


  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {
    // This makes sure that the autonomous stops running when
    // teleop starts running. If you want the autonomous to
    // continue until interrupted by another command, remove
    // this line or comment it out.
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {}

  @Override
  public void testInit() {
    // Cancels all running commands at the start of test mode.
    CommandScheduler.getInstance().cancelAll();
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
}
