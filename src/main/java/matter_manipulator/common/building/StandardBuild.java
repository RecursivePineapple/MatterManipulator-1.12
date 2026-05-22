package matter_manipulator.common.building;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.apache.commons.lang3.mutable.MutableObject;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import matter_manipulator.MatterManipulator;
import matter_manipulator.common.context.AnalysisContextImpl;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.items.ManipulatorFlags;
import matter_manipulator.common.networking.MMPacketBuffer;
import matter_manipulator.common.utils.MCUtils;
import matter_manipulator.common.utils.world.ProxiedWorld;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.building.PendingBlock;
import matter_manipulator.core.building.PendingBlockBuildable;
import matter_manipulator.core.building.Plannable;
import matter_manipulator.core.context.HeldManipulatorContext;
import matter_manipulator.core.context.ManipulatorPlacingContext;
import matter_manipulator.core.context.PlanningContextImpl;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.planning.BuildPlan;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.util.Coroutine;

public class StandardBuild implements PendingBlockBuildable, Plannable {

    public final ArrayDeque<PendingBlock> pendingBlocks;
    private final ObjectOpenHashSet<BlockPos> visited = new ObjectOpenHashSet<>();
    private boolean done = false;

    public StandardBuild(ArrayDeque<PendingBlock> pendingBlocks) {
        this.pendingBlocks = pendingBlocks;
    }

    @Override
    public List<PendingBlock> getPendingBlocks() {
        return new ArrayList<>(pendingBlocks);
    }

    @Override
    public void onBuildTick(ManipulatorPlacingContext placingContext) {
        int placeSpeed = placingContext.getPlaceSpeed();

        Integer lastChunkX = null, lastChunkZ = null;
        int shuffleCount = 0;

        World world = placingContext.getWorld();
        ProxiedWorld proxiedWorld = new ProxiedWorld(world);

        AnalysisContextImpl analysisContext = new AnalysisContextImpl(placingContext);

        int quota = placeSpeed;

        List<PendingBlock> retry = new ArrayList<>();

        ResourceStack filter = null;

        while (quota > 0 && !pendingBlocks.isEmpty()) {
            PendingBlock pendingBlock = pendingBlocks.getFirst();

            int x = pendingBlock.x, z = pendingBlock.z;
            BlockPos pos = pendingBlock.toPos();
            IBlockState pendingState = pendingBlock.getBlockState();

            placingContext.setTarget(pos, pendingBlock.spec);

            if (!pendingBlock.spec.isValid()) {
                pendingBlocks.removeFirst();
                placingContext.error(new Localized("mm.info.error.unplaceable_block", pendingState.toString()));
                continue;
            }

            int chunkX = x >> 4;
            int chunkZ = z >> 4;

            // if this block's chunk isn't loaded, ignore it completely
            if (!Objects.equals(chunkX, lastChunkX) || !Objects.equals(chunkZ, lastChunkZ)) {
                if (world.getChunkProvider().getLoadedChunk(chunkX, chunkZ) == null) {
                    pendingBlocks.removeFirst();
                    continue;
                } else {
                    lastChunkX = chunkX;
                    lastChunkZ = chunkZ;
                }
            }

            if (world.isOutsideBuildHeight(pos)) {
                pendingBlocks.removeFirst();
                continue;
            }

            // If this block is protected, ignore it completely and print a warning because it'll
//            if (!isEditable(world, x, y, z)) {
//                pendingBlocks.removeFirst();
//                continue;
//            }

            if (pendingBlock.spec.isAir() && world.isAirBlock(pos)) {
                pendingBlocks.removeFirst();
                continue;
            }

            ResourceStack pendingResource = pendingBlock.spec.getResource();

            if (filter != null && !pendingResource.isSameType(filter)) break;

            analysisContext.setPos(pos);
            BlockSpec existing = MMRegistriesInternal.getPartialBlockSpec(analysisContext);

            if (existing == null) {
                placingContext.error(new Localized("mm.info.error.existing_block_missing_spec"));
                pendingBlocks.removeFirst();
                continue;
            }

            // Check if the existing block is removable
//            boolean canPlace = switch (state.config.removeMode) {
//                case NONE -> existing.getBlock().isAir(world, x, y, z);
//                case REPLACEABLE -> existing.getBlock().isReplaceable(world, x, y, z);
//                case ALL -> true;
//            };

            if (!visited.add(pos)) {
                MatterManipulator.LOG.warn("Tried to place block twice! {}", pendingBlock);
                pendingBlocks.removeFirst();
                continue;
            }

            placingContext.setTarget(pos, pendingBlock.spec);

            EnumSet<ApplyResult> result = EnumSet.noneOf(ApplyResult.class);

            // If there's already a block at this location, we need to remove it if it's different
            if (!pendingBlock.spec.matches(existing)) {
                boolean canRemove = isRemovable(placingContext, existing, pendingBlock, proxiedWorld);

                // We don't want to remove these even though they'll never be placed because we want to see how many blocks
                // couldn't be placed
                if (!canRemove) {
                    pendingBlocks.addLast(pendingBlocks.removeFirst());
                    shuffleCount++;

                    if (shuffleCount > pendingBlocks.size()) {
                        break;
                    } else {
                        continue;
                    }
                }

                placingContext.removeBlock();
                result.add(ApplyResult.DidSomething);

                if (!world.isAirBlock(pos)) {
                    pendingBlocks.add(pendingBlock);
                    visited.remove(pos);

                    placingContext.error(new Localized("mm.info.error.could_not_remove"));
                    quota--;
                    continue;
                }
            }

            // Place the block if there isn't one (or if it was just removed).
            if (world.isAirBlock(pos)) {
                result.add(pendingBlock.spec.place(placingContext));

                if (filter == null && (result.contains(ApplyResult.DidSomething)
                    || result.contains(ApplyResult.Wrenched))) {
                    filter = pendingResource.copy();
                }
            }

            if (!ApplyResult.hasFailure(result)) {
                MutableObject<IBlockState> state = new MutableObject<>(world.getBlockState(pos));

                MMRegistriesInternal.mutateBlock(state, pendingState, result);

                if (!ApplyResult.hasFailure(result)) {
                    world.setBlockState(pos, state.getValue());
                }
            }

            if (!ApplyResult.hasFailure(result)) {
                result.addAll(pendingBlock.spec.update(placingContext));
            }

            // TODO: wrench sounds and particles

            if (result.contains(ApplyResult.DidSomething)) {
                placingContext.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT);
                quota--;
            }

            if (result.contains(ApplyResult.Retry)) {
                retry.add(pendingBlock);
            }

            pendingBlocks.remove();
        }

