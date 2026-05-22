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
import appeng.api.parts.IPartItem;
import appeng.api.util.AEPartLocation;
import appeng.facade.IFacadeItem;
import appeng.tile.networking.TileCableBus;
import matter_manipulator.MatterManipulator;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.core.analysis.ApplyMode;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.interop.InteropModule;
import matter_manipulator.core.context.AnalysisContext;
import matter_manipulator.core.context.ManipulatorPlacingContext;
import matter_manipulator.core.item.ItemStackLike;
import matter_manipulator.core.persist.DataStorage;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.resources.ResourceIdentity;
import matter_manipulator.core.resources.item.ItemStackWrapper;

public class AECableBusInteropModule implements InteropModule<AECableAnalysisResult> {

    @Override
    public Optional<AECableAnalysisResult> analyze(AnalysisContext context) {
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
    public Set<ApplyResult> apply(ManipulatorPlacingContext context, AECableAnalysisResult analysis) {
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
    public Set<ApplyResult> getRequiredResourcesForExistingBlock(ManipulatorPlacingContext context, AECableAnalysisResult analysis) {
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
    public Set<ApplyResult> getRequiredResourcesForNewBlock(ManipulatorPlacingContext context, AECableAnalysisResult analysis) {
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

    @Override
    public void exchangeResource(AECableAnalysisResult analysis, ResourceIdentity stack, ResourceIdentity replacement) {
        if (!(stack instanceof ItemStackLike stackItem)) return;
        if (!(replacement instanceof ItemStackLike replacementItem)) return;

        if (stackItem.getItem() instanceof IFacadeItem) {
            if (replacementItem.getItem() instanceof IFacadeItem) {
                analysis.facades.fastEntrySet().forEach(e -> {
                    FacadeData data = e.getValue();

                    if (stackItem.matches(data.getFacadeStack())) {
                        e.setValue(new FacadeData(replacementItem.toStack(data.getFacadeStack().getCount())));
                    }
                });
            }
        }

        if (stackItem.getItem() instanceof IPartItem<?>) {
            if (replacementItem.getItem() instanceof IPartItem) {
                analysis.parts.fastEntrySet().forEach(e -> {
                    PartData data = e.getValue();

                    if (stackItem.matches(data.getPartStack())) {
                        data = data.clone();

                        data.partStack = replacementItem.toStack(data.getPartStack().getCount());

                        e.setValue(data);
                    }
                });
            }
        }

        analysis.parts.fastEntrySet().forEach(e -> {
            PartData data = e.getValue();

            if (data.upgrades != null) {
                data.upgrades.exchangeResource(stack, replacement);
            }

            if (data.config != null) {
                data.config.exchangeResource(stack, replacement);
            }
        });
    }

    @Override
    public AECableAnalysisResult cloneAnalysis(AECableAnalysisResult result) {
        return result.clone();
    }
}
