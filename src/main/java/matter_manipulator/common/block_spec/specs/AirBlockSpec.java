package matter_manipulator.common.block_spec.specs;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import matter_manipulator.common.block_spec.SingletonBlockSpec;
import matter_manipulator.common.block_spec.adapters.AirBlockSpecAdapter;
import matter_manipulator.core.block_spec.IBlockSpecLoader;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.IntItemResourceStack;

public class AirBlockSpec extends SingletonBlockSpec {

    public static final AirBlockSpec INSTANCE = new AirBlockSpec();

    private AirBlockSpec() { }

    @Override
    public IBlockSpecLoader getLoader() {
        return AirBlockSpecAdapter.INSTANCE;
    }

    @Override
    public IBlockState getBlockState() {
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public ResourceStack getResource() {
        return IntItemResourceStack.EMPTY;
    }
}
