package matter_manipulator.common.block_spec;

import java.util.EnumSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.common.utils.world.ProxiedWorld;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.block_spec.IBlockSpecLoader;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.resources.ResourceProvider;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.IntItemResourceStack;

public abstract class SingletonBlockSpec implements IBlockSpec, Cloneable {

    @Override
    public abstract IBlockSpecLoader getLoader();

    @Override
    public abstract IBlockState getBlockState();

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public abstract ResourceStack getResource();

    @Override
    public SingletonBlockSpec clone() {
        return this;
    }

    @Override
    public SingletonBlockSpec sanitized() {
        return this;
    }

    @Override
    public void transform(Transform transform) {

    }

    @Override
    public boolean canPlaceAt(ProxiedWorld world, BlockPos pos) {
        IBlockState state = getBlockState();

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

    @Override
    public boolean matches(IBlockSpec other) {
        return this == other;
    }

    @Override
    public Localized getDisplayName() {
        return getResource().getName();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ApplyResult place(BlockPlacingContext context) {
        ResourceStack stack = this.getResource();

        ResourceProvider resource = context.resource(stack.getResource());
        ResourceStack extracted = null;

        if (!stack.isEmpty()) {
            extracted = resource.extract(stack);

            if (ResourceStack.getStackAmount(extracted) != ResourceStack.getStackAmount(stack)) {
                // We couldn't extract the right amount of items/fluids/etc. Reinsert whatever we got and try again later.
                resource.insert(extracted);
                context.extractionFailure(stack);
                return ApplyResult.Retry;
            }
        }

        ApplyResult result = doPlace(context, extracted);

        switch (result) {
            case DidNothing, NotApplicable, Retry, Error -> {
                if (!stack.isEmpty()) {
                    // For whatever reason the adapter couldn't place the block, so we have to reinsert it.
                    resource.insert(extracted);
                }
            }
            default -> {

            }
        }

        return result;
    }

    protected ApplyResult doPlace(BlockPlacingContext context, ResourceStack extracted) {
        var world = context.getWorld();
        var pos = context.getPos();

        IBlockState toPlace = this.getBlockState();

        if (toPlace == world.getBlockState(pos)) return ApplyResult.DidNothing;

        world.setBlockState(pos, toPlace);

        var placed = world.getBlockState(pos);

        placed.getBlock()
            .onBlockPlacedBy(
                world,
                pos,
                placed,
                context.getRealPlayer(),
                extracted instanceof IntItemResourceStack item ? item.toStack() : ItemStack.EMPTY);

        return ApplyResult.DidSomething;
    }

    @Override
    public EnumSet<ApplyResult> update(BlockPlacingContext context) {
        return EnumSet.noneOf(ApplyResult.class);
    }
}
