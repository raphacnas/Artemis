package frc.robot.subsystems.Sensors;

import java.util.Set;
import java.util.Optional;

import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.TagsID.HubTagSelector;
import frc.robot.TagsID.OutPostSelector;
import frc.robot.TagsID.TowerTagSelector;
import frc.robot.TagsID.OutPostSelector.OutPost;

public class ViewSubsystem extends SubsystemBase {

  // ================= TAG FILTER =================

  private Set<Integer> allowedFrontTags = Set.of(31, 32, 15, 16); // Torre
  private Set<Integer> allowedBackTags  = Set.of(25, 26, 18, 27, 21, 24, 9, 10, 11, 2, 8, 5); // Hub 

  // ================= LIMELIGHTS =================

  private final NetworkTable limeFront =
      NetworkTableInstance.getDefault().getTable("limelight-front");

  private final NetworkTable limeBack =
      NetworkTableInstance.getDefault().getTable("limelight-back");

  // ===================== TAG SELECTION =====================

  public void selectHub(HubTagSelector.HubSide side) {
    Optional<DriverStation.Alliance> alliance =
        DriverStation.getAlliance();

    if (alliance.isPresent()) {
      allowedBackTags =
          HubTagSelector.getTags(side, alliance.get());
    }
  }

  public void selectOutpost(HubTagSelector.HubSide side) {
    Optional<DriverStation.Alliance> alliance =
        DriverStation.getAlliance();

    if (alliance.isPresent()) {
      allowedBackTags =
          HubTagSelector.getTags(side, alliance.get());
    }
  }

  public void selectTower(TowerTagSelector.TowerSide side) {
    Optional<DriverStation.Alliance> alliance =
        DriverStation.getAlliance();

    if (alliance.isPresent()) {
      allowedFrontTags =
          TowerTagSelector.getTags(side, alliance.get());
    }
  }

  public boolean hasFrontTarget() {
    return limeFront.getEntry("tv").getDouble(0) == 1;
  }

  public int getFrontTagId() {
    if (!hasFrontTarget()) return -1;
    return (int) limeFront.getEntry("tid").getDouble(-1);
  }

  public boolean isFrontTagAllowed() {
    return allowedFrontTags.contains(getFrontTagId());
  }

  public boolean hasValidFrontTarget() {
    return hasFrontTarget() && isFrontTagAllowed();
  }

  public double getFrontTxRad() {
    return Units.degreesToRadians(
        limeFront.getEntry("tx").getDouble(0.0));
  }

  /**
   * Obtém a distância da câmera frontal usando 3D Pose (PnP)
   * Ideal para câmeras sem inclinação física.
   */
  public double getFrontDistanceToTag() {
    if (!hasValidFrontTarget()) return Double.MAX_VALUE;

    // Obtém o array de pose [x, y, z, roll, pitch, yaw]
    double[] pose = limeFront.getEntry("targetpose_cameraspace").getDoubleArray(new double[0]);

    if (pose.length >= 6) {
        // O índice 2 é o eixo Z (distância frontal em metros)
        return Math.abs(pose[2]);
    }

    return Double.MAX_VALUE;
  }

  // ===================== BACK (HUB + OUTPOST) =====================

  public boolean hasBackTarget() {
    return limeBack.getEntry("tv").getDouble(0) == 1;
  }

  public int getBackTagId() {
    if (!hasBackTarget()) return -1;
    return (int) limeBack.getEntry("tid").getDouble(-1);
  }

  public boolean isBackTagAllowed() {
    return allowedBackTags.contains(getBackTagId());
  }

  public boolean hasValidBackTarget() {
    return hasBackTarget() && isBackTagAllowed();
  }

  public double getBackTxRad() {
    return Units.degreesToRadians(
        limeBack.getEntry("tx").getDouble(0.0));      
  }

  public double getBackTy() {
    return limeBack.getEntry("ty").getDouble(0.0);
  }

  public double getBackDistanceToTag() {
    if (!hasValidBackTarget()) return Double.MAX_VALUE;

    double[] pose = limeBack.getEntry("targetpose_cameraspace").getDoubleArray(new double[0]);

    if (pose.length >= 6) {
        return Math.abs(pose[2]); // O índice 2 é o eixo Z (distância direta até a Tag em metros)
    }

    return Double.MAX_VALUE;
  }

 public void selectAllHubTags() {

  var alliance = DriverStation.getAlliance();
  if (alliance.isEmpty()) return;

  allowedBackTags = new java.util.HashSet<>();

  allowedBackTags.addAll(HubTagSelector.getTags(
      HubTagSelector.HubSide.CENTER,
      alliance.get()));
}

public void selectAllTowerTags() {

  var alliance = DriverStation.getAlliance();
  if (alliance.isEmpty()) return;

  allowedFrontTags = new java.util.HashSet<>();
  allowedFrontTags.addAll(TowerTagSelector.getTags(
      TowerTagSelector.TowerSide.CENTER,
      alliance.get()));
  allowedFrontTags.addAll(TowerTagSelector.getTags(
      TowerTagSelector.TowerSide.RIGHT,
      alliance.get()));
}

public void selectTagOutpost()
{
  var alliance = DriverStation.getAlliance();
  if(alliance.isEmpty()) return;

  allowedBackTags = new java.util.HashSet<>();
  allowedBackTags.addAll(OutPostSelector.getTags(
      OutPost.CENTER,
      alliance.get()));
}

}

