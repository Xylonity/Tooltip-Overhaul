package dev.xylonity.tooltipoverhaul.client.layer.impl;

import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;

import java.awt.*;

public interface BackgroundLayer extends ITooltipLayer {

    @Override
    default void renderInternal(TooltipContext context) {
        context.push(() -> {
            context.translate(0, 0, getLayerDepth().getZ());
            context.setLayerDepth(getLayerDepth());
            render(context, context.getTooltipPosition());
        });

    }

    @Override
    default LayerDepth getLayerDepth() {
        return LayerDepth.BACKGROUND;
    }

}
