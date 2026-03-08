package frc.robot.subsystems.Score.PreShooter;

import com.revrobotics.spark.*;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.config.*;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class PreShooterSubsystem extends SubsystemBase {

    private final SparkMax redlineMotor = new SparkMax(
        Constants.PreShooterConstants.PRE_SHOOTER_ID,
        MotorType.kBrushed);

    @SuppressWarnings("removal")
    public PreShooterSubsystem() {
        SparkMaxConfig config = new SparkMaxConfig();

        config.idleMode(SparkBaseConfig.IdleMode.kBrake)
            .inverted(true)
            .smartCurrentLimit(40);

        redlineMotor.configure(
            config,
            ResetMode.kResetSafeParameters,
            PersistMode.kPersistParameters);
    }

    public void feed() {
        redlineMotor.set(Constants.PreShooterConstants.FEED_POWER);
    }

    public void reverseFeed() {
    redlineMotor.set(-Constants.PreShooterConstants.FEED_POWER);
}

    public void stop() {
        redlineMotor.stopMotor();
    }
}
