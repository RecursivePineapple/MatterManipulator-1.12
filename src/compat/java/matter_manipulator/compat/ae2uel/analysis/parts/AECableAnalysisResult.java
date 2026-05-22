package matter_manipulator.compat.ae2uel.analysis.parts;

import matter_manipulator.core.util.DirectionMap;

public class AECableAnalysisResult implements Cloneable {

    public DirectionMap<PartData> parts = new DirectionMap<>();
    public DirectionMap<FacadeData> facades = new DirectionMap<>();

    @Override
    public AECableAnalysisResult clone() {
        try {
            AECableAnalysisResult clone = (AECableAnalysisResult) super.clone();

            clone.parts = this.parts.copy(PartData::clone);
            clone.facades = this.facades.copy(FacadeData::clone);

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
