package dev.xylonity.tooltipoverhaul.client.style.inner;

import dev.xylonity.tooltipoverhaul.client.layer.impl.InnerOverlayLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import net.minecraft.world.phys.Vec2;

public class StaticInnerOverlay implements InnerOverlayLayer {

    private final int color;

    public StaticInnerOverlay(int color) {
        this.color = color;
    }

    @Override
    public void render(TooltipContext context, Vec2 position) {
        final int x0 = (int) (context.getTooltipPosition().x - 3);
        final int y0 = (int) (context.getTooltipPosition().y - 2);
        final int width = (int) (context.getTooltipSize().x + 5);
        final int height = (int) (context.getTooltipSize().y + 4);

        // The notch background leaves the corner pixels of the inner rect transparent
        final String cornerType = RenderUtils.getInnerFrameCornerType(context);
        final int trim = Math.max(RenderUtils.getBackgroundCornerType(context).equals("notch") ? 1 : 0, RenderUtils.cornerTrim(cornerType));
        final int sideTrim = Math.max(1, trim);

        // Top
        context.getGraphics().fill(x0 + trim, y0, x0 + width - trim, y0 + 1, color);

        // Bottom
        context.getGraphics().fill(x0 + trim, y0 + height - 1, x0 + width - trim, y0 + height, color);

        // Left
        context.getGraphics().fill(x0, y0 + sideTrim, x0 + 1, y0 + height - sideTrim, color);

        // Right
        context.getGraphics().fill(x0 + width - 1, y0 + sideTrim, x0 + width, y0 + height - sideTrim, color);

        RenderUtils.applyFrameCorners(context.getGraphics(), x0, y0, width, height, color, color, cornerType);
    }

}
