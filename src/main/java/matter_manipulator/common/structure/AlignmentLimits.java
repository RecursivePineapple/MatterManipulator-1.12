package matter_manipulator.common.structure;

import net.minecraft.util.EnumFacing;

import com.github.bsideup.jabel.Desugar;
import matter_manipulator.common.utils.enums.Flip;
import matter_manipulator.common.utils.enums.Rotation;

@Desugar
record AlignmentLimits(boolean[] validStates) implements IAlignmentLimits {

    @Override
    public boolean isNewExtendedFacingValid(EnumFacing direction, Rotation rotation, Flip flip) {
        return validStates[Alignment.getAlignmentIndex(direction, rotation, flip)];
    }
}
