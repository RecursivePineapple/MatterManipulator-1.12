package matter_manipulator.common.block_spec;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.common.utils.world.ProxiedWorld;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.block_spec.BlockSpecLoader;
import matter_manipulator.core.context.AnalysisContext;
import matter_manipulator.core.context.ManipulatorPlacingContext;
import matter_manipulator.core.context.PlacingContext;
import matter_manipulator.core.i18n.JoiningLocalizer;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.interop.InteropMap;
import matter_manipulator.core.resources.ResourceIdentity;
import matter_manipulator.core.resources.ResourceProvider;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.IntItemResourceStack;

@EqualsAndHashCode
public abstract class AbstractBlockSpec implements BlockSpec, Cloneable {

    public InteropMap interop = new InteropMap();

    @Override
    public abstract BlockSpecLoader getLoader();

    @Override
    public abstract IBlockState getBlockState();

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public abstract ResourceStack getResource();

    @SneakyThrows
    @Override
    public AbstractBlockSpec clone() {
        AbstractBlockSpec copy = (AbstractBlockSpec) super.clone();

        copy.resetResource();
        copy.interop = copy.interop.clone();

        return copy;
    }

    protected abstract void resetResource();

    @Override
    public abstract BlockSpec sanitized();

    @Override
    @OverridingMethodsMustInvokeSuper
    public void transform(Transform transform) {
        this.interop.transform(transform);
    }

    @Override
    public boolean canPlaceAt(ProxiedWorld world, BlockPos pos) {
        IBlockState state = getBlockState();

        world.overrides.clear();
        world.setBlockToAir(pos);
        if (!state.getBlock()
            .canPlaceBlockAt(world, pos)) {
            return false;
        }

        // Check block dependencies for things like levers by placing it in a fake world and mocking a block update.
        // If it removes itself, then it can't be placed here yet.
        world.overrides.clear();
        world.setBlockState(pos, state);
        //noinspection deprecation
        state.getBlock()
            .neighborChanged(state, world, pos, Blocks.AIR, pos.add(0, 1, 0));

        return world.getBlockState(pos) == state;
    }

    @Override
    public boolean matches(BlockSpec other) {
        if (this.getClass() != other.getClass()) return false;

        if (!this.getResource().isSameType(other.getResource())) return false;

        return ResourceStack.getStackAmount(this.getResource()) == ResourceStack.getStackAmount(other.getResource());
    }

    @Override
    public @NotNull Localized getDisplayName() {
        ResourceStack stack = getResource();

        Localized name;

        if (stack.isEmpty()) {
            name = new Localized("tile.air.name");
        } else {
            name = stack.getName();
        }

        List<Localized> details = this.interop.getDetails();

        if (!details.isEmpty()) {
            Localized detailsJoined = new Localized(JoiningLocalizer.COMMAS, (Object[]) details.toArray(new Localized[0]));

            name = new Localized("mm.misc.stack-details", name, detailsJoined);
        }

        return name;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ApplyResult place(PlacingContext context) {
        ResourceStack stack = this.getResource();

        ResourceProvider resource = context.resource(stack.getResource());
        ResourceStack extracted = null;

        if (!stack.isEmpty()) {
            extracted = resource.extract(stack);

            if (ResourceStack.getStackAmount(extracted) != ResourceStack.getStackAmount(stack)) {
                // We couldn't extract the right amount of items/fluids/etc. Reinsert whatever we got and try again later.
                resource.insert(extracted);
                context.extractionFailure(stack);
                return ApplyResult.Retry;
            }
        }

        ApplyResult result = doPlace(context, extracted);

        switch (result) {
            case DidNothing, NotApplicable, Retry, Error -> {
                if (!stack.isEmpty()) {
                    // For whatever reason we couldn't place the block, so we have to reinsert it.
                    resource.insert(extracted);
                }
            }
            default -> {

            }
        }

        return result;
    }

    protected ApplyResult doPlace(PlacingContext context, ResourceStack extracted) {
        var world = context.getWorld();
        var pos = context.getPos();

        IBlockState toPlace = this.getBlockState();

        if (toPlace == world.getBlockState(pos)) return ApplyResult.DidNothing;

        world.setBlockState(pos, toPlace);

        var placed = world.getBlockState(pos);

        placed.getBlock()
            .onBlockPlacedBy(
                world,
                pos,
                placed,
                context.getRealPlayer(),
                extracted instanceof IntItemResourceStack item ? item.toStack() : ItemStack.EMPTY);

        return ApplyResult.DidSomething;
    }

    @Override
    public EnumSet<ApplyResult> update(ManipulatorPlacingContext context) {
        return this.interop.apply(context);
    }

    @Override
    public void getRequiredResourcesForUpdate(ManipulatorPlacingContext context, boolean skipExisting) {
        this.interop.getRequiredResources(context, skipExisting);
    }

    public void saveInterop(JsonObject specRoot) {
        this.interop.saveInterop(specRoot);
    }

    public void loadInterop(JsonObject specRoot) {
        this.interop.loadInterop(specRoot);
    }

    public void analyze(AnalysisContext context) {
        this.interop.analyze(context);
    }

    public void modifyResource(ResourceStack resource) {
        for (var e : interop.object2ObjectEntrySet()) {
            //noinspection unchecked
            e.getKey().modifyResource(resource, e.getValue());
        }
    }

    public void exchangeInterop(ResourceIdentity stack, ResourceIdentity replacement) {
        this.interop.exchange(stack, replacement);
    }
}
