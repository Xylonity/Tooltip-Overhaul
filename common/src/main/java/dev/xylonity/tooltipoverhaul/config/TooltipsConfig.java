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
            comment = "Size (in pixels) of the second panel in the X axis when a 3D preview is shown."
    )
    public static int SECOND_PANEL_SIZE_X = 30;

    @ConfigEntry(
            comment = "Size (in pixels) of the second panel in the Y axis when a 3D preview is shown."
    )
    public static int SECOND_PANEL_SIZE_Y = 60;

    @ConfigEntry(
            comment = "Automatically reposition the preview panel (second panel) to the right side when there " +
                    "is insufficient space on the left. If set to false, the panel will always remain on the left."
    )
    public static boolean AUTO_REPOSITION_PREVIEW_PANEL = true;

    @ConfigEntry(
            comment = "Bedrock-like centering. When the tooltip fits neither to the right nor to the left of the cursor, " +
                    "centers it horizontally on the screen and places it above the cursor (or below if there is no room " +
                    "above), so it doesn't cover the hovered item."
    )
    public static boolean BEDROCK_CENTERING = false;

    @ConfigEntry(
            comment = "Horizontal offset (in pixels) for the tooltip. Negative = left, positive = right."
    )
    public static int TOOLTIP_POSITION_X = 0;

    @ConfigEntry(
            comment = "Vertical offset (in pixels) for the tooltip. Negative = up, positive = down."
    )
    public static int TOOLTIP_POSITION_Y = 0;

    @ConfigEntry(
            comment = "Extra padding above the divider line"
    )
    public static int DIVIDER_LINE_TOP_PADDING = 0;

    @ConfigEntry(
            comment = "Extra padding below the divider line"
    )
    public static int DIVIDER_LINE_BOTTOM_PADDING = 0;

    @ConfigEntry(
            comment = "Default inner overlay style for tooltips. Options: glint, static (monochrome), gradient."
    )
    public static String DEFAULT_INNER_OVERLAY_TYPE = "gradient";

    @ConfigEntry(
            comment = "Corner style for the inner frame of the tooltip. Options: default (square), rounded (corner " +
                    "pixel removed), bevel (45 degree diagonal cut), inner (extra pixel in the inner corner), cut (2px " +
                    "chamfer), thick (solid triangular corner, top-right only), bracket (inner corner bracket), " +
                    "block (2x2 solid block corner), notch (rectangular inner offset), weld (reinforced inner " +
                    "corner), gem (hollow triangle). The notch style pairs with the matching notch background corner style."
    )
    public static String INNER_FRAME_CORNER_TYPE = "default";

    @ConfigEntry(
            comment = "Corner style for the outer background border of the tooltip. Options: default (vanilla notch), " +
                    "square (sharp 90 degree), rounded, notch (rectangular inner offset, matches the " +
                    "inner frame notch style)."
    )
    public static String BACKGROUND_CORNER_TYPE = "default";

    @ConfigEntry(
            comment = "Default tooltip background color in ARGB format (#AARRGGBB). Example: #F0010110."
    )
    public static String DEFAULT_BACKGROUND_COLOR = "#F0000000";

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
            comment = "Effects. Options: cinder, echo, galaxy, magic_orbs, speed_lines, nebula, spiral, white_dust, metal_shining, rim_light, ripples, sonar, stars. You can chain effects together, for example: 'white_dust, nebula'"
    )
    public static String EFFECTS = "";

    @ConfigEntry(
            comment = "Icon background type. Options: focus, void, slot, slot_border and glow"
    )
    public static String ICON_BACKGROUND_TYPE = "slot_border";

    @ConfigEntry(
            comment = "Show rating text."
    )
    public static boolean SHOW_RATING = false;

    @ConfigEntry(
            comment = "Appends the display name of the mod that adds the hovered item as the last tooltip line " +
                    "(blue italic), always visible in both survival and creative (previously visible only in creative mode)."
    )
    public static boolean SHOW_MOD_NAME = false;

    @ConfigEntry(
            comment = "Disable the item icon."
    )
    public static boolean DISABLE_ICON = false;

    @ConfigEntry(
            comment = "Enable the tooltip shadow."
    )
    public static boolean SHOW_TOOLTIP_SHADOW = false;

    @ConfigEntry(
            comment = "Vignette entries for gradient overlays around the tooltip background. " +
                    "Each vignette must be written in parentheses in this exact order: " +
                    "(type, position, color, radius, extraPositionX, extraPositionY). " +
                    "Example: (circular, top_left, #FF567823, 0.4, 0, 0). " +
                    "You can define multiple vignettes by separating them with commas: " +
                    "(...), (...), (...). " +
                    "type = vignette shape (options: circular, hole). position = anchor on the tooltip " +
                    "(e.g. top_left, top_right, middle, right, bottom_left). color = ARGB hex in #AARRGGBB. " +
                    "radius = relative size factor (e.g. radius 0.4). extraPositionX / extraPositionY = " +
                    "additional pixel offset from the chosen position."
    )
    public static String VIGNETTES = "";

    @ConfigEntry(
            comment = "Animation style for the icon appearance."
    )
    public static String ICON_APPEAR_ANIMATION = "skew";

    @ConfigEntry(
            comment = "Animation played when a tooltip appears (on hover) and disappears. Options: none, fade, pop, " +
                    "rise, unfold, zoom, slide, swing, emerge, squash, card."
    )
    public static String TOOLTIP_APPEAR_ANIMATION = "pop";

    @ConfigEntry(
            comment = "Duration (in seconds) of the tooltip appear/disappear animation. Lower values are snappier."
    )
    public static float TOOLTIP_ANIMATION_DURATION = 0.15f;

    @ConfigEntry(
            comment = "Rotation speed of the icon."
    )
    public static float ICON_ROTATING_SPEED = 0f;

    @ConfigEntry(
            comment = "Rotation speed multiplier for the tiered item preview in the second panel."
    )
    public static float SECOND_PANEL_RENDERER_SPEED = 1f;

    @ConfigEntry(
            comment = "Divider line color. Options: 'match_inner_frame_color', 'match_item_name_color' or a hex ARGB color (e.g., 0xA0EFEFEF)."
    )
    public static String DIVIDER_LINE_COLOR = "match_inner_frame_color";

    @ConfigEntry(
            comment = "Divider line type. Options: 'gradient', 'static', 'linear'"
    )
    public static String DIVIDER_LINE_TYPE = "gradient";

    @ConfigEntry(
            comment = "Preview model to render in the second panel. Options: armor_stand, player_skin."
    )
    public static String PREVIEW_PANEL_MODEL = "armor_stand";

    @ConfigEntry(
            comment = "If PREVIEW_PANEL_MODEL is set to player_skin, renders the current player skin (true) or a placeholder skin (false)."
    )
    public static boolean USE_PLAYER_SKIN_IN_PREVIEW = true;

    @ConfigEntry(
            comment = "Color palette for tooltips without a stack. Must specify exactly 3 ARGB colors."
    )
    public static String NO_STACK_PALETTE_COLORS = "0xFF8C909A, 0xFF4E515A, 0xFF282A30";

    @ConfigEntry(
            comment = "Color palette for COMMON rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String COMMON_PALETTE_COLORS = "0xFF8C909A, 0xFF4E515A, 0xFF282A30";

    @ConfigEntry(
            comment = "Color palette for UNCOMMON rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String UNCOMMON_PALETTE_COLORS = "0xFFC8CE3A, 0xFF8E931F, 0xFF53560E";

    @ConfigEntry(
            comment = "Color palette for RARE rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String RARE_PALETTE_COLORS = "0xFF4D9BE8, 0xFF2B66B5, 0xFF123A6B";

    @ConfigEntry(
            comment = "Color palette for EPIC rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String EPIC_PALETTE_COLORS = "0xFFB14BE0, 0xFF7A28A8, 0xFF431463";

    @ConfigEntry(
            comment = "Color palette for LEGENDARY rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String LEGENDARY_PALETTE_COLORS = "0xFFE8B84A, 0xFFB5832A, 0xFF6B4A12";

    @ConfigEntry(
            comment = "Color palette for CHAOS rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String CHAOS_PALETTE_COLORS = "0xFFE8483F, 0xFFB5251F, 0xFF5E0F0C";

    @ConfigEntry(
            comment = "Default color palette for items whose rarity is not a vanilla one " +
                    "(COMMON/UNCOMMON/RARE/EPIC), such as custom rarities added by other mods. Must specify exactly " +
                    "3 ARGB colors. Defaults to the same colors as the legendary palette."
    )
    public static String CUSTOM_RARITY_PALETTE_COLORS = "0xFFE8B84A, 0xFFB5832A, 0xFF6B4A12";

}