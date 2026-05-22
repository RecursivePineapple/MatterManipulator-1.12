package matter_manipulator.core.resources;

import matter_manipulator.core.context.HeldManipulatorContext;
import matter_manipulator.core.context.PlayerContext;

/// A ResourceProviderFactory is something that can examine a manipulator's context and produce an object that can
/// extract or insert a specific type of resource. Any implementation details beyond this are undefined, and each
/// [Resource] will have its own mechanism for registering sub-providers, assuming such a concept makes sense for that
/// resource.
public interface ResourceProviderFactory<Provider extends ResourceProvider<?>> {

    /// Returns the resource that is associated with this factory.
    Resource<Provider> getResource();

    /// Performs whatever logic is necessary to create the [ResourceProvider] from a [PlayerContext].
    Provider createProvider(HeldManipulatorContext context);

    /// Creates a resource provider that only tracks how many items are inserted and extracted. The returned object must
    /// implement [SimulatedResourceProvider].
    Provider createSimulatedProvider(HeldManipulatorContext context);
}
