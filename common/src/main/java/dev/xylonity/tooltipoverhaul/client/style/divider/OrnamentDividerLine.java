package dev.xylonity.tooltipoverhaul.client.style.divider;

import dev.xylonity.tooltipoverhaul.client.layer.impl.DividerLineLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import net.minecraft.world.phys.Vec2;

public final class OrnamentDividerLine implements DividerLineLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {
        final int width = Math.max(0, (int) context.getTooltipSize().x - context.getPaddingX() * 2);
        final int left = (int) position.x;
        final int right = left + width;
        final int y = (int) position.y;
        final int center = left + width / 2;
        final int color = ColorUtils.getDividerLineColor(context);
        if (width < 9) {
            context.getGraphics().fill(left, y, right, y + 1, color);
            return;
        }

        context.getGraphics().fill(left, y, center - 3, y + 1, color);
        context.getGraphics().fill(center + 4, y, right, y + 1, color);

        // +
        context.getGraphics().fill(center - 1, y, center + 2, y + 1, color);
        context.getGraphics().fill(center, y - 1, center + 1, y, color);
        context.getGraphics().fill(center, y + 1, center + 1, y + 2, color);
    }

}
