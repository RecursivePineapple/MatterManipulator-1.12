package matter_manipulator.common.structure;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import com.github.bsideup.jabel.Desugar;
import matter_manipulator.client.rendering.MMRenderConstants;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.context.StructureContext;
import matter_manipulator.core.context.StructureInteractContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.meta.MetaKey;

/// A simple [StructureElement] that only checks if the block is a specific [IBlockState].
@Desugar
public record BlockSpecStructureElement<T>(BlockSpec spec) implements StructureElement<T> {

    @Override
    public <K> K getMetadata(MetaKey<K> key) {
        return null;
    }

    @Override
    public boolean check(StructureContext<? extends T> context, BlockPos pos) {
        return context.getWorld().getBlockState(pos) == spec.getBlockState();
    }

    @Override
    public boolean build(StructureInteractContext<? extends T> context, BlockPos pos) {
        if (check(context, pos)) return true;

        if (!context.getWorld().isAirBlock(pos)) {
            context.warn(new Localized("mm.structure.occupied"));
            return false;
        }

        if (spec.place(context) == ApplyResult.DidSomething) {
            context.consumePlaceQuota();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void emitHint(StructureInteractContext<? extends T> context, BlockPos pos) {
        if (!check(context, pos)) {
            context.emitHint(pos, spec, MMRenderConstants.WHITE);
        }
    }
}
