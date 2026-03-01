package frc.robot.commands.Poses;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import frc.robot.commands.vision.AimAtTagCommand;
import frc.robot.commands.vision.AimAtTagCommand.CameraSide;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
import frc.robot.subsystems.Sensors.ViewSubsystem;
import java.util.HashMap;
import java.util.Map;

public class AutoGoAndAlignOutpost extends SequentialCommandGroup {

    public AutoGoAndAlignOutpost(SwerveSubsystem swerve, ViewSubsystem view) {

        Map<String, Command> eventMap = new HashMap<>();
        eventMap.put("SelectOutpostTag", new InstantCommand(view::selectTagOutpost));
        eventMap.put("AimAtTag", new AimAtTagCommand(swerve, view, CameraSide.BACK)
                                     .withTimeout(1.0)); 

        Command pathCommand = new InstantCommand();
        addCommands(pathCommand);
    }
}