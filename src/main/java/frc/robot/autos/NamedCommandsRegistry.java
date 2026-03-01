// package frc.robot.autos;

// import com.pathplanner.lib.auto.NamedCommands;

// import edu.wpi.first.wpilibj2.command.Commands;
// import edu.wpi.first.wpilibj2.command.InstantCommand;
// import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
// import frc.robot.subsystems.Score.Climb.ClimberManager;
// import frc.robot.subsystems.Score.PreShooter.PreShooterManager;
// import frc.robot.subsystems.Score.Shooter.ShooterManager;
// import frc.robot.subsystems.Score.Spindexer.SpindexerManager;
// import frc.robot.subsystems.Sensors.ViewSubsystem;
// import frc.robot.subsystems.Swervedrive.SwerveSubsystem;

// import frc.robot.commands.vision.AimAtTagCommand;
// import frc.robot.commands.vision.AimAtTagCommand.CameraSide;
// import frc.robot.commands.Poses.AutoOutpost;
// //import frc.robot.Poses.FieldPoses;
// import frc.robot.commands.auto_blocks.AutoGoAndAlign;
// //import frc.robot.commands.auto_blocks.AutoShootSequence;
// import frc.robot.commands.driveAuto.PathfindToPose;

// public final class NamedCommandsRegistry {

//   public static void registerAll(
//       SwerveSubsystem swerve,
//       ViewSubsystem vision,
//       ShooterManager shooterManager,
//       PreShooterManager preShooterManager,
//       SpindexerManager spindexerManager,
//       ClimberManager climbManager) {

//     // NamedCommands.registerCommand(
//     //     "AimAtHub",
//     //     new AimAtTagCommand(swerve, vision, CameraSide.BACK)
//     // );

//     // NamedCommands.registerCommand(
//     //     "AimAtTower",
//     //     new AimAtTagCommand(swerve, vision, CameraSide.FRONT)
//     // );

//    NamedCommands.registerCommand(
//     "GoAndAlignOutpost",
//     new SequentialCommandGroup(
//         new PathfindToPose(
//             swerve,
//             AutoOutpost.BLUE_OUTPOST
//         ),
//         new InstantCommand(vision::selectAllHubTags),
//         new AimAtTagCommand(
//             swerve,
//             vision,
//             CameraSide.BACK
//         ).withTimeout(2.0)
//     )
// );


//     // NamedCommands.registerCommand(
//     //     "GoAndAlignOutpost",
//     //     new AutoGoAndAlign(
//     //         swerve,
//     //         vision,
//     //         //FieldPoses.BLUE_OUTPOST,
//     //         CameraSide.BACK
//     //     )
//     // );

//     // NamedCommands.registerCommand(
//     //     "GoAndAlignToTower",
//     //     new AutoGoAndAlign(
//     //         swerve,
//     //         vision,
//     //         //FieldPoses.BLUE_TOWER,
//     //         CameraSide.FRONT
//     //     )
//     // );

//     // NamedCommands.registerCommand(
//     //     "SpinUpShooter",
//     //     Commands.runOnce(
//     //         shooterManager::enable,
//     //         shooterManager
//     //     )
//     // );

//     // NamedCommands.registerCommand(
//     //     "AutoShoot",
//     //     new AutoShootSequence(
//     //         shooterManager,
//     //         preShooterManager,
//     //         spindexerManager
//     //     )
//     // );

//     // NamedCommands.registerCommand(
//     //     "ClimbUp",
//     //     Commands.runOnce(
//     //         climbManager::moveToExtended,
//     //         climbManager
//     //     )
//     // );

//     // NamedCommands.registerCommand(
//     //     "ClimbDown",
//     //     Commands.runOnce(
//     //         climbManager::moveToRetracted,
//     //         climbManager
//     //     )
//     // );

//     // NamedCommands.registerCommand(
//     //     "WaitClimb",
//     //     Commands.waitUntil(climbManager::atTarget)
//     // );
//   }
// }