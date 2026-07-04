package dev.xylonity.tooltipoverhaul.client.util;

import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;

/**
 * Main color palette for generic color definitions
 */
public final class Palette {

    public static int PANEL_BG;

    public static int[] NO_STACK;
    public static int[] COMMON;
    public static int[] UNCOMMON;
    public static int[] RARE;
    public static int[] EPIC;
    public static int[] LEGENDARY;
    public static int[] CHAOS;
    public static int[] CUSTOM_RARITY;

    static {
        reload();
    }

    public static void reload() {
        PANEL_BG = ConfigColorParser.parseColor(TooltipsConfig.DEFAULT_BACKGROUND_COLOR);
        NO_STACK = ConfigColorParser.parsePalette(TooltipsConfig.NO_STACK_PALETTE_COLORS);
        COMMON = ConfigColorParser.parsePalette(TooltipsConfig.COMMON_PALETTE_COLORS);
        UNCOMMON = ConfigColorParser.parsePalette(TooltipsConfig.UNCOMMON_PALETTE_COLORS);
        RARE = ConfigColorParser.parsePalette(TooltipsConfig.RARE_PALETTE_COLORS);
        EPIC = ConfigColorParser.parsePalette(TooltipsConfig.EPIC_PALETTE_COLORS);
        LEGENDARY = ConfigColorParser.parsePalette(TooltipsConfig.LEGENDARY_PALETTE_COLORS);
        CHAOS = ConfigColorParser.parsePalette(TooltipsConfig.CHAOS_PALETTE_COLORS);
        CUSTOM_RARITY = ConfigColorParser.parsePalette(TooltipsConfig.CUSTOM_RARITY_PALETTE_COLORS);
    }

    public static int[] of(CustomFrameData.GradientType type) {
        return switch (type) {
            case UNCOMMON -> UNCOMMON;
            case RARE -> RARE;
            case EPIC -> EPIC;
            case LEGENDARY -> LEGENDARY;
            case CHAOS -> CHAOS;
            case CUSTOM_RARITY -> CUSTOM_RARITY;
            default -> COMMON;
        };

    }

}