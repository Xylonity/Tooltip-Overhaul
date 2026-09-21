package dev.xylonity.tooltipoverhaul.client.style.shadow;

import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.impl.PreviewBackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import net.minecraft.world.phys.Vec2;

public class DefaultPreviewShadow implements PreviewBackgroundLayer {

    @Override
    public void render(TooltipContext context, Vec2 startPosition, Vec2 endPosition) {
        final int x0 = (int) startPosition.x;
        final int y0 = (int) startPosition.y;
        final int x1 = (int) endPosition.x;
        final int y1 = (int) endPosition.y;
        if (x0 - x1 < 2 || y1 - y0 < 2) {
            return;
        }

        RenderUtils.fillBackgroundShape(context.getGraphics(), x1 + 2, y0 + 2, x0 + 2, y1 + 2, DefaultShadow.COLOR, RenderUtils.getPreviewPanelBackgroundCornerType(context));
    }

    @Override
    public LayerDepth getLayerDepth() {
        return LayerDepth.SHADOW;
    }

}
