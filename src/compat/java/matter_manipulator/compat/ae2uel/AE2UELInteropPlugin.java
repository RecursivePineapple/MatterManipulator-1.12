package matter_manipulator.compat.ae2uel;

import net.minecraftforge.fml.common.registry.GameRegistry;

import matter_manipulator.compat.ModInteropPlugin;
import matter_manipulator.core.interop.MMRegistries;
import matter_manipulator.core.item.FastImmutableItemStack;
import matter_manipulator.compat.ae2uel.analysis.machine.AEMachineInteropModule;
import matter_manipulator.compat.ae2uel.analysis.parts.AECableBusInteropModule;
import matter_manipulator.compat.ae2uel.io.WirelessTerminalItemStackIOFactory;

public class AE2UELInteropPlugin implements ModInteropPlugin {

    @Override
    public void init() {
        MMRegistries.itemIOFactories()
            .addObject("backpack/ae-terminal", new WirelessTerminalItemStackIOFactory());

        MMRegistries.interop()
            .addObject("block-identity/ae-cable-bus", new AECableBusInteropModule());
        MMRegistries.interop()
            .addObject("block-identity/ae-machine", new AEMachineInteropModule());
    }

    @Override
    public void postInit() {
        MMRegistries.addFreeItem(new FastImmutableItemStack(GameRegistry.makeItemStack(
            "appliedenergistics2:cable_bus",
            0,
            1,
            null)));
    }
}
