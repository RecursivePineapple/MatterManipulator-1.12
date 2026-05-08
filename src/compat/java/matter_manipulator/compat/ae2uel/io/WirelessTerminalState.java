package matter_manipulator.compat.ae2uel.io;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.cluster.implementations.QuantumCluster;
import appeng.me.helpers.PlayerSource;
import appeng.tile.misc.TileSecurityStation;
import appeng.tile.networking.TileWireless;
import appeng.tile.qnb.TileQuantumBridge;
import appeng.util.Platform;
import com.github.bsideup.jabel.Desugar;
import matter_manipulator.compat.ae2uel.util.InvSlotHandle;

@Desugar
public record WirelessTerminalState(InvSlotHandle slot, IWirelessTermHandler handler, TileSecurityStation securityStation,
    IWirelessAccessPoint accessPoint, QuantumCluster cluster, IEnergySource energy, IMEMonitor<IAEItemStack> items,
    IMEMonitor<IAEFluidStack> fluids, PlayerSource source) {

    public IAEItemStack extractItem(IAEItemStack request, Actionable mode) {
        return Platform.poweredExtraction(energy, items, request, source, mode);
    }

    public IAEItemStack insertItem(IAEItemStack request, Actionable mode) {
        return Platform.poweredInsert(energy, items, request, source, mode);
    }

    public IAEFluidStack extractFluid(IAEFluidStack request, Actionable mode) {
        return Platform.poweredExtraction(energy, fluids, request, source, mode);
    }

    public IAEFluidStack insertFluid(IAEFluidStack request, Actionable mode) {
        return Platform.poweredInsert(energy, fluids, request, source, mode);
    }

    @Nullable
    public static WirelessTerminalState getWirelessTerminal(EntityPlayer player) {
        // Any terminal can supply items and fluids because it's too much of a PITA to filter them properly
        InvSlotHandle slot = InvSlotHandle.find(player, stack -> {
            return AEApi.instance()
                .registries()
                .wireless()
                .getWirelessTerminalHandler(stack) != null;
        });

        if (slot == null) return null;

        ItemStack terminal = slot.getStack();

        IWirelessTermHandler handler = AEApi.instance()
            .registries()
            .wireless()
            .getWirelessTerminalHandler(terminal);

        if (handler == null) return null;

        String unparsedKey = handler.getEncryptionKey(terminal);

        long parsedKey = Long.parseLong(unparsedKey);
        ILocatable securityStation = AEApi.instance()
            .registries()
            .locatable()
            .getLocatableBy(parsedKey);

        if (securityStation == null) return null;
        if (!(securityStation instanceof TileSecurityStation sec)) return null;

        IStorageGrid cache;
        IEnergySource energy;
        IWirelessAccessPoint accessPoint = null;
        QuantumCluster cluster = null;

        try {
            IGrid grid = sec.getProxy()
                .getGrid();

            accessPoint = findAccessPoint(player, grid);

            if (accessPoint == null) {
                cluster = findQC(grid);

                if (cluster == null) {
                    return null;
                }
            }

            cache = grid
                .getCache(IStorageGrid.class);
            energy = grid
                .getCache(IEnergyGrid.class);
        } catch (GridAccessException e) {
            return null;
        }

        IMEMonitor<IAEItemStack> items = cache.getInventory(AEApi.instance()
            .storage()
            .getStorageChannel(IItemStorageChannel.class));

        IMEMonitor<IAEFluidStack> fluids = cache.getInventory(AEApi.instance()
            .storage()
            .getStorageChannel(IFluidStorageChannel.class));

        PlayerSource source = new PlayerSource(player, sec);

        return new WirelessTerminalState(slot, handler, sec, accessPoint, cluster, energy, items, fluids, source);
    }

    private static IWirelessAccessPoint findAccessPoint(EntityPlayer player, IGrid grid) {
        for (IGridNode node : grid.getMachines(TileWireless.class)) {
            if (checkAEDistance(player, grid, (IWirelessAccessPoint) node.getMachine())) {
                return (IWirelessAccessPoint) node.getMachine();
            }
        }

        return null;
    }

    private static QuantumCluster findQC(IGrid grid) {
        for(IGridNode n : grid.getMachines(TileQuantumBridge.class)) {
            TileQuantumBridge tqb = (TileQuantumBridge)n.getMachine();
            if (tqb.getCluster() != null) {
                TileQuantumBridge center = ((QuantumCluster)tqb.getCluster()).getCenter();
                if (center != null && center.getInternalInventory().getStackInSlot(1).isItemEqual(AEApi.instance().definitions().materials().cardQuantumLink().maybeStack(1).get())) {
                    return (QuantumCluster) tqb.getCluster();
                }
            }
        }

        return null;
    }

    private static boolean checkAEDistance(EntityPlayer player, IGrid grid, IWirelessAccessPoint accessPoint) {
        if (accessPoint != null && accessPoint.getGrid() == grid && accessPoint.isActive()) {
            DimensionalCoord coord = accessPoint.getLocation();

            if (coord.getWorld().provider.getDimension() != player.getEntityWorld().provider.getDimension()) return false;

            double distance = player.getDistanceSq(coord.x, coord.y, coord.z);
            double range = accessPoint.getRange();

            return range * range >= distance;
        } else {
            return false;
        }
    }
}
