package frc.robot.adl;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringSubscriber;

public class HumanIntentSource {

    private final StringSubscriber intentSub;
    private String lastCommand = "";

    public HumanIntentSource() {
        intentSub = NetworkTableInstance.getDefault()
            .getStringTopic("/ADL/intent")
            .subscribe("");
    }

    public HumanIntent pollIntent() {

        String cmd = intentSub.get();
        if (cmd.isEmpty() || cmd.equals(lastCommand)) {
            return null;
        }

        lastCommand = cmd;

        switch (cmd) {

            case "ACQUIRE_PIECE":
                return HumanIntent.acquirePiece(
                    HumanIntent.GameZone.BUMP,
                    true 
                );

            case "SCORE":
                return HumanIntent.scorePiece(
                    HumanIntent.GameZone.HUB,
                    true
                );

            case "MOVE_DEPOT":
                return HumanIntent.moveTo(
                    HumanIntent.GameZone.DEPOT
                );

            case "MOVE_BUMP":
                return HumanIntent.moveTo(
                    HumanIntent.GameZone.BUMP
                );

            case "CLIMB":
                return HumanIntent.climb();

            case "ABORT":
                return HumanIntent.abort();

            default:
                return null;
        }
    }
}
