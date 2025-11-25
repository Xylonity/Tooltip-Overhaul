package dev.xylonity.tooltipoverhaul.client.layer.impl;

import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextAxis;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import net.minecraft.world.phys.Vec2;

public interface PreviewBackgroundLayer extends ITooltipLayer {

    void render(TooltipContext context, Vec2 startPosition, Vec2 endPosition);

    @Override
    default void renderInternal(TooltipContext context) {
        context.push(() -> {
            context.translate(0, 0, getLayerDepth().getZ());
            context.setLayerDepth(getLayerDepth());

            int margin = 22;
            Vec2 position = context.getTooltipPosition();
            int sizeX = RenderUtils.calculateSecondPanelSize(context, TextAxis.X);
            int sizeY = RenderUtils.calculateSecondPanelSize(context, TextAxis.Y);
            int tooltipSizeX = (int) context.getTooltipSize().x;
            int x0 = (int) (position.x - margin);
            int y0 = (int) (position.y - 2);
            int x1 = (int) (position.x - margin - sizeX - 4);
            int y1 = (int) (position.y + sizeY);

            Vec2 originalPos;
            Vec2 finalPos;

            // Reposition the preview panel components if there is insufficient space at the left
            if (x1 < 0 && TooltipsConfig.AUTO_REPOSITION_PREVIEW_PANEL) {
                originalPos = new Vec2(x0 + tooltipSizeX + margin * 2 + context.getPaddingX() + sizeX, y0);
                finalPos = new Vec2(x1 + tooltipSizeX + margin * 2 + context.getPaddingX() + sizeX, y1);
            }
            else {
                originalPos = new Vec2(x0, y0);
                finalPos = new Vec2(x1, y1);
            }

            render(context, originalPos, finalPos);
        });

    }

    @Override
    default LayerDepth getLayerDepth() {
        return LayerDepth.BACKGROUND;
    }

}
