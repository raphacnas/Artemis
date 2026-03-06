package frc.robot.adl;
import edu.wpi.first.wpilibj.Timer;

public final class HumanIntent {
    public enum Type {
        ACQUIRE_PIECE,   // pegar uma peça
        SCORE_PIECE,     // pontuar uma peça
        MOVE_TO_ZONE,    // ir para uma área do campo
        ESCAPE,          // sair de uma situação perigosa
        CLIMB,           // escalar
        HOLD_POSITION,   // manter posição
        ABORT            // cancelar imediatamente
    }

    public enum GameZone {
        DEPOT,
        TRENCH,
        HUB,
        OUTPOST,
        TOWER,
        BUMP,
        UNKNOWN
    }

    private final Type type;
    private final GameZone targetZone;
    private final double urgency;
    private final boolean requiresVision;
    private final double timestamp;

    private HumanIntent(
            Type type,
            GameZone targetZone,
            double urgency,
            boolean requiresVision
    ) 
    
    {
        this.type = type;
        this.targetZone = targetZone;
        this.urgency = urgency;
        this.requiresVision = requiresVision;
        this.timestamp = Timer.getFPGATimestamp();
    }

    public static HumanIntent acquirePiece(GameZone zone, boolean vision) {
        return new HumanIntent(
                Type.ACQUIRE_PIECE,
                zone,
                0.7,
                vision
        );
    }

    public static HumanIntent scorePiece(GameZone zone, boolean vision) {
        return new HumanIntent(
                Type.SCORE_PIECE,
                zone,
                1.0,
                vision
        );
    }

    public static HumanIntent moveTo(GameZone zone) {
        return new HumanIntent(
                Type.MOVE_TO_ZONE,
                zone,
                0.5,
                false
        );
    }

    public static HumanIntent climb() {
        return new HumanIntent(
                Type.CLIMB,
                GameZone.TOWER,
                0.9,
                false
        );
    }

    public static HumanIntent abort() {
        return new HumanIntent(
                Type.ABORT,
                GameZone.UNKNOWN,
                1.0,
                false
        );
    }
    
    public Type getType() {
        return type;
    }

    public GameZone getTargetZone() {
        return targetZone;
    }

    public double getUrgency() {
        return urgency;
    }

    public boolean requiresVision() {
        return requiresVision;
    }

    public double getTimestamp() {
        return timestamp;
    }
}
