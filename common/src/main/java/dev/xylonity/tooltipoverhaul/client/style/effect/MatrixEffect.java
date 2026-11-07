package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectRuntime;
import net.minecraft.client.Minecraft;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class MatrixEffect extends AmbientEffect {

    private static final int TRAIL = 8;

    /// Glyph pattern variation
    private static final String[] GLYPHS = {
            "111101101101111", "010110010010111", "111001011010100", "101101010100100",
            "111010010010010", "110001110001110", "100111100100111", "011100111001110",
            "101010101010101", "111100110100100", "010111010010001", "001010100010001"
    };

    @Override
    protected boolean clipToTooltip() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.5f;
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final int rain = color(0, 0xFF3CFF6E);
        final int dim = color(1, 0xFF0F8F3A);
        final int head = color(2, 0xFFD8FFE0);

        // Glyph pixels snap to whole screen pixels
        final float guiScale = (float) Math.max(1, Minecraft.getInstance().getWindow().getGuiScale());
        final float scale = Math.max(1, Math.round(parameter(SIZE) * guiScale)) / guiScale;
        final float cellWidth = 4 * scale, cellHeight = 6 * scale;

        final int fromX = (int) Math.floor(canvas.left) + 1, top = (int) Math.floor(canvas.top) + 1;
        final int columns = Math.max(1, (int) ((canvas.width - 1) / cellWidth)), rows = (int) Math.ceil(canvas.height / cellHeight);

        final float density = EffectRuntime.density();
        final int streams = (int) Math.ceil(density);

        /// Column alignment
        for (int column = 0; column < columns; column++) {
            for (int stream = 0; stream < streams; stream++) {
                // Below one some columns stay independent, above one extra drops share the same column so the density option works properly
                if (density - stream < 1 && seed(column, 6 + stream) >= density - stream) {
                    continue;
                }

                final int drop = column + stream * 977;
                final float speed = 7 + seed(drop, 1) * 7, loop = rows + TRAIL + 4 + seed(drop, 2) * 10;
                final float travel = (float) canvas.time * speed - seed(drop, 3) * 14 - stream * loop / streams;
                final float position = travel < 0 ? travel : travel % loop;

                final int lead = (int) Math.floor(position);

                for (int trail = 0; trail < TRAIL; trail++) {
                    final int row = lead - trail;
                    if (row < 0 || row >= rows) {
                        continue;
                    }

                    // Glyph decor
                    final int cell = column * 131 + row;
                    final int shape = (int) (seed(cell, 4 + (int) Math.floor(canvas.time * (1 + seed(cell, 5) * 2))) * GLYPHS.length);
                    final int tint = trail == 0 ? head : mix(rain, dim, trail / (float) TRAIL);
                    final float alpha = trail == 0 ? 0.95f : 0.75f * (1 - trail / (float) TRAIL);
                    glyph(canvas, GLYPHS[shape], fromX + column * cellWidth, top + row * cellHeight, scale, tint, alpha);
                }

            }

        }

    }

    private static void glyph(EffectCanvas canvas, String bits, float x, float y, float scale, int color, float alpha) {
        for (int row = 0; row < 5; row++) {
            int start = -1;
            for (int column = 0; column <= 3; column++) {
                final boolean lit = column < 3 && bits.charAt(row * 3 + column) == '1';
                // First one
                if (lit && start < 0) {
                    start = column;
                }
                // Trail
                else if (!lit && start >= 0) {
                    canvas.rectangle(x + start * scale, y + row * scale, x + column * scale, y + (row + 1) * scale, color, alpha);
                    start = -1;
                }

            }

        }

    }

}
