package matter_manipulator.core.tooltip;

import java.util.List;

@FunctionalInterface
public interface TooltipSupplier {

    List<String> getTooltip();

    default void onResourcesReloaded() {

    }
}
