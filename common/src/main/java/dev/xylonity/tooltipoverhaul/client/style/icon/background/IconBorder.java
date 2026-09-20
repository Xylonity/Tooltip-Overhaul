package dev.xylonity.tooltipoverhaul.client.style.icon.background;

import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import net.minecraft.client.gui.GuiGraphics;

final class IconBorder {

    public static void render(TooltipContext context, int left, int top, int right, int bottom, int color) {
        final String style = context.getFrameData() == null ? TooltipsConfig.ICON_BORDER_STYLE : context.getFrameData().getIconBorderStyle();
        render(context.getGraphics(), left, top, right, bottom, color, style);
    }

    public static void render(GuiGraphics graphics, int left, int top, int right, int bottom, int color, String style) {
        final int corner = "style_2".equals(style) ? 1 : 0;

        // Top border
        graphics.fill(left - corner, top - 1, right + corner, top, color);

        // Bottom border
        graphics.fill(left - corner, bottom, right + corner, bottom + 1, color);

        // Left border
        graphics.fill(left - 1, top, left, bottom, color);

        // Right border
        graphics.fill(right, top, right + 1, bottom, color);
    }

}