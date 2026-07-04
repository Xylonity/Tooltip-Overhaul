package dev.xylonity.tooltipoverhaul.client.style.inner;

import dev.xylonity.tooltipoverhaul.client.layer.impl.InnerOverlayLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class GradientInnerOverlay implements InnerOverlayLayer {

    private final int color1;
    private final int color2;
    private final int color3;

    public GradientInnerOverlay(int color1, int color2, int color3) {
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
    }

    @Override
    public void render(TooltipContext context, Vec2 position) {
        int x0 = (int) (context.getTooltipPosition().x - 3);
        int y0 = (int) (context.getTooltipPosition().y - 2);
        int width = (int) (context.getTooltipSize().x + 5);
        int height = (int) (context.getTooltipSize().y + 4);

        // The notch background leaves the corner pixels of the inner rect transparent
        final int trim = RenderUtils.getBackgroundCornerType(context).equals("notch") ? 1 : 0;

        RenderUtils.renderFrameGradient(context.getGraphics(), x0, y0 + 1, width, height - 2, color1, color2, color3);

        // Top and bottom lines
        context.getGraphics().fill(x0 + trim, y0, x0 + width - trim, y0 + 1, color1);
        context.getGraphics().fill(x0 + trim, y0 + height - 1, x0 + width - trim, y0 + height, color3);

        RenderUtils.applyFrameCorners(context.getGraphics(), x0, y0, width, height, color1, color3, ColorUtils.getBackgroundColor(context), RenderUtils.getInnerFrameCornerType(context), trim == 1);
    }

}
