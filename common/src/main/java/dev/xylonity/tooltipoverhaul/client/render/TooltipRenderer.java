package dev.xylonity.tooltipoverhaul.client.render;

import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipPositionCalculator;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipSizeCalculator;
import dev.xylonity.tooltipoverhaul.client.util.TooltipScrollState;
import net.minecraft.world.phys.Vec2;

import javax.annotation.Nullable;
import java.util.List;

public class TooltipRenderer {

    private final @Nullable TooltipContext context;

    public static float COUNTER = 0;

    public TooltipRenderer(@Nullable TooltipContext context) {
        this.context = context;
    }

    public void init() {
        if (context == null) return;

        if (context.getComponents().isEmpty()) return;

        TooltipSizeCalculator sizeCalculator = context.getSizeCalculator();
        TooltipPositionCalculator positionCalculator = context.getPositionCalculator();

        int margin = 5;

        // Uncapped size calculation
        Vec2 uncappedSize = sizeCalculator.calculate();

        int screenHeight = context.getScreenHeight();
        int maxTooltipHeight = screenHeight - margin;
        int cappedHeight = Math.min((int) uncappedSize.y, maxTooltipHeight);

        context.setTooltipSize(new Vec2(uncappedSize.x, cappedHeight));
        context.setTooltipPosition(positionCalculator.calculate());

        // Calculates the header (non-scrollable)
        int headerHeight = sizeCalculator.calculateHeaderHeight();

        // Calculates the scrollable height
        int scrollableContentHeight = sizeCalculator.calculateScrollableContentHeight();

        // The max height to show scrollable content
        int availableViewportHeight = cappedHeight - headerHeight - (context.getPaddingY() * 2);

        TooltipScrollState.begin(scrollableContentHeight, availableViewportHeight);
        TooltipScrollState.tick();
    }

    /**
     * Simple bridge to readjust the tooltip positions in case the equipped stack is enabled and any (or both) tooltip layouts are
     * exceeding the screen margins. The init predicate is called to compute the default layout values.
     */
    public void adjustLayout() {
        if (context == null) return;

        if (context.getComponents().isEmpty()) return;

        context.setTooltipPosition(context.getPositionCalculator().adjustPosition());
        context.setTooltipSize(context.getSizeCalculator().adjustSize());
    }

    public boolean render() {

        if (context == null) return false;

        if (context.getComponents().isEmpty()) return false;

        List<ITooltipLayer> layers = context.getTooltipLayers();

        for (ITooltipLayer layer : layers) {
            layer.renderInternal(context);
        }

        return true;
    }

}
