package dev.xylonity.tooltipoverhaul.client.old;

import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;

/**
 * Main color palette for generic color definitions
 */
public final class Palette {

    public static final int PANEL_BG = TooltipsConfig.DEFAULT_BACKGROUND_COLOR;

    public static final int[] COMMON = ConfigColorParser.parsePalette(TooltipsConfig.COMMON_PALETTE_COLORS);

    public static final int[] UNCOMMON = ConfigColorParser.parsePalette(TooltipsConfig.UNCOMMON_PALETTE_COLORS);

    public static final int[] RARE = ConfigColorParser.parsePalette(TooltipsConfig.RARE_PALETTE_COLORS);

    public static final int[] EPIC = ConfigColorParser.parsePalette(TooltipsConfig.EPIC_PALETTE_COLORS);

    public static final int[] LEGENDARY = ConfigColorParser.parsePalette(TooltipsConfig.LEGENDARY_PALETTE_COLORS);

    public static final int[] CHAOS = ConfigColorParser.parsePalette(TooltipsConfig.CHAOS_PALETTE_COLORS);

    public static int[] of(CustomFrameData.GradientType t) {
        return switch (t) {
            case UNCOMMON -> UNCOMMON;
            case RARE -> RARE;
            case EPIC -> EPIC;
            case LEGENDARY -> LEGENDARY;
            case CHAOS -> CHAOS;
            default -> COMMON;
        };
    }

}