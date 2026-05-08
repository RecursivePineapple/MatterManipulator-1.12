package matter_manipulator.common.interop.state_mutators;

import java.util.function.Predicate;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import org.apache.commons.lang3.mutable.MutableObject;

import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.interop.BlockStateMutator;

public class PropertyCopyStateMutator implements BlockStateMutator {

    private final Predicate<IProperty<?>> filter;
    private final Class<?> expectedType;

    public PropertyCopyStateMutator(String propertyName, Class<?> expectedType) {
        filter = p -> p.getName().equals(propertyName);
        this.expectedType = expectedType;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ApplyResult transform(MutableObject<IBlockState> state, IBlockState target) {
        IBlockState curr = state.getValue();

        IProperty prop = DataUtils.find(curr.getPropertyKeys(), filter);
        IProperty targetProp = DataUtils.find(target.getPropertyKeys(), filter);

        if (prop == null || prop.getValueClass() != expectedType) return ApplyResult.NotApplicable;
        if (targetProp == null || targetProp.getValueClass() != expectedType) return ApplyResult.NotApplicable;

        if (curr.getValue(prop) == target.getValue(targetProp)) return ApplyResult.DidNothing;

        state.setValue(curr.withProperty(prop, target.getValue(targetProp)));

        return ApplyResult.DidSomething;
    }
}
