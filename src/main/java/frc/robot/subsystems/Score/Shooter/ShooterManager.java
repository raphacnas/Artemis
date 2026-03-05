package frc.robot.subsystems.Score.Shooter;

import edu.wpi.first.math.interpolation.InterpolatingDoubleTreeMap;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.Sensors.ViewSubsystem;

public class ShooterManager extends SubsystemBase {

    public enum ShooterState {
        IDLE,
        SPINNING,
        AT_SPEED,
        DISABLED
    }

    private ShooterState state = ShooterState.IDLE;

    private final ShooterSubsystem shooter;
    private final ViewSubsystem vision;

    private double lastValidDistance = 0.0;

    // Tabela de interpolação da WPILib
    private final InterpolatingDoubleTreeMap rpmTable = new InterpolatingDoubleTreeMap();

    public ShooterManager(ShooterSubsystem shooter, ViewSubsystem vision) {
        this.shooter = shooter;
        this.vision = vision;

        // Configuração da tabela: (Distância, RPM)
        // O próprio mapa cuida da interpolação linear e dos limites (clamping)
        rpmTable.put(1.0, 3400.0);
        rpmTable.put(2.0, 3700.0);
        rpmTable.put(2.5, 4000.0);
        rpmTable.put(3.0, 4300.0);
        rpmTable.put(3.5, 4600.0);
        rpmTable.put(4.0, 5000.0);
    }

    // ================= TELEOP =================

    public void toggleShooter() {
        if (state == ShooterState.IDLE) {
            state = ShooterState.SPINNING;
        } else {
            state = ShooterState.IDLE;
        }
    }

    // ================= AUTO =================

    public void enable() {
        if (state != ShooterState.DISABLED) {
            state = ShooterState.SPINNING;
        }
    }

    public void disable() {
        state = ShooterState.IDLE;
    }

    public boolean isEnabled() {
        return state == ShooterState.SPINNING || state == ShooterState.AT_SPEED;
    }

    public boolean isAtSpeed() {
        return state == ShooterState.AT_SPEED;
    }

    public ShooterState getState() {
        return state;
    }

    @Override
    public void periodic() {
        
        // System.out.println("Distance: " + vision.getBackDistanceToTag());

        if (state == ShooterState.IDLE || state == ShooterState.DISABLED) {
            shooter.stop();
            return;
        }

        double distance = vision.getBackDistanceToTag();

        if (distance != Double.MAX_VALUE) {
            lastValidDistance = distance;
        }

        // Obtém o RPM interpolado automaticamente
        double rpm = rpmTable.get(lastValidDistance);

        shooter.setTargetRPM(rpm);

        // Atualização de estado baseada no feedback do motor
        if (shooter.isAtSpeed()) {
            state = ShooterState.AT_SPEED;
        } else {
            state = ShooterState.SPINNING;
        }
    }
}
