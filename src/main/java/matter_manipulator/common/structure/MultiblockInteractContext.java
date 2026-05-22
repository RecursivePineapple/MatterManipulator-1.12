package matter_manipulator.common.structure;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import matter_manipulator.client.rendering.MMHintRenderer;
import matter_manipulator.common.resources.item.ios.DroppingItemStackIOFactory;
import matter_manipulator.common.resources.item.ios.PlayerInventoryItemStackIOFactory;
import matter_manipulator.common.structure.coords.ControllerRelativeCoords;
import matter_manipulator.common.structure.coords.Offset;
import matter_manipulator.common.structure.coords.StructureRelativeCoords;
import matter_manipulator.common.utils.enums.ExtendedFacing;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.color.ImmutableColor;
import matter_manipulator.core.context.StructureInteractContext;
import matter_manipulator.core.i18n.JoiningLocalizer;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.i18n.MMTextBuilder;
import matter_manipulator.core.item.ItemStackIO;
import matter_manipulator.core.misc.BuildFeedback;
import matter_manipulator.core.misc.FeedbackSeverity;
import matter_manipulator.core.resources.Resource;
import matter_manipulator.core.resources.ResourceIdentity;
import matter_manipulator.core.resources.ResourceProvider;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemResource;
import matter_manipulator.core.resources.item.ItemResourceProvider;

public class MultiblockInteractContext<T extends TileEntity & MultiblockController<T>> implements StructureInteractContext<T> {

    public T controller;
    public String part = "main";
    public Offset<StructureRelativeCoords> partOffset;

    public EntityPlayer player;
    public ItemStack trigger;
    public int placeQuota = 0;

    public final ObjectArrayList<Localized> feedbackContext = new ObjectArrayList<>();
    public List<BuildFeedback> feedback = new ArrayList<>(0);

    private final Object2LongOpenHashMap<ResourceIdentity> extractionFailures = new Object2LongOpenHashMap<>();

    private ItemResourceProvider itemProvider;

    @Getter
    @Setter
    private BlockPos pos;

    public MultiblockInteractContext(T controller) {
        this.controller = controller;
    }

    public MultiblockInteractContext(T controller, EntityPlayer player, ItemStack trigger, int placeQuota) {
        this.controller = controller;
        this.player = player;
        this.trigger = trigger;
        this.placeQuota = placeQuota;

        this.itemProvider = new ItemResourceProvider(new ItemStackIO[] {
            PlayerInventoryItemStackIOFactory.INSTANCE.createIO(player),
            DroppingItemStackIOFactory.INSTANCE.createIO(player)
        });
    }

    @Override
    public T getData() {
        return controller;
    }

    @Override
    public IStructureDefinition<? super T> getStructureDefinition() {
        return controller.getDefinition();
    }

    @Override
    public World getWorld() {
        return controller.getWorld();
    }

    @Override
    public EntityPlayer getRealPlayer() {
        return player;
    }

    @Override
    public ExtendedFacing getOrientation() {
        return controller.getOrientation();
    }

    @Override
    public ControllerRelativeCoords getControllerPos() {
        return controller.getControllerPos();
    }

    @Override
    public String getPartName() {
        return part;
    }

    @Override
    public Offset<StructureRelativeCoords> getPartOffset() {
        return partOffset;
    }

    @Override
    public @NotNull ItemStack getTrigger() {
        return trigger;
    }

    @Override
    public void emitHint(BlockPos pos, BlockSpec spec, ImmutableColor tint) {
        MMHintRenderer.INSTANCE.addHint(pos.getX(), pos.getY(), pos.getZ(), spec, tint);
    }

    @Override
    public boolean hasPlaceQuota() {
        return placeQuota > 0;
    }

    @Override
    public void consumePlaceQuota() {
        placeQuota--;
    }

    @Override
    public <Provider extends ResourceProvider<?>> Provider resource(Resource<Provider> resource) {
        if (resource == ItemResource.ITEMS) {
            //noinspection unchecked
            return (Provider) itemProvider;
        }

        return null;
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
        this.extractionFailures.addTo(stack.getIdentity(), ResourceStack.getStackAmount(stack));
    }

    public void onInteractionFinished() {
        if (!extractionFailures.isEmpty()) {
            new Localized("mm.info.warning.could_not_find").setBase(TextFormatting.GRAY).sendChat(getRealPlayer());

            extractionFailures.object2LongEntrySet().fastForEach(e -> {
                new MMTextBuilder("mm.info.warning.missing_resource")
                    .setBase(TextFormatting.GRAY)
                    .addLocalized(e.getKey().getName())
                    .addNumber(e.getLongValue())
                    .toLocalized()
                    .sendChat(getRealPlayer());
            });

            extractionFailures.clear();
        }
    }
}
