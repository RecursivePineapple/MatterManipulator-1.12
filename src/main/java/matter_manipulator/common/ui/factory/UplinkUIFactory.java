package matter_manipulator.common.ui.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.PosGuiData;
import matter_manipulator.common.ui.holder.UplinkUI;
import matter_manipulator.common.uplink.TileUplinkController;

public class UplinkUIFactory extends AbstractUIFactory<PosGuiData> {

    public static final UplinkUIFactory INSTANCE = new UplinkUIFactory();

    protected UplinkUIFactory() {
        super("mm:uplink");
    }

    @Override
    public @NotNull IGuiHolder<PosGuiData> getGuiHolder(PosGuiData data) {
        return new UplinkUI((TileUplinkController) data.getTileEntity());
    }

    @Override
    public void writeGuiData(PosGuiData guiData, PacketBuffer buffer) {
        buffer.writeInt(guiData.getX());
        buffer.writeInt(guiData.getY());
        buffer.writeInt(guiData.getZ());
    }

    @Override
    public @NotNull PosGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new PosGuiData(player, buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    public void register() {
        GuiManager.registerFactory(this);
    }

    public void open(EntityPlayerMP player, TileUplinkController controller) {
        GuiManager.open(this, new PosGuiData(player, controller.getPos()), player);
    }
}
