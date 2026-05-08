package matter_manipulator.common.block_spec.adapters;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import matter_manipulator.common.block_spec.specs.SlabBlockSpec;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.block_spec.BlockSpecExtractor;
import matter_manipulator.core.block_spec.IBlockSpecLoader;
import matter_manipulator.core.context.BlockAnalysisContext;
import matter_manipulator.core.context.TargetedManipulatorContext;
import matter_manipulator.core.persist.NBTPersist;
import matter_manipulator.mixin.mixins.minecraft.AccessorItemSlab;

public class SlabBlockSpecAdapter implements BlockSpecExtractor, IBlockSpecLoader {

    public static final SlabBlockSpecAdapter INSTANCE = new SlabBlockSpecAdapter();

    private SlabBlockSpecAdapter() { }

    @Override
    public @Nullable SlabBlockSpec getSpecPartial(TargetedManipulatorContext context) {
        IBlockState state = context.getBlockState();

        if (!(state.getBlock() instanceof BlockSlab slab)) return null;

        Item item = slab.getItemDropped(state, ThreadLocalRandom.current(), 0);

        if (!(item instanceof AccessorItemSlab)) return null;

        return new SlabBlockSpec(state);
    }

    @Override
    public @Nullable SlabBlockSpec getSpecFull(BlockAnalysisContext context) {
        SlabBlockSpec spec = getSpecPartial(context);

        if (spec != null) spec.analyze(context);

        return spec;
    }

    @Override
    public String getKey() {
        return "core:slab";
    }

    @Override
    public SlabBlockSpec load(JsonElement element) {
        if (!(element instanceof JsonObject obj)) return null;

        IBlockState state = NBTPersist.GSON.fromJson(obj.get("state"), IBlockState.class);

        SlabBlockSpec spec = new SlabBlockSpec(state);

        spec.loadInterop(obj);

        return spec;
    }

    @Override
    public JsonElement save(IBlockSpec spec2) {
        SlabBlockSpec spec = (SlabBlockSpec) spec2;

        JsonObject obj = new JsonObject();

        obj.add("state", NBTPersist.GSON.toJsonTree(spec.state, IBlockState.class));

        spec.saveInterop(obj);

        return obj;
    }
}
