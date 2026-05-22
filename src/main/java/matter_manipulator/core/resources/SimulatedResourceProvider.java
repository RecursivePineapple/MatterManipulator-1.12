package matter_manipulator.core.resources;

import it.unimi.dsi.fastutil.objects.Object2LongMap;

public interface SimulatedResourceProvider {

    /// Returns the list of all inserted or extracted stacks.
    /// Required/extracted stacks have a positive sign.
    /// Excess/inserted stacks have a negative sign.
    Object2LongMap<ResourceIdentity> getNetStacks();
}
