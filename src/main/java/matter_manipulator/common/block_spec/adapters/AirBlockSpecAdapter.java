package matter_manipulator.common.block_spec.adapters;

import net.minecraft.init.Blocks;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import matter_manipulator.common.block_spec.specs.AirBlockSpec;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.block_spec.BlockSpecExtractor;
import matter_manipulator.core.block_spec.IBlockSpecLoader;
import matter_manipulator.core.context.BlockAnalysisContext;
import matter_manipulator.core.context.TargetedManipulatorContext;

public class AirBlockSpecAdapter implements BlockSpecExtractor, IBlockSpecLoader {

    public static final AirBlockSpecAdapter INSTANCE = new AirBlockSpecAdapter();

    private AirBlockSpecAdapter() { }

    @Override
    public @Nullable IBlockSpec getSpecPartial(TargetedManipulatorContext context) {
        return context.getBlockState().getBlock() == Blocks.AIR ? AirBlockSpec.INSTANCE : null;
    }

    @Override
    public @Nullable IBlockSpec getSpecFull(BlockAnalysisContext context) {
        return getSpecPartial(context);
    }

    @Override
    public String getKey() {
        return "core:air";
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
