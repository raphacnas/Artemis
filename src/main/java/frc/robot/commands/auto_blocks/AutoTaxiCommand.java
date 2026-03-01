package frc.robot.commands.auto_blocks;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;

public class AutoTaxiCommand extends Command {

    private final SwerveSubsystem drive;

    public AutoTaxiCommand(SwerveSubsystem drive) {
        this.drive = drive;
        addRequirements(drive);
    }

    @Override
    public void initialize() {
        drive.drive(new Translation2d(-1.0, 0), 0.0, false);
    }

    @Override
    public void end(boolean interrupted) {
        drive.drive(new Translation2d(), 0.0, false);
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}