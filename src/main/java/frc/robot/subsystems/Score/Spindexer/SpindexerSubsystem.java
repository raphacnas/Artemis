package frc.robot.subsystems.Score.Spindexer;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class SpindexerSubsystem extends SubsystemBase {

  private SparkMax SpinMotor = new SparkMax(11, MotorType.kBrushed);
   

    private SparkMaxConfig cfg = new SparkMaxConfig();

  public SpindexerSubsystem() {
    cfg.idleMode(IdleMode.kBrake)
            .smartCurrentLimit(40,20);

    SpinMotor.configure(cfg, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}
