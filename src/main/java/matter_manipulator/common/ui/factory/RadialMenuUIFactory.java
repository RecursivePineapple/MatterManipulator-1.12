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
import matter_manipulator.common.items.ItemMatterManipulator;
import matter_manipulator.common.ui.factory.RadialMenuUIFactory.RadialMenuGuiData;

public class RadialMenuUIFactory extends AbstractUIFactory<RadialMenuGuiData> {

    public static final RadialMenuUIFactory INSTANCE = new RadialMenuUIFactory();

    protected RadialMenuUIFactory() {
        super("mm:radial-menu");
    }

    @Override
    public @NotNull IGuiHolder<RadialMenuGuiData> getGuiHolder(RadialMenuGuiData data) {
        return (ItemMatterManipulator) data.getManipulatorStack().getItem();
    }

    @Override
    public void writeGuiData(RadialMenuGuiData guiData, PacketBuffer buffer) {
        buffer.writeInt(guiData.hand.ordinal());
    }

    @Override
    public @NotNull RadialMenuUIFactory.RadialMenuGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new RadialMenuGuiData(player, EnumHand.values()[buffer.readInt()]);
    }

    public void register() {
        GuiManager.registerFactory(this);
    }

    public void open(EntityPlayerMP player, EnumHand hand) {
        GuiManager.open(this, new RadialMenuGuiData(player, hand), player);
    }

    public static class RadialMenuGuiData extends GuiData {

        public final EnumHand hand;

        public RadialMenuGuiData(EntityPlayer player, EnumHand hand) {
            super(player);
            this.hand = hand;
        }

        public ItemStack getManipulatorStack() {
            return getPlayer().getHeldItem(hand);
        }
    }
}
