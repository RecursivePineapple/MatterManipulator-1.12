package matter_manipulator.client.rendering;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.joml.Vector3i;

import matter_manipulator.CommonProxy;
import matter_manipulator.GlobalMMConfig.RenderingConfig;
import matter_manipulator.common.block_spec.specs.SimpleBlockSpec;
import matter_manipulator.common.context.AnalysisContextImpl;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.utils.data.Lazy;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.building.PendingBlock;
import matter_manipulator.core.building.PendingBlockBuildable;
import matter_manipulator.core.color.ImmutableColor;
import matter_manipulator.core.color.RGBColor;
import matter_manipulator.core.context.ManipulatorRenderingContext;
import matter_manipulator.core.misc.BuildFeedback;

public class StandardModeRenderer<Config, Buildable extends PendingBlockBuildable>
    implements ModeRenderer<Config, Buildable> {

    private static final ImmutableColor WHITE = RGBColor.fromRGB(0xE5F2FF);
    private static final ImmutableColor INFO = RGBColor.fromRGB(0x3e73ff);
    private static final ImmutableColor WARNING = RGBColor.fromRGB(0xFFAA00);
    private static final ImmutableColor ERROR = RGBColor.fromRGB(0xFF5555);

    public static final Lazy<BlockSpec> HINT_BLANK = new Lazy<>(() -> new SimpleBlockSpec(CommonProxy.HINT_BLANK.getDefaultState()));
    public static final Lazy<BlockSpec> HINT_DOT = new Lazy<>(() -> new SimpleBlockSpec(CommonProxy.HINT_DOT.getDefaultState()));
    public static final Lazy<BlockSpec> HINT_WARNING = new Lazy<>(() -> new SimpleBlockSpec(CommonProxy.HINT_WARNING.getDefaultState()));
    public static final Lazy<BlockSpec> HINT_X = new Lazy<>(() -> new SimpleBlockSpec(CommonProxy.HINT_X.getDefaultState()));

    @Override
    public void renderOverlay(ManipulatorRenderingContext context, Config config, Buildable buildable) {

    }

    @Override
    public void emitHints(ManipulatorRenderingContext context, Config config, Buildable buildable) {
        OptionalInt maxRange = context.getMaxRange();

        int i = 0;

        EntityPlayer player = context.getRealPlayer();

        World world = player.world;

        Vector3i playerPos = new Vector3i(
            MathHelper.floor(player.posX),
            MathHelper.floor(player.posY),
            MathHelper.floor(player.posZ));

        Map<BlockPos, BuildFeedback> feedbackMap = new HashMap<>();

        for (BuildFeedback f : context.getFeedback()) {
            feedbackMap.put(f.pos(), f);
        }

        AnalysisContextImpl analysisContext = new AnalysisContextImpl(context);

        for (PendingBlock pendingBlock : buildable.getPendingBlocks()) {
            if (!pendingBlock.isInWorld(world)) continue;

            if (maxRange.isPresent()) {
                int dist2 = pendingBlock.distanceTo2(playerPos);

                if (dist2 > maxRange.getAsInt() * maxRange.getAsInt()) continue;
            }

            BlockPos pos = pendingBlock.toPos();

            if (pendingBlock.spec == null || pendingBlock.spec.isAir() && world.isAirBlock(pos)) continue;

            if (++i > RenderingConfig.maxHints) break;

            ImmutableColor tint = WHITE;

            BuildFeedback feedback = feedbackMap.remove(pos);

            if (feedback != null) {
                tint = switch (feedback.severity()) {
                    case ERROR -> ERROR;
                    case WARNING -> WARNING;
                    case INFO -> INFO;
                };
            }

            BlockSpec sanitizedTarget = pendingBlock.spec.sanitized();

            analysisContext.setPos(pos);

            BlockSpec existing = MMRegistriesInternal.getPartialBlockSpec(analysisContext);

            if (existing == null) {
                MMHintRenderer.INSTANCE.addHint(
                    pendingBlock.x,
                    pendingBlock.y,
                    pendingBlock.z,
                    HINT_WARNING.get(),
                    ERROR);

                continue;
            }

            BlockSpec sanitizedExisting = existing.sanitized();

            if (sanitizedTarget.matches(sanitizedExisting)) continue;

            MMHintRenderer.INSTANCE.addHint(
                pendingBlock.x,
                pendingBlock.y,
                pendingBlock.z,
                sanitizedTarget.getBlockState().getBlock() == Blocks.AIR ? HINT_X.get() : sanitizedTarget,
                tint);
        }

        feedbackMap.forEach((pos, feedback) -> {
            ImmutableColor tint = WHITE;

            if (feedback != null) {
                tint = switch (feedback.severity()) {
                    case ERROR -> ERROR;
                    case WARNING -> WARNING;
                    case INFO -> INFO;
                };
            }

            MMHintRenderer.INSTANCE.addHint(pos.getX(), pos.getY(), pos.getZ(), HINT_WARNING.get(), tint);
        });
    }

    @Override
    public void reset(Config config, Buildable buildable) {

    }

    /// Used for detecting when the renderer changes, to invalidate the hints. Since a new render object is created each
    /// frame, we just need to make sure the two are the same class. Renderers shouldn't contain any state.
    ///
    /// @see MMRenderer#renderSelectionImpl(RenderWorldLastEvent)
    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass();
    }

    /// Not necessarily needed, but this is implemented to fulfill the contract of [#equals(Object)].
    @Override
    public int hashCode() {
        return this.getClass()
            .hashCode();
    }
}
