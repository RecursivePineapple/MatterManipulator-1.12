package matter_manipulator.client.rendering.modes;

import org.joml.Vector3i;

import matter_manipulator.client.rendering.MMRenderConstants;
import matter_manipulator.client.rendering.StandardModeRenderer;
import matter_manipulator.common.building.StandardBuild;
import matter_manipulator.common.modes.exchanging.ExchangeConfig;
import matter_manipulator.common.utils.MathUtils;
import matter_manipulator.common.utils.math.Location;
import matter_manipulator.common.utils.math.VoxelAABB;
import matter_manipulator.core.color.ImmutableColor;
import matter_manipulator.core.context.RenderingContext;

public class ExchangeModeRenderer extends StandardModeRenderer<ExchangeConfig, StandardBuild> {

    @Override
    public void renderOverlay(RenderingContext context, ExchangeConfig config, StandardBuild buildable) {
        super.renderOverlay(context, config, buildable);

        if (config.action != null) {
            ImmutableColor ruler = config.action.getRulerColor();

            if (ruler != null) {
                context.drawRulers(context.getLookedAtBlock().toPos(), ruler);
            }
        }

        if (!Location.areCompatible(config.a, config.b) || !config.a.isInWorld(context.getWorld())) {
            context.clearHints();
            return;
        }

        Vector3i a = MathUtils.copy(config.a);
        Vector3i b = MathUtils.copy(config.b);

        VoxelAABB box = new VoxelAABB(a, b);

        context.drawBox(box, MMRenderConstants.BLUE);
    }
}
