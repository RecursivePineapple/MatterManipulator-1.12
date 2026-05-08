package matter_manipulator.compat.ae2uel.analysis.machine;

import java.lang.invoke.MethodHandle;
import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;

import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.util.AEColor;
import appeng.api.util.IConfigurableObject;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IPriorityHost;
import appeng.tile.AEBaseTile;
import appeng.tile.misc.TileInterface;
import com.github.bsideup.jabel.Desugar;
import matter_manipulator.MatterManipulator;
import matter_manipulator.common.inventory_adapter.ItemHandlerInventoryAdapterFactory.ItemHandlerInventoryAdapter;
import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.core.analysis.ApplyMode;
import matter_manipulator.core.analysis.InventoryAnalysis;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.resources.item.ItemStackWrapper;

@Desugar
public record MachineData(EnumFacing up, EnumFacing forward, NBTTagCompound data, AEColor color, String customName,
    InventoryAnalysis upgrades, InventoryAnalysis config, int priority, boolean omniDirectional) {

    public static MachineData fromMachine(AEBaseTile tile) {
        EnumFacing up = tile.getUp();
        EnumFacing forward = tile.getForward();

        boolean omniDirectional = false;

        if (tile instanceof TileInterface iface) {
            omniDirectional = iface.isOmniDirectional();
        }

        NBTTagCompound data = null;

        if (tile instanceof IConfigurableObject configurable && configurable.getConfigManager() != null) {
            data = new NBTTagCompound();
            configurable.getConfigManager()
                .writeToNBT(data);
            if (data.isEmpty()) data = null;
        }

        AEColor color = tile instanceof IColorableTile colorable ? colorable.getColor() : null;

        String customName = tile.getCustomInventoryName();

        InventoryAnalysis upgrade = null, config = null;

        if (tile instanceof ISegmentedInventory inv) {
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

        if (tile instanceof IPriorityHost priorityHost) {
            priority = priorityHost.getPriority();
        }

        return new MachineData(up, forward, data, color, customName, upgrade, config, priority, omniDirectional);
    }

    private static final MethodHandle OMNIDIRECTIONAL = DataUtils.exposeFieldSetter(
        TileInterface.class,
        "omniDirectional");

    public void apply(BlockPlacingContext context, TileEntity tile, EnumSet<ApplyResult> result) {
        if (tile instanceof AEBaseTile ae) {
            if (this.forward != null && this.up != null) {
                ae.setOrientation(this.forward, this.up);
            }
        }

        if (tile instanceof TileInterface iface) {
            try {
                OMNIDIRECTIONAL.invokeExact(iface, (boolean) this.omniDirectional);
            } catch (Throwable e) {
                MatterManipulator.LOG.error("Could not set TileInterface.omniDirectional", e);
            }
        }

        if (tile instanceof ICustomNameObject c) {
            if (this.customName != null) c.setCustomName(this.customName);
        }

        if (tile instanceof IConfigurableObject configurable && configurable.getConfigManager() != null) {
            configurable.getConfigManager()
                .readFromNBT(data == null ? new NBTTagCompound() : data);
        }

        if (tile instanceof ISegmentedInventory inv) {
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

        if (tile instanceof IPriorityHost priorityHost) {
            priorityHost.setPriority(this.priority);
        }
    }

    public void getRequiredItemsForExistingBlock(BlockPlacingContext context, TileEntity tile, EnumSet<ApplyResult> result) {
        if (tile instanceof ISegmentedInventory inv) {
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
        if (this.upgrades != null) {
            this.upgrades.slots.forEach((slot, item) -> {
                context.items().extract(new ItemStackWrapper(item));
            });
        }
    }
}
