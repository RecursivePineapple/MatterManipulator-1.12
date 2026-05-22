package matter_manipulator.common.blocks;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.world.World;

import matter_manipulator.common.uplink.TileUplinkEnergyConnector;
import matter_manipulator.common.uplink.TileUplinkModule;
import mcp.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockUplinkEnergyConnector extends BlockUplinkModuleBase {

    public BlockUplinkEnergyConnector() {
        super("uplink-energy-connector");
    }

    @Override
    public TileUplinkModule createNewTileEntity(World worldIn, int meta) {
        return new TileUplinkEnergyConnector();
    }
}
