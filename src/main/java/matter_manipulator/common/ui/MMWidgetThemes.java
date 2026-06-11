package matter_manipulator.common.ui;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.theme.SlotTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeKey;
import com.cleanroommc.modularui.utils.Color;

public class MMWidgetThemes {

    private static final IThemeApi themeApi = IThemeApi.get();

    public static WidgetThemeKey<WidgetTheme> TEXT_TITLE = themeApi
        .widgetThemeKeyBuilder("textTitle", WidgetTheme.class)
        .defaultTheme(new WidgetTheme(0, 0, null, Color.WHITE.main, 0x404040, false, 0))
        .defaultHoverTheme(null)
        .register();

    // Use for plain/unlocalized display strings and dynamic values (numbers, status text, mode text)
    public static WidgetThemeKey<WidgetTheme> DISPLAY_TEXT = themeApi
        .widgetThemeKeyBuilder("displayText", WidgetTheme.class)
        .defaultTheme(new WidgetTheme(0, 0, null, Color.WHITE.main, 0xFAFAFA, false, 0))
        .defaultHoverTheme(null)
        .register();

    public static WidgetThemeKey<WidgetTheme> BACKGROUND_POPUP = registerThemedTexture("backgroundPopup");
    public static WidgetThemeKey<WidgetTheme> BACKGROUND_TITLE = registerThemedTexture("backgroundTitle");
    public static WidgetThemeKey<WidgetTheme> BACKGROUND_TERMINAL = themeApi
        .widgetThemeKeyBuilder("backgroundTerminal", WidgetTheme.class)
        .defaultTheme(new WidgetTheme(0, 0, MMGuiTextures.BACKGROUND_TEXT_FIELD, Color.WHITE.main, 0xFAFAFA, false, 0))
        .defaultHoverTheme(null)
        .register();

    public static WidgetThemeKey<SlotTheme> OVERLAY_ITEM_SLOT_IN = registerThemedItemSlot("overlayItemSlotIn");
    public static WidgetThemeKey<SlotTheme> OVERLAY_ITEM_SLOT_OUT = registerThemedItemSlot("overlayItemSlotOut");
    public static WidgetThemeKey<SlotTheme> OVERLAY_FLUID_SLOT_IN = registerThemedFluidSlot("overlayFluidSlotIn");

    private static WidgetThemeKey<WidgetTheme> registerThemedTexture(String textureThemeId) {
        return themeApi.widgetThemeKeyBuilder(textureThemeId, WidgetTheme.class)
            .defaultTheme(new WidgetTheme(0, 0, null, Color.WHITE.main, 0xFF404040, false, 0))
            .defaultHoverTheme(null)
            .register();
    }

    private static WidgetThemeKey<SlotTheme> registerThemedItemSlot(String textureThemeId) {
        return themeApi.widgetThemeKeyBuilder(textureThemeId, SlotTheme.class)
            .defaultTheme(new SlotTheme(GuiTextures.SLOT_ITEM, Color.withAlpha(Color.WHITE.main, 0x60)))
            .defaultHoverTheme(null)
            .register();
    }

    private static WidgetThemeKey<SlotTheme> registerThemedFluidSlot(String textureThemeId) {
        return themeApi.widgetThemeKeyBuilder(textureThemeId, SlotTheme.class)
            .defaultTheme(new SlotTheme(GuiTextures.SLOT_FLUID, Color.withAlpha(Color.WHITE.main, 0x60)))
            .defaultHoverTheme(null)
            .register();
    }

    private static WidgetThemeKey<WidgetTheme> registerThemedButton(String textureThemeId) {
        return themeApi.widgetThemeKeyBuilder(textureThemeId, WidgetTheme.class)
            .defaultTheme(new WidgetTheme(0, 0, null, Color.WHITE.main, 0xFF404040, false, 0))
            .defaultHoverTheme(null)
            .register();
    }
}
