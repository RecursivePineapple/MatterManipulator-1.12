package matter_manipulator.core.interop;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import matter_manipulator.core.context.TargetedManipulatorContext;
import matter_manipulator.core.resources.ResourceStack;

/// Something that resets a block before the block is removed. Every resetter is called for each block, no filtering is
/// done.
public interface BlockResetter {

    @NotNull
    List<ResourceStack> resetBlock(@NotNull TargetedManipulatorContext context);
}
