package frc.robot.commands.teleopDrive;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;

import java.util.function.DoubleSupplier;
import swervelib.math.SwerveMath;

public class DriveCommand extends Command {

    private final SwerveSubsystem drivebase;
    private final DoubleSupplier xSup;
    private final DoubleSupplier ySup;
    private final DoubleSupplier rotSup;

    public DriveCommand(SwerveSubsystem drivebase,
                        DoubleSupplier xSup,
                        DoubleSupplier ySup,
                        DoubleSupplier rotSup) {

        this.drivebase = drivebase;
        this.xSup = xSup;
        this.ySup = ySup;
        this.rotSup = rotSup;

        addRequirements(drivebase);
    }

    @Override
    public void execute() {

        double x = xSup.getAsDouble();
        double y = ySup.getAsDouble();
        double rot = rotSup.getAsDouble();

        if (Math.abs(x) < 0.06) x = 0.0;
        if (Math.abs(y) < 0.06) y = 0.0;
        if (Math.abs(rot) < 0.06) rot = 0.0;

        drivebase.drive(
            SwerveMath.scaleTranslation(
                new Translation2d(
                    x * drivebase.getSwerveDrive().getMaximumChassisVelocity(),
                    y * drivebase.getSwerveDrive().getMaximumChassisVelocity()),
                0.8),
            Math.pow(rot, 3) * drivebase.getSwerveDrive().getMaximumChassisAngularVelocity(),
            false
        );
    }
}