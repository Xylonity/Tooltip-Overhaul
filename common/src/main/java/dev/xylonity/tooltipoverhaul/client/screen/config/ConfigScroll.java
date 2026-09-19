package dev.xylonity.tooltipoverhaul.client.screen.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.dimAccent;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.withAlpha;

/**
 * Shared scrollbar geometry
 */
final class ConfigScroll {

    private static final int WIDTH = 4;
    private static final int RIGHT_INSET = 2;
    private static final int END_INSET = 3;
    private static final int DEFAULT_MINIMUM_THUMB = 20;
    private static final int TRACK_COLOR = 0x3825252B;
    private static final int IDLE_THUMB_COLOR = 0xE0707078;

    static Geometry fromContent(int top, int bottom, int contentHeight, double scroll, int minimumThumb) {
        final int viewport = Math.max(0, bottom - top);
        if (viewport == 0 || contentHeight <= viewport) {
            return null;
        }

        final int trackTop = top + END_INSET;
        final int trackBottom = Math.max(trackTop, bottom - END_INSET);
        final int trackHeight = trackBottom - trackTop;
        if (trackHeight == 0) {
            return null;
        }

        int thumb = Math.min(trackHeight, Math.max(minimumThumb, trackHeight * viewport / contentHeight));
        final double maxScroll = contentHeight - viewport;
        final int thumbTop = trackTop + (int) ((trackHeight - thumb) * Mth.clamp(scroll / maxScroll, 0, 1));
        return new Geometry(trackTop, trackBottom, thumbTop, thumb);
    }

    static Geometry fromContent(int top, int bottom, int contentHeight, double scroll) {
        return fromContent(top, bottom, contentHeight, scroll, DEFAULT_MINIMUM_THUMB);
    }

    static Geometry fromMaximum(int top, int bottom, double maximum, double scroll, int minimumThumb) {
        return fromContent(top, bottom, (int) Math.ceil(bottom - top + maximum), scroll, minimumThumb);
    }

    static double valueFromMouse(double mouseY, Geometry geometry, double maximum) {
        if (geometry == null || geometry.span() <= 0) {
            return 0;
        }

        double progress = (mouseY - geometry.top() - geometry.thumbHeight() / 2.0) / geometry.span();
        return Mth.clamp(progress, 0, 1) * maximum;
    }

    static double valueFromDrag(double mouseY, double dragOffset, Geometry geometry, double maximum) {
        if (geometry == null || geometry.span() <= 0) {
            return 0;
        }

        final double progress = (mouseY - dragOffset - geometry.top()) / geometry.span();
        return Mth.clamp(progress, 0, 1) * maximum;
    }

    static void render(GuiGraphics graphics, int rightEdge, Geometry geometry, int accent, double mouseX, double mouseY, boolean active) {
        if (geometry == null) {
            return;
        }

        final int x = rightEdge - RIGHT_INSET - WIDTH;
        drawVerticalPill(graphics, x + 1, geometry.top(), WIDTH - 2, geometry.bottom() - geometry.top(), TRACK_COLOR);
        final int thumbColor = active || isHovered(rightEdge, geometry, mouseX, mouseY) ? withAlpha(dimAccent(accent), 0xE0) : IDLE_THUMB_COLOR;
        drawVerticalPill(graphics, x, geometry.thumbTop(), WIDTH, geometry.thumbHeight(), thumbColor);
    }

    static boolean isHovered(int rightEdge, Geometry geometry, double mouseX, double mouseY) {
        return geometry != null && mouseX >= rightEdge - 8 && mouseX < rightEdge && mouseY >= geometry.top() && mouseY < geometry.bottom();
    }

    private static void drawVerticalPill(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        if (width <= 0 || height <= 0) {
            return;
        }

        if (width < 3 || height < 3) {
            graphics.fill(x, y, x + width, y + height, color);
            return;
        }

        graphics.fill(x + 1, y, x + width - 1, y + 1, color);
        graphics.fill(x, y + 1, x + width, y + height - 1, color);
        graphics.fill(x + 1, y + height - 1, x + width - 1, y + height, color);
    }

    record Geometry(
            int top,
            int bottom,
            int thumbTop,
            int thumbHeight
    ) {

        int span() {
            return bottom - top - thumbHeight;
        }

    }

}
