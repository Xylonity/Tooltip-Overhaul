package dev.xylonity.tooltipoverhaul.config;

import dev.xylonity.tooltipoverhaul.config.wrapper.AutoConfig;
import dev.xylonity.tooltipoverhaul.config.wrapper.ConfigEntry;

@AutoConfig(file = "tooltipoverhaul", title = "Tooltip Overhaul", accentColor = 0xFF4D9BE8)
public final class TooltipsConfig {

    @ConfigEntry(
            category = "effects",
            min = 0.0,
            max = 10.0,
            comment = "Effect animation speed multiplier. Zero freezes effects."
    )
    public static float EFFECT_SPEED = 1f;

    @ConfigEntry(
            category = "effects",
            min = 0.0,
            max = 1.0,
            comment = "Effect opacity multiplier. Zero hides effects."
    )
    public static float EFFECT_INTENSITY = 1f;

    @ConfigEntry(
            category = "effects",
            min = 0.0,
            max = 5.0,
            comment = "Particle density multiplier."
    )
    public static float EFFECT_DENSITY = 1f;

    @ConfigEntry(
            category = "effects",
            comment = "Freezes decorative effects and disable tooltip/icon movement, including custom frame overrides."
    )
    public static boolean REDUCED_MOTION = false;

    @ConfigEntry(
            category = "effects",
            comment = "Render effects right over the background, behind the text, icon and frame, instead of on top of " +
                    "everything."
    )
    public static boolean EFFECTS_BEHIND_TEXT = false;

    @ConfigEntry(
            category = "layout",
            comment = "Tooltip layout. Options: classic (icon inside), badge (icon halfway across the left frame), floating " +
                    "(compact header, external icon above the 3D preview), compact (small icon beside the title and rating specified in a subtle footer)."
    )
    public static String TOOLTIP_LAYOUT = "classic";

    @ConfigEntry(
            category = "layout",
            comment = "Show the item's mod name on the left of Compact's footer, with the rating on the right. Long names are " +
                    "shortened to fit. The mod name is moved from the body when already present."
    )
    public static boolean COMPACT_SHOW_MOD_NAME = true;

    @ConfigEntry(
            category = "layout",
            color = true,
            comment = "Color of the mod name in Compact's footer, in ARGB format (#AARRGGBB). Defaults to dark gray."
    )
    public static String COMPACT_MOD_NAME_COLOR = "#FF555555";

    @ConfigEntry(
            category = "preview",
            comment = "Render a 3D preview of tiered items (swords, axes, etc.) on the left side of the tooltip."
    )
    public static boolean TIERED_ITEMS_RENDERER = true;

    @ConfigEntry(
            category = "preview",
            comment = "Render a 3D preview of armor pieces on the left side of the tooltip."
    )
    public static boolean ARMOR_ITEMS_RENDERER = true;

    @ConfigEntry(
            category = "general",
            comment = "Apply a custom frame overlay texture to every item. This option is overrided if the hovered itemstack has " +
                    "a custom frame. Write down the texture location (such as tooltipoverhaul:textures/overlay/silver_frame.png) or " +
                    "leave it empty. No frame overlays are applied globally by default."
    )
    public static String GLOBAL_FRAME_OVERLAY_LOCATION = "";

    @ConfigEntry(
            category = "general",
            comment = "Disable tooltip scrolling"
    )
    public static boolean DISABLE_TOOLTIP_SCROLLING = false;

    @ConfigEntry(
            category = "general",
            comment = "Automatically scroll overflowed tooltips after three seconds. Manual scrolling disables it until the tooltip changes."
    )
    public static boolean AUTO_SCROLL_TOOLTIPS = true;

    @ConfigEntry(
            category = "general",
            comment = "Always show the equipment comparison tooltip when hovering a comparable item, " +
                    "without needing to hold the compare key (Left Shift by default)."
    )
    public static boolean ALWAYS_SHOW_COMPARISON = false;

    @ConfigEntry(
            category = "general",
            min = 1,
            max = 6,
            slider = true,
            comment = "Maximum number of tooltips that can be pinned at the same time. Pinning past the limit replaces the oldest one."
    )
    public static int MAX_PINNED_TOOLTIPS = 3;

    @ConfigEntry(
            category = "divider",
            comment = "Disable the divider line"
    )
    public static boolean DISABLE_DIVIDER_LINE = false;

