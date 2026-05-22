package matter_manipulator.core.resources.fluid;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import matter_manipulator.core.fluid.FluidId;
import matter_manipulator.core.resources.ResourceIdentity;
import matter_manipulator.core.resources.ResourceProvider;
import matter_manipulator.core.resources.ResourceProviderFactory;
import matter_manipulator.core.resources.SimulatedResourceProvider;

public class SimulatedFluidResourceProvider implements ResourceProvider<IntFluidResourceStack>, SimulatedResourceProvider {

    public final Object2LongOpenHashMap<FluidId> required = new Object2LongOpenHashMap<>();

    @Override
    public ResourceProviderFactory<?> getFactory() {
        return null;
    }

    @Override
    public boolean canExtract(IntFluidResourceStack request) {
        return true;
    }

    @Override
    public IntFluidResourceStack extract(IntFluidResourceStack request) {
        required.addTo(request.getIdentity(), request.getAmountInt());

        return request.copy();
    }

    @Override
    public IntFluidResourceStack insert(IntFluidResourceStack stack) {
        required.addTo(stack.getIdentity(), -stack.getAmountInt());

        return null;
    }

    @Override
    public Object2LongMap<ResourceIdentity> getNetStacks() {
        //noinspection unchecked,rawtypes
        return (Object2LongMap<ResourceIdentity>) (Object2LongMap) required;
    }
}
