package dev.xylonity.tooltipoverhaul.client.style.background;

import dev.xylonity.tooltipoverhaul.client.layer.impl.BackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class DefaultBackground implements BackgroundLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {
        int x0 = (int) (position.x - 3);
        int y0 = (int) (position.y - 2);
        int x1 = (int) (position.x + context.getTooltipSize().x + 2);
        int y1 = (int) (position.y + context.getTooltipSize().y + 2);

        int backgroundColor = ColorUtils.getBackgroundColor(context);

        // Background
        context.getGraphics().fill(x0, y0, x1, y1, 0, backgroundColor);

        // Top border
        context.getGraphics().fill(x0, y0 - 1, x1, y0, 0, backgroundColor);
        // Bottom border
        context.getGraphics().fill(x0, y1 + 1, x1, y1, 0, backgroundColor);
        // Left border
        context.getGraphics().fill(x0 - 1, y0, x0, y1, 0, backgroundColor);
        // Right border
        context.getGraphics().fill(x1 + 1, y0, x1, y1, 0, backgroundColor);
    }

}
