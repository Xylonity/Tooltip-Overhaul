package dev.xylonity.tooltipoverhaul.client.style.preview;

import net.minecraft.client.gui.GuiGraphics;

import java.util.Locale;
import java.util.function.IntUnaryOperator;

public final class PreviewPanelDecorations {

    public static final int TRIANGLE_WIDTH = 4;

    public enum SideTriangles {
        NONE,
        STYLE_1,
        STYLE_2;

        public static SideTriangles fromString(String value) {
            if ("true".equalsIgnoreCase(value)) {
                return STYLE_2;
            }
            try {
                return valueOf(value.trim().toUpperCase(Locale.ROOT));
            }
            catch (IllegalArgumentException | NullPointerException ignored) {
                return NONE;
            }

        }

    }

    public enum CornerType {
        DEFAULT,
        ROUNDED,
        BEVEL,
        INNER,
        CUT,
        THICK,
        BRACKET,
        BLOCK,
        NOTCH,
        WELD,
        GEM;

        public static CornerType fromString(String value) {
            try {
                return valueOf(value.trim().toUpperCase(Locale.ROOT));
            }
            catch (IllegalArgumentException | NullPointerException ignored) {
                return DEFAULT;
            }

        }

    }

    public enum BackgroundCornerType {
        DEFAULT,
        SQUARE,
        ROUNDED,
        NOTCH;

        public static BackgroundCornerType fromString(String value) {
            try {
                return valueOf(value.trim().toUpperCase(Locale.ROOT));
            }
            catch (IllegalArgumentException | NullPointerException ignored) {
                return DEFAULT;
            }

        }

    }

    public static int triangleOutset(int height, int row) {
        if (row <= 0 || row >= height - 1) {
            return 0;
        }

        int size = Math.min(TRIANGLE_WIDTH, Math.max(0, (height - 2) / 2));
        final int distance = Math.max((height - 1) / 2 - row, row - height / 2);
        return Math.max(0, size - distance);
    }

    public static void renderSides(GuiGraphics graphics, int left, int top, int right, int bottom, SideTriangles style, IntUnaryOperator colorAtRow) {
        final int height = bottom - top;
        for (int row = 1; row < height - 1; row++) {
            final int outset = style == SideTriangles.STYLE_2 ? triangleOutset(height, row) : 0;
            int color = colorAtRow.applyAsInt(row);
            graphics.fill(left - outset, top + row, left - outset + 1, top + row + 1, color);
            graphics.fill(right + outset - 1, top + row, right + outset, top + row + 1, color);
        }

        if (style != SideTriangles.STYLE_1) {
            return;
        }

        // Filled triangles
        final int size = Math.min(TRIANGLE_WIDTH, Math.max(0, (height - 2) / 2));
        for (int column = 1; column <= size; column++) {
            final int first = (height - 1) / 2 - size + column;
            final int last = height / 2 + size - column;
            for (int row = first; row <= last; row++) {
                final int color = colorAtRow.applyAsInt(row);
                graphics.fill(left - column, top + row, left - column + 1, top + row + 1, color);
                graphics.fill(right + column - 1, top + row, right + column, top + row + 1, color);
            }

        }

    }

}