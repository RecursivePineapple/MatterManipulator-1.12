package matter_manipulator.core.manipulator_state;

import java.util.Optional;

import net.minecraft.util.ResourceLocation;

import matter_manipulator.MatterManipulator;
import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.persist.IDataStorage;

public class RFEnergyStateLoader implements ManipulatorStateLoader<RFEnergyManipulatorState> {

    public static final ResourceLocation ID = MatterManipulator.loc("rf");
    public static final RFEnergyStateLoader INSTANCE = new RFEnergyStateLoader();

    @Override
    public ResourceLocation getResourceID() {
        return ID;
    }

    @Override
    public Optional<RFEnergyManipulatorState> load(ManipulatorContext context, IDataStorage storage) {
        return Optional.of(new RFEnergyManipulatorState(context.getTier(), storage.getSandbox(getResourceID())));
    }
}
