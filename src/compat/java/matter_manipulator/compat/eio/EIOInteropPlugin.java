package matter_manipulator.compat.eio;

import matter_manipulator.compat.ModInteropPlugin;
import matter_manipulator.compat.eio.block_spec.adapters.EIOTopBlockSpecAdapter;
import matter_manipulator.core.interop.MMRegistries;

public class EIOInteropPlugin implements ModInteropPlugin {

    @Override
    public void preInit() {
        MMRegistries.blockSpecExtractors().addObject("eio-top", EIOTopBlockSpecAdapter.INSTANCE, "before:simple");
        MMRegistries.registerSpecLoader(EIOTopBlockSpecAdapter.INSTANCE);
    }
}
