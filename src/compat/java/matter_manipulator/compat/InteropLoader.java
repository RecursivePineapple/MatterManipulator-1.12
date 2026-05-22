package matter_manipulator.compat;

import java.util.ArrayList;
import java.util.List;

import matter_manipulator.MatterManipulator;
import matter_manipulator.compat.ae2uel.AE2UELInteropPlugin;
import matter_manipulator.compat.eio.EIOInteropPlugin;
import matter_manipulator.compat.emc.ProjectEInteropPlugin;
import matter_manipulator.compat.probe.TOPInteropPlugin;
import matter_manipulator.core.misc.Reflected;

public class InteropLoader {

    private static final List<ModInteropPlugin> PLUGINS = new ArrayList<>();

    @Reflected
    public static void preInit() {
        gatherPlugins();

        for (var plugin : PLUGINS) {
            plugin.preInit();
        }
    }

    @Reflected
    public static void init() {
        for (var plugin : PLUGINS) {
            plugin.init();
        }
    }

    @Reflected
    public static void postInit() {
        for (var plugin : PLUGINS) {
            plugin.postInit();
        }
    }

    private static void gatherPlugins() {
        if (Mods.AppliedEnergistics2.isModLoaded()) {
            PLUGINS.add(new AE2UELInteropPlugin());
        }

        if (Mods.EnderIO.isModLoaded()) {
            PLUGINS.add(new EIOInteropPlugin());
        }

        if (Mods.ProjectEX.isModLoaded()) {
            PLUGINS.add(new ProjectEInteropPlugin());
        }

        if (Mods.TheOneProbe.isModLoaded()) {
            PLUGINS.add(new TOPInteropPlugin());
        }

        MatterManipulator.LOG.info("Loaded Interop Plugins: {}", PLUGINS);
    }
}
