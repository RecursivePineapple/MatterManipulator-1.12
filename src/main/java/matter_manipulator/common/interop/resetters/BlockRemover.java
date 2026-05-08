package matter_manipulator.common.interop.resetters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.oredict.OreDictionary;

import org.jetbrains.annotations.NotNull;

import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.context.TargetedManipulatorContext;
import matter_manipulator.core.interop.BlockResetter;
import matter_manipulator.core.item.ItemStackLike;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemStackWrapper;
import matter_manipulator.mixin.BlockCaptureDrops;

public class BlockRemover implements BlockResetter {

    @Override
    public @NotNull List<ResourceStack> resetBlock(@NotNull TargetedManipulatorContext context) {
        World world = context.getWorld();
        IBlockState state = context.getBlockState();
        BlockPos pos = context.getPos();

        if (state.getBlockHardness(world, pos) < 0) return Collections.emptyList();

        IBlockSpec spec = MMRegistriesInternal.getPartialBlockSpec(context);

        // Something strange that the MM doesn't understand
        if (spec == null) return Collections.emptyList();
        // A fluid or something
        if (!(spec.getResource() instanceof ItemStackLike stack)) return Collections.emptyList();

        boolean isOre = false;

        ItemStack stackFast = stack.toStackFast(1);

        if (!stackFast.isEmpty()) {
            for (int id : OreDictionary.getOreIDs(stackFast)) {
                if (OreDictionary.getOreName(id).startsWith("ore")) {
                    isOre = true;
                    break;
                }
            }
        }

        List<ResourceStack> resources = new ArrayList<>();

        try (var ignored = BlockCaptureDrops.mm$captureDrops(world, resources::addAll)) {
            NonNullList<ItemStack> drops = NonNullList.create();

            state.getBlock().getDrops(drops, world, pos, state, 0);

            float chance = ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, 0, 1f, false, context.getRealPlayer());

            for (ItemStack drop : drops) {
                if (world.rand.nextFloat() <= chance) {
                    resources.add(new ItemStackWrapper(drop));
                }
            }

            world.setBlockToAir(pos);
        }

        return isOre ? Collections.emptyList() : resources;
    }
}
