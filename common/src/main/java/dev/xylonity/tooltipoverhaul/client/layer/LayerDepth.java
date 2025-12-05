package dev.xylonity.tooltipoverhaul.client.layer;

/**
 * Defines multiple predefined locations along the Z-Axis to blit things into. Per priority order
 */
public enum LayerDepth {
    SHADOW(500),
    BACKGROUND(700),
    TEXT(900),
    ICON_BACKGROUND(1100),
    INNER_FRAME(1300),
    DIVIDER_LINE(1500),
    RENDERS(1700),
    VIGNETTE(1900),
    OVERLAY(2100),
    EFFECT(2300);

    private final int z;

    LayerDepth(int z) {
        this.z = z;
    }

    public int getZ() {
        return z;
    }

}