package matter_manipulator.core.manipulator_state;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.context.PlayerContext;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.persist.StateSandbox;

/// Some sort of stateful object that's attached to a matter manipulator. This can be anything, but fluid tanks must
/// instead implement [FluidManipulatorState] and energy buffers must implement [EnergyManipulatorState]. Operations on
/// these objects must be immediately flushed to the backing data storage to prevent dupes and other synchronization
/// bugs. To achieve this, the [StateSandbox] retrieved from the [IDataStorage] in
/// [ManipulatorStateLoader#load(ManipulatorContext, IDataStorage)] should be stored in the [ManipulatorState]. If
/// this object interacts with external machines or stacks, care should be taken to avoid 'use-after-invalidate' bugs.
/// As an example, try to avoid [ItemStack] references to persistent stacks and instead store a coordinate or a slot
/// index.
public interface ManipulatorState extends ICapabilityProvider {

    @Override
    default boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return false;
    }

    @Override
    @Nullable
    default <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        return null;
    }

    default void addManipulatorTooltipInfo(PlayerContext context, List<String> lines) {

    }
}
