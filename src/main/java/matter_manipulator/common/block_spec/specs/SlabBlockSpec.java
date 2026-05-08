package matter_manipulator.common.block_spec.specs;

import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import org.apache.commons.lang3.mutable.MutableObject;

import lombok.EqualsAndHashCode;
import matter_manipulator.common.block_spec.AbstractBlockSpec;
import matter_manipulator.common.block_spec.adapters.SlabBlockSpecAdapter;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.block_spec.BlockSpecLoader;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemStackWrapper;
import matter_manipulator.mixin.mixins.minecraft.AccessorItemSlab;

@EqualsAndHashCode(callSuper = false)
public class SlabBlockSpec extends AbstractBlockSpec {

    public IBlockState state;

    @EqualsAndHashCode.Exclude
    private boolean hasResource = false;
    @EqualsAndHashCode.Exclude
    private ItemStackWrapper resource;

    public SlabBlockSpec(IBlockState state) {
        this.state = state;
    }

    @Override
    public BlockSpecLoader getLoader() {
        return SlabBlockSpecAdapter.INSTANCE;
    }

    @Override
    public boolean isValid() {
        if (!(state.getBlock() instanceof BlockSlab slab)) return false;

        Item item = slab.getItemDropped(state, ThreadLocalRandom.current(), 0);

        return item instanceof AccessorItemSlab;
    }

    @Override
    public IBlockState getBlockState() {
        return state;
    }

    @Override
    protected void resetResource() {
        hasResource = false;
        resource = null;
    }

    @Override
    public ItemStackWrapper getResource() {
        if (hasResource) return resource;

        hasResource = true;
        resource = new ItemStackWrapper(new ItemStack(
            state.getBlock().getItemDropped(state, ThreadLocalRandom.current(), 0),
            state.getBlock().quantityDropped(ThreadLocalRandom.current()),
            state.getBlock().damageDropped(state)));;

        return resource;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void transform(Transform transform) {
        super.transform(transform);

        for (IProperty<?> prop : state.getPropertyKeys()) {
            if (prop.getName().equals("facing") && prop.getValueClass() == EnumFacing.class) {
                EnumFacing facing = state.getValue((IProperty<EnumFacing>) prop);

                transform.apply(facing);

                state = state.withProperty((IProperty<EnumFacing>) prop, facing);
            }
        }
    }

    @Override
    public SlabBlockSpec clone() {
        return (SlabBlockSpec) super.clone();
    }

    @Override
    public BlockSpec sanitized() {
        ItemStackWrapper stack = getResource();

        if (!(stack.getItem() instanceof ItemBlock itemBlock)) return clone();
        if (!(stack.getItem() instanceof AccessorItemSlab slab)) return clone();

        Block block = ResourceStack.getStackAmount(stack) > 1 ? slab.getDoubleSlab() : slab.getSingleSlab();

        int meta = itemBlock.getMetadata(stack.toStackFast(stack.getAmountInt()).getMetadata());

        //noinspection deprecation
        IBlockState base = block.getStateFromMeta(meta);

        MutableObject<IBlockState> state = new MutableObject<>(base);

        MMRegistriesInternal.transformBlock(state, getBlockState(), EnumSet.noneOf(ApplyResult.class));

        SlabBlockSpec copy = clone();

        copy.state = state.getValue();

        return copy;
    }
}
