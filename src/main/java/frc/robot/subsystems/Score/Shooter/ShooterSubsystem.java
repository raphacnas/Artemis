package frc.robot.subsystems.Score.Shooter;

import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.RelativeEncoder;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class ShooterSubsystem extends SubsystemBase {

    private final SparkMax neoMotor =
        new SparkMax(Constants.ShooterConstants.SHOOTER_ID, MotorType.kBrushless);

    private final SparkClosedLoopController controller;
    private final RelativeEncoder encoder;

    private double targetRPM = 0.0;

    public ShooterSubsystem() {

        SparkMaxConfig config = new SparkMaxConfig();

        config.idleMode(IdleMode.kBrake)
              .inverted(true)
              .smartCurrentLimit(40);

        config.closedLoop
              .pid(
                  Constants.ShooterConstants.NEO_kP,
                  Constants.ShooterConstants.NEO_kI,
                  Constants.ShooterConstants.NEO_kD)
              .velocityFF(Constants.ShooterConstants.NEO_kFF)
              .outputRange(-1, 1);

        neoMotor.configure(
            config,
            ResetMode.kResetSafeParameters,
            PersistMode.kPersistParameters
        );

        controller = neoMotor.getClosedLoopController();
        encoder = neoMotor.getEncoder();
    }

    public void setTargetRPM(double rpm) {
        targetRPM = rpm;

        controller.setReference(
            rpm,
            SparkMax.ControlType.kVelocity
        );
    }

    public void stop() {
        targetRPM = 0;
        neoMotor.stopMotor();
    }

    public double getCurrentRPM() {
        return encoder.getVelocity();
    }

    public double getTargetRPM() {
        return targetRPM;
    }

    public boolean isAtSpeed() {
        return Math.abs(getCurrentRPM() - targetRPM)
            < Constants.ShooterConstants.RPM_TOLERANCE;
    }
}
