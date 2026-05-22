package matter_manipulator.common.uplink;

import java.util.HashSet;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import matter_manipulator.common.blocks.BlockUplinkModuleBase;

public class TileUplinkModule extends TileEntity {

    public final HashSet<TileUplinkController> controllers = new HashSet<>();

    public EnumFacing getFrontFacing() {
        return world.getBlockState(pos).getValue(BlockUplinkModuleBase.FACING);
    }

    public void connect(TileUplinkController controller) {
        controllers.add(controller);
    }

    public void disconnect(TileUplinkController controller) {
        controllers.remove(controller);
    }

    public boolean isConnected() {
        return !controllers.isEmpty();
    }

    public boolean isActive() {
        return false;
    }
}
