package frc.robot.adl;
public class DecisionResult {

    public final DecisionResultType type;
    public final ADLState state;
    public final String reason;

    private DecisionResult(
            DecisionResultType type,
            ADLState state,
            String reason
    ) {
        this.type = type;
        this.state = state;
        this.reason = reason;
    }

    public static DecisionResult execute(ADLState state, String reason) {
        return new DecisionResult(
            DecisionResultType.EXECUTE,
            state,
            reason
        );
    }

    public static DecisionResult modify(ADLState state, String reason) {
        return new DecisionResult(
            DecisionResultType.MODIFY,
            state,
            reason
        );
    }

    public static DecisionResult hold(ADLState state, String reason) {
        return new DecisionResult(
            DecisionResultType.HOLD,
            state,
            reason
        );
    }

    public static DecisionResult reject(ADLState state, String reason) {
        return new DecisionResult(
            DecisionResultType.REJECT,
            state,
            reason
        );
    }
}
