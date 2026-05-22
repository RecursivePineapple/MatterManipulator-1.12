package matter_manipulator.common.modes;

import matter_manipulator.core.context.HeldManipulatorContext;

public interface CuttableMode {

    boolean onCutPressed(HeldManipulatorContext context);

}
