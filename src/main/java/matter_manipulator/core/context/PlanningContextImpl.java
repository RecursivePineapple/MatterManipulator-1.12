package matter_manipulator.core.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import matter_manipulator.MatterManipulator;
import matter_manipulator.common.context.HeldManipulatorContextImpl;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.state.MMState;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.i18n.JoiningLocalizer;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.interop.BlockResetter;
import matter_manipulator.core.misc.BuildFeedback;
import matter_manipulator.core.misc.FeedbackSeverity;
import matter_manipulator.core.resources.Resource;
import matter_manipulator.core.resources.ResourceIdentity;
import matter_manipulator.core.resources.ResourceProvider;
import matter_manipulator.core.resources.ResourceProviderFactory;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.SimulatedResourceProvider;

public class PlanningContextImpl extends HeldManipulatorContextImpl implements ManipulatorPlacingContext {

    @SuppressWarnings("rawtypes")
    private final Map<Resource<?>, ResourceProvider> cachedProviders = new Object2ObjectArrayMap<>();

    public final ObjectArrayList<Localized> feedbackContext = new ObjectArrayList<>();
    public List<BuildFeedback> feedback = new ArrayList<>(0);

    public BlockPos pos;
    public BlockSpec spec;

    public PlanningContextImpl(World world, EntityPlayer player, ItemStack manipulator, MMState state) {
        super(world, player, manipulator, state);
    }

    public PlanningContextImpl(HeldManipulatorContext base) {
        super(base.getWorld(), base.getRealPlayer(), base.getManipulator(), base.getState());
    }

    @Override
    public void playSound(BlockPos pos, SoundEvent sound) {

    }

    @Override
    public void setTarget(BlockPos pos, BlockSpec spec) {
        this.pos = pos;
        this.spec = spec;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public BlockSpec getSpec() {
        return spec;
    }

    @Override
    public boolean drainEnergy(double multiplier) {
        return true;
    }

    @Override
    public boolean drainEnergy(BlockPos pos, double multiplier) {
        return true;
    }

    @Override
    public void removeBlock() {
        if (this.pos == null) throw new IllegalStateException("Target was not set");

        List<ResourceStack> drops = new ArrayList<>();

        for (BlockResetter resetter : MMRegistriesInternal.BLOCK_RESETTERS.sorted()) {
            drops.addAll(resetter.resetBlockSimulated(this));
        }

        insert(drops);
    }

    @Override
    public void pushMessageContext(Localized context) {
        feedbackContext.push(context);
    }

    @Override
    public void popMessageContext() {
        feedbackContext.pop();
    }

    @Override
    public void warn(Localized message) {
        List<Localized> args = new ArrayList<>(feedbackContext);
        args.add(message);

        feedback.add(new BuildFeedback(this.pos, new Localized(JoiningLocalizer.COLONS, args), FeedbackSeverity.WARNING));
    }

    @Override
    public void error(Localized message) {
        List<Localized> args = new ArrayList<>(feedbackContext);
        args.add(message);

        feedback.add(new BuildFeedback(this.pos, new Localized(JoiningLocalizer.COLONS, args), FeedbackSeverity.ERROR));
    }

    @Override
    public void extractionFailure(ResourceStack stack) {

    }

    @Override
    public <P extends ResourceProvider<?>> P resource(Resource<P> resource) {
        var cached = cachedProviders.get(resource);

        if (cached != null) {
            //noinspection unchecked
            return (P) cached;
        }

        @SuppressWarnings("unchecked")
        ResourceProviderFactory<P> factory = MMRegistriesInternal.RESOURCES.get(resource);

        if (factory == null) {
            MatterManipulator.LOG.error("Tried to get a ResourceProvider for a Resource that does not have a registered ResourceProviderFactory: {}", resource);
            return null;
        }

        P provider = factory.createSimulatedProvider(this);

        cachedProviders.put(resource, provider);

        return provider;
    }

    public Object2LongOpenHashMap<ResourceIdentity> getNetStacks() {
        Object2LongOpenHashMap<ResourceIdentity> net = new Object2LongOpenHashMap<>();

        for (var resource : cachedProviders.values()) {
            ((SimulatedResourceProvider) resource).getNetStacks().object2LongEntrySet().forEach(e -> {
                net.addTo(e.getKey(), e.getLongValue());
            });
        }

        return net;
    }
}
