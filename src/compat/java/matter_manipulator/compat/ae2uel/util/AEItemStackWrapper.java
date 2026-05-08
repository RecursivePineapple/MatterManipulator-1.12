package matter_manipulator.compat.ae2uel.util;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

import appeng.api.storage.data.IAEItemStack;
import matter_manipulator.common.utils.MathUtils;
import matter_manipulator.core.item.ImmutableItemStack;
import matter_manipulator.core.item.ItemUtils;

public class AEItemStackWrapper implements ImmutableItemStack {

    public IAEItemStack stack;

    public AEItemStackWrapper(IAEItemStack stack) {
        this.stack = stack;
    }

    public AEItemStackWrapper set(IAEItemStack stack) {
        this.stack = stack;
        return this;
    }

    @Override
    public @NotNull Item getItem() {
        return stack == null ? Items.AIR : stack.getItem();
    }

    @Override
    public int getItemMeta() {
        return stack == null ? 0 : stack.getItemDamage();
    }

    @Override
    public int getCount() {
        return stack == null ? 0 : MathUtils.longToInt(stack.getStackSize());
    }

    @Override
    public NBTTagCompound getTag() {
        return stack == null ? null : stack.getDefinition()
            .getTagCompound();
    }

    @Override
    public NBTTagCompound getCapTag() {
        return stack == null ? null : ItemUtils.getCapTag(stack.getDefinition());
    }
}
