package dev.xylonity.tooltipoverhaul.client.layer.impl;

import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.layout.FloatingLayout;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipLayout;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.style.preview.PreviewPanelDecorations;
import dev.xylonity.tooltipoverhaul.client.util.PositionUtils;
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

            if (context.getLayoutStyle() == TooltipLayout.Style.FLOATING) {
                final FloatingLayout layout = context.getFloatingLayout();
                final Vec2 position = context.getTooltipPosition();
                render(context, position.add(layout.previewStart()), position.add(layout.previewEnd()));
                return;
            }

            final int leftOverflow = TooltipLayout.leftOverflow(context);
            final int sideExtent = RenderUtils.hasPreviewPanelSideTriangles(context) ? PreviewPanelDecorations.TRIANGLE_WIDTH : 0;
            final int margin = 22 + leftOverflow + sideExtent;
            final Vec2 position = context.getTooltipPosition();
            final int sizeX = RenderUtils.calculateSecondPanelSize(context, TextAxis.X);
            final int sizeY = RenderUtils.calculateSecondPanelSize(context, TextAxis.Y);
            final int tooltipSizeX = (int) context.getTooltipSize().x;
            final int x0 = (int) (position.x - margin);
            final int y0 = (int) (position.y - 2);
            final int x1 = (int) (position.x - margin - sizeX - 4);
            final int y1 = (int) (position.y + sizeY);

            final Vec2 originalPos;
            final Vec2 finalPos;

            final int extraX = PositionUtils.getSecondPanelPosition(context, TextAxis.X);
            final int extraY = PositionUtils.getSecondPanelPosition(context, TextAxis.Y);

            // Repositions the preview panel components if there is insufficient space at the left
            if (x1 - sideExtent < 0 && TooltipsConfig.AUTO_REPOSITION_PREVIEW_PANEL) {
                originalPos = new Vec2(x0 + tooltipSizeX + margin * 2 - leftOverflow + context.getPaddingX() + sizeX - extraX, y0 + extraY);
                finalPos = new Vec2(x1 + tooltipSizeX + margin * 2 - leftOverflow + context.getPaddingX() + sizeX - extraX, y1 + extraY);
            }
            else {
                originalPos = new Vec2(x0 + extraX, y0 + extraY);
                finalPos = new Vec2(x1 + extraX, y1 + extraY);
            }

            render(context, originalPos, finalPos);
        });

    }

    @Override
    default LayerDepth getLayerDepth() {
        return LayerDepth.BACKGROUND;
    }

}
