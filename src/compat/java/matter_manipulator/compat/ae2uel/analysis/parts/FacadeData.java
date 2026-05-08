package matter_manipulator.compat.ae2uel.analysis.parts;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;

import appeng.api.parts.IFacadePart;
import appeng.api.util.AEPartLocation;
import appeng.facade.IFacadeItem;
import appeng.tile.networking.TileCableBus;
import com.github.bsideup.jabel.Desugar;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.resources.item.ItemStackWrapper;

@Desugar
public record FacadeData(ItemStack facadeStack) {

    public static FacadeData fromPart(IFacadePart facade) {
        return new FacadeData(facade.getItemStack());
    }

    public void apply(BlockPlacingContext context, TileCableBus cableBus, AEPartLocation location,
        EnumSet<ApplyResult> result) {
        if (!(this.facadeStack.getItem() instanceof IFacadeItem facadeItem)) {
            context.error(new Localized("mm+ae2uel.chat.not_facade", this.facadeStack.copy()));
            result.add(ApplyResult.Error);
            return;
        }

        var container = cableBus.getFacadeContainer();
        IFacadePart facade = container.getFacade(location);

        if (facade != null && !ItemStack.areItemsEqual(facade.getItemStack(), this.facadeStack)) {
            context.items()
                .insert(new ItemStackWrapper(facade.getItemStack()
                    .copy()));
            container.removeFacade(cableBus, location);
            facade = null;
        }

        if (facade == null) {
            IFacadePart toInstall = facadeItem.createPartFromItemStack(this.facadeStack.copy(), location);

            if (!container.canAddFacade(toInstall)) {
                context.error(new Localized("mm+ae2uel.chat.not_facade", this.facadeStack.copy()));
                result.add(ApplyResult.Error);
                return;
            }

            ItemStackWrapper toExtract = new ItemStackWrapper(this.facadeStack.copy());

            var extracted = context.items()
                .tryExtract(toExtract);

            if (extracted == null) {
                context.extractionFailure(toExtract);
                result.add(ApplyResult.Retry);
            } else {
                container.addFacade(toInstall);
            }
        }
    }

    public void getRequiredItemsForExistingBlock(BlockPlacingContext context, TileCableBus cableBus, AEPartLocation location, EnumSet<ApplyResult> result) {
        if (!(this.facadeStack.getItem() instanceof IFacadeItem facadeItem)) {
            context.error(new Localized("mm+ae2uel.chat.not_facade", this.facadeStack.copy()));
            result.add(ApplyResult.Error);
            return;
        }

        var container = cableBus.getFacadeContainer();
        IFacadePart facade = container.getFacade(location);

        if (facade != null && !ItemStack.areItemsEqual(facade.getItemStack(), this.facadeStack)) {
            context.items()
                .insert(new ItemStackWrapper(facade.getItemStack()
                    .copy()));
            facade = null;
        }

        if (facade == null) {
            IFacadePart toInstall = facadeItem.createPartFromItemStack(this.facadeStack.copy(), location);

            if (!container.canAddFacade(toInstall)) {
                context.error(new Localized("mm+ae2uel.chat.not_facade", this.facadeStack.copy()));
                result.add(ApplyResult.Error);
                return;
            }

            ItemStackWrapper toExtract = new ItemStackWrapper(this.facadeStack.copy());

            context.items().extract(toExtract);
        }
    }

    public void getRequiredItemsForNewBlock(BlockPlacingContext context,
        EnumSet<ApplyResult> result) {
        if (!(this.facadeStack.getItem() instanceof IFacadeItem)) {
            context.error(new Localized("mm+ae2uel.chat.not_facade", this.facadeStack.copy()));
            result.add(ApplyResult.Error);
            return;
        }

        ItemStackWrapper toExtract = new ItemStackWrapper(this.facadeStack.copy());

        context.items().extract(toExtract);
    }
}
