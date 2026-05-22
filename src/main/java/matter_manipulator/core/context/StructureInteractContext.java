package matter_manipulator.core.context;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.color.ImmutableColor;

public interface StructureInteractContext<T> extends StructureContext<T>, PlayerContext, PlacingContext {

    void setPos(BlockPos pos);

    @NotNull ItemStack getTrigger();

    void emitHint(BlockPos pos, BlockSpec spec, ImmutableColor tint);

    /// Checks if there is more place quota remaining.
    boolean hasPlaceQuota();

    /// Consumes one place quota.
    void consumePlaceQuota();
}
