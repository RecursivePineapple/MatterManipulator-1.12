package matter_manipulator.core.building;

import matter_manipulator.core.context.HeldManipulatorContext;
import matter_manipulator.core.planning.BuildPlan;
import matter_manipulator.core.util.Coroutine;

/// A [Buildable] object that can (optionally) scan existing blocks and produce a list of required resources
public interface Plannable extends Buildable {

    /// Scans the contained instructions in this [Buildable] and produces a plan for them.
    /// @param skipExisting When true, any existing blocks will be skipped.
    Coroutine<BuildPlan> createPlan(HeldManipulatorContext context, boolean skipExisting);
}
