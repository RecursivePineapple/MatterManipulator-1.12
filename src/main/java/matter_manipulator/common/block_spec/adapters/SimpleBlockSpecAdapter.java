package matter_manipulator.common.block_spec.adapters;

import java.util.EnumSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import matter_manipulator.common.block_spec.BlockSpecData;
import matter_manipulator.common.block_spec.specs.SimpleBlockSpec;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.block_spec.BlockSpecExtractor;
import matter_manipulator.core.block_spec.BlockSpecLoader;
import matter_manipulator.core.context.AnalysisContext;
import matter_manipulator.core.context.TargetedContext;
import matter_manipulator.core.persist.NBTPersist;
import matter_manipulator.core.resources.item.IntItemResourceStack;

public class SimpleBlockSpecAdapter implements BlockSpecExtractor, BlockSpecLoader {

    public static final SimpleBlockSpecAdapter INSTANCE = new SimpleBlockSpecAdapter();

    private SimpleBlockSpecAdapter() { }

    @Override
    public @Nullable SimpleBlockSpec getSpecPartial(TargetedContext context) {
        IBlockState state = context.getBlockState();

        boolean valid = Item.getItemFromBlock(state.getBlock()) instanceof ItemBlock;

        if (!valid) return null;

        return new SimpleBlockSpec(state);
    }

    @Override
    public @Nullable SimpleBlockSpec getSpecFull(AnalysisContext context) {
        SimpleBlockSpec spec = getSpecPartial(context);

        if (spec != null) spec.analyze(context);

        return spec;
    }

    @Override
    public @Nullable BlockSpec reconstructSpec(BlockSpecData data) {
        if (!(data.stack instanceof IntItemResourceStack item)) return null;
        if (!(item.getItem() instanceof ItemBlock itemBlock)) return null;

        int meta = itemBlock.getMetadata(item.getItemMeta());

        @SuppressWarnings("deprecation")
        IBlockState baseState = itemBlock.getBlock().getStateFromMeta(meta);

        MutableObject<IBlockState> state = new MutableObject<>(baseState);

        MMRegistriesInternal.mutateBlock(state, data.state, EnumSet.noneOf(ApplyResult.class));

        SimpleBlockSpec spec = new SimpleBlockSpec(state.getValue());

        if (data.interopData != null) {
            spec.interop.putAllCopied(data.interopData);
        }

        return spec;
    }

    @Override
    public @NotNull String getKey() {
        return "core:block";
    }

    @Override
    public @NotNull BlockSpec load(@NotNull JsonElement element) {
        if (!(element instanceof JsonObject obj)) return BlockSpec.air();

        IBlockState state = NBTPersist.GSON.fromJson(obj.get("state"), IBlockState.class);

        SimpleBlockSpec spec = new SimpleBlockSpec(state);

        spec.loadInterop(obj);

        return spec;
    }

    @Override
    public @NotNull JsonElement save(@NotNull BlockSpec spec2) {
        SimpleBlockSpec spec = (SimpleBlockSpec) spec2;

        JsonObject obj = new JsonObject();

        obj.add("state", NBTPersist.GSON.toJsonTree(spec.state, IBlockState.class));

        spec.saveInterop(obj);

        return obj;
    }
}
