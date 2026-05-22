package matter_manipulator.common.modes;

import matter_manipulator.core.context.HeldManipulatorContext;

public interface CopyableMode {

    boolean onCopyPressed(HeldManipulatorContext context);

}
