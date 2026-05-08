package matter_manipulator.common.modes.geometry.shapes;

import matter_manipulator.core.block_spec.BlockSpec;

public interface GeometryBlockPalette {

    BlockSpec corners();
    BlockSpec edges();
    BlockSpec faces();
    BlockSpec volumes();

}
