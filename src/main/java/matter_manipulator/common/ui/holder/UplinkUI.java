package matter_manipulator.common.ui.holder;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.SingleChildWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import matter_manipulator.common.ui.MMWidgetThemes;
import matter_manipulator.common.uplink.TileUplinkController;

public class UplinkUI implements IGuiHolder<PosGuiData> {

    private final TileUplinkController controller;

    public UplinkUI(TileUplinkController controller) {
        this.controller = controller;
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ModularPanel panel = new ModularPanel("Uplink");

        panel.size(198, 203).themeOverride("gregtech:standard");

        var inv = controller.getInventory();

        panel.child(Flow.col().child(Flow.row().child(new ItemSlot().slot(inv, 0)).child(new ItemSlot().slot(inv, 1))));

        if (syncManager.isClient()) {
            panel.child(createMachineTitle(controller.getBlockType().getLocalizedName(), 198));
        }

        panel.bindPlayerInventory();

        return panel;
    }

    @SideOnly(Side.CLIENT)
    public static Widget<?> createMachineTitle(String title, int panelWidth) {
        int borderRadius = 5;
        int maxWidth = panelWidth - borderRadius * 2;

        int titleWidth = TextRenderer.getFontRenderer().getStringWidth(title);
        int widgetWidth = Math.min(maxWidth, titleWidth);

        int rows = (int) Math.ceil((double) titleWidth / maxWidth);
        int heightPerRow = TextRenderer.getFontRenderer().FONT_HEIGHT;
        int height = heightPerRow * rows;

        return new SingleChildWidget<>().coverChildren()
            .topRelAnchor(0, 1)
            .widgetTheme(MMWidgetThemes.BACKGROUND_TITLE)
            .child(IKey.str(title)
                .asWidget()
                .size(widgetWidth, height)
                .widgetTheme(MMWidgetThemes.TEXT_TITLE)
                .marginLeft(5)
                .marginRight(5)
                .marginTop(5)
                .marginBottom(1));
    }
}
