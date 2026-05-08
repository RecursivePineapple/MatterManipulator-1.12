package matter_manipulator.common.block_spec.adapters;

import net.minecraft.init.Blocks;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import matter_manipulator.common.block_spec.specs.RedstoneBlockSpec;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.block_spec.BlockSpecExtractor;
import matter_manipulator.core.block_spec.IBlockSpecLoader;
import matter_manipulator.core.context.BlockAnalysisContext;
import matter_manipulator.core.context.TargetedManipulatorContext;

public class RedstoneBlockSpecAdapter implements BlockSpecExtractor, IBlockSpecLoader {

    public static final RedstoneBlockSpecAdapter INSTANCE = new RedstoneBlockSpecAdapter();

    private RedstoneBlockSpecAdapter() { }

    @Override
    public @Nullable IBlockSpec getSpecPartial(TargetedManipulatorContext context) {
        return context.getBlockState().getBlock() == Blocks.REDSTONE_WIRE ? RedstoneBlockSpec.INSTANCE : null;
    }

    @Override
    public @Nullable IBlockSpec getSpecFull(BlockAnalysisContext context) {
        return getSpecPartial(context);
    }

    @Override
    public String getKey() {
        return "core:redstone";
    }

    @Override
    public IBlockSpec load(JsonElement element) {
        return null;
    }

    @Override
    public JsonElement save(IBlockSpec spec) {
        return null;
    }
}
