package matter_manipulator.common.block_spec.adapters;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import matter_manipulator.common.block_spec.specs.SimpleBlockSpec;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.block_spec.BlockSpecExtractor;
import matter_manipulator.core.block_spec.IBlockSpecLoader;
import matter_manipulator.core.context.BlockAnalysisContext;
import matter_manipulator.core.context.TargetedManipulatorContext;
import matter_manipulator.core.persist.NBTPersist;

public class SimpleBlockSpecAdapter implements BlockSpecExtractor, IBlockSpecLoader {

    public static final SimpleBlockSpecAdapter INSTANCE = new SimpleBlockSpecAdapter();

    private SimpleBlockSpecAdapter() { }

    @Override
    public @Nullable SimpleBlockSpec getSpecPartial(TargetedManipulatorContext context) {
        IBlockState state = context.getBlockState();

        boolean valid = state.getBlock().getItemDropped(state, ThreadLocalRandom.current(), 0) instanceof ItemBlock;

        if (!valid) return null;

        return new SimpleBlockSpec(state);
    }

    @Override
    public @Nullable SimpleBlockSpec getSpecFull(BlockAnalysisContext context) {
        SimpleBlockSpec spec = getSpecPartial(context);

        if (spec != null) spec.analyze(context);

        return spec;
    }

    @Override
    public String getKey() {
        return "core:block";
    }

    @Override
    public SimpleBlockSpec load(JsonElement element) {
        if (!(element instanceof JsonObject obj)) return null;

        IBlockState state = NBTPersist.GSON.fromJson(obj.get("state"), IBlockState.class);

        SimpleBlockSpec spec = new SimpleBlockSpec(state);

        spec.loadInterop(obj);

        return spec;
    }

    @Override
    public JsonElement save(IBlockSpec spec2) {
        SimpleBlockSpec spec = (SimpleBlockSpec) spec2;

        JsonObject obj = new JsonObject();

        obj.add("state", NBTPersist.GSON.toJsonTree(spec.state, IBlockState.class));

        spec.saveInterop(obj);

        return obj;
    }
}
