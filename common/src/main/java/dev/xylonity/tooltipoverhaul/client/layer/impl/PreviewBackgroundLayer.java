package dev.xylonity.tooltipoverhaul.client.layer.impl;

import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextAxis;
import net.minecraft.world.phys.Vec2;

public interface PreviewBackgroundLayer extends ITooltipLayer {

    void render(TooltipContext context, Vec2 startPosition, Vec2 endPosition);

    @Override
    default void renderInternal(TooltipContext context) {
        context.push(() -> {
            context.translate(0, 0, getLayerDepth().getZ());
            context.setLayerDepth(getLayerDepth());

            Vec2 position = context.getTooltipPosition();
            int sizeX = RenderUtils.calculateSecondPanelSize(context, TextAxis.X);
            int sizeY = RenderUtils.calculateSecondPanelSize(context, TextAxis.Y);
            int x0 = (int) (position.x - 22);
            int y0 = (int) (position.y - 2);
            int x1 = (int) (position.x - 26 - sizeX);
            int y1 = (int) (position.y + sizeY);

            render(context, new Vec2(x0, y0), new Vec2(x1, y1));
        });

    }

    @Override
    default LayerDepth getLayerDepth() {
        return LayerDepth.BACKGROUND;
    }

}
