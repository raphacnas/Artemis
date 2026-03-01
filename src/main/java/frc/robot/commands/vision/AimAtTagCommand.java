package frc.robot.commands.vision;

import java.lang.constant.Constable;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.PS5Controller;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
import frc.robot.subsystems.Sensors.ViewSubsystem;

public class AimAtTagCommand extends Command {

  public enum CameraSide {
    FRONT,
    BACK
  }

  private final SwerveSubsystem swerve;
  private final ViewSubsystem vision;
  private final CameraSide side;
  private final double xSupplier, ySupplier;

  private boolean enabled = false;

  public AimAtTagCommand(
      SwerveSubsystem swerve,
      ViewSubsystem vision,
      CameraSide side,
      double xSupplier,
      double ySupplier
  ) {
    this.swerve = swerve;
    this.vision = vision;
    this.side = side;
    this.xSupplier = xSupplier;
    this.ySupplier = ySupplier;

    addRequirements(swerve);
  }

  public void toggle() {
    enabled = !enabled;

    if (enabled) {
      schedule();
    } else {
      cancel();
    }
  }

  @Override
  public void initialize() {
    swerve.getHeadingPID().reset(0);
  }

  @Override
  public void execute() {

    if (!enabled) return;

    boolean valid;
    double tx;

    if (side == CameraSide.FRONT) {
      valid = vision.hasValidFrontTarget();
      tx = vision.getFrontTxRad();
    } else {
      valid = vision.hasValidBackTarget();
      tx = vision.getBackTxRad();
    }

    if (!valid) {
      swerve.stop();
      return;
    }

    double rot = swerve.getHeadingPID().calculate(tx, 0.0);

    rot = Math.max(Math.min(rot, 2.0), -2.0);

    swerve.drive(new Translation2d(xSupplier, ySupplier), rot, false);
  }

  @Override
  public void end(boolean interrupted) {
    swerve.stop();
  }

  @Override
  public boolean isFinished() {
    return false; // só termina quando cancelar
  }
}