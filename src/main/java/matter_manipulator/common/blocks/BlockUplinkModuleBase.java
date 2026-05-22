package matter_manipulator.common.blocks;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import matter_manipulator.Tags;
import matter_manipulator.common.structure.StructureOverlord;
import matter_manipulator.common.uplink.TileUplinkModule;
import mcp.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BlockUplinkModuleBase extends BlockContainer {

    public static final IProperty<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class);

    public BlockUplinkModuleBase(String name) {
        super(Material.IRON);

        setRegistryName(Tags.MODID, name);
        setTranslationKey(name);

        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @SuppressWarnings("deprecation")
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public abstract TileUplinkModule createNewTileEntity(World worldIn, int meta);

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

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        worldIn.setBlockState(pos, state.withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)), 2);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).ordinal();
    }
}
