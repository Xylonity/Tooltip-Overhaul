package dev.xylonity.tooltipoverhaul.client.style.background;

import dev.xylonity.tooltipoverhaul.client.layer.impl.BackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.style.preview.PreviewPanelDecorations;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import net.minecraft.world.phys.Vec2;

public class DefaultBackground implements BackgroundLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {
        final int x0 = (int) (position.x - 3);
        final int y0 = (int) (position.y - 2);
        final int x1 = (int) (position.x + context.getTooltipSize().x + 2);
        final int y1 = (int) (position.y + context.getTooltipSize().y + 2);

        final int backgroundColor = ColorUtils.getBackgroundColor(context);
        final PreviewPanelDecorations.BackgroundCornerType corner = PreviewPanelDecorations.BackgroundCornerType.fromString(RenderUtils.getBackgroundCornerType(context));

        RenderUtils.fillBackgroundShape(context.getGraphics(), x0, y0, x1, y1, backgroundColor, corner);
    }

}
