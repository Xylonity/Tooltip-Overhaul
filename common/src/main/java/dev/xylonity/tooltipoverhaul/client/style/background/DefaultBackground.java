package dev.xylonity.tooltipoverhaul.client.style.background;

import dev.xylonity.tooltipoverhaul.client.layer.impl.BackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
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

        final String corner = RenderUtils.getBackgroundCornerType(context);
        if (corner.equals("notch")) {
            final int pixelOffset = 1;
            context.getGraphics().fill(x0, y0 + pixelOffset, x1, y1 - pixelOffset, backgroundColor);
            context.getGraphics().fill(x0 + pixelOffset, y0, x1 - pixelOffset, y1, backgroundColor);
            context.getGraphics().fill(x0 + pixelOffset, y0 - 1, x1 - pixelOffset, y0, backgroundColor);
            context.getGraphics().fill(x0 + pixelOffset, y1, x1 - pixelOffset, y1 + 1, backgroundColor);
            context.getGraphics().fill(x0 - 1, y0 + pixelOffset, x0, y1 - pixelOffset, backgroundColor);
            context.getGraphics().fill(x1, y0 + pixelOffset, x1 + 1, y1 - pixelOffset, backgroundColor);
            return;
        }

        final int trim = corner.equals("rounded") ? 1 : 0;

        // Background
        context.getGraphics().fill(x0, y0, x1, y1, backgroundColor);

        // Top border
        context.getGraphics().fill(x0 + trim, y0 - 1, x1 - trim, y0, backgroundColor);
        // Bottom border
        context.getGraphics().fill(x0 + trim, y1 + 1, x1 - trim, y1, backgroundColor);
        // Left border
        context.getGraphics().fill(x0 - 1, y0 + trim, x0, y1 - trim, backgroundColor);
        // Right border
        context.getGraphics().fill(x1 + 1, y0 + trim, x1, y1 - trim, backgroundColor);

        if (corner.equals("square")) {
            context.getGraphics().fill(x0 - 1, y0 - 1, x0, y0, backgroundColor);
            context.getGraphics().fill(x1, y0 - 1, x1 + 1, y0, backgroundColor);
            context.getGraphics().fill(x0 - 1, y1, x0, y1 + 1, backgroundColor);
            context.getGraphics().fill(x1, y1, x1 + 1, y1 + 1, backgroundColor);
        }
    }

}