package frc.robot.commands.driveAuto;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.PathConstraints;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.Command;

public class DriveToPoseCommand {

  private static final PathConstraints CONSTRAINTS =
  new PathConstraints(
      3.0,          
      2.5,         
      Math.PI,      // max angular velocity
      Math.PI * 2,  // max angular accel
      12  
  );

  public static Command goTo(Pose2d pose) {
    return AutoBuilder.pathfindToPose(
        pose,
        CONSTRAINTS,
        0.0 
    );
  }
}
