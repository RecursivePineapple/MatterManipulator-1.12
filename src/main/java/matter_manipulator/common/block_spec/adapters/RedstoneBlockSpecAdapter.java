package matter_manipulator.common.block_spec.adapters;

import net.minecraft.init.Blocks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import matter_manipulator.common.block_spec.BlockSpecData;
import matter_manipulator.common.block_spec.specs.RedstoneBlockSpec;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.block_spec.BlockSpecExtractor;
import matter_manipulator.core.block_spec.BlockSpecLoader;
import matter_manipulator.core.context.AnalysisContext;
import matter_manipulator.core.context.TargetedContext;

public class RedstoneBlockSpecAdapter implements BlockSpecExtractor, BlockSpecLoader {

    public static final RedstoneBlockSpecAdapter INSTANCE = new RedstoneBlockSpecAdapter();

    private RedstoneBlockSpecAdapter() { }

    @Override
    public @Nullable BlockSpec getSpecPartial(TargetedContext context) {
        return context.getBlockState().getBlock() == Blocks.REDSTONE_WIRE ? RedstoneBlockSpec.INSTANCE : null;
    }

    @Override
    public @Nullable BlockSpec getSpecFull(AnalysisContext context) {
        return getSpecPartial(context);
    }

    @Override
    public @Nullable BlockSpec reconstructSpec(BlockSpecData data) {
        return data.stack.isSameType(RedstoneBlockSpec.INSTANCE.getResource()) ? RedstoneBlockSpec.INSTANCE : null;
    }

    @Override
    public @NotNull String getKey() {
        return "core:redstone";
    }

    @Override
    public @NotNull BlockSpec load(@NotNull JsonElement element) {
        return RedstoneBlockSpec.INSTANCE;
    }

    @Override
    public @NotNull JsonElement save(@NotNull BlockSpec variant) {
        return JsonNull.INSTANCE;
    }
}
