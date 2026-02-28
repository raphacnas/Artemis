package frc.robot.subsystems.Score.Shooter;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.Score.PreShooter.PreShooterManager.PreShooterState;

    public class ShooterManager extends SubsystemBase {

    public enum ShooterState {
        IDLE,
        SPINNING,
        AT_SPEED,
        DISABLED
    }

    private ShooterState state = ShooterState.IDLE;

    private final ShooterSubsystem shooter;

    private double lastValidDistance = 2.0;

    private final double[] distances = {1.0, 2.0, 2.5, 3.0, 3.5, 4.0};
    private final double[] rpms      = {3400, 3700, 4000, 4300, 4600, 5000};

    public ShooterManager(ShooterSubsystem shooter) {
        this.shooter = shooter;
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

    private double interpolateRPM(double distance) {

        if (distance <= distances[0])
            return rpms[0];

        if (distance >= distances[distances.length - 1])
            return rpms[rpms.length - 1];

        for (int i = 0; i < distances.length - 1; i++) {
            if (distance >= distances[i] && distance <= distances[i + 1]) {

                double t = (distance - distances[i]) /
                           (distances[i + 1] - distances[i]);

                return MathUtil.interpolate(rpms[i], rpms[i + 1], t);
            }
        }

        return 4000;
    }

        @Override
        public void periodic() {

            // System.out.println("Distance: " + vision.getDistanceToTag());


        if (state == ShooterState.IDLE || state == ShooterState.DISABLED) {
            shooter.stop();
            return;
        }

        // double distance = vision.getBackDistanceToTag();
        double distance = 2;

        if (distance != Double.MAX_VALUE) {
            lastValidDistance = distance;
        }

        double rpm = interpolateRPM(lastValidDistance);

        shooter.setTargetRPM(rpm);

        if (shooter.isAtSpeed()) {
            state = ShooterState.AT_SPEED;
        } else {
            state = ShooterState.SPINNING;
        }
    }
}