package matter_manipulator;

import java.lang.reflect.InvocationTargetException;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import matter_manipulator.common.blocks.BlockHint;
import matter_manipulator.common.blocks.BlockUplinkCasing;
import matter_manipulator.common.blocks.BlockUplinkController;
import matter_manipulator.common.blocks.BlockUplinkEnergyConnector;
import matter_manipulator.common.items.ItemCluster;
import matter_manipulator.common.items.ItemHologramProjector;
import matter_manipulator.common.items.ItemMatterManipulator;
import matter_manipulator.common.items.ItemWrench;
import matter_manipulator.common.items.MMItemList;
import matter_manipulator.common.items.MMMetaItem;
import matter_manipulator.common.items.ManipulatorTier;
import matter_manipulator.common.items.RecipeInstallUpgrade;
import matter_manipulator.common.keybind.MMKeybinds;
import matter_manipulator.common.networking.MMNetwork;
import matter_manipulator.common.ui.MMGuiTheme;
import matter_manipulator.common.ui.factory.PlanEditUIFactory;
import matter_manipulator.common.ui.factory.RadialMenuUIFactory;
import matter_manipulator.common.ui.factory.UplinkUIFactory;
import matter_manipulator.common.uplink.TileUplinkController;
import matter_manipulator.common.uplink.TileUplinkEnergyConnector;
import matter_manipulator.core.tooltip.MMTooltipManager;

public class CommonProxy {

    public static MMMetaItem META_ITEM = new MMMetaItem("metaitem");

    public static final BlockHint HINT_BLANK = new BlockHint("hint_blank");
    public static final BlockHint HINT_DOT = new BlockHint("hint_dot");
    public static final BlockHint HINT_WARNING = new BlockHint("hint_warning");
    public static final BlockHint HINT_X = new BlockHint("hint_x");

    public static final BlockUplinkCasing UPLINK_STRUCTURE_WHITE = new BlockUplinkCasing("uplink-structure-white", false);
    public static final BlockUplinkCasing UPLINK_STRUCTURE_BLACK = new BlockUplinkCasing("uplink-structure-black", false);
    public static final BlockUplinkCasing UPLINK_COIL = new BlockUplinkCasing("uplink-coil", true);
    public static final BlockUplinkCasing UPLINK_SUPPORT_BLACK = new BlockUplinkCasing("uplink-support-black", false);
    public static final BlockUplinkCasing UPLINK_SUPPORT_WHITE = new BlockUplinkCasing("uplink-support-white", false);

    public static final BlockUplinkEnergyConnector UPLINK_ENERGY_CONNECTOR = new BlockUplinkEnergyConnector();

    public static final ItemHologramProjector HOLOGRAM_PROJECTOR = new ItemHologramProjector();
    public static final ItemWrench WRENCH = new ItemWrench();

    public static final BlockUplinkController UPLINK_CONTROLLER = new BlockUplinkController();

    public void preInit(FMLPreInitializationEvent event) {
        MatterManipulator.LOG.info("Loading Matter Manipulator version " + Tags.VERSION);

        GlobalMMConfig.init();
        MMNetwork.init();
        RadialMenuUIFactory.INSTANCE.register();
        PlanEditUIFactory.INSTANCE.register();
        UplinkUIFactory.INSTANCE.register();
        MMKeybinds.init();
        MMGuiTheme.registerThemes();

        try {
            Class.forName("matter_manipulator.compat.InteropLoader").getMethod("preInit").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
            ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void init(FMLInitializationEvent event) {
        try {
            Class.forName("matter_manipulator.compat.InteropLoader").getMethod("init").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
            ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void postInit(FMLPostInitializationEvent event) {
        try {
            Class.forName("matter_manipulator.compat.InteropLoader").getMethod("postInit").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
            ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void serverStarting(FMLServerStartingEvent event) {

    }

    public EntityPlayer getThePlayer() {
        return null;
    }

    public void registerItems(RegistryEvent.Register<Item> event) {
        MatterManipulator.LOG.info("Registering Items");

        MMItemList.Cluster.set(registerItem(new ItemCluster()));

        MMItemList.MK0.set(registerItem(new ItemMatterManipulator(ManipulatorTier.MK0)));
        MMItemList.MK1.set(registerItem(new ItemMatterManipulator(ManipulatorTier.MK1)));
        MMItemList.MK2.set(registerItem(new ItemMatterManipulator(ManipulatorTier.MK2)));
        MMItemList.MK3.set(registerItem(new ItemMatterManipulator(ManipulatorTier.MK3)));

        MMItemList.HologramProjector.set(registerItem(HOLOGRAM_PROJECTOR));
        MMItemList.Wrench.set(registerItem(WRENCH));

        registerItem(META_ITEM, false);
    }

    public Block registerBlock(Block block) {
        return registerBlock(block, new ItemBlock(block));
    }

    public Block registerBlock(Block block, ItemBlock itemBlock) {
        ForgeRegistries.BLOCKS.register(block);
        registerItem(itemBlock.setRegistryName(block.getRegistryName()));
        return block;
    }

    public Item registerItem(Item item) {
        return registerItem(item, true);
    }

    public Item registerItem(Item item, boolean model) {
        ForgeRegistries.ITEMS.register(item);
        if (model) registerModel(item);
        return item;
    }

    public void registerModel(Item item) {

    }

    public void registerBlocks(Register<Block> event) {
        MatterManipulator.LOG.info("Registering Blocks");

        registerBlock(HINT_BLANK);
        registerBlock(HINT_DOT);
        registerBlock(HINT_WARNING);
        registerBlock(HINT_X);
        registerBlock(UPLINK_CONTROLLER);
        registerBlock(UPLINK_STRUCTURE_WHITE);
        registerBlock(UPLINK_STRUCTURE_BLACK);
        registerBlock(UPLINK_COIL);
        registerBlock(UPLINK_SUPPORT_BLACK);
        registerBlock(UPLINK_SUPPORT_WHITE);
        registerBlock(UPLINK_ENERGY_CONNECTOR);

        MMTooltipManager.addTooltip(UPLINK_ENERGY_CONNECTOR, MMTooltipManager.markdown("energy-connector"));

        GameRegistry.registerTileEntity(TileUplinkController.class, MatterManipulator.loc("uplink"));
        GameRegistry.registerTileEntity(TileUplinkEnergyConnector.class, MatterManipulator.loc("uplink-energy-connector"));
    }

    public void registerRecipes(Register<IRecipe> event) {
        RecipeInstallUpgrade.register(event);
    }
}
