package matter_manipulator.core.resources.fluid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import matter_manipulator.core.fluid.BigFluidStack;
import matter_manipulator.core.fluid.FluidId;
import matter_manipulator.core.persist.NBTPersist;
import matter_manipulator.core.resources.Resource;
import matter_manipulator.core.resources.ResourceProvider;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.ResourceTrait;

public class FluidResource implements Resource<ResourceProvider<IntFluidResourceStack>> {

    public static final FluidResource FLUIDS = new FluidResource();

    private FluidResource() { }

    @Override
    public String getKey() {
        return "core:fluid";
    }

    @Override
    public @NotNull ResourceStack load(@NotNull JsonElement element) {
        if (!(element instanceof JsonObject obj)) throw new JsonParseException("Expected json object: " + element);

        Fluid fluid = FluidRegistry.getFluid(obj.get("id").getAsString());

        if (fluid == null) return IntFluidResourceStack.EMPTY;

        boolean isLong = false;
        long amount;

        if (obj.has("amountLong")) {
            isLong = true;
            amount = obj.get("amountLong").getAsLong();
        } else {
            amount = obj.get("amountInt").getAsInt();
        }

        NBTTagCompound tag = null;

        if (obj.has("tag")) {
            tag = (NBTTagCompound) NBTPersist.toNbtExact(obj.get("tag"));
        }

        if (isLong) {
            return new BigFluidStack(FluidId.create(fluid, tag), amount);
        } else {
            return new FluidStackWrapper(new FluidStack(fluid, (int) amount, tag));
        }
    }

    @Override
    public @NotNull JsonElement save(@NotNull ResourceStack stack) {
        if (!(stack instanceof FluidResourceStack fluid)) throw new IllegalStateException("Cannot use FluidResource to save " + stack);

        JsonObject obj = new JsonObject();

        obj.addProperty("id", FluidRegistry.getFluidName(fluid.getFluid()));

        if (fluid.hasTrait(ResourceTrait.LongAmount)) {
            obj.addProperty("amountLong", fluid.asLong().getAmountLong());
        } else if (fluid.hasTrait(ResourceTrait.IntAmount)) {
            obj.addProperty("amountInt", fluid.asInt().getAmountInt());
        }

        if (fluid.getTag() != null) {
            obj.add("tag", NBTPersist.toJsonObjectExact(fluid.getTag()));
        }

        return obj;
    }
}
