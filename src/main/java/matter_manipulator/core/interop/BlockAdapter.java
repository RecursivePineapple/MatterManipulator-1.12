package matter_manipulator.core.interop;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import matter_manipulator.common.utils.world.ProxiedWorld;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.IntItemResourceStack;

public interface BlockAdapter {

    boolean canAdapt(IBlockState state);

    boolean canAdapt(ResourceStack resource);

    @NotNull ResourceStack getResourceForm(IBlockState state);

    @NotNull IBlockState getBlockForm(ResourceStack resource);

    /// Checks if this spec can be placed at the given location.
    default boolean canPlaceAt(ProxiedWorld world, BlockPos pos, IBlockState state) {
        world.overrides.clear();
        world.setBlockToAir(pos);
        if (!state.getBlock()
            .canPlaceBlockAt(world, pos)) {
            return false;
        }

        // Check block dependencies for things like levers by placing it in a fake world and mocking a block update.
        // If it removes itself, then it can't be placed here yet.
        world.overrides.clear();
        world.setBlockState(pos, state);
        //noinspection deprecation
        state.getBlock()
            .neighborChanged(state, world, pos, Blocks.AIR, pos.add(0, 1, 0));

        return world.getBlockState(pos) == state;
    }

    default boolean areMatch(IBlockSpec left, IBlockSpec right) {
        return this.getResourceForm(left.getBlockState())
            .isSameType(this.getResourceForm(right.getBlockState()));
    }

    default IBlockState sanitized(IBlockState state) {
        return getBlockForm(getResourceForm(state));
    }

    default ApplyResult place(BlockPlacingContext placingContext, ResourceStack resource) {
        var world = placingContext.getWorld();
        var pos = placingContext.getPos();

        IBlockState toPlace = getBlockForm(resource);

        if (toPlace == world.getBlockState(pos)) return ApplyResult.DidNothing;

        world.setBlockState(pos, toPlace);

        var placed = world.getBlockState(pos);

        placed.getBlock()
            .onBlockPlacedBy(
                world,
                pos,
                placed,
                placingContext.getRealPlayer(),
                resource instanceof IntItemResourceStack item ? item.toStack() : ItemStack.EMPTY);

        return ApplyResult.DidSomething;
    }
}
