package dev.xylonity.tooltipoverhaul.client.style.shadow;

import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.impl.PreviewBackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
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

        context.getGraphics().fill(x1 + 1, y0 + 1, x0 + 2, y1 + 3, DefaultShadow.COLOR);
        context.getGraphics().fill(x0 + 2, y0 + 2, x0 + 3, y1 + 2, DefaultShadow.COLOR);
    }

    @Override
    public LayerDepth getLayerDepth() {
        return LayerDepth.SHADOW;
    }

}
