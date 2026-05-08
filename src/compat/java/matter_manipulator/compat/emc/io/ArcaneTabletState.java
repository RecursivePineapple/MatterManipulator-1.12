package matter_manipulator.compat.emc.io;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.github.bsideup.jabel.Desugar;
import com.latmod.mods.projectex.integration.PersonalEMC;
import com.latmod.mods.projectex.item.ItemArcaneTablet;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import matter_manipulator.compat.util.InvSlotHandle;
import matter_manipulator.core.item.ItemId;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.gameObjs.items.TransmutationTablet;

@Desugar
public record ArcaneTabletState(InvSlotHandle tablet, IKnowledgeProvider knowledge, EntityPlayer player, ObjectOpenCustomHashSet<ItemStack> knownStacks) {

    public static ArcaneTabletState fromPlayer(EntityPlayer player) {
        InvSlotHandle tablet = InvSlotHandle.find(player, stack -> {
            return stack.getItem() instanceof TransmutationTablet || stack.getItem() instanceof ItemArcaneTablet;
        });

        if (tablet == null) return null;

        var knowledge = PersonalEMC.get(player);

        ObjectOpenCustomHashSet<ItemStack> knownStacks = new ObjectOpenCustomHashSet<>(knowledge.getKnowledge(), ItemId.STACK_ITEM_META_NBT_STRATEGY);

        return new ArcaneTabletState(tablet, knowledge, player, knownStacks);
    }

    public boolean hasKnowledge(ItemStack stack) {
        return knownStacks.contains(stack);
    }

    public long getBuyEMCValue(ItemStack stack) {
        if (!hasKnowledge(stack)) return 0;

        return ProjectEAPI.getEMCProxy().getValue(stack);
    }

    public long getSellEMCValue(ItemStack stack) {
        if (!hasKnowledge(stack)) return 0;

        return ProjectEAPI.getEMCProxy().getSellValue(stack);
    }

    public long getAvailable(ItemStack stack) {
        long value = getBuyEMCValue(stack);

        if (value == 0) return 0;

        return knowledge.getEmc() / value;
    }

    public long extract(ItemStack stack, long amount) {
        long value = getBuyEMCValue(stack);

        if (value == 0) return 0;

        long emc = knowledge.getEmc();

        long available = emc / value;

        long extracted = Math.min(amount, available);

        long emcExtracted = extracted * value;

        knowledge.setEmc(emc - emcExtracted);

        return extracted;
    }

    public boolean insert(ItemStack stack, long amount) {
        long value = getSellEMCValue(stack);

        if (value == 0) return false;

        knowledge.setEmc(knowledge.getEmc() + value * amount);

        return true;
    }

    public boolean insert(ItemStack stack) {
        long value = getSellEMCValue(stack);

        if (value == 0) return false;

        knowledge.setEmc(knowledge.getEmc() + value * stack.getCount());

        return true;
    }
}
