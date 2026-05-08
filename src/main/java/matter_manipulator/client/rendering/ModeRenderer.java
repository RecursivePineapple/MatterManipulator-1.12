package matter_manipulator.client.rendering;

import matter_manipulator.core.building.Buildable;
import matter_manipulator.core.context.ManipulatorRenderingContext;

public interface ModeRenderer<TConfig, TBuildable extends Buildable> {

    void renderOverlay(ManipulatorRenderingContext context, TConfig config, TBuildable buildable);

    void emitHints(ManipulatorRenderingContext context, TConfig config, TBuildable buildable);

    void reset(TConfig config, TBuildable buildable);
}
