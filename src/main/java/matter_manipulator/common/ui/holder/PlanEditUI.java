package matter_manipulator.common.ui.holder;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import matter_manipulator.common.ui.factory.PlanEditUIFactory.PlanEditGuiData;
import matter_manipulator.common.uplink.Uplink;
import matter_manipulator.common.uplink.UplinkPlanReceiver;
import matter_manipulator.core.context.HeldManipulatorContext;
import matter_manipulator.core.manipulator_state.UplinkStateLoader;

public class PlanEditUI implements IGuiHolder<PlanEditGuiData> {

    private final HeldManipulatorContext context;

    public PlanEditUI(HeldManipulatorContext context) {
        this.context = context;
    }

    @Override
    public ModularPanel buildUI(PlanEditGuiData data, PanelSyncManager syncManager, UISettings settings) {
        settings.canInteractWith(player -> context.getState()
            .getState(context, UplinkStateLoader.INSTANCE)
            .hasPlanReceiver());

        ModularPanel panel = new ModularPanel("ManipulatorPlanEdit");

        panel.size(200, 400);

        var content = Flow.col();

        Uplink uplink = context.getState().getState(context, UplinkStateLoader.INSTANCE).getUplink();

        if (uplink != null) {
            UplinkPlanReceiver planReceiver = uplink.getPlanReceiver();

            if (planReceiver != null) {
                for (var plan : planReceiver.getPlans()) {
                    var row = Flow.row();
                    content.child(row);

                    row.child(IKey.str((plan.autoSubmit ? "[A] " : "[M] ") + plan.name).asWidget().expanded());

                    row.child(new ButtonWidget<>().widgetTheme(IThemeApi.CLOSE_BUTTON)
                        .overlay(GuiTextures.CROSS_TINY)
                        .onMousePressed(mouseButton -> {
                            if (mouseButton == 0 || mouseButton == 1) {
                                planReceiver.deletePlan(plan);
                                row.setEnabled(false);
                                return true;
                            }
                            return false;
                        }));
                }
            }
        }

        panel.child(Flow.column().child(IKey.str("Edit Plans").asWidget()).child(new ScrollWidget<>().expanded().child(content)));

        return panel;
    }
}
