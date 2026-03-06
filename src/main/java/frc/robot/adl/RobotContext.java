package frc.robot.adl;
import edu.wpi.first.math.geometry.Pose2d;

public class RobotContext {

    public final double batteryVoltage;
    public final double stressScore;
    public final String stressLevel;
    public final boolean speedLimited;

    public final boolean hasGamePiece;
    public final boolean intakeActive;
    public final boolean shooterReady;
    public final boolean climbAvailable;

    public final boolean visionHasTarget;
    public final boolean visionAligned;
    public final double visionConfidence;

    public final Pose2d robotPose;
    public final boolean robotMoving;
    public final HumanIntent.GameZone currentZone;
    
    public final boolean endgame;

    private RobotContext(Builder b) {
        this.batteryVoltage   = b.batteryVoltage;
        this.stressScore      = b.stressScore;
        this.stressLevel      = b.stressLevel;
        this.speedLimited     = b.speedLimited;

        this.hasGamePiece     = b.hasGamePiece;
        this.intakeActive     = b.intakeActive;
        this.shooterReady     = b.shooterReady;
        this.climbAvailable   = b.climbAvailable;

        this.visionHasTarget  = b.visionHasTarget;
        this.visionAligned    = b.visionAligned;
        this.visionConfidence = b.visionConfidence;

        this.robotPose        = b.robotPose;
        this.robotMoving      = b.robotMoving;
        this.currentZone      = b.currentZone;

        this.endgame          = b.endgame;
    }

    public boolean isRobotHealthy() {
        return !speedLimited && stressScore < 70;
    }

    public boolean canUseVision() {
        return visionHasTarget && visionConfidence > 0.6;
    }

    public boolean safeToMoveFast() {
        return isRobotHealthy() && !robotMoving;
    }

    public static class Builder {

        private double batteryVoltage;
        private double stressScore;
        private String stressLevel;
        private boolean speedLimited;

        private boolean hasGamePiece;
        private boolean intakeActive;
        private boolean shooterReady;
        private boolean climbAvailable;

        private boolean visionHasTarget;
        private boolean visionAligned;
        private double visionConfidence;

        private Pose2d robotPose;
        private boolean robotMoving;
        private HumanIntent.GameZone currentZone;

        private boolean endgame;

        public Builder battery(double voltage, double stressScore, String stressLevel, boolean speedLimited) {
            this.batteryVoltage = voltage;
            this.stressScore = stressScore;
            this.stressLevel = stressLevel;
            this.speedLimited = speedLimited;
            return this;
        }

        public Builder mechanisms(boolean hasPiece, boolean intake, boolean shooter, boolean climb) {
            this.hasGamePiece = hasPiece;
            this.intakeActive = intake;
            this.shooterReady = shooter;
            this.climbAvailable = climb;
            return this;
        }

        public Builder vision(boolean hasTarget, boolean aligned, double confidence) {
            this.visionHasTarget = hasTarget;
            this.visionAligned = aligned;
            this.visionConfidence = confidence;
            return this;
        }

        public Builder movement(Pose2d pose, boolean moving, HumanIntent.GameZone zone) {
            this.robotPose = pose;
            this.robotMoving = moving;
            this.currentZone = zone;
            return this;
        }

        public Builder gameTime(boolean endgame) {
            this.endgame = endgame;
            return this;
        }

        public RobotContext build() {
            return new RobotContext(this);
        }
    }
}
