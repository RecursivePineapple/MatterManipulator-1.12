package matter_manipulator.common.block_spec.adapters;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlockSpecial;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import matter_manipulator.common.block_spec.specs.SpecialBlockSpec;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.block_spec.BlockSpecExtractor;
import matter_manipulator.core.block_spec.BlockSpecLoader;
import matter_manipulator.core.context.BlockAnalysisContext;
import matter_manipulator.core.context.TargetedManipulatorContext;
import matter_manipulator.core.persist.NBTPersist;

public class SpecialBlockSpecAdapter implements BlockSpecExtractor, BlockSpecLoader {

    public static final SpecialBlockSpecAdapter INSTANCE = new SpecialBlockSpecAdapter();

    private SpecialBlockSpecAdapter() { }

    @Override
    public @Nullable SpecialBlockSpec getSpecPartial(TargetedManipulatorContext context) {
        IBlockState state = context.getBlockState();

        Item item = state.getBlock().getItemDropped(state, ThreadLocalRandom.current(), 0);

        if (!(item instanceof ItemBlockSpecial)) return null;

        return new SpecialBlockSpec(state);
    }

    @Override
    public @Nullable SpecialBlockSpec getSpecFull(BlockAnalysisContext context) {
        SpecialBlockSpec spec = getSpecPartial(context);

        if (spec != null) spec.analyze(context);

        return spec;
    }

    @Override
    public String getKey() {
        return "core:special";
    }

    @Override
    public SpecialBlockSpec load(JsonElement element) {
        if (!(element instanceof JsonObject obj)) return null;

        IBlockState state = NBTPersist.GSON.fromJson(obj.get("state"), IBlockState.class);

        SpecialBlockSpec spec = new SpecialBlockSpec(state);

        spec.loadInterop(obj);

        return spec;
    }

    @Override
    public JsonElement save(BlockSpec spec2) {
        SpecialBlockSpec spec = (SpecialBlockSpec) spec2;

        JsonObject obj = new JsonObject();

        obj.add("state", NBTPersist.GSON.toJsonTree(spec.state, IBlockState.class));

        spec.saveInterop(obj);

        return obj;
    }
}
