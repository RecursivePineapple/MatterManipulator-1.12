package matter_manipulator.common.structure;

import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.Nullable;

import matter_manipulator.core.context.StructureContext;
import matter_manipulator.core.context.StructureInteractContext;
import matter_manipulator.core.meta.MetaKey;

public interface StructureElement<T> {

    /// Gets some metadata about this element. Using this method is preferred over using interfaces or casting for
    /// element introspection as it can be chained arbitrarily for nested elements.
    @Nullable
    <K> K getMetadata(MetaKey<K> key);

    boolean check(StructureContext<? extends T> context, BlockPos pos);

    boolean build(StructureInteractContext<? extends T> context, BlockPos pos);
    void emitHint(StructureInteractContext<? extends T> context, BlockPos pos);
}
