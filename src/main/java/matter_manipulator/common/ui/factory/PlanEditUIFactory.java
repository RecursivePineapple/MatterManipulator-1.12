package matter_manipulator.common.ui.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.GuiManager;
import com.github.bsideup.jabel.Desugar;
import matter_manipulator.common.context.HeldManipulatorContextImpl;
import matter_manipulator.common.items.ItemMatterManipulator;
import matter_manipulator.common.state.MMState;
import matter_manipulator.common.ui.holder.PlanEditUI;
import matter_manipulator.common.ui.factory.PlanEditUIFactory.PlanEditGuiData;
import matter_manipulator.core.context.HeldManipulatorContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.manipulator_state.UplinkStateLoader;

public class PlanEditUIFactory extends AbstractUIFactory<PlanEditGuiData> {

    public static final PlanEditUIFactory INSTANCE = new PlanEditUIFactory();

    @Desugar
    public static class PlanEditGuiData extends GuiData {
        public final EnumHand hand;

        public PlanEditGuiData(@NotNull EntityPlayer player, EnumHand hand) {
            super(player);
            this.hand = hand;
        }

        public ItemStack getManipulatorStack() {
            return getPlayer().getHeldItem(hand);
        }
    }

    protected PlanEditUIFactory() {
        super("mm:edit-plans");
    }

    @Override
    public @NotNull IGuiHolder<PlanEditGuiData> getGuiHolder(PlanEditGuiData data) {
        MMState state = ItemMatterManipulator.getState(data.getManipulatorStack());

        HeldManipulatorContextImpl context = new HeldManipulatorContextImpl(data.getWorld(), data.getPlayer(), data.getManipulatorStack(), state);

        return new PlanEditUI(context);
    }

    @Override
    public void writeGuiData(PlanEditGuiData guiData, PacketBuffer buffer) {
        buffer.writeInt(guiData.hand.ordinal());
    }

    @Override
    public @NotNull PlanEditGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new PlanEditGuiData(player, EnumHand.values()[buffer.readInt()]);
    }

    public void register() {
        GuiManager.registerFactory(this);
    }

    public void open(HeldManipulatorContext context) {
        if (!context.getState().getState(context, UplinkStateLoader.INSTANCE).hasPlanReceiver()) {
            new Localized("mm.chat.no-plan-module").sendChat(context.getRealPlayer());
            return;
        }

        EntityPlayer player = context.getRealPlayer();

        if (context.getManipulator() == player.getHeldItemMainhand()) {
            GuiManager.open(this, new PlanEditGuiData(player, EnumHand.MAIN_HAND), (EntityPlayerMP) player);
        }

        if (context.getManipulator() == player.getHeldItemOffhand()) {
            GuiManager.open(this, new PlanEditGuiData(player, EnumHand.OFF_HAND), (EntityPlayerMP) player);
        }
    }
}
