package dev.xylonity.tooltipoverhaul.client.style.shadow;

import dev.xylonity.tooltipoverhaul.client.layer.impl.ShadowLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.style.preview.PreviewPanelDecorations;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import net.minecraft.world.phys.Vec2;

public class DefaultShadow implements ShadowLayer {

    public static final int COLOR = 0x80000000;

    @Override
    public void render(TooltipContext context, Vec2 position) {
        final int x0 = (int) position.x - 1;
        final int y0 = (int) position.y;
        final int x1 = (int) (position.x + context.getTooltipSize().x + 4);
        final int y1 = (int) (position.y + context.getTooltipSize().y + 4);

        final PreviewPanelDecorations.BackgroundCornerType corner = PreviewPanelDecorations.BackgroundCornerType.fromString(RenderUtils.getBackgroundCornerType(context));
        RenderUtils.fillBackgroundShape(context.getGraphics(), x0, y0, x1, y1, COLOR, corner);
    }

}
