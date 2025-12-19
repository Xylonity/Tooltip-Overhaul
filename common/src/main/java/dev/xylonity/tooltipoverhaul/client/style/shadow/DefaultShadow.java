package dev.xylonity.tooltipoverhaul.client.style.shadow;

import dev.xylonity.tooltipoverhaul.client.layer.impl.ShadowLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import net.minecraft.world.phys.Vec2;

public class DefaultShadow implements ShadowLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {
        int x0 = (int) position.x - 1;
        int y0 = (int) position.y;
        int x1 = (int) (position.x + context.getTooltipSize().x + 4);
        int y1 = (int) (position.y + context.getTooltipSize().y + 4);

        int bgColor = 0x80000000;

        // Background
        context.getGraphics().fill(x0, y0, x1, y1, 0, bgColor);

        // Top border
        context.getGraphics().fill(x0, y0 - 1, x1, y0, 0, bgColor);
        // Bottom border
        context.getGraphics().fill(x0, y1 + 1, x1, y1, 0, bgColor);
        // Left border
        context.getGraphics().fill(x0 - 1, y0, x0, y1, 0, bgColor);
        // Right border
        context.getGraphics().fill(x1 + 1, y0, x1, y1, 0, bgColor);
    }

}
