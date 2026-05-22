package matter_manipulator.common.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import matter_manipulator.Tags;

public class ItemHologramProjector extends Item {

    public ItemHologramProjector() {
        setTranslationKey("hologram-projector");
        setRegistryName(Tags.MODID, "hologram-projector");
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }
}
