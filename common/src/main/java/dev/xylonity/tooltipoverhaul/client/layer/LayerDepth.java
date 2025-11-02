package dev.xylonity.tooltipoverhaul.client.layer;

/**
 * Defines multiple predefined locations along the Z-Axis to blit things into. Per priority order
 */
public enum LayerDepth {
    BACKGROUND(500),
    TEXT(1000),
    ICON_BACKGROUND(1500),
    RENDERS(2000),
    DIVIDER_LINE(2500),
    EFFECT(3000),
    INNER_FRAME(3500),
    OVERLAY(4000);

    private final int z;

    LayerDepth(int z) {
        this.z = z;
    }

    public int getZ() {
        return z;
    }

}