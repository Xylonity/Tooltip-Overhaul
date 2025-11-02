package dev.xylonity.tooltipoverhaul.client.style.background;

import dev.xylonity.tooltipoverhaul.client.layer.impl.BackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class DefaultBackground implements BackgroundLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {
        int x0 = (int) (position.x - 3);
        int y0 = (int) (position.y - 2);
        int x1 = (int) (position.x + context.getTooltipSize().x + 2);
        int y1 = (int) (position.y + context.getTooltipSize().y + 2);

        int bgColor = 0xF0000000;//Util.getBackgroundColor(ctx);

        // Background
        context.getGraphics().fill(x0, y0, x1, y1, context.getLayerDepth().getZ(), bgColor);

        // Top border
        context.getGraphics().fill(x0, y0 - 1, x1, y0, context.getLayerDepth().getZ(), bgColor);
        // Bottom border
        context.getGraphics().fill(x0, y1 + 1, x1, y1, context.getLayerDepth().getZ(), bgColor);
        // Left border
        context.getGraphics().fill(x0 - 1, y0, x0, y1, context.getLayerDepth().getZ(), bgColor);
        // Right border
        context.getGraphics().fill(x1 + 1, y0, x1, y1, context.getLayerDepth().getZ(), bgColor);
    }

}
