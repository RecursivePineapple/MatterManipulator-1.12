package matter_manipulator.common.analysis;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Contract;

import matter_manipulator.MatterManipulator;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.core.analysis.InventoryAnalysis;
import matter_manipulator.core.analysis.ApplyMode;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.interop.InteropModule;
import matter_manipulator.core.context.AnalysisContext;
import matter_manipulator.core.context.ManipulatorPlacingContext;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.resources.ResourceIdentity;

public class InventoryInteropModule implements InteropModule<InventoryAnalysis> {

    @Override
    public Optional<InventoryAnalysis> analyze(AnalysisContext context) {
        TileEntity te = context.getTileEntity();

        if (te == null) return Optional.empty();

        var adapter = MMRegistriesInternal.getInventoryAdapter(te, null);

        if (adapter == null) return Optional.empty();

        return Optional.of(InventoryAnalysis.fromInventory(adapter));
    }

    @Override
    public Set<ApplyResult> apply(ManipulatorPlacingContext context, InventoryAnalysis analysis) {
        TileEntity te = context.getTileEntity();

        if (te == null) {
            return EnumSet.of(ApplyResult.NotApplicable);
        }

        var adapter = MMRegistriesInternal.getInventoryAdapter(te, null);

        if (adapter == null) {
            return EnumSet.of(ApplyResult.NotApplicable);
        }

        return analysis.apply(context, adapter, ApplyMode.NORMAL);
    }

    @Override
    public Set<ApplyResult> getRequiredResourcesForExistingBlock(ManipulatorPlacingContext context, InventoryAnalysis analysis) {
        // Inventories are skipped entirely because things like double chests are too problematic to implement cleanly.

        return Collections.emptySet();
    }

    @Override
    public Set<ApplyResult> getRequiredResourcesForNewBlock(ManipulatorPlacingContext context, InventoryAnalysis analysis) {
        // Inventories are skipped entirely because things like double chests are too problematic to implement cleanly.

        return Collections.emptySet();
    }

    private static final ResourceLocation LOC = MatterManipulator.loc("inv");

    @Override
    public void save(IDataStorage storage, InventoryAnalysis analysis) {
        storage.getSandbox(LOC).save(analysis);
    }

    @Override
    public Optional<InventoryAnalysis> load(IDataStorage storage) {
        return Optional.ofNullable(storage.getSandbox(LOC).load(InventoryAnalysis.class));
    }

    @Contract(mutates = "param1")
    @Override
    public InventoryAnalysis transform(InventoryAnalysis analysis, Transform transform) {
        return analysis;
    }

    @Contract(mutates = "param1")
    @Override
    public void exchangeResource(InventoryAnalysis analysis, ResourceIdentity stack, ResourceIdentity replacement) {
        analysis.exchangeResource(stack, replacement);
    }

    @Override
    public InventoryAnalysis cloneAnalysis(InventoryAnalysis inventoryAnalysis) {
        return inventoryAnalysis.clone();
    }
}
