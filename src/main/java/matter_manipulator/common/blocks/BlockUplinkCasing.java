package matter_manipulator.common.blocks;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import matter_manipulator.Tags;
import matter_manipulator.common.structure.StructureOverlord;
import mcp.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockUplinkCasing extends Block {

    private final boolean solid;

    public BlockUplinkCasing(String name, boolean solid) {
        super(Material.IRON);
        this.solid = solid;

        setRegistryName(Tags.MODID, name);
        setTranslationKey(name);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);

        if (worldIn instanceof WorldServer server) {
            StructureOverlord.get(server).causeMachineUpdate(pos);
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);

        if (worldIn instanceof WorldServer server) {
            StructureOverlord.get(server).causeMachineUpdate(pos);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return solid ? BlockRenderLayer.SOLID : BlockRenderLayer.CUTOUT;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return solid;
    }
}
