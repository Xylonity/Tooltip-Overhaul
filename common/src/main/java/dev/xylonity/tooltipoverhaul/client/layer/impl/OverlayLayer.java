package dev.xylonity.tooltipoverhaul.client.layer.impl;

import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;

public interface OverlayLayer extends ITooltipLayer {

    @Override
    default void renderInternal(TooltipContext context) {
        context.push(() -> {
            LayerDepth layerDepth = LayerDepth.OVERLAY;
            context.translate(0, 0, layerDepth.getZ());
            context.setLayerDepth(layerDepth);
            render(context, context.getTooltipPosition());
        });

    }

}
