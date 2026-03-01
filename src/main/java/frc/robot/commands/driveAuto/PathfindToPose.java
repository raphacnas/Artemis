package frc.robot.commands.driveAuto;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.PathConstraints;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;

public class PathfindToPose extends Command {

    private final Command internalCommand;

    public PathfindToPose(SwerveSubsystem swerve, Pose2d targetPose) {

        PathConstraints constraints = swerve.getPathConstraints();

        internalCommand = AutoBuilder.pathfindToPose(
                targetPose,
                constraints
        );

        addRequirements(swerve);
    }

    @Override
    public void initialize() {
        internalCommand.initialize();
    }

    @Override
    public void execute() {
        internalCommand.execute();
    }

    @Override
    public void end(boolean interrupted) {
        internalCommand.end(interrupted);
    }

    @Override
    public boolean isFinished() {
        return internalCommand.isFinished();
    }
}
