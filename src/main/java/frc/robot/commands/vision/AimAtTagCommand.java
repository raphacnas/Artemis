package frc.robot.commands.vision;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
import frc.robot.subsystems.Sensors.ViewSubsystem;

public class AimAtTagCommand extends Command {

  public enum CameraSide { FRONT, BACK }

  private final SwerveSubsystem swerve;
  private final ViewSubsystem vision;
  private final CameraSide side;

  private final ProfiledPIDController headingPID =
      new ProfiledPIDController(
          3.0,
          0.0,
          0.2,
          new TrapezoidProfile.Constraints(
              Units.degreesToRadians(150),
              Units.degreesToRadians(300)));

  public AimAtTagCommand(
      SwerveSubsystem swerve,
      ViewSubsystem vision,
      CameraSide side) {

    this.swerve = swerve;
    this.vision = vision;
    this.side = side;

    headingPID.enableContinuousInput(-Math.PI, Math.PI);
    addRequirements(swerve);
  }

  @Override
  public void initialize() {
    headingPID.reset(0, 0); // 🔥 reset seguro
  }

  @Override
  public void execute() {

    if (vision == null) return;

    boolean valid =
        side == CameraSide.FRONT
            ? vision.hasValidFrontTarget()
            : vision.hasValidBackTarget();

    if (!valid) {
      swerve.drive(new Translation2d(), 0.0, false);
      return;
    }

    double tx =
        side == CameraSide.FRONT
            ? vision.getFrontTxRad()
            : vision.getBackTxRad();

    double rot = headingPID.calculate(tx, 0.0);

    rot = Math.max(Math.min(rot, 3.0), -3.0);

    swerve.drive(new Translation2d(), rot, false);
  }

  @Override
  public void end(boolean interrupted) {
    swerve.drive(new Translation2d(), 0.0, false);
  }

  @Override
  public boolean isFinished() {
    return false; // necessário para toggle
  }
}