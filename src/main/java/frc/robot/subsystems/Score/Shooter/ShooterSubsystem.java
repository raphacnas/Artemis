package frc.robot.subsystems.Score.Shooter;

import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.RelativeEncoder;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class ShooterSubsystem extends SubsystemBase {

    private final SparkMax neoMotor =
        new SparkMax(Constants.ShooterConstants.SHOOTER_ID, MotorType.kBrushless);

    

    public ShooterSubsystem() {

        SparkMaxConfig config = new SparkMaxConfig();

        config.idleMode(IdleMode.kBrake)
              .inverted(false)
              .smartCurrentLimit(40);

        neoMotor.configure(
            config,
            ResetMode.kResetSafeParameters,
            PersistMode.kPersistParameters
        );

        
    }


    public void stop() {
        neoMotor.stopMotor();
    }

    public void setpower(){
        neoMotor.set(0.61);
    }

   
}
