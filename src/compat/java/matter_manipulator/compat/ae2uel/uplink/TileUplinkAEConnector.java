package matter_manipulator.compat.ae2uel.uplink;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextFormatting;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.helpers.ItemStackHelper;
import appeng.helpers.PatternHelper;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import appeng.util.item.AEItemStack;
import com.google.common.collect.ImmutableSet;
import matter_manipulator.MatterManipulator;
import matter_manipulator.common.items.MMItemList;
import matter_manipulator.common.uplink.TileUplinkModule;
import matter_manipulator.common.uplink.UplinkPlanReceiver;
import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.common.utils.MCUtils;
import matter_manipulator.core.fluid.FluidStackLike;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.item.ItemStackLike;
import matter_manipulator.core.planning.BuildPlan;

public class TileUplinkAEConnector extends TileUplinkModule implements IGridProxyable, IGridHost, UplinkPlanReceiver, ICraftingProvider, ICraftingRequester, IPowerChannelState, ITickable {

    protected IActionSource requestSource = null;
    protected AENetworkProxy gridProxy = null;

    private final List<ManipulatorRequest> requests = new ArrayList<>();

    /**
     * Stored items that are being pushed to the ME network.
     */
    private List<ItemStack> pendingCraft;

    private int tickCounter;

    private NBTTagCompound deferredProxyTag;

