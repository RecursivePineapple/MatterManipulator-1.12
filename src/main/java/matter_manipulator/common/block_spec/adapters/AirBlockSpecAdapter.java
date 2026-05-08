package matter_manipulator.common.block_spec.adapters;

import net.minecraft.init.Blocks;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import matter_manipulator.common.block_spec.specs.AirBlockSpec;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.block_spec.BlockSpecExtractor;
import matter_manipulator.core.block_spec.BlockSpecLoader;
import matter_manipulator.core.context.BlockAnalysisContext;
import matter_manipulator.core.context.TargetedManipulatorContext;

public class AirBlockSpecAdapter implements BlockSpecExtractor, BlockSpecLoader {

    public static final AirBlockSpecAdapter INSTANCE = new AirBlockSpecAdapter();

    private AirBlockSpecAdapter() { }

    @Override
    public @Nullable BlockSpec getSpecPartial(TargetedManipulatorContext context) {
        return context.getBlockState().getBlock() == Blocks.AIR ? AirBlockSpec.INSTANCE : null;
    }

    @Override
    public @Nullable BlockSpec getSpecFull(BlockAnalysisContext context) {
        return getSpecPartial(context);
    }

    @Override
    public String getKey() {
        return "core:air";
    }

    @Override
    public BlockSpec load(JsonElement element) {
        return null;
    }

    @Override
    public JsonElement save(BlockSpec spec) {
        return null;
    }
}
