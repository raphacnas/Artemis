package frc.robot.commands.auto_blocks;

import com.pathplanner.lib.auto.NamedCommands;

import edu.wpi.first.wpilibj2.command.Commands;

import frc.robot.commands.vision.AimAtTagCommand;
import frc.robot.commands.vision.AimAtTagCommand.CameraSide;
import frc.robot.subsystems.Sensors.ViewSubsystem;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
import frc.robot.subsystems.Score.Shooter.ShooterManager;
import frc.robot.subsystems.Score.PreShooter.PreShooterManager;
import frc.robot.subsystems.Score.Spindexer.SpindexerManager;
import frc.robot.subsystems.Score.Climb.ClimberManager;

public class NamedCommandsRegistry {

  public static void registerAll(
      SwerveSubsystem drive,
      ViewSubsystem vision,
      ShooterManager shooter,
      PreShooterManager preshooter,
      SpindexerManager spindexer
    //   ClimberManager climb
  ) {

    // ================= SHOOTER =================

    NamedCommands.registerCommand(
        "ShooterStart",
        Commands.runOnce(() -> shooter.enable())
    );

    NamedCommands.registerCommand(
        "ShooterStop",
        Commands.runOnce(() -> shooter.disable())
    );

    // ================= PRESHOOTER =================

    NamedCommands.registerCommand(
        "PreShooterFeed",
        Commands.runOnce(() -> preshooter.enableAuto())
    );

    NamedCommands.registerCommand(
        "PreShooterStop",
        Commands.runOnce(() -> preshooter.stop())
    );

    // ================= SPINDEXER =================

    NamedCommands.registerCommand(
        "SpindexerStart",
        Commands.runOnce(() -> spindexer.toggleSpin())
    );

    NamedCommands.registerCommand(
        "SpindexerStop",
        Commands.runOnce(() -> spindexer.stop())
    );

    // ================= CLIMB =================

    // NamedCommands.registerCommand(
    //     "ClimbExtend",
    //     Commands.runOnce(() -> climb.goToMax())
    // );

    // NamedCommands.registerCommand(
    //     "ClimbRetract",
    //     Commands.runOnce(() -> climb.goToMin())
    // );

    // ================= AIM =================

    // NamedCommands.registerCommand(
    //     // "AimFront",
    //     // new AimAtTagCommand(drive, vision, CameraSide.FRONT)
    // );

    // NamedCommands.registerCommand(
    //     // "AimBack",
    //     // new AimAtTagCommand(drive, vision, CameraSide.BACK)
    // );
  }
}