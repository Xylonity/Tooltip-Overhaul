package dev.xylonity.tooltipoverhaul.client.style.background;

import dev.xylonity.tooltipoverhaul.client.layer.impl.BackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipLayout;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;

public class CompactFooter implements BackgroundLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {
        final int footerHeight = TooltipLayout.footerHeight(context);
        if (footerHeight == 0) {
            return;
        }

        final GuiGraphics graphics = context.getGraphics();
        final int x = (int) position.x;
        final int bottom = (int) (position.y + context.getTooltipSize().y);
        final int right = (int) (position.x + context.getTooltipSize().x);
        final int top = bottom - footerHeight + TooltipLayout.footerTopGap(context);
        final int[] colors = ColorUtils.getRenderedInnerOverlayColors(context);
        graphics.fillGradient(x - 2, top, right + 1, bottom + 1, ColorUtils.mulAlpha(colors[1], 0.05f), ColorUtils.mulAlpha(colors[2], 0.12f));
        if (context.hasDividerLine()) {
            graphics.fill(x + context.getPaddingX(), top, right - context.getPaddingX(), top + 1, ColorUtils.mulAlpha(colors[2], 0.35f));
        }

    }

}