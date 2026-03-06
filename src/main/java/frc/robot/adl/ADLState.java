package frc.robot.adl;

public enum ADLState {
    IDLE,
    MOVING,
    ACQUIRING,
    SCORING,
    CLIMBING,
    BLOCKED,
    EMERGENCY;

    public boolean isBusy() {
        return this == MOVING
            || this == ACQUIRING
            || this == SCORING
            || this == CLIMBING;
    }

    public boolean isCritical() {
        return this == BLOCKED || this == EMERGENCY;
    }
}