    public TileUplinkAEConnector() {}

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound tag) {
        super.writeToNBT(tag);

        if (pendingCraft != null) {
            NBTTagList pendingItems = new NBTTagList();

            for (ItemStack stack : pendingCraft) {
                pendingItems.appendTag(stack.writeToNBT(new NBTTagCompound()));
            }

            tag.setTag("items", pendingItems);
        }

        NBTTagList req = new NBTTagList();

        for (ManipulatorRequest request : requests) {
            req.appendTag(request.writeToNBT(new NBTTagCompound()));
        }

        tag.setTag("requests", req);

        getProxy().writeToNBT(tag);

        return tag;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tag) {
        super.readFromNBT(tag);

        if (tag.hasKey("items")) {
            pendingCraft = new LinkedList<>();

            for (NBTTagCompound item : MCUtils.getCompoundTagList(tag, "items")) {
                pendingCraft.add(new ItemStack(item));
            }
        }

        requests.clear();

        for (NBTTagCompound request : MCUtils.getCompoundTagList(tag, "requests")) {
            requests.add(ManipulatorRequest.readFromNBT(this, request));
        }

        requests.remove(null);

        deferredProxyTag = tag;
    }

    @Override
    public AENetworkProxy getProxy() {
        if (this.world == null) throw new IllegalStateException("Cannot call getProxy() without world");
        if (this.isInvalid()) throw new IllegalStateException("Cannot call getProxy() on a deleted tile");

        if (gridProxy == null) {
            gridProxy = new AENetworkProxy(this, "proxy", MMItemList.UplinkHatch.toStack(1), true);
            gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);

            if (deferredProxyTag != null) {
                gridProxy.readFromNBT(deferredProxyTag);
                deferredProxyTag = null;
            }

            gridProxy.setValidSides(EnumSet.of(getFrontFacing()));

            onRequestsChanged();
        }

        return gridProxy;
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if (gridProxy != null) {
            gridProxy.getNode().destroy();
        }
    }

    @Override
    public @NotNull IGridNode getActionableNode() {
        return getProxy().getNode();
    }

    @Override
    public @Nullable IGridNode getGridNode(@NotNull AEPartLocation aePartLocation) {
        return getProxy().getNode();
    }

    @Override
    public @NotNull AECableType getCableConnectionType(@NotNull AEPartLocation location) {
        return getFrontFacing() == location.getFacing() ? AECableType.SMART : AECableType.NONE;
    }

    @Override
    public void securityBreak() {}

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(getWorld(), getPos().getX(), getPos().getY(), getPos().getZ());
    }

    @Override
    public void gridChanged() {

    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        ImmutableSet.Builder<ICraftingLink> jobs = ImmutableSet.builder();

        for (ManipulatorRequest request : requests) {
            if (request.link != null) jobs.add(request.link);
        }

        return jobs.build();
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
        return items;
    }

    @Override
    public void jobStateChange(ICraftingLink link) {

    }

    @Override
    public void addPlan(BuildPlan plan) {
        List<ItemStack> items = new ArrayList<>();

        for (var e : plan.required.object2LongEntrySet()) {
            if (e.getKey() instanceof ItemStackLike item) {
                long l = e.getLongValue();

                while (l > 0) {
                    ItemStack split = item.toStack((int) Math.min(l, Integer.MAX_VALUE));

                    l -= split.getCount();

                    items.add(split);
                }

                continue;
            }

            if (e.getKey() instanceof FluidStackLike fluid) {
                // TODO: fluids
            }

            EntityPlayer player = plan.getPlayer(world.getMinecraftServer());

            if (player != null) {
                MCUtils.sendInfoToPlayer(player, new Localized("mm.info.invalid_ae_stack", e.getKey().getName()));
            }
        }

        requests.add(new ManipulatorRequest(plan, items));
        onRequestsChanged();
    }

    @Override
    public List<BuildPlan> getPlans() {
        return DataUtils.mapToList(requests, r -> r.plan);
    }

    @Override
    public void deletePlan(BuildPlan plan) {
        requests.removeIf(req -> {
            if (req.plan == plan) {
                if (req.link != null) {
                    req.link.cancel();

                    EntityPlayer player = req.plan.getPlayer(world.getMinecraftServer());
                    if (player != null) {
                        MCUtils.sendErrorToPlayer(player, new Localized("mm.info.error.craft_failed", req.plan.name));
                    }
                }

                if (req.job != null) {
                    req.job.cancel(false);
                }

                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        for (ManipulatorRequest request : requests) {
            try {
                PatternHelper pattern = new PatternHelper(request.getPattern(), world);
                craftingTracker.addCraftingOption(this, pattern);
            } catch (IllegalStateException e) {
                MatterManipulator.LOG.error("Could not load matter manipulator plan", e);
            }
        }
    }

    private void pushPendingCraft() {
        if (pendingCraft == null) return;

        IStorageGrid grid = getStorageGrid();

        if (grid == null) return;

        IMEMonitor<IAEItemStack> itemInventory = grid.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));

        if (itemInventory == null) return;

        Iterator<ItemStack> iter = pendingCraft.iterator();

        while (iter.hasNext()) {
            ItemStack current = iter.next();

            IAEItemStack result = itemInventory.injectItems(AEItemStack.fromItemStack(current), Actionable.MODULATE, getRequestSource());

            if (result != null) {
                current.setCount((int) result.getStackSize());
            } else {
                iter.remove();
            }
        }

        if (pendingCraft.isEmpty()) {
            pendingCraft = null;
        }
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting inventory) {
        pushPendingCraft();

        if (isBusy()) {
            return false;
        }

        pendingCraft = MCUtils.streamInventory(inventory).filter(i -> !i.isEmpty()).collect(Collectors.toCollection(LinkedList::new));

        PatternHelper pattern = (PatternHelper) patternDetails;

        IAEItemStack hologram = pattern.getCondensedOutputs()[0];

        Iterator<ManipulatorRequest> iter = requests.iterator();

        while (iter.hasNext()) {
            ManipulatorRequest request = iter.next();

            if (!request.plan.autoSubmit) continue;

            if (hologram.isSameType(request.hologram)) {
                if (request.link != null) {
                    request.link.cancel();

                    EntityPlayer player = request.plan.getPlayer(world.getMinecraftServer());

                    if (player != null) {
                        MCUtils.sendInfoToPlayer(player, new Localized("mm.info.craft_finished", request.plan.name));
                    }
                }

                iter.remove();
            }
        }

        onRequestsChanged();

        pushPendingCraft();

        return true;
    }

    @Override
    public boolean isBusy() {
        return pendingCraft != null;
    }

    @Override
    public void update() {
        if (world.isRemote) return;

        if (tickCounter == 0) {
            getProxy().onReady();
        }

        if (tickCounter++ % 20 == 0) {
            boolean isActive = isActive();

            if (isActive && !requests.isEmpty()) {
                pollRequests();
            }

            pushPendingCraft();
        }
    }

    private void pollRequests() {
        Iterator<ManipulatorRequest> iter = requests.iterator();

        while (iter.hasNext()) {
            ManipulatorRequest request = iter.next();

            if (!request.plan.autoSubmit) continue;

            if (!request.poll()) {
                EntityPlayer player = request.plan.getPlayer(world.getMinecraftServer());
                if (player != null) {
                    MCUtils.sendErrorToPlayer(player, new Localized("mm.info.error.craft_failed", request.plan.name));
                }
                iter.remove();
                onRequestsChanged();
            }
        }
    }

    private void onRequestsChanged() {
        try {
            getProxy().getGrid().postEvent(new MENetworkCraftingPatternChange(this, getProxy().getNode()));
        } catch (final GridAccessException e) {
            // :P
        }
    }

    public IGrid getGrid() {
        try {
            return getProxy().getGrid();
        } catch (GridAccessException e) {
            return null;
        }
    }

    public IStorageGrid getStorageGrid() {
        IGrid grid = getGrid();

        if (grid == null) return null;

        return grid.getCache(IStorageGrid.class);
    }

    public ICraftingGrid getCraftingGrid() {
        IGrid grid = getGrid();

        if (grid == null) return null;

        return grid.getCache(ICraftingGrid.class);
    }

    public IActionSource getRequestSource() {
        if (requestSource == null) requestSource = new MachineSource(this);
        return requestSource;
    }

    @Override
    public boolean isPowered() {
        return getProxy() != null && getProxy().isPowered();
    }

    @Override
    public boolean isActive() {
        return getProxy() != null && getProxy().isActive();
    }

    private class ManipulatorRequest {

        public final BuildPlan plan;
        public final List<ItemStack> requiredItems;
        public final ItemStack hologram;

        public Future<ICraftingJob> job;
        public ICraftingLink link;

        private static int counter;

        public ManipulatorRequest(BuildPlan plan, List<ItemStack> requiredItems) {
            this.plan = plan;
            this.requiredItems = requiredItems;

            hologram = MMItemList.Hologram.toStack(1);
            hologram.setStackDisplayName(TextFormatting.RESET + plan.name);

            // add a random number so that holograms with the same name are still different
            hologram.getTagCompound().setInteger("discriminator", counter++);
        }

        private ManipulatorRequest(BuildPlan plan, List<ItemStack> requiredItems, ICraftingLink link) {
            this.plan = plan;
            this.requiredItems = requiredItems;
            this.link = link;

            hologram = MMItemList.Hologram.toStack(1);
            hologram.setStackDisplayName(TextFormatting.RESET + plan.name);

            // add a random number so that holograms with the same name are still different
            hologram.getTagCompound().setInteger("discriminator", counter++);
        }

        public NBTTagCompound writeToNBT(NBTTagCompound tag) {
            tag.setTag("plan", BuildPlan.writeToTag(plan));

            if (link != null) {
                NBTTagCompound linkTag = new NBTTagCompound();
                link.writeToNBT(linkTag);
                tag.setTag("link", linkTag);
            }

            NBTTagList items = new NBTTagList();

            for (ItemStack item : requiredItems) {
                items.appendTag(ItemStackHelper.stackToNBT(item));
            }

            tag.setTag("items", items);

            return tag;
        }

        public static ManipulatorRequest readFromNBT(TileUplinkAEConnector hatch, NBTTagCompound tag) {
            BuildPlan plan = BuildPlan.readFromTag(tag.getCompoundTag("plan"));

            ICraftingLink link = null;

            if (tag.hasKey("link")) {
                link = AEApi.instance().storage().loadCraftingLink(tag.getCompoundTag("link"), hatch);
            }

            ArrayList<ItemStack> requiredItems = new ArrayList<>();

            for (NBTTagCompound item : MCUtils.getCompoundTagList(tag, "items")) {
                requiredItems.add(ItemStackHelper.stackFromNBT(item));
            }

            requiredItems.removeIf(s -> s == null || s.isEmpty());

            if (requiredItems.isEmpty()) return null;

            return hatch.new ManipulatorRequest(plan, requiredItems, link);
        }

        /// Check the job future and crafting link. If the job future has finished, submit the plan.
        /// If the crafting link was cancelled, tell the hatch to remove this request.
        /// The crafting job will never actually get completed. It gets cancelled and this request is removed when the
        /// fake pattern is pushed.
        boolean poll() {
            if (!isActive()) return true;

            ICraftingGrid cg;

            try {
                cg = getProxy().getCrafting();
            } catch (GridAccessException e) {
                return true;
            }

            if (link != null) {
                return !link.isCanceled();
            }

            if (job == null) {
                job = cg.beginCraftingJob(getWorld(), getGrid(), getRequestSource(), AEItemStack.fromItemStack(hologram), null);
            }

            if (job == null) return false;

            try {
                ICraftingJob job = null;

                if (this.job.isDone()) {
                    job = this.job.get();
                }

                if (job != null) {
                    link = cg.submitJob(job, TileUplinkAEConnector.this, null, false, getRequestSource());

                    if (link == null) return false;

                    EntityPlayer player = plan.getPlayer(TileUplinkAEConnector.this.world.getMinecraftServer());

                    if (player != null) {
                        MCUtils.sendInfoToPlayer(player, new Localized("mm.info.submitted_job", plan.name));
                    }
                }
            } catch (final InterruptedException | ExecutionException e) {
                // :P
            }

            return true;
        }

        /**
         * Creates a fake encoded pattern representing this request.
         */
        public ItemStack getPattern() {
            NBTTagCompound tag = new NBTTagCompound();

            tag.setBoolean("crafting", false);
            tag.setBoolean("substitute", false);

            NBTTagList out = new NBTTagList();

            out.appendTag(ItemStackHelper.stackToNBT(this.hologram));

            tag.setTag("out", out);

            NBTTagList in = new NBTTagList();

            for (ItemStack stack : requiredItems) {
                in.appendTag(ItemStackHelper.stackToNBT(stack));
            }

            tag.setTag("in", in);

            ItemStack pattern = AEApi.instance().definitions().items().encodedPattern().maybeStack(1).get();

            pattern.setTagCompound(tag);

            return pattern;
        }
    }
}
