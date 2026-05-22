package matter_manipulator.core.context;

import matter_manipulator.core.resources.Resource;
import matter_manipulator.core.resources.ResourceProvider;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.fluid.FluidResource;
import matter_manipulator.core.resources.fluid.IntFluidResourceStack;
import matter_manipulator.core.resources.item.IntItemResourceStack;
import matter_manipulator.core.resources.item.ItemResource;

public interface ResourceContext {

    <Provider extends ResourceProvider<?>> Provider resource(Resource<Provider> resource);

    default ResourceProvider<IntItemResourceStack> items() {
        return resource(ItemResource.ITEMS);
    }

    default ResourceProvider<IntFluidResourceStack> fluids() {
        return resource(FluidResource.FLUIDS);
    }

    default void insert(Iterable<ResourceStack> stacks) {
        for (ResourceStack stack : stacks) {
            //noinspection unchecked
            resource(stack.getResource()).insert(stack);
        }
    }
}
