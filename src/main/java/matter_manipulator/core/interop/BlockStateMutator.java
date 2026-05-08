package matter_manipulator.core.interop;

import net.minecraft.block.state.IBlockState;

import org.apache.commons.lang3.mutable.MutableObject;

import matter_manipulator.core.block_spec.ApplyResult;

/// A block state mutator is an object that can update one [IBlockState] to match another [IBlockState].
/// This is used for showing accurate previews, and for updating the default state of blocks to match the requested
/// state (without changing a property that should not be changed).
/// This is useful for 'config' properties, like `facing`, `powered`, etc.
public interface BlockStateMutator {

    /// Modifies `state` to match `target` as best as possible. This should not spawn in items or change block
    /// materials, it should only update things like `facing` properties.
    ApplyResult transform(MutableObject<IBlockState> state, IBlockState target);

}
