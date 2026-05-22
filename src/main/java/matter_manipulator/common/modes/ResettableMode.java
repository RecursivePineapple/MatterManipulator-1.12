package matter_manipulator.common.modes;

import matter_manipulator.core.context.HeldManipulatorContext;

public interface ResettableMode {

    boolean onResetPressed(HeldManipulatorContext context);

}
