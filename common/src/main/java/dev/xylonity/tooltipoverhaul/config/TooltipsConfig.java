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
            comment = "Rotation speed multiplier for the armor preview in the second panel (1.0 = default)."
    )
    public static float ARMOR_PREVIEW_ROTATING_SPEED = 1f;

    @ConfigEntry(
            comment = "Default inner overlay style for tooltips. Valid: glint, solid (monochrome), gradient. Default: glint."
    )
    public static String DEFAULT_INNER_OVERLAY_TYPE = "gradient";

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
            comment = "Title position X"
    )
    public static int TITLE_POSITION_X = 1;

    @ConfigEntry(
            comment = "Title position Y"
    )
    public static int TITLE_POSITION_Y = 0;

    @ConfigEntry(
            comment = "Rating position X"
    )
    public static int RATING_POSITION_X = 1;

    @ConfigEntry(
            comment = "Rating position Y"
    )
    public static int RATING_POSITION_Y = 0;

    @ConfigEntry(
            comment = "Tooltip description (the content of the tooltip, apart from the rating text and the name of the item) position X"
    )
    public static int TOOLTIP_DESCRIPTION_POSITION_X = 0;

    @ConfigEntry(
            comment = "Tooltip description (the content of the tooltip, apart from the rating text and the name of the item) position Y"
    )
    public static int TOOLTIP_DESCRIPTION_POSITION_Y = 0;

    @ConfigEntry(
            comment = "Show rating text"
    )
    public static boolean SHOW_RATING = true;

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
    public static float ICON_ROTATING_SPEED = 0f;

    @ConfigEntry(
            comment = "Icon rotating speed"
    )
    public static float ICON_SIZE = 1.35f;

    @ConfigEntry(
            comment = "Second panel renderer size"
    )
    public static float SECOND_PANEL_RENDERER_SIZE = 2.75f;


    @ConfigEntry(
            comment = "Rotation speed multiplier for the tiered item preview in the second panel (1.0 = default)"
    )
    public static float SECOND_PANEL_RENDERER_SPEED = 1f;

    @ConfigEntry(
            comment = "Divider line color. You can either specify 'match_inner_frame_color', 'match_item_name_color' or a color, such as '0xA0EFEFEF'"
    )
    public static String DIVIDER_LINE_COLOR = "match_inner_frame_color";

    @ConfigEntry(
            comment = "Common palette colors (from brigth to dark). You can only specify 3 exact colors"
    )
    public static String COMMON_PALETTE_COLORS = "0xFF969696, 0xFF575757, 0xFF3C3C3C";

    @ConfigEntry(
            comment = "Uncommon palette colors (from brigth to dark). You can only specify 3 exact colors"
    )
    public static String UNCOMMON_PALETTE_COLORS = "0xFF9CA028, 0xFF6A6D18, 0xFF4B4D0E";

    @ConfigEntry(
            comment = "Rare palette colors (from brigth to dark). You can only specify 3 exact colors"
    )
    public static String RARE_PALETTE_COLORS = "0xFF335FA0, 0xFF193A6D, 0xFF0A1D38";

    @ConfigEntry(
            comment = "Epic palette colors (from brigth to dark). You can only specify 3 exact colors"
    )
    public static String EPIC_PALETTE_COLORS = "0xFFA02283, 0xFF691356, 0xFF3C0730";

    @ConfigEntry(
            comment = "Legendary palette colors (from brigth to dark). You can only specify 3 exact colors"
    )
    public static String LEGENDARY_PALETTE_COLORS = "0xFFA0803C, 0xFF815F19, 0xFF372808";

    @ConfigEntry(
            comment = "Chaos palette colors (from brigth to dark). You can only specify 3 exact colors"
    )
    public static String CHAOS_PALETTE_COLORS = "0xFF9F373A, 0xFF81191B, 0xFF370809";

}
