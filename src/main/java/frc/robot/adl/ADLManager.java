package frc.robot.adl;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringPublisher;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.Constants;

public class ADLManager {

    private ADLState currentState = ADLState.IDLE;

    private final HumanIntentSource intentSource;
    private final RobotContextProvider contextProvider;
    private final ADLExecutor executor;

    private DecisionResult lastDecision = null;
    private double lastDecisionTime = 0.0;

    private final StringPublisher statePub;
    private final StringPublisher decisionPub;

    public ADLManager(
            HumanIntentSource intentSource,
            RobotContextProvider contextProvider,
            ADLExecutor executor
    ) {
        this.intentSource    = intentSource;
        this.contextProvider = contextProvider;
        this.executor        = executor;

        var nt = NetworkTableInstance.getDefault();
        statePub    = nt.getStringTopic("/ADL/state").publish();
        decisionPub = nt.getStringTopic("/ADL/decision").publish();

        publishState("ADL inicializado");
    }

    public void periodic() {
        HumanIntent intent = intentSource.pollIntent();
        RobotContext context = contextProvider.build();
        double now = Timer.getFPGATimestamp();

        if (intent == null) return;

        if (now - lastDecisionTime < Constants.ADLManager.MIN_DECISION_INTERVAL) return;

        DecisionResult result = ADLDecision.decide(intent, currentState, context);
        lastDecision = result;
        lastDecisionTime = now;
        handleDecision(result);
    }

    private void handleDecision(DecisionResult result) {
        switch (result.type) {
            case EXECUTE:
            case MODIFY:
                currentState = result.state;
                executor.execute(currentState);
                publishState(result.reason);
                break;
            case HOLD:
                publishDecision("HOLD: " + result.reason);
                break;
            case REJECT:
                publishDecision("REJECT: " + result.reason);
                break;
        }
    }

    private void publishState(String reason) {
        statePub.set(currentState.name());
        decisionPub.set(reason);
    }

    private void publishDecision(String reason) {
        decisionPub.set(reason);
    }

    public ADLState getCurrentState() { return currentState; }
    public DecisionResult getLastDecision() { return lastDecision; }
}