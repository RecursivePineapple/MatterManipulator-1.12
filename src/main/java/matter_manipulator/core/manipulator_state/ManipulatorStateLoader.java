package matter_manipulator.core.manipulator_state;

import java.util.Optional;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Contract;

import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.persist.IDataStorage;

/// An adapter that loads a specific [ManipulatorState] implementation from a manipulator item.
public interface ManipulatorStateLoader<State extends ManipulatorState> {

    @Contract(pure = true)
    ResourceLocation getResourceID();

    /// Loads a resource from the storage, or creates it if it doesn't already exist.
    Optional<State> load(ManipulatorContext context, IDataStorage storage);
}
