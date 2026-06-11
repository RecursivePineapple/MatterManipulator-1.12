package matter_manipulator.common.uplink;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.HashSet;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import lombok.Getter;
import lombok.Setter;
import matter_manipulator.CommonProxy;
import matter_manipulator.client.rendering.MMHintRenderer;
import matter_manipulator.common.block_spec.specs.SimpleBlockSpec;
import matter_manipulator.common.blocks.BlockUplinkController;
import matter_manipulator.common.structure.Alignment;
import matter_manipulator.common.structure.IAlignmentLimits;
import matter_manipulator.common.structure.IStructureDefinition;
import matter_manipulator.common.structure.MultiblockController;
import matter_manipulator.common.structure.MultiblockInteractContext;
import matter_manipulator.common.structure.StructureElement;
import matter_manipulator.common.structure.StructureOverlord;
import matter_manipulator.common.structure.StructureUtils;
import matter_manipulator.common.structure.coords.ControllerRelativeCoords;
import matter_manipulator.common.ui.factory.UplinkUIFactory;
import matter_manipulator.common.utils.enums.ExtendedFacing;
import matter_manipulator.common.utils.math.VoxelAABB;
import matter_manipulator.core.context.StructureContext;
import matter_manipulator.core.context.StructureInteractContext;
import matter_manipulator.core.meta.MetaKey;

