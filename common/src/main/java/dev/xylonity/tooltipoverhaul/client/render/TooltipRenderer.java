package dev.xylonity.tooltipoverhaul.client.render;

import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipPositionCalculator;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipSizeCalculator;
import dev.xylonity.tooltipoverhaul.client.util.TooltipScrollState;
import net.minecraft.world.phys.Vec2;

import java.util.List;

public class TooltipRenderer {

    private final TooltipContext context;

    public TooltipRenderer(TooltipContext context) {
        this.context = context;
    }

    public boolean render() {

        if (context.getComponents().isEmpty()) return false;

        List<ITooltipLayer> layers = context.getTooltipLayers();
        TooltipSizeCalculator sizeCalculator = new TooltipSizeCalculator(context);

        int margin = 5;

        // Uncapped size calculation
        Vec2 uncappedSize = sizeCalculator.calculate();

        int screenHeight = context.getScreenHeight();
        int maxTooltipHeight = screenHeight - margin;
        int cappedHeight = Math.min((int) uncappedSize.y, maxTooltipHeight);

        context.setTooltipSize(new Vec2(uncappedSize.x, cappedHeight));
        context.setTooltipPosition(new TooltipPositionCalculator(context).calculate());

        // Calculates the header (non-scrollable)
        int headerHeight = sizeCalculator.calculateHeaderHeight();

        // Calculates the scrollable height
        int scrollableContentHeight = sizeCalculator.calculateScrollableContentHeight();

        // The max height to show scrollable content
        int availableViewportHeight = cappedHeight - headerHeight - (context.getPaddingY() * 2);

        TooltipScrollState.begin(scrollableContentHeight, availableViewportHeight);
        TooltipScrollState.tick();

        for (ITooltipLayer layer : layers) {
            layer.renderInternal(context);
        }

        return true;
    }

}
