package dev.xylonity.tooltipoverhaul.client.style.preview.inner;

import dev.xylonity.tooltipoverhaul.client.layer.impl.PreviewInnerOverlayLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;

public class DefaultPreviewGradientInnerOverlay implements PreviewInnerOverlayLayer {

    private final int color1;
    private final int color2;
    private final int color3;

    public DefaultPreviewGradientInnerOverlay(int color1, int color2, int color3) {
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
    }

    @Override
    public void render(TooltipContext context, Vec2 startPosition, Vec2 endPosition) {
        int x0 = (int) startPosition.x;
        int y0 = (int) startPosition.y;
        int x1 = (int) endPosition.x;
        int y1 = (int) endPosition.y;

        renderFrameGradient(context.getGraphics(), x1, y0 + 1, x0 - x1, y1 - y0, context.getLayerDepth().getZ(), color1, color2, color3);

        // Top and bottom lines
        context.getGraphics().fill(x1, y0, x0, y0 + 1, context.getLayerDepth().getZ(), color1);
        context.getGraphics().fill(x1, y1, x0, y1 - 1, context.getLayerDepth().getZ(), color3);
    }

    private static void renderFrameGradient(GuiGraphics graphics, int x, int y, int width, int height, int z, int c1, int c2, int c3) {
        int mid = height / 2;

        // Left border (top and bottom sections)
        graphics.fillGradient(x, y, x + 1, y + mid, z, c1, c2);
        graphics.fillGradient(x, y + mid, x + 1, y + height - 1, z, c2, c3);

        // Right border (top and bottom sections)
        graphics.fillGradient(x + width - 1, y, x + width, y + mid, z, c1, c2);
        graphics.fillGradient(x + width - 1, y + mid, x + width, y + height - 1, z, c2, c3);
    }

}
