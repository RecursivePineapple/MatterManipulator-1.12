package matter_manipulator.common.block_spec.specs;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import crazypants.enderio.base.machine.base.block.BlockMachineExtension;
import matter_manipulator.common.block_spec.AbstractBlockSpec;
import matter_manipulator.common.block_spec.adapters.EIOTopBlockSpecAdapter;
import matter_manipulator.common.utils.world.ProxiedWorld;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.block_spec.IBlockSpecLoader;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemStackWrapper;

public class EIOTopBlockSpec extends AbstractBlockSpec {

    public IBlockState state;

    public EIOTopBlockSpec(IBlockState state) {
        this.state = state;
    }

    @Override
    public IBlockSpecLoader getLoader() {
        return EIOTopBlockSpecAdapter.INSTANCE;
    }

    @Override
    public boolean isValid() {
        return state.getBlock() instanceof BlockMachineExtension;
    }

    @Override
    public IBlockState getBlockState() {
        return state;
    }

    @Override
    public ResourceStack getResource() {
        return ItemStackWrapper.EMPTY;
    }

    @Override
    public boolean matches(IBlockSpec other) {
        if (!(other instanceof EIOTopBlockSpec topSpec)) return false;

        return this.state == topSpec.state;
    }

    @Override
    public boolean canPlaceAt(ProxiedWorld world, BlockPos pos) {
        return true;
    }

    @Override
    public ApplyResult place(BlockPlacingContext context) {
        return ApplyResult.DidNothing;
    }

    @Override
    protected void resetResource() {

    }

    @Override
    public EIOTopBlockSpec clone() {
        return (EIOTopBlockSpec) super.clone();
    }

    @Override
    public EIOTopBlockSpec sanitized() {
        return this.clone();
    }
}
