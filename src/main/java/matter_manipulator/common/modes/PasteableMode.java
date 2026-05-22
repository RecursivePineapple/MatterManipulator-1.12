package matter_manipulator.common.modes;

import matter_manipulator.core.context.HeldManipulatorContext;

public interface PasteableMode {

    boolean onPastePressed(HeldManipulatorContext context);

}