    @ConfigEntry(
            category = "preview",
            comment = "Horizontal offset (in pixels) for the second panel when a 3D preview is shown. Negative = left, positive = right."
    )
    public static int SECOND_PANEL_X = 0;

    @ConfigEntry(
            category = "preview",
            comment = "Vertical offset (in pixels) for the second panel when a 3D preview is shown. Negative = up, positive = down."
    )
    public static int SECOND_PANEL_Y = 0;

    @ConfigEntry(
            category = "preview",
            comment = "Size (in pixels) of the second panel in the X axis when a 3D preview is shown."
    )
    public static int SECOND_PANEL_SIZE_X = 30;

    @ConfigEntry(
            category = "preview",
            comment = "Size (in pixels) of the second panel in the Y axis when a 3D preview is shown."
    )
    public static int SECOND_PANEL_SIZE_Y = 60;

    @ConfigEntry(
            category = "preview",
            comment = "Automatically reposition the preview panel (second panel) to the right side when there " +
                    "is insufficient space on the left. If set to false, the panel will always remain on the left."
    )
    public static boolean AUTO_REPOSITION_PREVIEW_PANEL = true;

    @ConfigEntry(
            category = "layout",
            comment = "Bedrock-like centering. When the tooltip fits neither to the right nor to the left of the cursor, " +
                    "centers it horizontally on the screen and places it above the cursor (or below if there is no room " +
                    "above), so it doesn't cover the hovered item."
    )
    public static boolean BEDROCK_CENTERING = false;

    @ConfigEntry(
            category = "layout",
            comment = "Horizontal offset (in pixels) for the tooltip. Negative = left, positive = right."
    )
    public static int TOOLTIP_POSITION_X = 0;

    @ConfigEntry(
            category = "layout",
            comment = "Vertical offset (in pixels) for the tooltip. Negative = up, positive = down."
    )
    public static int TOOLTIP_POSITION_Y = 0;

    @ConfigEntry(
            category = "layout",
            min = 0,
            max = 700,
            comment = "Depth (Z) offset of the whole hovered tooltip. Vanilla draws toasts (advancements, recipes) at 800, " +
                    "so the default keeps the tooltip over them."
    )
    public static int TOOLTIP_Z_OFFSET = 400;

    @ConfigEntry(
            category = "layout",
            min = 10,
            max = 100,
            comment = "Maximum tooltip height as a percentage of the screen height."
    )
    public static int MAX_TOOLTIP_HEIGHT = 100;

    @ConfigEntry(
            category = "layout",
            min = 20,
            max = 100,
            comment = "Maximum tooltip width as a percentage of the screen width."
    )
    public static int MAX_TOOLTIP_WIDTH = 100;

    @ConfigEntry(
            category = "divider",
            comment = "Extra padding above the divider line"
    )
    public static int DIVIDER_LINE_TOP_PADDING = 0;

    @ConfigEntry(
            category = "divider",
            comment = "Extra padding below the divider line"
    )
    public static int DIVIDER_LINE_BOTTOM_PADDING = 0;

    @ConfigEntry(
            category = "style",
            options = { "gradient", "glint", "static" },
            comment = "Default inner overlay style for tooltips. Options: glint, static (monochrome), gradient."
    )
    public static String DEFAULT_INNER_OVERLAY_TYPE = "gradient";

    @ConfigEntry(
            category = "style",
            options = { "default", "rounded", "bevel", "inner", "cut", "thick", "full_thick", "bracket", "block", "notch", "weld", "gem" },
            comment = "Corner style for the inner frame of the tooltip. Options: default (square), rounded (corner " +
                    "pixel removed), bevel (45 degree diagonal cut), inner (extra pixel in the inner corner), cut (2px " +
                    "chamfer), thick (solid triangular corner, top-right only), full_thick (thick on all four corners), bracket (inner corner bracket), " +
                    "block (2x2 solid block corner), notch (rectangular inner offset), weld (reinforced inner " +
                    "corner), gem (hollow triangle). The notch style pairs with the matching notch background corner style."
    )
    public static String INNER_FRAME_CORNER_TYPE = "default";

