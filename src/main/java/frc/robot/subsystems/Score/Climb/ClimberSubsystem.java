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
import frc.robot.Constants;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ClimberSubsystem extends SubsystemBase {
private final SparkMax climb_left = new SparkMax(Constants.ClimbConstants.CLIMBER_LEFT_ID, MotorType.kBrushless); 
private final SparkMax climb_right = new SparkMax(Constants.ClimbConstants.CLIMBER_RIGHT_ID, MotorType.kBrushless); 

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
    .inverted(true);


    climb_right.configure(
        cfg2, 
        ResetMode.kResetSafeParameters, 
        PersistMode.kPersistParameters);

   climb_left.configure(cfg1, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
  }


  public void setClimbPower(double power) {
    climb_left.set(power);
   climb_right.set(power);
  } 

  public void stopClimb() {
    climb_left.stopMotor();
    climb_right.stopMotor();
  }

  public double getPosition() {
    return climb_left.getEncoder().getPosition();
}

  @Override
  public void periodic() {
  SmartDashboard.putNumber("Left climb", climb_left.getEncoder().getPosition());

}
}