package matter_manipulator.common.structure;

import matter_manipulator.common.structure.coords.Position;
import matter_manipulator.common.structure.coords.WorldCoords;
import matter_manipulator.core.context.StructureContext;

@FunctionalInterface
public interface StructureWalker<T> {

    boolean step(StructureContext<? extends T> context, Position<WorldCoords> pos, StructureElement<? super T> element);

}
