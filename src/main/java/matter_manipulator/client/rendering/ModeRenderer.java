package matter_manipulator.client.rendering;

import matter_manipulator.core.building.Buildable;
import matter_manipulator.core.context.RenderingContext;

public interface ModeRenderer<TConfig, TBuildable extends Buildable> {

    void renderOverlay(RenderingContext context, TConfig config, TBuildable buildable);

    void emitHints(RenderingContext context, TConfig config, TBuildable buildable);

    void reset(TConfig config, TBuildable buildable);
}
