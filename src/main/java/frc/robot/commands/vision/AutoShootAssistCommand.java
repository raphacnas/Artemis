package frc.robot.commands.vision;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.subsystems.Score.Shooter.ShooterManager;
import frc.robot.subsystems.Sensors.ViewSubsystem;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;

public class AutoShootAssistCommand extends Command {

  private final SwerveSubsystem swerve;
  private final ViewSubsystem vision;
  private final ShooterManager shooter;

  private boolean enabled = false;

  // Ajustáveis
  private final double kPAim = -0.035;
  private final double kPDistance = -0.1;

  public AutoShootAssistCommand(
      SwerveSubsystem swerve,
      ViewSubsystem vision,
      ShooterManager shooter) {

    this.swerve = swerve;
    this.vision = vision;
    this.shooter = shooter;

    addRequirements(swerve);
  }

  public void toggle() {
    enabled = !enabled;

    if (enabled) {
      schedule();
      shooter.enable();
    } else {
      cancel();
      shooter.disable();
    }
  }

  @Override
  public void execute() {

    if (!enabled) return;

    if (!vision.hasValidBackTarget()) {
      swerve.stop();
      return;
    }

    double tx = vision.getBackTxRad();
    double ty = vision.getBackTy();

    // ================= AIM =================
    double rot =
        kPAim * tx * Constants.MAX_ANGULAR_SPEED;

    // ================= DISTANCE =================
    double forward =
        kPDistance * ty * Constants.MAX_SPEED;

    forward = MathUtil.clamp(forward,
        -Constants.MAX_SPEED,
        Constants.MAX_SPEED);

    rot = MathUtil.clamp(rot,
        -Constants.MAX_ANGULAR_SPEED,
        Constants.MAX_ANGULAR_SPEED);

    swerve.drive(
        new Translation2d(forward, 0),
        rot,
        false
    );
  }

  @Override
  public void end(boolean interrupted) {
    swerve.stop();
  }

  @Override
  public boolean isFinished() {
    return false;
  }
}