    @ConfigEntry(
            category = "style",
            options = { "default", "square", "rounded", "notch" },
            comment = "Corner style for the outer background border of the tooltip. Options: default (vanilla notch), " +
                    "square (sharp 90 degree), rounded, notch (rectangular inner offset, matches the " +
                    "inner frame notch style)."
    )
    public static String BACKGROUND_CORNER_TYPE = "default";

    @ConfigEntry(
            category = "style",
            color = true,
            comment = "Default tooltip background color in ARGB format (#AARRGGBB). Example: #F0010110."
    )
    public static String DEFAULT_BACKGROUND_COLOR = "#F0000000";

    @ConfigEntry(
            category = "general",
            comment = "Override vanilla tooltips even when no ItemStack is present (e.g., JEI category buttons or unsupported stacks). "
                    + "To disable custom tooltips for specific items, edit your custom_frames.json and set disableTooltip=true for those "
                    + "items (or tags)."
    )
    public static boolean SHOW_TOOLTIP_WITHOUT_STACK = true;

    @ConfigEntry(
            category = "layout",
            comment = "Horizontal padding for the main panel."
    )
    public static int MAIN_PANEL_PADDING_X = 2;

    @ConfigEntry(
            category = "layout",
            comment = "Vertical padding for the main panel."
    )
    public static int MAIN_PANEL_PADDING_Y = 2;

    @ConfigEntry(
            category = "layout",
            comment = "Horizontal padding for the main panel when there isn't a stack present."
    )
    public static int NO_STACK_TOOLTIP_PADDING_X = 1;

    @ConfigEntry(
            category = "layout",
            comment = "Vertical padding for the main panel when there isn't a stack present."
    )
    public static int NO_STACK_TOOLTIP_PADDING_Y = 1;

    @ConfigEntry(
            category = "text",
            options = { "left", "middle", "right" },
            comment = "Title alignment. Options: left, middle, right."
    )
    public static String TITLE_X_ALIGNMENT = "left";

    @ConfigEntry(
            category = "text",
            options = { "left", "middle", "right" },
            comment = "Rating alignment. Options: left, middle, right."
    )
    public static String RATING_X_ALIGNMENT = "left";

    @ConfigEntry(
            category = "effects",
            comment = "Effects. Options: bubbles, cinder, crystals, echo, fireflies, galaxy, magic_orbs, speed_lines, nebula, " +
                    "spiral, white_dust, metal_shining, rim_light, ripples, sonar, stars, fireflies_2, steel_shining, floating_stars, " +
                    "prism, snowfall, opal, aurora, fluorite, astral, comets, storm, sunbeams, fireworks, eruption, " +
                    "searchlights, blasts, firebreath, shield, lasers, missiles, wisps. You can chain effects together, for example: " +
                    "'white_dust, nebula'"
    )
    public static String EFFECTS = "";

    @ConfigEntry(
            category = "icon",
            options = { "focus", "void", "slot", "slot_border", "glow" },
            comment = "Icon background type. Options: focus, void, slot, slot_border and glow"
    )
    public static String ICON_BACKGROUND_TYPE = "slot_border";

    @ConfigEntry(
            category = "icon",
            options = { "style_1", "style_2" },
            comment = "Icon background border shape. Style 1 leaves out the corner pixels, style 2 fills them for square corners."
    )
    public static String ICON_BORDER_STYLE = "style_1";

    @ConfigEntry(
            category = "icon",
            color = true,
            options = { "default" },
            comment = "Icon background color in #AARRGGBB. Use 'default' for the original color of each background type."
    )
    public static String ICON_BACKGROUND_COLOR = "default";

    @ConfigEntry(
            category = "icon",
            color = true,
            options = { "default" },
            comment = "Icon border color in #AARRGGBB, used by the slot_border, focus and glow backgrounds. Use 'default' " +
                    "for the original color of each background type."
    )
    public static String ICON_BORDER_COLOR = "default";

    @ConfigEntry(
            category = "text",
            comment = "Show rating text."
    )
    public static boolean SHOW_RATING = false;

    @ConfigEntry(
            category = "text",
            comment = "Appends the display name of the mod that adds the hovered item as the last tooltip line " +
                    "(blue italic), always visible in both survival and creative (previously visible only in creative mode)."
    )
    public static boolean SHOW_MOD_NAME = false;

