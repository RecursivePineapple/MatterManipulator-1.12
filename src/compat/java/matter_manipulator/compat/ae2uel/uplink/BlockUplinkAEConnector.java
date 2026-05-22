package matter_manipulator.compat.ae2uel.uplink;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.world.World;

import matter_manipulator.common.blocks.BlockUplinkModuleBase;
import matter_manipulator.common.uplink.TileUplinkModule;
import mcp.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockUplinkAEConnector extends BlockUplinkModuleBase {

    public BlockUplinkAEConnector() {
        super("uplink-ae-connector");
    }

    @Override
    public TileUplinkModule createNewTileEntity(World worldIn, int meta) {
        return new TileUplinkAEConnector();
    }
}
