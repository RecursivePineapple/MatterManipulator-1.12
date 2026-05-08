package matter_manipulator.core.building;

import java.util.List;

/// A [Buildable] that builds a list of [PendingBlock]s.
public interface PendingBlockBuildable extends Buildable {

    List<PendingBlock> getPendingBlocks();

}
