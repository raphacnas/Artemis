package frc.robot.Dashboards.Drive;

import edu.wpi.first.networktables.IntegerPublisher;
import edu.wpi.first.networktables.NetworkTableInstance;

public class DriveModePublisher {

  private final IntegerPublisher aimModePub =
      NetworkTableInstance.getDefault()
          .getIntegerTopic("/Modes/AimLockLime2")
          .publish();

  private final IntegerPublisher alignModePub =
      NetworkTableInstance.getDefault()
          .getIntegerTopic("/Modes/AlignLime2")
          .publish();

  private final IntegerPublisher aimLime4Pub =
      NetworkTableInstance.getDefault()
          .getIntegerTopic("/Modes/AimLockLime4")
          .publish();

  private final IntegerPublisher aimLime2PlusPub =
      NetworkTableInstance.getDefault()
          .getIntegerTopic("/Modes/AimLockLime2Plus")
          .publish();

  private final IntegerPublisher shooterLime2PlusPub =
      NetworkTableInstance.getDefault()
          .getIntegerTopic("/Modes/ShooterLime2Plus")
          .publish();

  public void publishAim(int mode) {
    aimModePub.set(mode);
  }

  public void publishAlign(int mode) {
    alignModePub.set(mode);
  }

  public void publishAimLime4(int mode) {
    aimLime4Pub.set(mode);
  }

  public void publishAimLime2Plus(int mode) {
    aimLime2PlusPub.set(mode);
  }

  private final IntegerPublisher alignPiecePub =
      NetworkTableInstance.getDefault()
          .getIntegerTopic("/Modes/AlignPiece")
          .publish();

  public void publishShooterLime2Plus(int mode) {
    shooterLime2PlusPub.set(mode);
  }

  public void publishAlignPiece(int mode) {
    alignPiecePub.set(mode);
  }
}