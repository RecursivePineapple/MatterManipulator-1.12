package matter_manipulator.core.block_spec;

import org.jetbrains.annotations.Nullable;

import matter_manipulator.common.block_spec.BlockSpecData;
import matter_manipulator.core.context.AnalysisContext;
import matter_manipulator.core.context.TargetedContext;
import matter_manipulator.core.interop.InteropModule;

/// Something that can extract a [BlockSpec] from a block in the world.
public interface BlockSpecExtractor {

    /// Creates a shallow spec, which is useful for introspection on the block but not replicating it. The spec must not
    /// include any [InteropModule] data.
    @Nullable BlockSpec getSpecPartial(TargetedContext context);

    /// Creates a full spec, which can be used to re-create the block.
    @Nullable BlockSpec getSpecFull(AnalysisContext context);

    /// Reconstructs a spec from a [BlockSpecData], which may have come from another spec's [BlockSpec#exchange(matter_manipulator.core.resources.ResourceIdentity, matter_manipulator.core.resources.ResourceIdentity)].
    /// Returns null if the data is invalid. If no extractors return a spec, no exchange for this block will occur.
    @Nullable BlockSpec reconstructSpec(BlockSpecData data);
}
