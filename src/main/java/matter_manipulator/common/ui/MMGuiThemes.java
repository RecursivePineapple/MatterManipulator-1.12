package matter_manipulator.common.ui;

import matter_manipulator.client.rendering.MMRenderConstants;

public class MMGuiThemes {

    public static void init() {}

    public static final MMGuiTheme STANDARD = MMGuiTheme.builder("gregtech:standard")
        .panel(MMTextureIds.BACKGROUND_STANDARD)
        .itemSlot(MMTextureIds.SLOT_ITEM_STANDARD)
        .fluidSlot(MMTextureIds.SLOT_FLUID_STANDARD)
        .color(MMRenderConstants.GUI_METAL.toIntRGB())
        .textColor(0x404040)
        .textField(0xffffff)
        .customTextColor(MMWidgetThemes.TEXT_TITLE.getFullName(), 0x404040)
        .customTextColor(MMWidgetThemes.DISPLAY_TEXT.getFullName(), 0xFAFAFA)
        .button(MMTextureIds.BUTTON_STANDARD)
        .simpleToggleButton(MMTextureIds.BUTTON_STANDARD, MMTextureIds.BUTTON_STANDARD_PRESSED, MMRenderConstants.GUI_METAL.toIntRGB())
        .themedTexture(MMWidgetThemes.BACKGROUND_POPUP.getFullName(), MMTextureIds.BACKGROUND_POPUP_STANDARD)
        .themedTexture(MMWidgetThemes.BACKGROUND_TITLE.getFullName(), MMTextureIds.BACKGROUND_TITLE_STANDARD)
        .themedOverlayItemSlot(MMWidgetThemes.OVERLAY_ITEM_SLOT_IN.getFullName(), MMTextureIds.OVERLAY_SLOT_IN_STANDARD)
        .themedOverlayItemSlot(MMWidgetThemes.OVERLAY_ITEM_SLOT_OUT.getFullName(), MMTextureIds.OVERLAY_SLOT_OUT_STANDARD)
        .themedOverlayFluidSlot(MMWidgetThemes.OVERLAY_FLUID_SLOT_IN.getFullName(), MMTextureIds.OVERLAY_SLOT_IN_STANDARD)
        .build();
}