    @ConfigEntry(
            category = "text",
            comment = "Show the remaining and maximum durability of damaged items without enabling advanced tooltips (F3+H)."
    )
    public static boolean SHOW_ITEM_DURABILITY = false;

    @ConfigEntry(
            category = "text",
            comment = "Show the item's registry name (e.g. minecraft:diamond_sword) as a dark gray line without enabling " +
                    "advanced tooltips (F3+H)."
    )
    public static boolean SHOW_REGISTRY_NAME = false;

    @ConfigEntry(
            category = "icon",
            comment = "Disable the item icon."
    )
    public static boolean DISABLE_ICON = false;

    @ConfigEntry(
            category = "style",
            comment = "Enable the tooltip shadow."
    )
    public static boolean SHOW_TOOLTIP_SHADOW = false;

    @ConfigEntry(
            category = "effects",
            comment = "Vignette entries for gradient overlays around the tooltip background. Each vignette must be written in " +
                    "parentheses in this exact order: (type, position, color, radius, extraPositionX, extraPositionY). " +
                    "Example: (circular, top_left, #FF567823, 0.4, 0, 0). You can define multiple vignettes by separating them " +
                    "with commas: (...), (...), (...). type = vignette shape (options: circular, hole, ellipse, diamond, " +
                    "ring, linear). position = anchor on the tooltip (e.g. top_left, top_right, middle, right, bottom_left); for " +
                    "linear it is the edge the fade starts from (top_*, bottom_*, left, right; middle fades top and bottom). color = " +
                    "ARGB hex in #AARRGGBB. radius = relative size factor (e.g. radius 0.4); for linear, the fade depth as a fraction " +
                    "of the panel. extraPositionX / extraPositionY = additional offset as a percentage of the tooltip height / width, " +
                    "respectively."
    )
    public static String VIGNETTES = "";

    @ConfigEntry(
            category = "icon",
            comment = "Animation style for the icon appearance."
    )
    public static String ICON_APPEAR_ANIMATION = "skew";

    @ConfigEntry(
            category = "animations",
            options = { "none", "fade", "pop", "rise", "unfold", "zoom", "slide", "swing", "emerge", "squash", "card", "shake" },
            comment = "Animation played when a tooltip appears (on hover). Options: none, fade, pop, rise, unfold, zoom, " +
                    "slide, swing, emerge, squash, card, shake."
    )
    public static String TOOLTIP_APPEAR_ANIMATION = "none";

    @ConfigEntry(
            category = "animations",
            options = { "match_appear", "none", "fade", "pop", "rise", "unfold", "zoom", "slide", "swing", "emerge", "squash", "card", "shake" },
            comment = "Animation played when a tooltip disappears. Options: match_appear (same as the appear animation), " +
                    "none, fade, pop, rise, unfold, zoom, slide, swing, emerge, squash, card, shake."
    )
    public static String TOOLTIP_DISAPPEAR_ANIMATION = "match_appear";

    @ConfigEntry(
            category = "animations",
            min = 0,
            max = 5,
            comment = "Seconds the cursor must rest on an item before its tooltip (and its appear animation) shows up, so " +
                    "sweeping across an inventory doesn't flash tooltips. 0 shows them immediately."
    )
    public static float TOOLTIP_APPEAR_DELAY = 0f;

    @ConfigEntry(
            category = "animations",
            comment = "Replay the appear animation when moving straight from one item to another. Disable it to keep the " +
                    "tooltip settled while sweeping across slots, only animating when it appears from nothing."
    )
    public static boolean TOOLTIP_ANIMATE_ON_SWITCH = false;

    @ConfigEntry(
            category = "animations",
            comment = "Duration (in seconds) of the tooltip appear/disappear animation. Lower values are snappier."
    )
    public static float TOOLTIP_ANIMATION_DURATION = 0.15f;

    @ConfigEntry(
            category = "icon",
            comment = "Rotation speed of the icon."
    )
    public static float ICON_ROTATING_SPEED = 0f;

    @ConfigEntry(
            category = "preview",
            comment = "Rotation speed multiplier for the tiered item preview in the second panel."
    )
    public static float SECOND_PANEL_RENDERER_SPEED = 1f;

