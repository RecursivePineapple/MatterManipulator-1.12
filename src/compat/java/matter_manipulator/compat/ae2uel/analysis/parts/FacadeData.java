package matter_manipulator.compat.ae2uel.analysis.parts;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import appeng.api.parts.IFacadePart;
import appeng.api.util.AEPartLocation;
import appeng.facade.IFacadeItem;
import appeng.tile.networking.TileCableBus;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.context.ManipulatorPlacingContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.resources.item.ItemStackWrapper;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class FacadeData implements Cloneable {

    @NotNull
    public ItemStack facadeStack;

    public static FacadeData fromPart(IFacadePart facade) {
        return new FacadeData(facade.getItemStack());
    }

    public void apply(
        ManipulatorPlacingContext context, TileCableBus cableBus, AEPartLocation location,
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

    public void getRequiredItemsForExistingBlock(ManipulatorPlacingContext context, TileCableBus cableBus, AEPartLocation location, EnumSet<ApplyResult> result) {
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

    public void getRequiredItemsForNewBlock(
        ManipulatorPlacingContext context,
        EnumSet<ApplyResult> result) {
        if (!(this.facadeStack.getItem() instanceof IFacadeItem)) {
            context.error(new Localized("mm+ae2uel.chat.not_facade", this.facadeStack.copy()));
            result.add(ApplyResult.Error);
            return;
        }

        ItemStackWrapper toExtract = new ItemStackWrapper(this.facadeStack.copy());

        context.items().extract(toExtract);
    }

    @Override
    public FacadeData clone() {
        try {
            FacadeData copy = (FacadeData) super.clone();

            copy.facadeStack = this.facadeStack.copy();

            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
