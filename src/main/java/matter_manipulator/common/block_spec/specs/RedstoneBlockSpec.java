package matter_manipulator.common.block_spec.specs;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import matter_manipulator.common.block_spec.SingletonBlockSpec;
import matter_manipulator.common.block_spec.adapters.RedstoneBlockSpecAdapter;
import matter_manipulator.core.block_spec.BlockSpecLoader;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemStackWrapper;

public class RedstoneBlockSpec extends SingletonBlockSpec {

    public static final RedstoneBlockSpec INSTANCE = new RedstoneBlockSpec();

    private RedstoneBlockSpec() { }

    @Override
    public BlockSpecLoader getLoader() {
        return RedstoneBlockSpecAdapter.INSTANCE;
    }

    @Override
    public IBlockState getBlockState() {
        return Blocks.REDSTONE_WIRE.getDefaultState();
    }

    @Override
    public ResourceStack getResource() {
        return new ItemStackWrapper(new ItemStack(Items.REDSTONE, 1));
    }
}
