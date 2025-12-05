package dev.xylonity.tooltipoverhaul.client.style.preview.background;

import dev.xylonity.tooltipoverhaul.client.layer.impl.PreviewBackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import net.minecraft.world.phys.Vec2;

public class DefaultPreviewBackground implements PreviewBackgroundLayer {

    @Override
    public void render(TooltipContext context, Vec2 startPosition, Vec2 endPosition) {
        int x0 = (int) startPosition.x;
        int y0 = (int) startPosition.y;
        int x1 = (int) endPosition.x;
        int y1 = (int) endPosition.y;

        int backgroundColor = ColorUtils.getBackgroundColor(context);

        // Background
        context.getGraphics().fill(x0, y0, x1, y1, backgroundColor);

        // Top border
        context.getGraphics().fill(x1, y0 - 1, x0, y0, backgroundColor);
        // Bottom border
        context.getGraphics().fill(x1, y1, x0, y1 + 1, backgroundColor);
        // Left border
        context.getGraphics().fill(x1 - 1, y0, x1, y1, backgroundColor);
        // Right border
        context.getGraphics().fill(x0, y0, x0 + 1, y1, backgroundColor);
    }

}
