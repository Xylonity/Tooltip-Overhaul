package dev.xylonity.tooltipoverhaul.config;

import dev.xylonity.tooltipoverhaul.config.wrapper.AutoConfig;
import dev.xylonity.tooltipoverhaul.config.wrapper.ConfigEntry;

@AutoConfig(file = "tooltipoverhaul")
public final class TooltipsConfig {

    @ConfigEntry(
            comment = "Render a 3D preview of tiered items (swords, axes, etc.) on the left side of the tooltip."
    )
    public static boolean TIERED_ITEMS_RENDERER = true;

    @ConfigEntry(
            comment = "Render a 3D preview of armor pieces on the left side of the tooltip."
    )
    public static boolean ARMOR_ITEMS_RENDERER = true;

    @ConfigEntry(
            comment = "Second panel X offset (pixels). Used when a 3D preview is shown. Negative = left, positive = right."
    )
    public static int SECOND_PANEL_X = -5;

    @ConfigEntry(
            comment = "Second panel Y offset (pixels). Used when a 3D preview is shown. Negative = up, positive = down."
    )
    public static int SECOND_PANEL_Y = 0;

    @ConfigEntry(
            comment = "Rotation speed multiplier for the tiered item preview in the second panel (1.0 = default)."
    )
    public static float TIERED_ITEM_PREVIEW_ROTATING_SPEED = 1f;

    @ConfigEntry(
            comment = "Rotation speed multiplier for the armor preview in the second panel (1.0 = default)."
    )
    public static float ARMOR_PREVIEW_ROTATING_SPEED = 1f;

    @ConfigEntry(
            comment = "Default inner overlay style for tooltips. Valid: glint, solid (monochrome), gradient. Default: glint."
    )
    public static String DEFAULT_INNER_OVERLAY_TYPE = "glint";

    @ConfigEntry(
            comment = "Default tooltip background color in ARGB (0xAARRGGBB). For instance: 0xF0010110."
    )
    public static int DEFAULT_BACKGROUND_COLOR = 0xF0000000;

    @ConfigEntry(
            comment = "Override vanilla tooltips even when no ItemStack is present (e.g., JEI category buttons or unsupported stacks). "
                    + "To disable custom tooltips for specific items, edit your custom_frames.json and set disableTooltip=true for those "
                    + "items (or tags)."
    )
    public static boolean SHOW_TOOLTIP_WITHOUT_STACK = true;

    @ConfigEntry(
            comment = "Main panel padding X coordinate"
    )
    public static int MAIN_PANEL_PADDING_X = 4;

    @ConfigEntry(
            comment = "Main panel padding Y coordinate"
    )
    public static int MAIN_PANEL_PADDING_Y = 2;

    @ConfigEntry(
            comment = "Title alignment. left, middle or right"
    )
    public static String TITLE_X_ALIGNMENT = "left";

    @ConfigEntry(
            comment = "Rating alignment. left, middle or right"
    )
    public static String RATING_X_ALIGNMENT = "left";

    @ConfigEntry(
            comment = "Disable item rating text"
    )
    public static boolean DISABLE_RATING = false;

    @ConfigEntry(
            comment = "Disable icon"
    )
    public static boolean DISABLE_ICON = false;

    @ConfigEntry(
            comment = "Icon appear animation"
    )
    public static String ICON_APPEAR_ANIMATION = "skew";

    @ConfigEntry(
            comment = "Icon rotating speed"
    )
    public static float ICON_ROTATING_SPEED = 1.0f;

    @ConfigEntry(
            comment = "Common palette colors (from brigth to dark). You can only specify 3 exact colors. Remember this is a "
    )
    public static String COMMON_PALETTE_COLORS = "0xA0EFEFEF, 0xA08A8A8A, 0xA0606060";

    @ConfigEntry(
            comment = "Uncommon palette colors (from brigth to dark). You can only specify 3 exact colors. Remember this is a "
    )
    public static String UNCOMMON_PALETTE_COLORS = "0xA0F9FF40, 0xA0A9AD26, 0xA0787B16";

    @ConfigEntry(
            comment = "Rare palette colors (from brigth to dark). You can only specify 3 exact colors. Remember this is a "
    )
    public static String RARE_PALETTE_COLORS = "0xA05297FF, 0xA0285DAD, 0xA0102E5A";

    @ConfigEntry(
            comment = "Epic palette colors (from brigth to dark). You can only specify 3 exact colors. Remember this is a "
    )
    public static String EPIC_PALETTE_COLORS = "0xA0FF36D0, 0xA0A81E89, 0xA0600B4D";

    @ConfigEntry(
            comment = "Legendary palette colors (from brigth to dark). You can only specify 3 exact colors. Remember this is a "
    )
    public static String LEGENDARY_PALETTE_COLORS = "0xA0FFCC60, 0xA0CE9828, 0xA058400D";

    @ConfigEntry(
            comment = "Chaos palette colors (from brigth to dark). You can only specify 3 exact colors. Remember this is a "
    )
    public static String CHAOS_PALETTE_COLORS = "0xA0FD575C, 0xA0CE282B, 0xA0580D0E";

}
