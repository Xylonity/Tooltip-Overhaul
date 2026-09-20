package dev.xylonity.tooltipoverhaul.client.screen.config;

import java.util.List;

/**
 * Declarative field and section schema for custom frame forms
 */
final class FrameFieldSchema {

    static final List<Object> SECTIONS_AND_FIELDS = List.of(
            "entry",
            new FieldSpec("name", Kind.STRING),
            new FieldSpec("extends", Kind.CHOICE),
            new FieldSpec("createTemplate", Kind.STRING),
            "matching",
            new FieldSpec("priority", Kind.INT),
            new FieldSpec("items", Kind.LIST),
            new FieldSpec("tags", Kind.LIST),
            new FieldSpec("namespace", Kind.STRING),
            new FieldSpec("rarity", Kind.LIST),
            "conditions",
            new FieldSpec("durabilityMin", Kind.FLOAT),
            new FieldSpec("durabilityMax", Kind.FLOAT),
            new FieldSpec("enchanted", Kind.BOOL),
            new FieldSpec("enchantments", Kind.LIST),
            new FieldSpec("hasCustomName", Kind.BOOL),
            new FieldSpec("customName", Kind.STRING),
            new FieldSpec("nbt", Kind.LIST),
            "appearance",
            new FieldSpec("texture", Kind.STRING),
            new FieldSpec("backgroundColor", Kind.COLOR_INT),
            new FieldSpec("borderType", Kind.CHOICE, "gradient", "glint", "static", "auto_gradient", "auto_glint", "auto_static"),
            new FieldSpec("innerFrameCornerType", Kind.CHOICE, "default", "rounded", "bevel", "inner", "cut", "thick", "full_thick", "bracket", "block", "notch", "weld", "gem"),
            new FieldSpec("backgroundCornerType", Kind.CHOICE, "default", "square", "rounded", "notch"),
            new FieldSpec("gradientType", Kind.CHOICE, "common", "uncommon", "rare", "epic", "legendary", "chaos", "custom_rarity", "custom"),
            new FieldSpec("gradientColors", Kind.COLOR_LIST),
            "icon",
            new FieldSpec("disableIcon", Kind.BOOL),
            new FieldSpec("iconBackgroundType", Kind.CHOICE, "focus", "void", "slot", "slot_border", "glow"),
            new FieldSpec("iconBorderStyle", Kind.CHOICE, "style_1", "style_2"),
            new FieldSpec("iconBackgroundColor", Kind.COLOR),
            new FieldSpec("iconBorderColor", Kind.COLOR),
            new FieldSpec("iconAppearAnimation", Kind.CHOICE, "none", "zoom", "rotate", "rotate_fast", "rotate_zoom", "zoom_snap", "skew", "vibration", "tilt_wave", "flip", "pendulum", "bounce", "go_down", "pulse", "fan_in", "hover_pop", "barrel_roll"),
            new FieldSpec("iconSize", Kind.FLOAT),
            new FieldSpec("iconRotatingSpeed", Kind.FLOAT),
            "text",
            new FieldSpec("titleAlignment", Kind.CHOICE, "left", "middle", "right"),
            new FieldSpec("showRating", Kind.BOOL),
            new FieldSpec("itemRating", Kind.STRING),
            new FieldSpec("colorItemRating", Kind.COLOR),
            new FieldSpec("ratingAlignment", Kind.CHOICE, "left", "middle", "right"),
            "layout",
            new FieldSpec("tooltipLayout", Kind.CHOICE, "classic", "badge", "floating", "compact"),
            new FieldSpec("compactShowModName", Kind.BOOL),
            new FieldSpec("compactModNameColor", Kind.COLOR),
            new FieldSpec("tooltipPositionX", Kind.INT),
            new FieldSpec("tooltipPositionY", Kind.INT),
            new FieldSpec("mainPanelPaddingX", Kind.INT),
            new FieldSpec("mainPanelPaddingY", Kind.INT),
            "divider",
            new FieldSpec("disableDividerLine", Kind.BOOL),
            new FieldSpec("dividerLineType", Kind.CHOICE, "gradient", "static", "linear", "dashed", "dotted", "ornament", "gradient_ornament"),
            new FieldSpec("dividerLineColor", Kind.COLOR),
            new FieldSpec("dividerLineTopPadding", Kind.INT),
            new FieldSpec("dividerLineBottomPadding", Kind.INT),
            "preview",
            new FieldSpec("showSecondPanel", Kind.BOOL),
            new FieldSpec("previewPanelSideTriangles", Kind.CHOICE, "none", "style_1", "style_2"),
            new FieldSpec("previewPanelCornerType", Kind.CHOICE, "default", "rounded", "bevel", "inner", "cut", "thick", "full_thick", "bracket", "block", "notch", "weld", "gem"),
            new FieldSpec("previewPanelBackgroundCornerType", Kind.CHOICE, "default", "square", "rounded", "notch"),
            new FieldSpec("previewPanelModel", Kind.CHOICE, "armor_stand", "player_skin"),
            new FieldSpec("usePlayerSkinInPreview", Kind.BOOL),
            new FieldSpec("secondPanelX", Kind.INT),
            new FieldSpec("secondPanelY", Kind.INT),
            new FieldSpec("secondPanelSizeX", Kind.INT),
            new FieldSpec("secondPanelSizeY", Kind.INT),
            new FieldSpec("secondPanelRendererSpeed", Kind.FLOAT),
            "effects",
            new FieldSpec("specialEffect", Kind.STRING),
            new FieldSpec("effectSpeed", Kind.FLOAT),
            new FieldSpec("effectIntensity", Kind.FLOAT),
            new FieldSpec("effectDensity", Kind.FLOAT),
            new FieldSpec("effectsBehindText", Kind.BOOL),
            new FieldSpec("vignettes", Kind.TUPLE_LIST),
            "animation",
            new FieldSpec("tooltipAppearAnimation", Kind.CHOICE, "none", "fade", "pop", "rise", "unfold", "zoom", "slide", "swing", "emerge", "squash", "card", "shake"),
            new FieldSpec("tooltipDisappearAnimation", Kind.CHOICE, "match_appear", "none", "fade", "pop", "rise", "unfold", "zoom", "slide", "swing", "emerge", "squash", "card", "shake"),
            new FieldSpec("tooltipAnimationDuration", Kind.FLOAT),
            "misc",
            new FieldSpec("showShadow", Kind.BOOL),
            new FieldSpec("disableScrolling", Kind.BOOL),
            new FieldSpec("disableTooltip", Kind.BOOL)
    );

    enum Kind {

        STRING,
        INT,
        FLOAT,
        COLOR,
        COLOR_INT,
        LIST,
        TUPLE_LIST,
        COLOR_LIST,
        BOOL,
        CHOICE
    }

    record FieldSpec(
            String key,
            Kind kind,
            String... options
    ) {
        ;;
    }

}
