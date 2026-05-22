package matter_manipulator.core.building;

import matter_manipulator.core.context.ManipulatorPlacingContext;

/// Something that can be built.
public interface Buildable {

    void onBuildTick(ManipulatorPlacingContext context);
    void onStop(ManipulatorPlacingContext context);
    boolean isDone();
}
