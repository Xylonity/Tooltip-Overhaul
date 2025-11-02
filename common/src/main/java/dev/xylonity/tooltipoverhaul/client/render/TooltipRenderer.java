package dev.xylonity.tooltipoverhaul.client.render;

import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipPositionCalculator;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipSizeCalculator;

import java.awt.*;
import java.util.List;

public class TooltipRenderer {

    private final TooltipContext context;

    public TooltipRenderer(TooltipContext context) {
        this.context = context;
    }

    public boolean render() {

        List<ITooltipLayer> layers = context.getTooltipLayers();
        context.setTooltipSize(new TooltipSizeCalculator(context).calculate());
        context.setTooltipPosition(new TooltipPositionCalculator(context).calculate());
        for (ITooltipLayer layer : layers) {
            layer.renderInternal(context);
        }

        return true;
    }

}
