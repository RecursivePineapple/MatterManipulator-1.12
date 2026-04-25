package matter_manipulator.client.rendering.models;

import net.minecraftforge.common.property.IUnlistedProperty;

import matter_manipulator.common.utils.enums.ExtendedFacing;

public class MachineModelProperty {

    public static final IUnlistedProperty<ExtendedFacing> EXTENDED_FACING = new IUnlistedProperty<>() {

        @Override
        public String getName() {
            return "extended-facing";
        }

        @Override
        public boolean isValid(ExtendedFacing value) {
            return true;
        }

        @Override
        public Class<ExtendedFacing> getType() {
            return ExtendedFacing.class;
        }

        @Override
        public String valueToString(ExtendedFacing value) {
            return value.name().toLowerCase();
        }
    };
}
