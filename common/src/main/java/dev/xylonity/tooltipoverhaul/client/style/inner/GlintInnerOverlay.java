package dev.xylonity.tooltipoverhaul.client.style.inner;

import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipFrame;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class GlintInnerOverlay implements ITooltipFrame {

    private final int color1;
    private final int color2;
    private final int color3;
    private final int timesSegmentedColor1;
    private final int timesSegmentedColor2;

    public GlintInnerOverlay(int color1, int color2, int color3, int timesSegmentedColor1, int timesSegmentedColor2) {
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
        this.timesSegmentedColor1 = timesSegmentedColor1;
        this.timesSegmentedColor2 = timesSegmentedColor2;
    }

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        glint(ctx.graphics(), (int) pos.x - 3, (int) pos.y - 3, size.x + 6, size.y + 6, depth.getZ());
    }

    private void glint(GuiGraphics graphics, int x, int y, int width, int height, int z) {
        int per = 2 * (width + height) - 4;
        // Central index moved to the top left section of the tooltip
        int center = width / 4;

        int LIGHT = color1;
        int MEDIUM = color2;
        int DARK = color3;
        int LIGHT_RANGE = Math.max(1, width / timesSegmentedColor1);
        int MEDIUM_RANGE = Math.max(1, width / timesSegmentedColor2);

        for (int i = 0; i < per; i++) {
            // coordinate compute in clockwise direction
            int px;
            int py;
            if (i < width) {
                px = x + i;
                py = y;
            } else if (i < width + height - 1) {
                px = x + width - 1;
                py = y + (i - width + 1);
            } else if (i < 2 * width + height - 2) {
                int j = i - (width + height - 1);
                px = x + (width - 1) - j;
                py = y + height - 1;
            } else {
                int j = i - (2 * width + height - 2);
                px = x;
                py = y + (height - 2) - j;
            }

            // distances towards the center
            int cw = (i - center + per) % per;
            int ccw = (center - i + per) % per;

            // color (blended)
            int color;

            // Color amount priority based on the segment amount per color. The prominent one is the darker (which should
            // be transparent or a dark color) (this effect is called glint for a reason)
            if (cw <= LIGHT_RANGE) {
                color = blend(LIGHT, MEDIUM, cw / (float) LIGHT_RANGE);
            } else if (cw <= LIGHT_RANGE + MEDIUM_RANGE) {
                color = blend(MEDIUM, DARK, (cw - LIGHT_RANGE) / (float) MEDIUM_RANGE);
            } else if (ccw <= LIGHT_RANGE) {
                color = blend(LIGHT, MEDIUM, ccw / (float) LIGHT_RANGE);
            } else if (ccw <= LIGHT_RANGE + MEDIUM_RANGE) {
                color = blend(MEDIUM, DARK, (ccw - LIGHT_RANGE) / (float) MEDIUM_RANGE);
            } else {
                color = DARK;
            }

            graphics.fill(px, py, px + 1, py + 1, z, color);
        }

    }

    /**
     * ARGB color blender. Derived from:
     * https://github.com/BenSouchet/color-blend/blob/gh-pages/assets/js/main.js
     */
    private static int blend(int c1, int c2, float t) {
        int a1 = (c1 >>> 24);
        int r1 = (c1 >>> 16) & 0xFF;
        int g1 = (c1 >>> 8) & 0xFF;
        int b1 = c1 & 0xFF;
        int a2 = (c2 >>> 24);
        int r2 = (c2 >>> 16) & 0xFF;
        int g2 = (c2 >>> 8) & 0xFF;
        int b2 = c2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (a<<24) | (r<<16) | (g<<8) | b;
    }

}