package matter_manipulator.core.tooltip;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import matter_manipulator.MatterManipulator;
import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.core.item.ItemId;

@EventBusSubscriber(Side.CLIENT)
public class MMTooltipManager {

    private static final Object2ObjectOpenCustomHashMap<ItemStack, TooltipSupplier> TOOLTIPS = new Object2ObjectOpenCustomHashMap<>(ItemId.STACK_ITEM_META_NBT_STRATEGY);

    public static TooltipSupplier markdown(String path) {
        return new TooltipSupplier() {

            private List<String> cache = null;

            @Override
            public List<String> getTooltip() {
                if (cache == null) {
                    cache = MarkdownTooltipLoader.STANDARD.loadStandardPath(MatterManipulator.loc(path), Collections.emptyMap());
                }

                return cache;
            }

            @Override
            public void onResourcesReloaded() {
                cache = null;
            }
        };
    }

    public static void addTooltip(Block block, TooltipSupplier tooltip) {
        addTooltip(block, 0, tooltip);
    }

    public static void addTooltip(Block block, int meta, TooltipSupplier tooltip) {
        addTooltip(new ItemStack(block, 1, meta), tooltip);
    }

    public static void addTooltip(ItemStack stack, TooltipSupplier tooltip) {
        TOOLTIPS.compute(
            stack, ($, existing) -> {
                if (existing == null) {
                    return tooltip;
                } else {
                    return () -> DataUtils.concat(existing.getTooltip(), tooltip.getTooltip());
                }
            }
        );
    }

    @SubscribeEvent
    public static void drawTooltip(ItemTooltipEvent event) {
        var tt = TOOLTIPS.get(event.getItemStack());

        if (tt != null) {
            event.getToolTip().addAll(tt.getTooltip());
        }
    }

    @SideOnly(Side.CLIENT)
    private static void registerReloadHook() {
        ((IReloadableResourceManager) Minecraft.getMinecraft()
            .getResourceManager()).registerReloadListener((ISelectiveResourceReloadListener) (resourceManager, resourcePredicate) -> {
            if (!resourcePredicate.test(VanillaResourceType.LANGUAGES)) return;

            TOOLTIPS.values().forEach(TooltipSupplier::onResourcesReloaded);
        });
    }

    static {
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            registerReloadHook();
        }
    }
}
