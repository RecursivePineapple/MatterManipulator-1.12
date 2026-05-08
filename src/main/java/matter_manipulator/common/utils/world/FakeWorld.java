package matter_manipulator.common.utils.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.Optional.Method;

import com.cleanroommc.modularui.utils.fakeworld.DummyWorld;
import dev.redstudio.alfheim.lighting.LightingEngine;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

@SuppressWarnings("NullableProblems")
public class FakeWorld extends DummyWorld {

    public final Long2ObjectOpenHashMap<IBlockState> blocks = new Long2ObjectOpenHashMap<>();

    public WorldBorder border;

    public FakeWorld(World example) {
        super();

        border = example.getWorldBorder();

        blocks.defaultReturnValue(Blocks.AIR.getDefaultState());

        // De-allocate alfheim lighting engine
        if (Loader.isModLoaded("alfheim")) {
            ObfuscationReflectionHelper.setPrivateValue(World.class, this, null,
                "alfheim$lightingEngine");
        }
    }

    @Override
    public WorldBorder getWorldBorder() {
        return border;
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return true;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return blocks.get(pos.toLong());
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        blocks.put(pos.toLong(), newState);

        return true;
    }

    @Override
    @Method(modid = "alfheim")
    public World init() {
        return this;
    }

    @Override
    @Method(modid = "alfheim")
    public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos) {
        return 15;
    }

    @Method(modid = "alfheim")
    public int alfheim$getLight(BlockPos pos, boolean checkNeighbors) {
        return 15;
    }

    @Method(modid = "alfheim")
    public LightingEngine getAlfheim$lightingEngine() {
        return null;
    }
}
