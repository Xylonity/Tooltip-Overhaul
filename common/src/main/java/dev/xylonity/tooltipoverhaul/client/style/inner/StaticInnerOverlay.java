package dev.xylonity.tooltipoverhaul.client.style.inner;

import dev.xylonity.tooltipoverhaul.client.layer.impl.InnerOverlayLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import net.minecraft.world.phys.Vec2;

public class StaticInnerOverlay implements InnerOverlayLayer {

    private final int color;

    public StaticInnerOverlay(int color) {
        this.color = color;
    }

    @Override
    public void render(TooltipContext context, Vec2 position) {
        int x0 = (int) (context.getTooltipPosition().x - 3);
        int y0 = (int) (context.getTooltipPosition().y - 2);
        int width = (int) (context.getTooltipSize().x + 5);
        int height = (int) (context.getTooltipSize().y + 4);

        // Top
        context.getGraphics().fill(x0, y0, x0 + width, y0 + 1, color);

        // Bottom
        context.getGraphics().fill(x0, y0 + height - 1, x0 + width, y0 + height, color);

        // Left
        context.getGraphics().fill(x0, y0, x0 + 1, y0 + height, color);

        // Right
        context.getGraphics().fill(x0 + width - 1, y0, x0 + width, y0 + height, color);
    }

}
