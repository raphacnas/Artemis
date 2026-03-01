package frc.robot.TagsID;

import java.util.Set;

import edu.wpi.first.wpilibj.DriverStation;

public class OutPostSelector {
    public enum OutPost {
        CENTER,
        

    }
 public static Set<Integer> getTags(
        OutPost side,
        DriverStation.Alliance alliance
    ){
        if (alliance == DriverStation.Alliance.Blue) {
            switch (side) {
                case CENTER:
                    return Set.of(29);
            }
        } else {
            switch (side) {
                case CENTER:
                    return Set.of(13);
            }
        }

        return Set.of();
    } 
}
