package matter_manipulator.core.manipulator_state;

public interface EnergyManipulatorState extends ManipulatorState {

    /// Extracts a given amount of charge from this energy resource.
    double extract(double amount);

    double getStored();
    double getCapacity();

}
