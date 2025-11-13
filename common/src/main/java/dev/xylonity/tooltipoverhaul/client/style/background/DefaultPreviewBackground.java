package dev.xylonity.tooltipoverhaul.client.style.background;

import dev.xylonity.tooltipoverhaul.client.layer.impl.BackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextAxis;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import net.minecraft.world.phys.Vec2;

public class DefaultPreviewBackground implements BackgroundLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {
        int sizeX = RenderUtils.calculateSecondPanelSize(context, TextAxis.X);
        int sizeY = RenderUtils.calculateSecondPanelSize(context, TextAxis.Y);
        int x0 = (int) (position.x - sizeX - 15 - 9);
        int y0 = (int) (position.y - 2);
        int x1 = (int) (position.x - 15 - 9);
        int y1 = (int) (position.y + sizeY);

        int backgroundColor = ColorUtils.getBackgroundColor(context);

        // Background
        context.getGraphics().fill(x0, y0, x1, y1, context.getLayerDepth().getZ(), backgroundColor);

        // Top border
        context.getGraphics().fill(x0, y0 - 1, x1, y0, context.getLayerDepth().getZ(), backgroundColor);
        // Bottom border
        context.getGraphics().fill(x0, y1 + 1, x1, y1, context.getLayerDepth().getZ(), backgroundColor);
        // Left border
        context.getGraphics().fill(x0 - 1, y0, x0, y1, context.getLayerDepth().getZ(), backgroundColor);
        // Right border
        context.getGraphics().fill(x1 + 1, y0, x1, y1, context.getLayerDepth().getZ(), backgroundColor);
    }

}
