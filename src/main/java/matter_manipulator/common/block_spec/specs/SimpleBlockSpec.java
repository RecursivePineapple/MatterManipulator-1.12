package matter_manipulator.common.block_spec.specs;

import java.util.EnumSet;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import org.apache.commons.lang3.mutable.MutableObject;

import lombok.EqualsAndHashCode;
import matter_manipulator.common.block_spec.AbstractBlockSpec;
import matter_manipulator.common.block_spec.adapters.SimpleBlockSpecAdapter;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.block_spec.BlockSpecLoader;
import matter_manipulator.core.resources.item.ItemStackWrapper;

/// A [BlockSpec] that places a block, along with some interop state. The block must be backed 1:1 by a standard
/// [ItemBlock].
@EqualsAndHashCode(callSuper = false)
public class SimpleBlockSpec extends AbstractBlockSpec {

    public IBlockState state;

    @EqualsAndHashCode.Exclude
    private boolean hasResource = false;
    @EqualsAndHashCode.Exclude
    private ItemStackWrapper resource;

    public SimpleBlockSpec(IBlockState state) {
        this.state = state;
    }

    @Override
    public BlockSpecLoader getLoader() {
        return SimpleBlockSpecAdapter.INSTANCE;
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
            Item.getItemFromBlock(state.getBlock()),
            1,
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
    public SimpleBlockSpec clone() {
        return (SimpleBlockSpec) super.clone();
    }

    @Override
    public BlockSpec sanitized() {
        ItemStackWrapper stack = getResource();

        if (!(stack.getItem() instanceof ItemBlock itemBlock)) return clone();

        int meta = itemBlock.getMetadata(stack.toStackFast(stack.getAmountInt()).getMetadata());

        //noinspection deprecation
        IBlockState base = this.state.getBlock().getStateFromMeta(meta);

        MutableObject<IBlockState> state = new MutableObject<>(base);

        MMRegistriesInternal.transformBlock(state, getBlockState(), EnumSet.noneOf(ApplyResult.class));

        SimpleBlockSpec copy = clone();

        copy.state = state.getValue();

        return copy;
    }
}
