package matter_manipulator.core.interop;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import it.unimi.dsi.fastutil.Pair;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.common.utils.deps.IDependencyGraph;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.block_spec.BlockSpecExtractor;
import matter_manipulator.core.block_spec.BlockSpecLoader;
import matter_manipulator.core.fluid.FluidStackIO;
import matter_manipulator.core.i18n.Localizer;
import matter_manipulator.core.inventory_adapter.InventoryAdapter;
import matter_manipulator.core.inventory_adapter.InventoryAdapterFactory;
import matter_manipulator.core.item.ImmutableItemStack;
import matter_manipulator.core.item.ItemStackIO;
import matter_manipulator.core.keybind.ManipulatorKeybind;
import matter_manipulator.core.manipulator_state.ManipulatorStateLoader;
import matter_manipulator.core.modes.ManipulatorMode;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.resources.Resource;
import matter_manipulator.core.resources.ResourceIOFactory;
import matter_manipulator.core.resources.ResourceProvider;
import matter_manipulator.core.resources.ResourceProviderFactory;
import matter_manipulator.core.resources.fluid.FluidResourceStack;
import matter_manipulator.core.resources.item.IntItemResourceStack;
import matter_manipulator.core.settings.ManipulatorSetting;

/// All registries available for third party mods to add their own Matter Manipulator integrations.
/// See the static initializers in [MMRegistriesInternal] for built-in targets.
@SuppressWarnings("rawtypes")
public class MMRegistries {

    /// These are called when a block is removed, to erase its configuration before returning any items to the player.
    public static IDependencyGraph<BlockResetter> blockResetters() {
        return MMRegistriesInternal.BLOCK_RESETTERS;
    }

    /// These are used to copy, save, load, and apply some configuration item from a block. They have free rein over
    /// any field or trait of the block, so long as they only affect the requested block.
    public static IDependencyGraph<InteropModule<?>> interop() {
        return MMRegistriesInternal.INTEROP_MODULES;
    }

    /// [InventoryAdapterFactory]s are iterated in order until one returns a non-null [InventoryAdapter], which is used
    /// to inspect and modify inventories. Many machines have custom inventory logic that cannot be represented through
    /// an [IInventory], and this is the mechanism through which that logic is expressed (for manipulators).
    public static IDependencyGraph<InventoryAdapterFactory<? extends IntItemResourceStack>> inventoryAdapters() {
        return MMRegistriesInternal.INV_ADAPTERS;
    }

    /// [InventoryAdapterFactory]s are iterated in order until one returns a non-null [InventoryAdapter], which is used
    /// to inspect and modify inventories. Many machines have custom inventory logic that cannot be represented through
    /// an [IInventory], and this is the mechanism through which that logic is expressed (for manipulators).
    public static IDependencyGraph<InventoryAdapterFactory<? extends FluidResourceStack>> tankAdapters() {
        return MMRegistriesInternal.TANK_ADAPTERS;
    }

    /// A [BlockSpecExtractor] is used to extract [BlockSpec]s from in-world blocks.
    public static IDependencyGraph<BlockSpecExtractor> blockSpecExtractors() {
        return MMRegistriesInternal.SPEC_EXTRACTORS;
    }

    /// A [BlockSpecLoader] is used to transparently load or save [BlockSpec] implementations from or to json.
    public static void registerSpecLoader(BlockSpecLoader loader) {
        MMRegistriesInternal.SPEC_LOADERS.put(loader.getKey(), loader);
    }

    /// ItemStackIOFactories are used to create [ItemStackIO]s. They have full access to any piece of state on
    /// manipulators or the player, but caution must be taken to avoid corrupting anything. If an IO requires its own
    /// state, a [IDataStorage] object is provided to the factory. State modifications must be immediately flushed back
    /// to the [IDataStorage], and inserts or extracts must immediately update the world to prevent dupes or deletions.
    public static IDependencyGraph<ResourceIOFactory<ItemStackIO>> itemIOFactories() {
        return MMRegistriesInternal.ITEM_IO_FACTORIES;
    }

    /// FluidStackIOFactories are used to create [FluidStackIO]s. They have full access to any piece of state on
    /// manipulators or the player, but caution must be taken to avoid corrupting anything. If an IO requires its own
    /// state, a [IDataStorage] object is provided to the factory. State modifications must be immediately flushed back
    /// to the [IDataStorage], and inserts or extracts must immediately update the world to prevent dupes or deletions.
    public static IDependencyGraph<ResourceIOFactory<FluidStackIO>> fluidIOFactories() {
        return MMRegistriesInternal.FLUID_IO_FACTORIES;
    }

    public static void registerManipulatorMode(ManipulatorMode<?, ?> mode) {
        MMRegistriesInternal.MODES.put(mode.getModeID(), mode);
    }

    public static void registerManipulatorSetting(ManipulatorSetting<?> mode) {
        MMRegistriesInternal.SETTINGS.put(mode.getSettingID(), mode);
    }

    public static <Provider extends ResourceProvider<?>> void registerResourceType(Resource<Provider> resource, ResourceProviderFactory<Provider> factory) {
        MMRegistriesInternal.RESOURCES.put(resource, factory);
        MMRegistriesInternal.RESOURCE_LOADERS.put(resource.getKey(), resource);
        //noinspection unchecked
        MMRegistriesInternal.RESOURCE_ARRAY = MMRegistriesInternal.RESOURCES.entrySet()
            .stream()
            .map(e -> Pair.of(e.getKey(), e.getValue()))
            .toArray(Pair[]::new);
    }

    public static void registerManipulatorStateLoader(ManipulatorStateLoader loader) {
        MMRegistriesInternal.MANIPULATOR_RESOURCE_LOADERS.put(loader.getResourceID(), loader);
    }

    public static IDependencyGraph<BlockStateMutator> stateMutators() {
        return MMRegistriesInternal.BLOCK_STATE_MUTATORS;
    }

    public static void registerKeybind(ManipulatorKeybind keybind) {
        MMRegistriesInternal.KEYBINDS.put(keybind.getKeybindId(), keybind);
    }

    public static ManipulatorKeybind getKeybind(ResourceLocation id) {
        return MMRegistriesInternal.KEYBINDS.get(id);
    }

    public static void registerLocalizer(ResourceLocation id, Localizer localizer) {
        Localizer existing = MMRegistriesInternal.LOCALIZERS.put(id, localizer);

        if (existing != null) {
            throw new IllegalArgumentException("Localizer ID " + id + " is already used by " + existing);
        }
    }

    public static void addFreeItem(ImmutableItemStack stack) {
        MMRegistriesInternal.FREE_ITEMS = DataUtils.concat(MMRegistriesInternal.FREE_ITEMS, stack);
    }
}
