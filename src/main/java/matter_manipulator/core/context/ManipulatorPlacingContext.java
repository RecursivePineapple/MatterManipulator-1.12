package matter_manipulator.core.context;

import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

import matter_manipulator.core.block_spec.BlockSpec;

public interface ManipulatorPlacingContext extends PlacingContext, HeldManipulatorContext {

    default void playSound(SoundEvent sound) {
        playSound(getPos(), sound);
    }

    /// Queues a sound to be played at a specific spot.
    /// This mechanism finds the centre point for all played sounds of the same type and makes a single sound event so
    /// that several aren't played in the same tick.
    void playSound(BlockPos pos, SoundEvent sound);

    void setTarget(BlockPos pos, BlockSpec spec);

    BlockSpec getSpec();

    boolean drainEnergy(double multiplier);
    boolean drainEnergy(BlockPos pos, double multiplier);

    /// Removes the block at the current target (see [#setTarget(BlockPos, BlockSpec)]).
    void removeBlock();
}
