package matter_manipulator.compat.probe;

import matter_manipulator.compat.ModInteropPlugin;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;

public class TOPInteropPlugin implements ModInteropPlugin {

    @Override
    public void preInit() {
        ITheOneProbe oneProbe = TheOneProbe.theOneProbeImp;
        oneProbe.registerProvider(new UplinkModuleInfoProvider());
    }
}
