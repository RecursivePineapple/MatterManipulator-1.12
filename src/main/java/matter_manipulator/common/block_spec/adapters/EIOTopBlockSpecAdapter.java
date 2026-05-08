package matter_manipulator.common.block_spec.adapters;

import net.minecraft.block.state.IBlockState;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import crazypants.enderio.base.machine.base.block.BlockMachineExtension;
import matter_manipulator.common.block_spec.specs.EIOTopBlockSpec;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.block_spec.BlockSpecExtractor;
import matter_manipulator.core.block_spec.IBlockSpecLoader;
import matter_manipulator.core.context.BlockAnalysisContext;
import matter_manipulator.core.context.TargetedManipulatorContext;
import matter_manipulator.core.persist.NBTPersist;

public class EIOTopBlockSpecAdapter implements BlockSpecExtractor, IBlockSpecLoader {

    public static final EIOTopBlockSpecAdapter INSTANCE = new EIOTopBlockSpecAdapter();

    private EIOTopBlockSpecAdapter() { }

    @Override
    public @Nullable EIOTopBlockSpec getSpecPartial(TargetedManipulatorContext context) {
        IBlockState state = context.getBlockState();

        if (!(state.getBlock() instanceof BlockMachineExtension)) return null;

        return new EIOTopBlockSpec(state);
    }

    @Override
    public @Nullable EIOTopBlockSpec getSpecFull(BlockAnalysisContext context) {
        return getSpecPartial(context);
    }

    @Override
    public String getKey() {
        return "eio:top";
    }

    @Override
    public EIOTopBlockSpec load(JsonElement element) {
        if (!(element instanceof JsonObject obj)) return null;

        IBlockState state = NBTPersist.GSON.fromJson(obj.get("state"), IBlockState.class);

        return new EIOTopBlockSpec(state);
    }

    @Override
    public JsonElement save(IBlockSpec spec2) {
        EIOTopBlockSpec spec = (EIOTopBlockSpec) spec2;

        JsonObject obj = new JsonObject();

        obj.add("state", NBTPersist.GSON.toJsonTree(spec.state, IBlockState.class));

        spec.saveInterop(obj);

        return obj;
    }
}
