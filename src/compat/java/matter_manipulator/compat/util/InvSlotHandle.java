package matter_manipulator.compat.util;

import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandlerModifiable;

import baubles.api.BaublesApi;
import com.github.bsideup.jabel.Desugar;
import matter_manipulator.compat.Mods;

@Desugar
public record InvSlotHandle(EntityPlayer player, InvType invType, int slot) {

    public enum InvType {
        Armor,
        Inventory,
        Offhand,
        Baubles
    }

    public ItemStack getStack() {
        if (invType == InvType.Baubles) {
            if (Mods.Baubles.isModLoaded()) {
                IItemHandlerModifiable baubles = BaublesApi.getBaublesHandler(player);

                return slot >= 0 && slot < baubles.getSlots() ? baubles.getStackInSlot(slot) : ItemStack.EMPTY;
            } else {
                return ItemStack.EMPTY;
            }
        }

        var inv = switch (invType) {
            case Armor -> player.inventory.armorInventory;
            case Inventory -> player.inventory.mainInventory;
            case Offhand -> player.inventory.offHandInventory;
            case Baubles -> throw new AssertionError();
        };

        return slot >= 0 && slot < inv.size() ? inv.get(slot) : ItemStack.EMPTY;
    }

    public void setStack(ItemStack stack) {
        if (invType == InvType.Baubles) {
            if (Mods.Baubles.isModLoaded()) {
                IItemHandlerModifiable baubles = BaublesApi.getBaublesHandler(player);

                if (slot >= 0 && slot < baubles.getSlots()) {
                    baubles.setStackInSlot(slot, stack);
                }
            }

            return;
        }

        var inv = switch (invType) {
            case Armor -> player.inventory.armorInventory;
            case Inventory -> player.inventory.mainInventory;
            case Offhand -> player.inventory.offHandInventory;
            case Baubles -> throw new AssertionError();
        };

        if (slot >= 0 && slot < inv.size()) {
            inv.set(slot, stack);
        }
    }

    public static InvSlotHandle find(EntityPlayer player, Predicate<ItemStack> filter) {
        NonNullList<ItemStack> inv = player.inventory.offHandInventory;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.get(i);

            if (stack.isEmpty()) continue;
            if (!filter.test(stack)) continue;

            return new InvSlotHandle(player, InvType.Offhand, i);
        }

        inv = player.inventory.mainInventory;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.get(i);

            if (stack.isEmpty()) continue;
            if (!filter.test(stack)) continue;

            return new InvSlotHandle(player, InvType.Inventory, i);
        }

        inv = player.inventory.armorInventory;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.get(i);

            if (stack.isEmpty()) continue;
            if (!filter.test(stack)) continue;

            return new InvSlotHandle(player, InvType.Armor, i);
        }

        if (Mods.Baubles.isModLoaded()) {
            IItemHandlerModifiable baubles = BaublesApi.getBaublesHandler(player);

            for (int i = 0; i < baubles.getSlots(); i++) {
                ItemStack stack = baubles.getStackInSlot(i);

                if (stack.isEmpty()) continue;
                if (!filter.test(stack)) continue;

                return new InvSlotHandle(player, InvType.Baubles, i);
            }
        }

        return null;
    }
}
