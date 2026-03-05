package frc.robot.commands;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;

import java.util.function.DoubleSupplier;
import swervelib.math.SwerveMath;

public class HeadingDriveCommand extends Command {

    private final SwerveSubsystem drivebase;
    private final DoubleSupplier xSup;
    private final DoubleSupplier ySup;
    private final DoubleSupplier headingXSup;
    private final DoubleSupplier headingYSup;

    public HeadingDriveCommand(
            SwerveSubsystem drivebase,
            DoubleSupplier xSup,
            DoubleSupplier ySup,
            DoubleSupplier headingXSup,
            DoubleSupplier headingYSup) {

        this.drivebase = drivebase;
        this.xSup = xSup;
        this.ySup = ySup;
        this.headingXSup = headingXSup;
        this.headingYSup = headingYSup;

        addRequirements(drivebase);
    }

    @Override
    public void execute() {

        double x = xSup.getAsDouble();
        double y = ySup.getAsDouble();
        double hx = headingXSup.getAsDouble();
        double hy = headingYSup.getAsDouble();

        if (Math.abs(x) < 0.06) x = 0.0;
        if (Math.abs(y) < 0.06) y = 0.0;

        if (Math.hypot(hx, hy) < 0.05) {
            hx = 0.0;
            hy = 0.0;
        }

        Translation2d scaledInputs = SwerveMath.scaleTranslation(
                new Translation2d(x, y),
                0.8);

        drivebase.driveFieldOriented(
            drivebase.getTargetSpeeds(
                scaledInputs.getX(),
                scaledInputs.getY(),
                hx,
                hy
            )
        );
    }
}