// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.Score.Climb;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ClimberSubsystem extends SubsystemBase {
private final SparkMax climb_left = new SparkMax(14, MotorType.kBrushless); 
private final SparkMax climb_right = new SparkMax(15, MotorType.kBrushless); 

private final SparkMaxConfig cfg1;
private final SparkMaxConfig cfg2; 


  public ClimberSubsystem() {

    cfg1 = new SparkMaxConfig();
    cfg1.idleMode(IdleMode.kBrake)
    .smartCurrentLimit(40);

    climb_left.configure(cfg1, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    cfg2 = new SparkMaxConfig();
    cfg2.idleMode(IdleMode.kBrake)
    .smartCurrentLimit(40)
    .inverted(true)
    .follow(climb_left);

    climb_right.configure(
        cfg2, 
        ResetMode.kResetSafeParameters, 
        PersistMode.kPersistParameters);
  }


  public void setClimbPower(double power) {
    climb_left.set(power);
    climb_right.set(power);
  } 

  public void stopClimb() {
    climb_left.stopMotor();
    climb_right.stopMotor();
  }

  public void getPose(){
    climb_left.getEncoder().getPosition();
    climb_right.getEncoder().getPosition();

  }

  @Override
  public void periodic() {
  SmartDashboard.putNumber("Left climb", climb_left.getEncoder().getPosition());
  SmartDashboard.putNumber("Right climb", climb_right.getEncoder().getPosition());

}
}
