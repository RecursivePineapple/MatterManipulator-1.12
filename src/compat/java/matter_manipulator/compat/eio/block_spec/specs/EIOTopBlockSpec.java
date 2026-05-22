package matter_manipulator.compat.eio.block_spec.specs;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import crazypants.enderio.base.machine.base.block.BlockMachineExtension;
import matter_manipulator.common.block_spec.AbstractBlockSpec;
import matter_manipulator.common.block_spec.BlockSpecData;
import matter_manipulator.common.utils.world.ProxiedWorld;
import matter_manipulator.compat.eio.block_spec.adapters.EIOTopBlockSpecAdapter;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.block_spec.BlockSpecLoader;
import matter_manipulator.core.context.ManipulatorPlacingContext;
import matter_manipulator.core.context.PlacingContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.resources.ResourceIdentity;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemStackWrapper;

public class EIOTopBlockSpec extends AbstractBlockSpec {

    public IBlockState state;

    public EIOTopBlockSpec(IBlockState state) {
        this.state = state;
    }

    @Override
    public BlockSpecLoader getLoader() {
        return EIOTopBlockSpecAdapter.INSTANCE;
    }

    @Override
    public boolean isValid() {
        return state.getBlock() instanceof BlockMachineExtension;
    }

    @Override
    public IBlockState getBlockState() {
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public ResourceStack getResource() {
        return ItemStackWrapper.EMPTY;
    }

    @Override
    public @NotNull Localized getDisplayName() {
        return new Localized("mm+eio.spec.top");
    }

    @Override
    public boolean matches(BlockSpec other) {
        if (!(other instanceof EIOTopBlockSpec topSpec)) return false;

        return this.state == topSpec.state;
    }

    @Override
    public boolean canPlaceAt(ProxiedWorld world, BlockPos pos) {
        return true;
    }

    @Override
    public ApplyResult place(PlacingContext context) {
        return ApplyResult.DidNothing;
    }

    @Override
    public void getRequiredResourcesForUpdate(ManipulatorPlacingContext context, boolean skipExisting) {

    }

    @Override
    protected void resetResource() {

    }

    @Override
    public EIOTopBlockSpec clone() {
        return (EIOTopBlockSpec) super.clone();
    }

    @Override
    public EIOTopBlockSpec sanitized() {
        return this.clone();
    }

    @Contract(mutates = "this")
    @Override
    public @Nullable BlockSpecData exchange(ResourceIdentity stack, ResourceIdentity replacement) {
        return null;
    }
}
