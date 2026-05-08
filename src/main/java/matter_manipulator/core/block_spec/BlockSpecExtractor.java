package matter_manipulator.core.block_spec;

import org.jetbrains.annotations.Nullable;

import matter_manipulator.core.context.BlockAnalysisContext;
import matter_manipulator.core.context.TargetedManipulatorContext;

/// Something that can extract a [IBlockSpec] from a block in the world.
public interface BlockSpecExtractor {

    /// Creates a shallow spec, which is useful for introspection on the block but not replicating it. The spec not
    /// include any [IInteropModule] data.
    @Nullable IBlockSpec getSpecPartial(TargetedManipulatorContext context);

    /// Creates a full spec, which can be used to re-create the block.
    @Nullable IBlockSpec getSpecFull(BlockAnalysisContext context);
}
