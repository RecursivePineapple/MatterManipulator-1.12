package matter_manipulator.core.block_spec;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Nullable;

import matter_manipulator.common.block_spec.specs.AirBlockSpec;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.common.utils.world.ProxiedWorld;
import matter_manipulator.core.context.BlockAnalysisContext;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.resources.ResourceStack;

/// A specification that can recreate a block in full. This includes all block properties and all tile entity state.
/// Note that some specs may not completely erase whatever was previously present - as an example, a spec placing an AE
/// cable may keep any existing parts in the same block.
@NonExtendable
public interface IBlockSpec {

    IBlockSpec AIR = AirBlockSpec.INSTANCE;

    IBlockSpecLoader getLoader();

    boolean isValid();

    IBlockState getBlockState();
    ResourceStack getResource();

    /// Checks if the resource needed to place this spec differs from another.
    /// When this returns false, the existing block will be removed entirely and this spec will be placed in its
    /// location.
    /// When this returns true, [#canPlaceAt(ProxiedWorld, BlockPos)] and [#place(BlockPlacingContext)] will not be
    /// called.
    boolean matches(IBlockSpec other);

    /// Checks if this spec can be placed at the given location.
    boolean canPlaceAt(ProxiedWorld world, BlockPos pos);

    Localized getDisplayName();

    void transform(Transform transform);

    /// Places the spec's block at the context's location but does not update its interop data. Must consume the
    /// required resource from the context.
    ApplyResult place(BlockPlacingContext context);

    /// Updates the spec's interop data at the context's location but does not place the block at all.
    EnumSet<ApplyResult> update(BlockPlacingContext context);

    IBlockSpec clone();

    /// Returns a copy of this spec with the [IBlockState] set to the default variant for the resource.
    IBlockSpec sanitized();

    default boolean isAir() {
        return getBlockState().getMaterial() == Material.AIR;
    }
}
