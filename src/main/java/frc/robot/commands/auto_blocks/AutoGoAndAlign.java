// package frc.robot.commands.auto_blocks;

// import edu.wpi.first.math.geometry.Pose2d;
// import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

// import frc.robot.commands.driveAuto.PathfindToPose;
// import frc.robot.commands.vision.AimAtTagCommand;
// import frc.robot.commands.vision.AimAtTagCommand.CameraSide;
// import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
// import frc.robot.subsystems.Sensors.ViewSubsystem;

// public class AutoGoAndAlign extends SequentialCommandGroup {

//   public AutoGoAndAlign(
//       SwerveSubsystem swerve,
//       ViewSubsystem vision,
//       Pose2d targetPose,
//       CameraSide side) {

//     addCommands(
//         new PathfindToPose(swerve, targetPose),
//         new AimAtTagCommand(swerve, vision, side)
//     );
//   }
// }