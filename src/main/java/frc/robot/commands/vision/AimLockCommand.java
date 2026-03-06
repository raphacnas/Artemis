package frc.robot.commands.vision;

import java.util.function.DoubleSupplier;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
import frc.robot.subsystems.Sensors.ViewSubsystem;

public class AimLockCommand extends Command {

  public enum CameraSide { FRONT, BACK }

  private final SwerveSubsystem swerve;
  private final ViewSubsystem vision;
  private final CameraSide side;

  private final DoubleSupplier xSupplier;
  private final DoubleSupplier ySupplier;

  private static final double FRONT_CAMERA_OFFSET_RAD =
    Units.degreesToRadians(2); // ajuste fino para alinhar melhor com a torre, já que o centro da câmera frontal não é exatamente o centro do robô 

  private final ProfiledPIDController headingPID =
      new ProfiledPIDController(
          3.0,
          0.0,
          0.2,
          new TrapezoidProfile.Constraints(
              Units.degreesToRadians(150),
              Units.degreesToRadians(300)));

  public AimLockCommand(
      SwerveSubsystem swerve,
      ViewSubsystem vision,
      CameraSide side,
      DoubleSupplier xSupplier,
      DoubleSupplier ySupplier) {

    this.swerve = swerve;
    this.vision = vision;
    this.side = side;
    this.xSupplier = xSupplier;
    this.ySupplier = ySupplier;

    headingPID.enableContinuousInput(-Math.PI, Math.PI);
    addRequirements(swerve);
  }

  private boolean active = false;

  public boolean isActive() {
    return active;
  }

  @Override
  public void initialize() {
    headingPID.reset(0);
    active = true;
  }

  @Override
  public void execute() {

    if (vision == null) return;

    boolean valid =
        side == CameraSide.FRONT
            ? vision.hasValidFrontTarget()
            : vision.hasValidBackTarget();

    if (!valid) {
      swerve.drive(
          new Translation2d(
              xSupplier.getAsDouble(),
              ySupplier.getAsDouble()),
          0.0,
          false);
      return;
    }

    double tx;

    if (side == CameraSide.FRONT) {
    tx = vision.getFrontTxRad() + FRONT_CAMERA_OFFSET_RAD;
   } else {
    tx = vision.getBackTxRad();
    }

    double rot = headingPID.calculate(tx, 0.0);
    rot = Math.max(Math.min(rot, 3.0), -3.0);

    swerve.drive(
        new Translation2d(
            xSupplier.getAsDouble(),
            ySupplier.getAsDouble()),
        rot,
        false);
  }

  @Override
  public void end(boolean interrupted) {
    active = false;
    swerve.drive(new Translation2d(), 0.0, false);
  }

  @Override
public boolean isFinished() {
  if (side == CameraSide.FRONT) {
    return Math.abs(vision.getFrontTxRad()) < Units.degreesToRadians(1.5);
  } else {
    return Math.abs(vision.getBackTxRad()) < Units.degreesToRadians(1.5);
  }
}

}