        for (PendingBlock pendingBlock : retry) {
            pendingBlocks.add(pendingBlock);
            visited.remove(pendingBlock.toPos());
        }

        if (quota == placeSpeed) {
            if (pendingBlocks.isEmpty()) {
                MCUtils.sendInfoToPlayer(placingContext.getRealPlayer(), new Localized("mm.info.finished_placing"));
            } else {
                MCUtils.sendErrorToPlayer(placingContext.getRealPlayer(), new Localized("mm.info.error.could_not_place", pendingBlocks.size()));
            }

            this.done = true;
        }
    }

    private static boolean isRemovable(
        ManipulatorPlacingContext placingContext, BlockSpec existing, PendingBlock pendingBlock, ProxiedWorld proxiedWorld
    ) {
        World world = placingContext.getWorld();
        BlockPos pos = placingContext.getPos();

        if (existing.getBlockState().getBlockHardness(world, pos) < 0) {
            return false;
        }

        // if there's an existing block then skip it if we can't remove it
        if (!world.isAirBlock(pos)) {
            if (!placingContext.hasCapability(ManipulatorFlags.ALLOW_REMOVING)) {
                return false;
            }
        }

        return pendingBlock.spec.canPlaceAt(proxiedWorld, pos);
    }

    @Override
    public void onStop(ManipulatorPlacingContext context) {

    }

    @Override
    public boolean isDone() {
        return done;
    }

    public void encode(MMPacketBuffer buffer) {
        buffer.writeList(new ArrayList<>(this.pendingBlocks), PendingBlock::encode);
    }

    public void decode(MMPacketBuffer buffer) {
        pendingBlocks.clear();
        pendingBlocks.addAll(buffer.readList(PendingBlock::decodeNew));
    }

    @Override
    public Coroutine<BuildPlan> createPlan(HeldManipulatorContext context, boolean skipExisting) {
        PlanningContextImpl placingContext = new PlanningContextImpl(context);
        AnalysisContextImpl analysisContext = new AnalysisContextImpl(context);

        World world = context.getWorld();
        ProxiedWorld proxiedWorld = new ProxiedWorld(world);

        return Coroutine.forEach(
            this.pendingBlocks, pendingBlock -> {

                BlockPos pos = pendingBlock.toPos();
                IBlockState pendingState = pendingBlock.getBlockState();
                BlockSpec spec = pendingBlock.spec;

                placingContext.setTarget(pos, spec);

                if (!spec.isValid()) {
                    placingContext.error(new Localized("mm.info.error.unplaceable_block", pendingState.toString()));
                    return;
                }

                if (world.isOutsideBuildHeight(pos)) {
                    return;
                }

                if (spec.isAir() && world.isAirBlock(pos)) {
                    return;
                }

                analysisContext.setPos(pos);
                BlockSpec existing = MMRegistriesInternal.getPartialBlockSpec(analysisContext);

                if (existing == null) {
                    placingContext.error(new Localized("mm.info.error.existing_block_missing_spec"));
                    return;
                }

                boolean newBlock = false;

                // If there's already a block at this location, we need to remove it if it's different
                if (!spec.matches(existing)) {
                    boolean canRemove = isRemovable(placingContext, existing, pendingBlock, proxiedWorld);

                    if (!canRemove) {
                        return;
                    }

                    placingContext.removeBlock();

                    var stack = spec.getResource();

                    if (!stack.isEmpty()) {
                        //noinspection unchecked
                        placingContext.resource(stack.getResource()).extract(stack);
                    }

                    newBlock = true;
                }

                spec.getRequiredResourcesForUpdate(placingContext, skipExisting || newBlock);
            }, () -> new BuildPlan(placingContext.getNetStacks())
        );
    }
}
