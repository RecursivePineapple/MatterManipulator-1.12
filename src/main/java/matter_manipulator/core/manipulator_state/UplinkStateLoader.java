package matter_manipulator.core.manipulator_state;

import java.util.Optional;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Contract;

import matter_manipulator.MatterManipulator;
import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.persist.IDataStorage;

public class UplinkStateLoader implements ManipulatorStateLoader<UplinkManipulatorState> {

    public static final ResourceLocation ID = MatterManipulator.loc("uplink");
    public static final UplinkStateLoader INSTANCE = new UplinkStateLoader();

    @Contract(pure = true)
    @Override
    public ResourceLocation getResourceID() {
        return ID;
    }

    @Override
    public Optional<UplinkManipulatorState> load(ManipulatorContext context, IDataStorage storage) {
        return Optional.of(new UplinkManipulatorState(storage.getSandbox(getResourceID())));
    }
}
