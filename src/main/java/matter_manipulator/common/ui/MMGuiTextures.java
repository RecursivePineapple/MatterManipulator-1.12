package matter_manipulator.common.ui;

import com.cleanroommc.modularui.drawable.UITexture;
import matter_manipulator.Tags;

public class MMGuiTextures {

    public static final UITexture BACKGROUND_STANDARD = UITexture.builder()
        .location(Tags.MODID, "gui/background/singleblock_default")
        .imageSize(176, 166)
        .adaptable(4)
        .canApplyTheme()
        .name(MMTextureIds.BACKGROUND_STANDARD)
        .build();
    public static final UITexture BACKGROUND_TEXT_FIELD = UITexture.builder()
        .location(Tags.MODID, "gui/background/text_field")
        .imageSize(142, 28)
        .adaptable(1)
        .name(MMTextureIds.BACKGROUND_TERMINAL_STANDARD)
        .build();

    public static final UITexture BACKGROUND_TITLE_STANDARD = UITexture.builder()
        .location(Tags.MODID, "gui/tab/title_dark")
        .imageSize(28, 28)
        .adaptable(4)
        .canApplyTheme()
        .name(MMTextureIds.BACKGROUND_TITLE_STANDARD)
        .build();

    public static final UITexture SLOT_ITEM_STANDARD = UITexture.builder()
        .location(Tags.MODID, "gui/slot/item_standard")
        .imageSize(18, 18)
        .adaptable(1)
        .canApplyTheme()
        .name(MMTextureIds.SLOT_ITEM_STANDARD)
        .build();

    public static final UITexture BUTTON_STANDARD = UITexture.builder()
        .location(Tags.MODID, "gui/button/standard")
        .imageSize(18, 18)
        .adaptable(1)
        .canApplyTheme()
        .name(MMTextureIds.BUTTON_STANDARD)
        .build();
    public static final UITexture BUTTON_STANDARD_PRESSED = UITexture.builder()
        .location(Tags.MODID, "gui/button/standard_pressed")
        .imageSize(18, 18)
        .adaptable(1)
        .canApplyTheme()
        .name(MMTextureIds.BUTTON_STANDARD_PRESSED)
        .build();
}
