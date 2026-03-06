package frc.robot.adl;

public class ADLDecision {

    public static DecisionResult decide(
        HumanIntent intent,
        ADLState currentState,
        RobotContext context
    ) {

        if (intent == null) {
            return DecisionResult.hold(currentState, "Sem intenção");
        }

        if (intent.getType() == HumanIntent.Type.ABORT) {
            return DecisionResult.execute(
                ADLState.EMERGENCY,
                "Abort solicitado"
            );
        }

        if (currentState.isCritical()) {
            return DecisionResult.reject(
                currentState,
                "Estado crítico: " + currentState
            );
        }

        if (intent.requiresVision() && !context.canUseVision()) {
            return DecisionResult.hold(
                currentState,
                "Visão não confiável"
            );
        }

        if (!context.isRobotHealthy()
                && intent.getUrgency() < 0.9) {
            return DecisionResult.reject(
                currentState,
                "Robô sob stress alto"
            );
        }

        if (context.endgame
                && intent.getType() != HumanIntent.Type.CLIMB) {
            return DecisionResult.reject(
                currentState,
                "Endgame: apenas climb permitido"
            );
        }

        if (currentState.isBusy()) {
            return DecisionResult.hold(
                currentState,
                "Robô ocupado"
            );
        }

        switch (intent.getType()) {

            case ACQUIRE_PIECE:
                return DecisionResult.execute(
                    ADLState.ACQUIRING,
                    "Adquirindo peça em " + intent.getTargetZone()
                );

            case SCORE_PIECE:
                return DecisionResult.execute(
                    ADLState.SCORING,
                    "Pontuando em " + intent.getTargetZone()
                );

            case MOVE_TO_ZONE:
                return DecisionResult.execute(
                    ADLState.MOVING,
                    "Movendo para " + intent.getTargetZone()
                );

            case CLIMB:
                return DecisionResult.execute(
                    ADLState.CLIMBING,
                    "Iniciando climb"
                );

            case HOLD_POSITION:
                return DecisionResult.execute(
                    ADLState.IDLE,
                    "Mantendo posição"
                );

            case ESCAPE:
                return DecisionResult.execute(
                    ADLState.MOVING,
                    "Manobra de escape"
                );

            default:
                return DecisionResult.reject(
                    currentState,
                    "Intenção desconhecida"
                );
        }
    }
}
