package matter_manipulator.compat.ae2uel.analysis.parts;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.util.AEPartLocation;
import appeng.tile.networking.TileCableBus;
import matter_manipulator.MatterManipulator;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.core.analysis.ApplyMode;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.InteropModule;
import matter_manipulator.core.context.BlockAnalysisContext;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.resources.item.ItemStackWrapper;

public class AECableBusInteropModule implements InteropModule<AECableAnalysisResult> {

    @Override
    public Optional<AECableAnalysisResult> analyze(BlockAnalysisContext context) {
        TileEntity tile = context.getTileEntity();

        if (!(tile instanceof TileCableBus cableBus)) return Optional.empty();

        AECableAnalysisResult analysis = new AECableAnalysisResult();

        for (AEPartLocation location : AEPartLocation.values()) {
            IPart part = cableBus.getPart(location);

            if (part == null) continue;

            analysis.parts.put(location == AEPartLocation.INTERNAL ? null : location.getFacing(), PartData.fromPart(part));
        }

        for (AEPartLocation location : AEPartLocation.values()) {

            IFacadePart facade = cableBus.getFacadeContainer().getFacade(location);

            if (facade == null) continue;

            analysis.facades.put(location == AEPartLocation.INTERNAL ? null : location.getFacing(), FacadeData.fromPart(facade));
        }

        return Optional.of(analysis);
    }

    @Override
    public Set<ApplyResult> apply(BlockPlacingContext context, AECableAnalysisResult analysis) {
        TileEntity tile = context.getTileEntity();

        if (!(tile instanceof TileCableBus cableBus)) return Collections.emptySet();

        EnumSet<ApplyResult> results = EnumSet.noneOf(ApplyResult.class);

        for (var e : analysis.parts.fastEntrySet()) {
            AEPartLocation location = e.getKey() == null
                ? AEPartLocation.INTERNAL
                : AEPartLocation.fromFacing(e.getKey());

            if (e.getValue() == null) {
                PartData.removePart(context, cableBus, location, results, ApplyMode.NORMAL);
            } else {
                e.getValue().apply(context, cableBus, location, results);
            }
        }

        for (var e : analysis.facades.fastEntrySet()) {
            AEPartLocation location = e.getKey() == null
                ? AEPartLocation.INTERNAL
                : AEPartLocation.fromFacing(e.getKey());

            if (e.getValue() == null) {
                var facade = cableBus.getFacadeContainer().getFacade(location);

                if (facade != null) {
                    cableBus.getFacadeContainer().removeFacade(cableBus, location);
                    context.items().insert(new ItemStackWrapper(facade.getItemStack()));
                }
            } else {
                e.getValue().apply(context, cableBus, location, results);
            }
        }

        return results;
    }

    @Override
    public Set<ApplyResult> getRequiredItemsForExistingBlock(BlockPlacingContext context, AECableAnalysisResult analysis) {
        TileEntity tile = context.getTileEntity();

        if (!(tile instanceof TileCableBus cableBus)) return Collections.emptySet();

        EnumSet<ApplyResult> results = EnumSet.noneOf(ApplyResult.class);

        for (var e : analysis.parts.fastEntrySet()) {
            AEPartLocation location = e.getKey() == null
                ? AEPartLocation.INTERNAL
                : AEPartLocation.fromFacing(e.getKey());

            if (e.getValue() == null) {
                PartData.removePart(context, cableBus, location, results, ApplyMode.SIMULATE_EXISTING);
            } else {
                e.getValue().getRequiredItemsForExistingBlock(context, cableBus, location, results);
            }
        }

        for (var e : analysis.facades.fastEntrySet()) {
            AEPartLocation location = e.getKey() == null
                ? AEPartLocation.INTERNAL
                : AEPartLocation.fromFacing(e.getKey());

            if (e.getValue() == null) {
                var facade = cableBus.getFacadeContainer().getFacade(location);

                if (facade != null) {
                    context.items().insert(new ItemStackWrapper(facade.getItemStack()));
                }
            } else {
                e.getValue().getRequiredItemsForExistingBlock(context, cableBus, location, results);
            }
        }

        return results;
    }

    @Override
    public Set<ApplyResult> getRequiredItemsForNewBlock(BlockPlacingContext context, AECableAnalysisResult analysis) {
        EnumSet<ApplyResult> results = EnumSet.noneOf(ApplyResult.class);

        for (var e : analysis.parts.fastEntrySet()) {
            if (e.getValue() != null) {
                e.getValue().getRequiredItemsForNewBlock(context);
            }
        }

        for (var e : analysis.facades.fastEntrySet()) {
            if (e.getValue() != null) {
                e.getValue().getRequiredItemsForNewBlock(context, results);
            }
        }

        return results;
    }

    private static final ResourceLocation LOCATION = MatterManipulator.loc("cable-bus");

    @Override
    public void save(IDataStorage storage, AECableAnalysisResult analysis) {
        storage.getSandbox(LOCATION).save(analysis);
    }

    @Override
    public Optional<AECableAnalysisResult> load(IDataStorage storage) {
        return Optional.ofNullable(storage.getSandbox(LOCATION).load(AECableAnalysisResult.class));
    }

    @Override
    public AECableAnalysisResult transform(AECableAnalysisResult analysis, Transform transform) {
        analysis.parts.transform((EnumFacing dir) -> transform.apply(dir));
        analysis.facades.transform((EnumFacing dir) -> transform.apply(dir));
        return analysis;
    }
}
