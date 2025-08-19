package dev.xylonity.tooltipoverhaul.client.layer;

/**
 * Defines multiple predefined locations along the Z-Axis to blit things into. Per priority order
 */
public enum LayerDepth {
    BACKGROUND(500),
    BACKGROUND_INNER_FRAME(1000),
    BACKGROUND_TEXT(1500),
    BACKGROUND_RENDERS(2000),
    BACKGROUND_EFFECT(2500),
    BACKGROUND_OVERLAY(3000);

    private final int z;

    LayerDepth(int z) {
        this.z = z;
    }

    public int getZ() {
        return z;
    }

}
