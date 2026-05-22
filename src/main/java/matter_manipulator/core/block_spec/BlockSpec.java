package matter_manipulator.core.block_spec;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import matter_manipulator.common.block_spec.BlockSpecData;
import matter_manipulator.common.block_spec.specs.AirBlockSpec;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.common.utils.world.ProxiedWorld;
import matter_manipulator.core.context.ManipulatorPlacingContext;
import matter_manipulator.core.context.PlacingContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.persist.tagged_union.TaggedUnionVariant;
import matter_manipulator.core.resources.ResourceIdentity;
import matter_manipulator.core.resources.ResourceStack;

/// A specification that can recreate a block in full. This includes all block properties and all tile entity state.
/// Note that some specs may not completely erase whatever was previously present - as an example, a spec placing an AE
/// cable may keep any existing parts in the same block.
@NonExtendable
public interface BlockSpec extends TaggedUnionVariant<BlockSpec> {

    static BlockSpec air() {
        return AirBlockSpec.INSTANCE;
    }

    BlockSpecLoader getLoader();

    boolean isValid();

    IBlockState getBlockState();

    ResourceStack getResource();

    /// Checks if the resource needed to place this spec differs from another. When this returns false, the existing
    /// block will be removed entirely and this spec will be placed in its location. When this returns true,
    /// [#canPlaceAt(ProxiedWorld, BlockPos)] and [#place(PlacingContext)] will not be called.
    boolean matches(BlockSpec other);

    /// Checks if this spec can be placed at the given location.
    boolean canPlaceAt(ProxiedWorld world, BlockPos pos);

    @NotNull Localized getDisplayName();

    void transform(Transform transform);

    /// Swaps one stack for another within this spec. Returns a non-null [BlockSpecData] when this block's identity was
    /// exchanged. When this method returns null, this spec may have been mutated and should be kept.
    @Contract(mutates = "this")
    @Nullable BlockSpecData exchange(ResourceIdentity stack, ResourceIdentity replacement);

    /// Places the spec's block at the context's location but does not update its interop data. Must consume the
    /// required resource from the context.
    ApplyResult place(PlacingContext context);

    /// Updates the spec's interop data at the context's location but does not place the block at all.
    EnumSet<ApplyResult> update(ManipulatorPlacingContext context);

    /// Extracts the resources required to [#update(ManipulatorPlacingContext)] this [BlockSpec], for plan creation.
    /// [#place(PlacingContext)] extraction is handled externally, during plan creation.
    ///
    /// @param context      A planning context.
    /// @param skipExisting When true, the state of the world should be ignored and all required resources should be
    /// extracted. Otherwise, only missing resources should be extracted.
    void getRequiredResourcesForUpdate(ManipulatorPlacingContext context, boolean skipExisting);

    BlockSpec clone();

    /// Returns a copy of this spec with the [IBlockState] set to the default variant for the resource.
    BlockSpec sanitized();

    default boolean isAir() {
        return getBlockState().getMaterial() == Material.AIR;
    }
}
