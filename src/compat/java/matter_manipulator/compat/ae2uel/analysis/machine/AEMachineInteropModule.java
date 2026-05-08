package matter_manipulator.compat.ae2uel.analysis.machine;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import appeng.tile.AEBaseTile;
import matter_manipulator.MatterManipulator;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.InteropModule;
import matter_manipulator.core.context.BlockAnalysisContext;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.persist.IDataStorage;

public class AEMachineInteropModule implements InteropModule<MachineData> {

    @Override
    public Optional<MachineData> analyze(BlockAnalysisContext context) {
        TileEntity tile = context.getTileEntity();

        if (!(tile instanceof AEBaseTile ae)) return Optional.empty();

        return Optional.of(MachineData.fromMachine(ae));
    }

    @Override
    public Set<ApplyResult> apply(BlockPlacingContext context, MachineData analysis) {
        TileEntity tile = context.getTileEntity();

        if (!(tile instanceof AEBaseTile ae)) return EnumSet.of(ApplyResult.NotApplicable);

        EnumSet<ApplyResult> results = EnumSet.noneOf(ApplyResult.class);

        analysis.apply(context, ae, results);

        return results;
    }

    @Override
    public Set<ApplyResult> getRequiredItemsForExistingBlock(BlockPlacingContext context, MachineData analysis) {
        TileEntity tile = context.getTileEntity();

        if (!(tile instanceof AEBaseTile ae)) return EnumSet.of(ApplyResult.NotApplicable);

        EnumSet<ApplyResult> results = EnumSet.noneOf(ApplyResult.class);

        analysis.getRequiredItemsForExistingBlock(context, ae, results);

        return results;
    }

    @Override
    public Set<ApplyResult> getRequiredItemsForNewBlock(BlockPlacingContext context, MachineData analysis) {
        analysis.getRequiredItemsForNewBlock(context);

        return Collections.emptySet();
    }

    private static final ResourceLocation LOCATION = MatterManipulator.loc("ae-machine");

    @Override
    public void save(IDataStorage storage, MachineData analysis) {
        storage.getSandbox(LOCATION)
            .save(analysis);
    }

    @Override
    public Optional<MachineData> load(IDataStorage storage) {
        return Optional.ofNullable(storage.getSandbox(LOCATION)
            .load(MachineData.class));
    }

    @Override
    public MachineData transform(MachineData analysis, Transform transform) {
        return new MachineData(
            transform.apply(analysis.up()),
            transform.apply(analysis.forward()),
            analysis.data(),
            analysis.color(),
            analysis.customName(),
            analysis.upgrades(),
            analysis.config(),
            analysis.priority(),
            analysis.omniDirectional());
    }
}
