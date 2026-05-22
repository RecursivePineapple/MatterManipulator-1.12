package matter_manipulator.common.block_spec.adapters;

import net.minecraft.init.Blocks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import matter_manipulator.common.block_spec.BlockSpecData;
import matter_manipulator.common.block_spec.specs.AirBlockSpec;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.block_spec.BlockSpecExtractor;
import matter_manipulator.core.block_spec.BlockSpecLoader;
import matter_manipulator.core.context.AnalysisContext;
import matter_manipulator.core.context.TargetedContext;

public class AirBlockSpecAdapter implements BlockSpecExtractor, BlockSpecLoader {

    public static final AirBlockSpecAdapter INSTANCE = new AirBlockSpecAdapter();

    private AirBlockSpecAdapter() { }

    @Override
    public @Nullable BlockSpec getSpecPartial(TargetedContext context) {
        return context.getBlockState().getBlock() == Blocks.AIR ? AirBlockSpec.INSTANCE : null;
    }

    @Override
    public @Nullable BlockSpec getSpecFull(AnalysisContext context) {
        return getSpecPartial(context);
    }

    @Override
    public @Nullable BlockSpec reconstructSpec(BlockSpecData data) {
        return data.stack.isEmpty() ? AirBlockSpec.INSTANCE : null;
    }

    @Override
    public @NotNull String getKey() {
        return "core:air";
    }

    @Override
    public @NotNull BlockSpec load(@NotNull JsonElement element) {
        return AirBlockSpec.INSTANCE;
    }

    @Override
    public @NotNull JsonElement save(@NotNull BlockSpec variant) {
        return JsonNull.INSTANCE;
    }
}
