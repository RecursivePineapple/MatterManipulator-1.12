package matter_manipulator.core.context;

import net.minecraft.world.World;

import matter_manipulator.common.structure.IStructureDefinition;
import matter_manipulator.common.structure.coords.ControllerRelativeCoords;
import matter_manipulator.common.structure.coords.Offset;
import matter_manipulator.common.structure.coords.StructureRelativeCoords;
import matter_manipulator.common.utils.enums.ExtendedFacing;

public interface StructureContext<T> {

    /// Arbitrary user data (usually the invoking tile entity).
    T getData();

    IStructureDefinition<? super T> getStructureDefinition();

    World getWorld();

    ExtendedFacing getOrientation();

    ControllerRelativeCoords getControllerPos();

    String getPartName();
    Offset<StructureRelativeCoords> getPartOffset();
}
