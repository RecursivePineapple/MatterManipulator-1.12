package matter_manipulator.core.resources.item;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.core.item.ImmutableItemStack;
import matter_manipulator.core.item.ItemId;
import matter_manipulator.core.resources.ResourceIdentity;
import matter_manipulator.core.resources.ResourceProvider;
import matter_manipulator.core.resources.SimulatedResourceProvider;

public class SimulatedItemResourceProvider implements ResourceProvider<IntItemResourceStack>, SimulatedResourceProvider {

    public final Object2LongOpenHashMap<ItemId> required = new Object2LongOpenHashMap<>();

    @Override
    public ItemResourceProviderFactory getFactory() {
        return ItemResourceProviderFactory.INSTANCE;
    }

    @Override
    public boolean canExtract(IntItemResourceStack request) {
        return true;
    }

    @Override
    public IntItemResourceStack extract(IntItemResourceStack request) {
        for (ImmutableItemStack stack : MMRegistriesInternal.FREE_ITEMS) {
            if (stack.matches(request)) return request.copy();
        }

        required.addTo(request.getIdentity(), request.getAmountInt());

        return request.copy();
    }

    @Override
    public IntItemResourceStack insert(IntItemResourceStack stack) {
        for (ImmutableItemStack free : MMRegistriesInternal.FREE_ITEMS) {
            if (free.matches(stack)) return null;
        }

        required.addTo(stack.getIdentity(), -stack.getAmountInt());

        return null;
    }

    @Override
    public Object2LongMap<ResourceIdentity> getNetStacks() {
        //noinspection unchecked,rawtypes
        return (Object2LongMap<ResourceIdentity>) (Object2LongMap) required;
    }
}
