package matter_manipulator.client.rendering.modes;

import matter_manipulator.client.rendering.MMRenderConstants;
import matter_manipulator.client.rendering.StandardModeRenderer;
import matter_manipulator.common.building.StandardBuild;
import matter_manipulator.common.modes.copying.CopyingConfig;
import matter_manipulator.common.utils.math.Location;
import matter_manipulator.core.color.ImmutableColor;
import matter_manipulator.core.context.RenderingContext;

public class CopyingModeRenderer extends StandardModeRenderer<CopyingConfig, StandardBuild> {

    @Override
    public void renderOverlay(RenderingContext context, CopyingConfig config, StandardBuild buildable) {
        super.renderOverlay(context, config, buildable);

        if (config.action != null) {
            ImmutableColor ruler = config.action.getRulerColor();

            if (ruler != null) {
                context.drawRulers(context.getLookedAtBlock().toPos(), ruler);
            }
        }

        if (Location.areCompatible(config.copyA, config.copyB)) {
            if (config.copyA.worldId == context.getWorld().provider.getDimension()) {
                var box = config.getCopyVisualDeltas();
                if (box != null) context.drawBox(box, MMRenderConstants.BLUE);
            }
        }

        if (Location.isInWorld(config.paste, context.getWorld())) {
            var box = config.getPasteVisualDeltas();
            if (box != null) context.drawBox(box, MMRenderConstants.ORANGE);
        }
    }
}