    @ConfigEntry(
            category = "divider",
            color = true,
            options = { "match_inner_frame_color", "match_item_name_color" },
            comment = "Divider line color. Options: 'match_inner_frame_color', 'match_item_name_color' or a hex ARGB color (e.g., 0xA0EFEFEF)."
    )
    public static String DIVIDER_LINE_COLOR = "match_inner_frame_color";

    @ConfigEntry(
            category = "divider",
            options = { "gradient", "static", "linear", "dashed", "dotted", "ornament", "gradient_ornament" },
            comment = "Divider line type. Options: 'gradient', 'static', 'linear', 'dashed', 'dotted', 'ornament', 'gradient_ornament'"
    )
    public static String DIVIDER_LINE_TYPE = "gradient";

    @ConfigEntry(
            category = "preview",
            options = { "armor_stand", "player_skin" },
            comment = "Preview model to render in the second panel. Options: armor_stand, player_skin."
    )
    public static String PREVIEW_PANEL_MODEL = "armor_stand";

    @ConfigEntry(
            category = "preview",
            comment = "Side triangles. Options: none, style_1 (filled vertical strips), style_2 (outline only)."
    )
    public static String PREVIEW_PANEL_SIDE_TRIANGLES = "style_1";

    @ConfigEntry(
            category = "preview",
            comment = "Preview corner type, independent of the main tooltip. Options: default, rounded, bevel, inner, cut, " +
                    "thick, full_thick, bracket, block, notch, weld, gem."
    )
    public static String PREVIEW_PANEL_CORNER_TYPE = "default";

    @ConfigEntry(
            category = "preview",
            comment = "Preview background corners, independent of the border and main tooltip. Options: default (vanilla notch), " +
                    "square, rounded, notch. Pair notch with the preview border's notch corners."
    )
    public static String PREVIEW_PANEL_BACKGROUND_CORNER_TYPE = "default";

    @ConfigEntry(
            category = "preview",
            comment = "If PREVIEW_PANEL_MODEL is set to player_skin, renders the current player skin (true) or a placeholder skin (false)."
    )
    public static boolean USE_PLAYER_SKIN_IN_PREVIEW = true;

    @ConfigEntry(
            category = "palettes",
            color = true,
            comment = "Color palette for tooltips without a stack. Must specify exactly 3 ARGB colors."
    )
    public static String NO_STACK_PALETTE_COLORS = "0xFF8C909A, 0xFF4E515A, 0xFF282A30";

    @ConfigEntry(
            category = "palettes",
            color = true,
            comment = "Color palette for COMMON rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String COMMON_PALETTE_COLORS = "0xFF8C909A, 0xFF4E515A, 0xFF282A30";

    @ConfigEntry(
            category = "palettes",
            color = true,
            comment = "Color palette for UNCOMMON rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String UNCOMMON_PALETTE_COLORS = "0xFFC8CE3A, 0xFF8E931F, 0xFF53560E";

    @ConfigEntry(
            category = "palettes",
            color = true,
            comment = "Color palette for RARE rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String RARE_PALETTE_COLORS = "0xFF4D9BE8, 0xFF2B66B5, 0xFF123A6B";

    @ConfigEntry(
            category = "palettes",
            color = true,
            comment = "Color palette for EPIC rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String EPIC_PALETTE_COLORS = "0xFFB14BE0, 0xFF7A28A8, 0xFF431463";

    @ConfigEntry(
            category = "palettes",
            color = true,
            comment = "Color palette for LEGENDARY rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String LEGENDARY_PALETTE_COLORS = "0xFFE8B84A, 0xFFB5832A, 0xFF6B4A12";

    @ConfigEntry(
            category = "palettes",
            color = true,
            comment = "Color palette for CHAOS rarity (bright to dark). Must specify exactly 3 ARGB colors."
    )
    public static String CHAOS_PALETTE_COLORS = "0xFFE8483F, 0xFFB5251F, 0xFF5E0F0C";

    @ConfigEntry(
            category = "palettes",
            color = true,
            comment = "Default color palette for items whose rarity is not a vanilla one " +
                    "(COMMON/UNCOMMON/RARE/EPIC), such as custom rarities added by other mods. Must specify exactly " +
                    "3 ARGB colors. Defaults to the same colors as the legendary palette."
    )
    public static String CUSTOM_RARITY_PALETTE_COLORS = "0xFFE8B84A, 0xFFB5832A, 0xFF6B4A12";

}
