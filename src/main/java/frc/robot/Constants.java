package frc.robot;

import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;

import swervelib.math.Matter;

public final class Constants {

  public static final int PS5_ID = 0;
  public static final int LOGITECH_ID = 1;

  public static final double ROBOT_MASS = 56;
  public static final Matter CHASSIS = new Matter(new Translation3d(0, 0, Units.inchesToMeters(8)), ROBOT_MASS);
  public static final double LOOP_TIME = 0.13;
  public static final double MAX_SPEED = Units.feetToMeters(12);
  public static final double K_AUTO_PIECE_FORWARD = -0.1;
  public static final double TA_TARGET = 5;

  public static final class DrivebaseConstants {
    public static final double WHEEL_LOCK_TIME = 10;
  }

  public static class OperatorConstants {
    public static final double DEADBAND = 0.05;
    public static final double LEFT_Y_DEADBAND = 0.05;
    public static final double RIGHT_X_DEADBAND = 0.05;
    public static final double TURN_CONSTANT = 2;
  }

  public static class LimelightConstants {
    public static final double LIMELIGHT_HEIGHT = 0.70;   // metros
    public static final double TAG_HEIGHT = 1.041;         // metros
    public static final double LIMELIGHT_ANGLE = Units.degreesToRadians(0);    

  }
 
  public static class IntakeConstants {
    
    public static final int INTAKE_LEADER_ID = 9;  

    public static final int ANGLE_MOTOR_ID = 10;     
    public static final int ANGLE_ENCODER_ID = 0;  
    
    public static final double ANGLE_KP = 0.0015;
    public static final double ANGLE_KI = 0.0;
    public static final double ANGLE_KD = 0.001;

    public static final double ANGLE_KS = 0.08;
    public static final double ANGLE_KG = 0.07;
    public static final double ANGLE_KV = 0.0;
    public static final double ANGLE_KA = 0.0;
        
    public static final double ANGLE_TOLERANCE_DEG = 1;
    public static final String PREF_ENCODER_OFFSET = "Encoder offset Intake Angle";
    
  
    public static final double INTAKE_POWER = 1;
    public static final double OUTTAKE_POWER = -1;

    public static final double ANGLE_MAX_OUTPUT = 0.0;

  }

  public static class SpindexerConstants {

    public static final double SPIN_POWER = -1;
    public static final int SPINNER_ID = 11;
  
  }

  public static class PreShooterConstants {
    
    public static final int PRE_SHOOTER_ID = 12;
    public static final double FEED_POWER = 1;
  
  }

  public static class ShooterConstants{

    public static final int SHOOTER_ID = 13;

    public static final double NEO_kP = 0.0002;
    public static final double NEO_kI = 0.0;
    public static final double NEO_kD = 0.0;
    public static final double NEO_kFF = 0.00018;

    public static final double NEO_TARGET_RPM = 3100;

    public static final double RPM_TOLERANCE = 100;
    
  }
    public static class ClimbConstants {

    public static final double CLIMBER_kP = 0.02;
    public static final double CLIMBER_kI = 0.0;
    public static final double CLIMBER_kD = 0.0;

    public static final double CLIMBER_MAX_OUTPUT = 0.25;
    public static final double CLIMBER_MIN_OUTPUT = -0.25;
    
    public static final int CLIMBER_LEFT_ID = 14;
    public static final int CLIMBER_RIGHT_ID = 15;
    public static final double CLIMBER_TOLERANCE = 0.7;
   
    public static final String PREF_MIN_KEY = "Climber min position";
    public static final String PREF_MAX_KEY = "Climber max position";
}

}