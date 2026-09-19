package dev.xylonity.tooltipoverhaul.client.style.divider;

import dev.xylonity.tooltipoverhaul.client.layer.impl.DividerLineLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import net.minecraft.world.phys.Vec2;

public final class PatternDividerLine implements DividerLineLayer {

    private final int dash;
    private final int gap;

    public PatternDividerLine(int dash, int gap) {
        this.dash = dash;
        this.gap = gap;
    }

    @Override
    public void render(TooltipContext context, Vec2 position) {
        final int width = Math.max(0, (int) context.getTooltipSize().x - context.getPaddingX() * 2);
        final int count = Math.max(1, (width + gap) / (dash + gap));
        final int length = Math.min(width, count * (dash + gap) - gap);
        final int left = (int) position.x + (width - length) / 2;
        final int y = (int) position.y;
        final int color = ColorUtils.getDividerLineColor(context);
        for (int offset = 0; offset < length; offset += dash + gap) {
            context.getGraphics().fill(left + offset, y, left + Math.min(length, offset + dash), y + 1, color);
        }

    }

}
