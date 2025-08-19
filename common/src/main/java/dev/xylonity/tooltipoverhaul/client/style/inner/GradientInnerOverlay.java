package dev.xylonity.tooltipoverhaul.client.style.inner;

import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipFrame;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;

import java.awt.Point;

public class GradientInnerOverlay implements ITooltipFrame {

    private final int color1;
    private final int color2;
    private final int color3;

    public GradientInnerOverlay(int color1, int color2, int color3) {
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
    }

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        int x0 = (int) pos.x - 3;
        int y0 = (int) pos.y - 3;
        int width = size.x + 6;
        int height = size.y + 6;

        renderFrameGradient(ctx.graphics(), x0, y0 + 1, width, height - 2, depth.getZ(), color1, color2, color3);

        // Top and bottom lines
        ctx.graphics().fill(x0, y0, x0 + width, y0 + 1, depth.getZ(), color1);
        ctx.graphics().fill(x0, y0 + height - 1, x0 + width, y0 + height, depth.getZ(), color3);
    }

    private static void renderFrameGradient(GuiGraphics graphics, int x, int y, int width, int height, int z, int c1, int c2, int c3) {
        int mid = height / 2;
        // Left border (top and bottom sections)
        graphics.fillGradient(x, y, x + 1, y + mid, z, c1, c2);
        graphics.fillGradient(x, y + mid, x + 1, y + height, z, c2, c3);
        // Right border (top and bottom sections)
        graphics.fillGradient(x + width - 1, y, x + width, y + mid, z, c1, c2);
        graphics.fillGradient(x + width - 1, y + mid, x + width, y + height, z, c2, c3);
    }
}
