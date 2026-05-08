package matter_manipulator.compat.emc;

import matter_manipulator.compat.ModInteropPlugin;
import matter_manipulator.compat.emc.io.ArcaneTabletItemStackIOFactory;
import matter_manipulator.core.interop.MMRegistries;

public class ProjectEInteropPlugin implements ModInteropPlugin {

    @Override
    public void preInit() {
        MMRegistries.itemIOFactories()
            .addObject("backpack/arcane-tablet", new ArcaneTabletItemStackIOFactory(), "before:ae-terminal");
    }
}
