package frc.robot.adl;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.BooleanSubscriber;
import edu.wpi.first.networktables.DoubleSubscriber;
import edu.wpi.first.networktables.StringSubscriber;
import edu.wpi.first.math.geometry.Pose2d;

public class RobotContextProvider {

    private final DoubleSubscriber batteryVoltageSub, stressScoreSub, visionConfidenceSub;
    private final StringSubscriber stressLevelSub, currentZoneSub;
    private final BooleanSubscriber speedLimitedSub, hasPieceSub, intakeActiveSub,
            shooterReadySub, climbAvailableSub, visionHasTargetSub, visionAlignedSub,
            robotMovingSub, endgameSub;

    public RobotContextProvider() {
        var nt = NetworkTableInstance.getDefault();
        batteryVoltageSub  = nt.getDoubleTopic("/Robot/BatteryVoltage").subscribe(12.0);
        stressScoreSub     = nt.getDoubleTopic("/Robot/StressScore").subscribe(0.0);
        stressLevelSub     = nt.getStringTopic("/Robot/StressLevel").subscribe("LOW");
        speedLimitedSub    = nt.getBooleanTopic("/Robot/SpeedLimited").subscribe(false);
        hasPieceSub        = nt.getBooleanTopic("/Mechanisms/HasGamePiece").subscribe(false);
        intakeActiveSub    = nt.getBooleanTopic("/Mechanisms/IntakeActive").subscribe(false);
        shooterReadySub    = nt.getBooleanTopic("/Mechanisms/ShooterReady").subscribe(false);
        climbAvailableSub  = nt.getBooleanTopic("/Mechanisms/ClimbAvailable").subscribe(false);
        visionHasTargetSub = nt.getBooleanTopic("/Vision/HasTarget").subscribe(false);
        visionAlignedSub   = nt.getBooleanTopic("/Vision/Aligned").subscribe(false);
        visionConfidenceSub= nt.getDoubleTopic("/Vision/Confidence").subscribe(0.0);
        robotMovingSub     = nt.getBooleanTopic("/Drive/Moving").subscribe(false);
        currentZoneSub     = nt.getStringTopic("/Robot/CurrentZone").subscribe("UNKNOWN");
        endgameSub         = nt.getBooleanTopic("/Game/Endgame").subscribe(false);
    }

    public RobotContext build() {
        HumanIntent.GameZone zone;
        try { zone = HumanIntent.GameZone.valueOf(currentZoneSub.get()); }
        catch (Exception e) { zone = HumanIntent.GameZone.UNKNOWN; }

        return new RobotContext.Builder()
            .battery(batteryVoltageSub.get(), stressScoreSub.get(), stressLevelSub.get(), speedLimitedSub.get())
            .mechanisms(hasPieceSub.get(), intakeActiveSub.get(), shooterReadySub.get(), climbAvailableSub.get())
            .vision(visionHasTargetSub.get(), visionAlignedSub.get(), visionConfidenceSub.get())
            .movement(new Pose2d(), robotMovingSub.get(), zone)
            .gameTime(endgameSub.get())
            .build();
    }
}