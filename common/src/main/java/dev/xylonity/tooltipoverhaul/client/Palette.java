package dev.xylonity.tooltipoverhaul.client;

import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;

/**
 * Main color palette for generic color definitions
 */
public final class Palette {

    public static final int PANEL_BG = TooltipsConfig.DEFAULT_BACKGROUND_COLOR;

    public static final int[] COMMON = {
            0xFFEFEFEF, 0xFF8A8A8A, 0xFF606060
    };

    public static final int[] UNCOMMON = {
            0xFFF9FF40, 0xFFA9AD26, 0xFF787B16
    };

    public static final int[] RARE = {
            0xFF5297FF, 0xFF285DAD, 0xFF102E5A
    };

    public static final int[] EPIC = {
            0xFFFF36D0, 0xFFA81E89, 0xFF600B4D
    };

    public static final int[] LEGENDARY = {
            0xFFFFCC60, 0xFFCE9828, 0xFF58400D
    };

    public static final int[] CHAOS = {
            0xFFFD575C, 0xFFCE282B, 0xFF580D0E
    };

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