public class TileUplinkController extends TileEntity implements MultiblockController<TileUplinkController>, ITickable,
    Alignment, Uplink {

    private static final String[][] SHAPE = {
        {
            "         ",
            "         ",
            "         ",
            "         ",
            "  AA~AA  ",
            "         ",
            "         ",
            "         ",
            "         "
        }, {
            "         ",
            "         ",
            "  A   A  ",
            " AA   AA ",
            " AA   AA ",
            " AA   AA ",
            "  A   A  ",
            "         ",
            "         "
        }, {
            "         ",
            "  A   A  ",
            " ACCCCCA ",
            " AD   DA ",
            "A D   D A",
            " AD   DA ",
            " ACCCCCA ",
            "  A   A  ",
            "         "
        }, {
            "         ",
            " AA   AA ",
            " AD   DA ",
            "A       A",
            "A       A",
            "A       A",
            " AD   DA ",
            " AA   AA ",
            "         "
        }, {
            "  A   A  ",
            " AA   AA ",
            "A D   D A",
            "A       A",
            "ABBE EBBA",
            "A       A",
            "A D   D A",
            " AA   AA ",
            "  A   A  "
        }, {
            "         ",
            " AA   AA ",
            " AD   DA ",
            "A       A",
            "A       A",
            "A       A",
            " AD   DA ",
            " AA   AA ",
            "         "
        }, {
            "         ",
            "  A   A  ",
            " ACCCCCA ",
            " AD   DA ",
            "A D   D A",
            " AD   DA ",
            " ACCCCCA ",
            "  A   A  ",
            "         "
        }, {
            "         ",
            "         ",
            "  A   A  ",
            " AA   AA ",
            " AA   AA ",
            " AA   AA ",
            "  A   A  ",
            "         ",
            "         "
        }, {
            "         ",
            "         ",
            "         ",
            "         ",
            "  A   A  ",
            "         ",
            "         ",
            "         ",
            "         "
        }
    };

    private static final IStructureDefinition<TileUplinkController> STRUCTURE = IStructureDefinition.<TileUplinkController>builder()
        .addPart("main", SHAPE)
        .addElement('A', StructureUtils.chain(new ModuleStructureElement(), StructureUtils.spec(() -> new SimpleBlockSpec(CommonProxy.UPLINK_STRUCTURE_WHITE.getDefaultState()))))
        .addElement('B', StructureUtils.spec(() -> new SimpleBlockSpec(CommonProxy.UPLINK_SUPPORT_BLACK.getDefaultState())))
        .addElement('C', StructureUtils.spec(() -> new SimpleBlockSpec(CommonProxy.UPLINK_SUPPORT_WHITE.getDefaultState())))
        .addElement('D', StructureUtils.spec(() -> new SimpleBlockSpec(CommonProxy.UPLINK_COIL.getDefaultState())))
        .addElement('E', StructureUtils.spec(() -> new SimpleBlockSpec(CommonProxy.UPLINK_STRUCTURE_BLACK.getDefaultState())))
        .build();

    private static class ModuleStructureElement implements StructureElement<TileUplinkController> {

        @Override
        public @Nullable <K> K getMetadata(MetaKey<K> key) {
            return null;
        }

        @Override
        public boolean check(StructureContext<? extends TileUplinkController> context, BlockPos pos) {
            if (context.getWorld().getTileEntity(pos) instanceof TileUplinkModule module) {
                context.getData().addModule(module);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean build(StructureInteractContext<? extends TileUplinkController> context, BlockPos pos) {
            return false;
        }

        @Override
        public void emitHint(StructureInteractContext<? extends TileUplinkController> context, BlockPos pos) {

        }
    }

    @Setter
    private ExtendedFacing orientation = ExtendedFacing.DEFAULT;
    private ControllerRelativeCoords controllerCoords;
    private VoxelAABB aabb;

    private boolean structureDirty = true, formed = false;

    final HashSet<TileUplinkModule> modules = new HashSet<>();

    @Getter
    @Setter
    private long address = -1;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);

    @Override
    public void onMachineUpdate(BlockPos pos) {
        if (aabb.contains(pos.getX(), pos.getY(), pos.getZ())) {
            structureDirty = true;
        }
    }

    @Override
    public IStructureDefinition<? super TileUplinkController> getDefinition() {
        return STRUCTURE;
    }

    @Override
    public ExtendedFacing getOrientation() {
        return orientation;
    }

    @Override
    public ControllerRelativeCoords getControllerPos() {
        return controllerCoords;
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if (aabb != null && !this.world.isRemote) {
            UPLINKS.remove(address);
            StructureOverlord.get((WorldServer) this.world).removeController(this);
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public void updateBoundingBox() {
        if (aabb != null && !this.world.isRemote) {
            UPLINKS.remove(address);
            StructureOverlord.get((WorldServer) this.world).removeController(this);
        }

        controllerCoords = new ControllerRelativeCoords(this.pos);

        var min = STRUCTURE.getMinCorner("main");
        var min2 = orientation.asCoordinateSystem().translateInverse(min);
        var min3 = controllerCoords.translateInverse(min2);

        var max = STRUCTURE.getMaxCorner("main");
        var max2 = orientation.asCoordinateSystem().translateInverse(max);
        var max3 = controllerCoords.translateInverse(max2);

        this.aabb = new VoxelAABB(min3.toVector3i(), max3.toVector3i());

        this.aabb.origin.set(this.pos.getX(), this.pos.getY(), this.pos.getZ());

        if (!this.world.isRemote) {
            if (address == -1) {
                address = System.currentTimeMillis();
            }

            UPLINKS.put(address, new WeakReference<>(this));
            StructureOverlord.get((WorldServer) this.world).addController(this);
        }
    }

    public void updateState() {
        UplinkState state;

        if (!formed) {
            state = UplinkState.off;
        } else {
            state = UplinkState.idle;

            for (var module : modules) {
                if (module.isActive()) {
                    state = UplinkState.active;
                    break;
                }
            }
        }

        world.setBlockState(pos, CommonProxy.UPLINK_CONTROLLER.getDefaultState().withProperty(BlockUplinkController.STATE, state));
    }

    public void addModule(TileUplinkModule module) {
        this.modules.add(module);
        module.connect(this);
    }

    @Override
    public VoxelAABB getBoundingBox() {
        if (this.aabb == null) updateBoundingBox();

        return aabb;
    }

    @Override
    public void setPos(BlockPos posIn) {
        super.setPos(posIn);

        updateBoundingBox();
    }

    @Override
    public int getBlockX() {
        return pos.getX();
    }

    @Override
    public int getBlockY() {
        return pos.getY();
    }

    @Override
    public int getBlockZ() {
        return pos.getZ();
    }

    @Override
    public void update() {
        if (this.aabb == null) updateBoundingBox();

        if (this.structureDirty) {
            this.structureDirty = false;

            for (var module : modules) {
                module.disconnect(this);
            }

            modules.clear();

            MultiblockInteractContext<TileUplinkController> context = new MultiblockInteractContext<>(this);
            context.part = "main";
            formed = STRUCTURE.checkPart(context);

            if (formed) {
                for (var module : modules) {
                    module.connect(this);
                }
            }

            updateState();
        }
    }

    public void onBuild(EntityPlayer player, ItemStack trigger) {
        if (world.isRemote) return;

        MultiblockInteractContext<TileUplinkController> context = new MultiblockInteractContext<>(this, player, trigger, 10);

        STRUCTURE.build(context);

        context.onInteractionFinished();
    }

    public void emitHints(EntityPlayer player, ItemStack trigger) {
        MultiblockInteractContext<TileUplinkController> context = new MultiblockInteractContext<>(this, player, trigger, 0);

        MMHintRenderer.INSTANCE.start();
        MMHintRenderer.INSTANCE.setExpiry(Duration.ofSeconds(30));
        MMHintRenderer.INSTANCE.setDepthTest(true);

        STRUCTURE.emitHints(context);

        MMHintRenderer.INSTANCE.finish();

        context.onInteractionFinished();
    }

    public void openUI(EntityPlayer player) {
        if (player instanceof EntityPlayerMP playerMP) {
            UplinkUIFactory.INSTANCE.open(playerMP, this);
        }
    }

    public IItemHandlerModifiable getInventory() {
        return new ItemStackHandler(inventory) {

            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }
        };
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);

        readFromNBT(pkt.getNbtCompound());

        IBlockState state = this.world.getBlockState(this.pos);

        this.world.notifyBlockUpdate(this.pos, state, state, 3);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setString("facing", this.orientation.name());
        compound.setLong("address", address);

        if (!this.inventory.get(0).isEmpty()) {
            compound.setTag("input", this.inventory.get(0).writeToNBT(new NBTTagCompound()));
        }

        if (!this.inventory.get(1).isEmpty()) {
            compound.setTag("output", this.inventory.get(1).writeToNBT(new NBTTagCompound()));
        }

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        this.orientation = ExtendedFacing.valueOf(compound.getString("facing"));
        this.address = compound.getLong("address");

        if (compound.hasKey("input")) {
            this.inventory.set(0, new ItemStack(compound.getCompoundTag("input")));
        }

        if (compound.hasKey("output")) {
            this.inventory.set(1, new ItemStack(compound.getCompoundTag("output")));
        }
    }

    @Override
    public ExtendedFacing getExtendedFacing() {
        return this.orientation;
    }

    @Override
    public void setExtendedFacing(ExtendedFacing alignment) {
        this.orientation = alignment;

        updateBoundingBox();

        IBlockState state = this.world.getBlockState(this.pos);

        this.world.notifyBlockUpdate(this.pos, state, state, 3);
    }

    @Override
    public IAlignmentLimits getAlignmentLimits() {
        return (dir, rot, flip) -> !flip.isVerticallyFlipped();
    }

    @Override
    public long drainEnergy(long request) {
        long drained = 0;

        for (var module : modules) {
            if (module instanceof UplinkPowerProvider power) {
                long d = power.drainEnergy(request);
                request -= d;
                drained += d;

                if (request <= 0) break;
            }
        }

        return drained;
    }

    @Override
    public @org.jetbrains.annotations.Nullable UplinkPlanReceiver getPlanReceiver() {
        for (var module : modules) {
            if (module instanceof UplinkPlanReceiver recv) {
                return recv;
            }
        }

        return null;
    }
}
