package matter_manipulator.common.block_spec.adapters;

import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import matter_manipulator.common.block_spec.BlockSpecData;
import matter_manipulator.common.block_spec.specs.SlabBlockSpec;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.block_spec.BlockSpecExtractor;
import matter_manipulator.core.block_spec.BlockSpecLoader;
import matter_manipulator.core.context.AnalysisContext;
import matter_manipulator.core.context.TargetedContext;
import matter_manipulator.core.persist.NBTPersist;
import matter_manipulator.core.resources.item.IntItemResourceStack;
import matter_manipulator.mixin.mixins.minecraft.AccessorItemSlab;

public class SlabBlockSpecAdapter implements BlockSpecExtractor, BlockSpecLoader {

    public static final SlabBlockSpecAdapter INSTANCE = new SlabBlockSpecAdapter();

    private SlabBlockSpecAdapter() { }

    @Override
    public @Nullable SlabBlockSpec getSpecPartial(TargetedContext context) {
        IBlockState state = context.getBlockState();

        if (!(state.getBlock() instanceof BlockSlab slab)) return null;

        Item item = slab.getItemDropped(state, ThreadLocalRandom.current(), 0);

        if (!(item instanceof AccessorItemSlab)) return null;

        return new SlabBlockSpec(state);
    }

    @Override
    public @Nullable SlabBlockSpec getSpecFull(AnalysisContext context) {
        SlabBlockSpec spec = getSpecPartial(context);

        if (spec != null) spec.analyze(context);

        return spec;
    }

    @Override
    public @Nullable BlockSpec reconstructSpec(BlockSpecData data) {
        if (!(data.stack instanceof IntItemResourceStack item)) return null;
        if (!(item.getItem() instanceof AccessorItemSlab slab)) return null;
        if (item.getAmountInt() != 1 && item.getAmountInt() != 2) return null;

        int meta = item.getItem().getMetadata(item.getItemMeta());

        @SuppressWarnings("deprecation")
        IBlockState baseState = item.getAmountInt() == 1 ? slab.getSingleSlab().getStateFromMeta(meta) : slab.getDoubleSlab().getStateFromMeta(meta);

        MutableObject<IBlockState> state = new MutableObject<>(baseState);

        MMRegistriesInternal.mutateBlock(state, data.state, EnumSet.noneOf(ApplyResult.class));

        SlabBlockSpec spec = new SlabBlockSpec(state.getValue());

        if (data.interopData != null) {
            data.interopData.forEach((module, analysis) -> {
                //noinspection unchecked
                spec.interop.put(module, module.cloneAnalysis(analysis));
            });
        }

        return spec;
    }

    @Override
    public @NotNull String getKey() {
        return "core:slab";
    }

    @Override
    public @NotNull BlockSpec load(@NotNull JsonElement element) {
        if (!(element instanceof JsonObject obj)) return BlockSpec.air();

        IBlockState state = NBTPersist.GSON.fromJson(obj.get("state"), IBlockState.class);

        SlabBlockSpec spec = new SlabBlockSpec(state);

        spec.loadInterop(obj);

        return spec;
    }

    @Override
    public @NotNull JsonElement save(@NotNull BlockSpec spec2) {
        SlabBlockSpec spec = (SlabBlockSpec) spec2;

        JsonObject obj = new JsonObject();

        obj.add("state", NBTPersist.GSON.toJsonTree(spec.state, IBlockState.class));

        spec.saveInterop(obj);

        return obj;
    }
}
