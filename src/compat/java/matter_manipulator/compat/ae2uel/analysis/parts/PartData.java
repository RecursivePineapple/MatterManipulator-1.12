package matter_manipulator.compat.ae2uel.analysis.parts;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.parts.IPart;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigurableObject;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.cache.P2PCache;
import appeng.parts.p2p.PartP2PTunnel;
import appeng.tile.networking.TileCableBus;
import com.github.bsideup.jabel.Desugar;
import matter_manipulator.MatterManipulator;
import matter_manipulator.common.inventory_adapter.ItemHandlerInventoryAdapterFactory.ItemHandlerInventoryAdapter;
import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.common.utils.MCUtils;
import matter_manipulator.core.analysis.ApplyMode;
import matter_manipulator.core.analysis.InventoryAnalysis;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.resources.item.IntItemResourceStack;
import matter_manipulator.core.resources.item.ItemStackWrapper;

@Desugar
public record PartData(ItemStack partStack, NBTTagCompound data, String customName, InventoryAnalysis upgrades,
    InventoryAnalysis config, boolean isP2POutput, short p2pFreq, int priority) {

    public static PartData fromPart(IPart part) {
        ItemStack stack = part.getItemStack(PartItemStack.BREAK);

        String customName = part instanceof ICustomNameObject c && c.hasCustomInventoryName() ? c.getCustomInventoryName() : null;

        NBTTagCompound data = null;

        if (part instanceof IConfigurableObject configurable && configurable.getConfigManager() != null) {
            data = new NBTTagCompound();
            configurable.getConfigManager()
                .writeToNBT(data);
            if (data.isEmpty()) data = null;
        }

        boolean isP2POutput = false;
        short freq = 0;

        if (part instanceof PartP2PTunnel<?> p2p) {
            isP2POutput = p2p.isOutput();
            freq = p2p.getFrequency();
        }

        InventoryAnalysis upgrade = null, config = null;

        if (part instanceof ISegmentedInventory inv) {
            IItemHandler upgradeInv = inv.getInventoryByName("upgrades");
            if (upgradeInv != null) {
                upgrade = InventoryAnalysis.fromInventory(new ItemHandlerInventoryAdapter(upgradeInv));
            }

            IItemHandler configInv = inv.getInventoryByName("config");
            if (configInv != null) {
                config = InventoryAnalysis.fromInventory(new ItemHandlerInventoryAdapter(configInv));
            }
        }

        int priority = 0;

        if (part instanceof IPriorityHost priorityHost) {
            priority = priorityHost.getPriority();
        }

        return new PartData(stack, data, customName, upgrade, config, isP2POutput, freq, priority);
    }

    private static final MethodHandle SET_OUTPUT = DataUtils.exposeMethod(
        PartP2PTunnel.class,
        MethodType.methodType(void.class, boolean.class),
        "setOutput");

    public void apply(BlockPlacingContext context, TileCableBus cableBus, AEPartLocation location,
        EnumSet<ApplyResult> result) {
        IPart part = cableBus.getPart(location);

        if (part != null) {
            ItemStack existingStack = part.getItemStack(PartItemStack.BREAK);

            if (!ItemStack.areItemsEqual(existingStack, this.partStack)) {
                removePart(context, cableBus, location, result, ApplyMode.NORMAL);
            }
        }

        part = cableBus.getPart(location);

        if (part == null) {
            installPart(context, cableBus, location, result, this.partStack, ApplyMode.NORMAL);

            if (ApplyResult.hasFailure(result)) {
                return;
            }

            part = cableBus.getPart(location);
        }

        if (part instanceof ICustomNameObject c) {
            if (this.customName != null) {
                c.setCustomName(this.customName);
            }
        }

        if (part instanceof IConfigurableObject configurable && configurable.getConfigManager() != null) {
            configurable.getConfigManager()
                .readFromNBT(data == null ? new NBTTagCompound() : data);
        }

        if (part instanceof PartP2PTunnel<?> tunnel) {
            try {
                SET_OUTPUT.invokeExact(tunnel, this.isP2POutput);
            } catch (Throwable e) {
                MatterManipulator.LOG.error("Could not call PartP2PTunnel.setOutput", e);
            }

            if (tunnel.getFrequency() != p2pFreq) {
                try {
                    final P2PCache p2p = tunnel.getProxy()
                        .getP2P();

                    // calls setFrequency
                    p2p.updateFreq(tunnel, p2pFreq);
                } catch (final GridAccessException e) {
                    // not on a grid yet, so we just set the frequency directly
                    tunnel.setFrequency(p2pFreq);
                }
            }

            tunnel.onTunnelConfigChange();
        }

        if (part instanceof ISegmentedInventory inv) {
            IItemHandler upgradeInv = inv.getInventoryByName("upgrades");
            if (upgradeInv != null && this.upgrades != null) {
                result.addAll(this.upgrades.apply(
                    context,
                    new ItemHandlerInventoryAdapter(upgradeInv),
                    ApplyMode.NORMAL));
            }

            IItemHandler configInv = inv.getInventoryByName("config");
            if (configInv != null && this.config != null) {
                result.addAll(this.config.apply(context, new ItemHandlerInventoryAdapter(configInv), ApplyMode.NO_IO));
            }
        }

        if (part instanceof IPriorityHost priorityHost) {
            priorityHost.setPriority(this.priority);
        }
    }

    public void getRequiredItemsForExistingBlock(BlockPlacingContext context, TileCableBus cableBus, AEPartLocation location, EnumSet<ApplyResult> result) {
        IPart part = cableBus.getPart(location);

        if (part != null) {
            ItemStack existingStack = part.getItemStack(PartItemStack.BREAK);

            if (!ItemStack.areItemsEqual(existingStack, this.partStack)) {
                removePart(context, cableBus, location, result, ApplyMode.SIMULATE_EXISTING);
            }
        }

        part = cableBus.getPart(location);

        if (part == null) {
            installPart(context, cableBus, location, result, this.partStack, ApplyMode.SIMULATE_EXISTING);

            if (ApplyResult.hasFailure(result)) {
                return;
            }

            part = cableBus.getPart(location);
        }

        if (part instanceof ISegmentedInventory inv) {
            IItemHandler upgradeInv = inv.getInventoryByName("upgrades");
            if (upgradeInv != null && this.upgrades != null) {
                result.addAll(this.upgrades.apply(
                    context,
                    new ItemHandlerInventoryAdapter(upgradeInv),
                    ApplyMode.SIMULATE_EXISTING));
            }
        }
    }

    public void getRequiredItemsForNewBlock(BlockPlacingContext context) {
        ItemStackWrapper toExtract = new ItemStackWrapper(partStack.copy());

        context.items().extract(toExtract);

        if (this.upgrades != null) {
            this.upgrades.slots.forEach((slot, item) -> {
                context.items().extract(new ItemStackWrapper(item));
            });
        }
    }

    public static void removePart(BlockPlacingContext context, TileCableBus cableBus, AEPartLocation location,
        EnumSet<ApplyResult> result, ApplyMode applyMode) {
        IPart part = cableBus.getPart(location);

        if (part == null) return;

        result.add(ApplyResult.DidSomething);

        if (applyMode.doIO()) {
            List<ItemStack> drops = new ArrayList<>();
            part.getDrops(drops, false);

            for (ItemStack stack : drops) {
                context.items()
                    .insert(new ItemStackWrapper(stack));
            }
        }

        if (!applyMode.isSimulate()) {
            cableBus.removePart(location, false);
        }

        if (applyMode.doIO()) {
            ItemStack partStack = part.getItemStack(PartItemStack.BREAK)
                .copy();

            NBTTagCompound tag = partStack.getTagCompound();

            // Manually clear the name
            if (tag != null) {
                tag.removeTag("display");

                if (tag.isEmpty()) {
                    partStack.setTagCompound(null);
                }
            }

            context.items()
                .insert(new ItemStackWrapper(partStack));
        }
    }

    public static void installPart(BlockPlacingContext context, TileCableBus cableBus, AEPartLocation location,
        EnumSet<ApplyResult> result, ItemStack partStack, ApplyMode applyMode) {
        if (!cableBus.canAddPart(partStack, location)) {
            if (location != AEPartLocation.INTERNAL) {
                context.error(new Localized(
                    "mm+ae2uel.chat.cannot_install_part",
                    partStack.copy(),
                    MCUtils.getDirectionDisplayName(location.getFacing())));
            } else {
                context.error(new Localized("mm+ae2uel.chat.cannot_install_cable", partStack.copy()));
            }

            result.add(ApplyResult.Error);
            return;
        }

        ItemStackWrapper toExtract = new ItemStackWrapper(partStack.copy());

        IntItemResourceStack extracted;

        if (applyMode.doIO()) {
            extracted = context.items().tryExtract(toExtract);
        } else {
            extracted = toExtract;
        }

        if (extracted == null) {
            context.extractionFailure(toExtract);
            result.add(ApplyResult.Retry);
        } else {
            boolean installed = applyMode.isSimulate() || cableBus.addPart(partStack.copy(), location, null, null) != null;

            if (!installed) {
                if (applyMode.doIO()) {
                    context.items()
                        .insert(extracted);
                }

                if (location != AEPartLocation.INTERNAL) {
                    context.error(new Localized(
                        "mm+ae2uel.chat.cannot_install_part",
                        partStack.copy(),
                        MCUtils.getDirectionDisplayName(location.getFacing())));
                } else {
                    context.error(new Localized("mm+ae2uel.chat.cannot_install_cable", partStack.copy()));
                }

                result.add(ApplyResult.Error);
            }
        }
    }
}
