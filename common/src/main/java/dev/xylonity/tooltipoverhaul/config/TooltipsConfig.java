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
            comment = "Apply a custom frame overlay texture to every item. This option is overrided if the hovered itemstack has " +
                    "a custom frame. Write down the texture location (such as tooltipoverhaul:textures/overlay/silver_frame.png) or " +
                    "leave it empty. No frame overlays are applied globally by default."
    )
    public static String GLOBAL_FRAME_OVERLAY_LOCATION = "";

    @ConfigEntry(
            comment = "Disable tooltip scrolling"
    )
    public static boolean DISABLE_TOOLTIP_SCROLLING = false;

    @ConfigEntry(
            comment = "Disable the divider line"
    )
    public static boolean DISABLE_DIVIDER_LINE = false;

    @ConfigEntry(
            comment = "Horizontal offset (in pixels) for the second panel when a 3D preview is shown. Negative = left, positive = right."
    )
    public static int SECOND_PANEL_X = 0;

    @ConfigEntry(
            comment = "Vertical offset (in pixels) for the second panel when a 3D preview is shown. Negative = up, positive = down."
    )
    public static int SECOND_PANEL_Y = 0;

    @ConfigEntry(
            comment = "Horizontal offset (in pixels) for the tooltip. Negative = left, positive = right."
    )
    public static int TOOLTIP_POSITION_X = 0;

    @ConfigEntry(
            comment = "Vertical offset (in pixels) for the tooltip. Negative = up, positive = down."
    )
    public static int TOOLTIP_POSITION_Y = 0;

    @ConfigEntry(
            comment = "Default inner overlay style for tooltips. Options: glint, solid (monochrome), gradient."
    )
    public static String DEFAULT_INNER_OVERLAY_TYPE = "gradient";

    @ConfigEntry(
            comment = "Default tooltip background color in ARGB format (0xAARRGGBB). Example: 0xF0010110."
    )
    public static int DEFAULT_BACKGROUND_COLOR = 0xF0000000;

    @ConfigEntry(
            comment = "Override vanilla tooltips even when no ItemStack is present (e.g., JEI category buttons or unsupported stacks). "
                    + "To disable custom tooltips for specific items, edit your custom_frames.json and set disableTooltip=true for those "
                    + "items (or tags)."
    )
    public static boolean SHOW_TOOLTIP_WITHOUT_STACK = true;

    @ConfigEntry(
            comment = "Horizontal padding for the main panel."
    )
    public static int MAIN_PANEL_PADDING_X = 2;

    @ConfigEntry(
            comment = "Vertical padding for the main panel."
    )
    public static int MAIN_PANEL_PADDING_Y = 2;

    @ConfigEntry(
            comment = "Horizontal padding for the main panel when there isn't a stack present."
    )
    public static int NO_STACK_TOOLTIP_PADDING_X = 1;

    @ConfigEntry(
            comment = "Vertical padding for the main panel when there isn't a stack present."
    )
    public static int NO_STACK_TOOLTIP_PADDING_Y = 1;

    @ConfigEntry(
            comment = "Title alignment. Options: left, middle, right."
    )
    public static String TITLE_X_ALIGNMENT = "left";

    @ConfigEntry(
            comment = "Rating alignment. Options: left, middle, right."
    )
    public static String RATING_X_ALIGNMENT = "left";

    @ConfigEntry(
            comment = "Icon background type. Options: focus, void, slot, slot_border and glow"
    )
    public static String ICON_BACKGROUND_TYPE = "slot_border";

    @ConfigEntry(
            comment = "Horizontal position offset for the title."
    )
    public static int TITLE_POSITION_X = 1;

    @ConfigEntry(
            comment = "Vertical position offset for the title."
    )
    public static int TITLE_POSITION_Y = 0;

    @ConfigEntry(
            comment = "Horizontal position offset for the rating text."
    )
    public static int RATING_POSITION_X = 1;

    @ConfigEntry(
            comment = "Vertical position offset for the rating text."
    )
    public static int RATING_POSITION_Y = 0;

    @ConfigEntry(
            comment = "Horizontal position offset for the tooltip description (the main content text)."
    )
    public static int TOOLTIP_DESCRIPTION_POSITION_X = 0;

    @ConfigEntry(
            comment = "Vertical position offset for the tooltip description (the main content text)."
    )
    public static int TOOLTIP_DESCRIPTION_POSITION_Y = 0;

    @ConfigEntry(
            comment = "Show rating text."
    )
    public static boolean SHOW_RATING = false;

    @ConfigEntry(
            comment = "Disable the item icon."
    )
    public static boolean DISABLE_ICON = false;

    @ConfigEntry(
            comment = "Enable the tooltip shadow."
    )
    public static boolean SHOW_TOOLTIP_SHADOW = false;

    @ConfigEntry(
            comment = "Animation style for the icon appearance."
    )
    public static String ICON_APPEAR_ANIMATION = "skew";

    @ConfigEntry(
            comment = "Rotation speed of the icon."
    )
    public static float ICON_ROTATING_SPEED = 0f;

    @ConfigEntry(
            comment = "Size of the icon."
    )
    public static float ICON_SIZE = 1.35f;

    @ConfigEntry(
            comment = "Size of the second panel renderer."
    )
    public static float SECOND_PANEL_RENDERER_SIZE = 2.75f;

    @ConfigEntry(
            comment = "Rotation speed multiplier for the tiered item preview in the second panel."
    )
    public static float SECOND_PANEL_RENDERER_SPEED = 1f;

    @ConfigEntry(
            comment = "Divider line color. Options: 'match_inner_frame_color', 'match_item_name_color' or a hex ARGB color (e.g., 0xA0EFEFEF)."
    )
    public static String DIVIDER_LINE_COLOR = "match_inner_frame_color";

    @ConfigEntry(
            comment = "Color palette for tooltips without a stack. Must specify exactly 3 ARGB colors."
    )
    public static String NO_STACK_PALETTE_COLORS = "0xFF969696, 0xFF575757, 0xFF3C3C3C";

    @ConfigEntry(
            comment = "Color palette for COMMON rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String COMMON_PALETTE_COLORS = "0xFF969696, 0xFF575757, 0xFF3C3C3C";

    @ConfigEntry(
            comment = "Color palette for UNCOMMON rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String UNCOMMON_PALETTE_COLORS = "0xFF9CA028, 0xFF6A6D18, 0xFF4B4D0E";

    @ConfigEntry(
            comment = "Color palette for RARE rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String RARE_PALETTE_COLORS = "0xFF335FA0, 0xFF193A6D, 0xFF0A1D38";

    @ConfigEntry(
            comment = "Color palette for EPIC rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String EPIC_PALETTE_COLORS = "0xFFA02283, 0xFF691356, 0xFF3C0730";

    @ConfigEntry(
            comment = "Color palette for LEGENDARY rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String LEGENDARY_PALETTE_COLORS = "0xFFA0803C, 0xFF815F19, 0xFF372808";

    @ConfigEntry(
            comment = "Color palette for CHAOS rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String CHAOS_PALETTE_COLORS = "0xFF9F373A, 0xFF81191B, 0xFF370809";

}