package matter_manipulator.common.block_spec;

import java.util.Collections;
import java.util.Map;

import net.minecraft.block.state.IBlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Builder;
import matter_manipulator.core.interop.InteropModule;
import matter_manipulator.core.meta.MetaMap;
import matter_manipulator.core.resources.ResourceStack;

@Builder
@SuppressWarnings("rawtypes")
public class BlockSpecData {

    /// The source of truth for what this spec represents. Takes priority over [#state].
    @NotNull
    public ResourceStack stack;
    /// The previous block state. Used for keeping properties such as `facing`, etc. This likely isn't the same object as [#stack].
    @NotNull
    public IBlockState state;
    /// Any interop data from the previous [BlockSpec].
    @Nullable
    @Builder.Default
    public Map<InteropModule, Object> interopData = Collections.emptyMap();
    /// A scratchpad for arbitrary inter-spec data, in case two or more third party specs need to communicate.
    @NotNull
    @Builder.Default
    public MetaMap metadata = new MetaMap();
}
