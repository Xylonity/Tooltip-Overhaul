package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectField;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class PrismEffect extends AmbientEffect {

    @Override
    protected EffectField field() {
        return EffectField.PRISM;
    }

    @Override
    protected boolean clipToTooltip() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.85f;
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final EffectField.Grid grid = field().grid(canvas.width, canvas.height);
        final float step = grid.step(), fieldScale = grid.scale();

        final int columns = grid.columns();
        final int rows = grid.rows();

        int[] upperColors = new int[columns + 1];
        int[] lowerColors = new int[columns + 1];
        float[] upperAlpha = new float[columns + 1];
        float[] lowerAlpha = new float[columns + 1];

        sampleRow(canvas, canvas.top, step, fieldScale, upperColors, upperAlpha);

        for (int row = 0; row < rows; row++) {
            final float y0 = canvas.top + row * step, y1 = Math.min(canvas.bottom, canvas.top + (row + 1) * step);

            sampleRow(canvas, y1, step, fieldScale, lowerColors, lowerAlpha);

            for (int col = 0; col < columns; col++) {
                final float x0 = canvas.left + col * step, x1 = Math.min(canvas.right, canvas.left + (col + 1) * step);
                canvas.vertex(x0, y0, upperColors[col], upperAlpha[col]);
                canvas.vertex(x1, y0, upperColors[col + 1], upperAlpha[col + 1]);
                canvas.vertex(x1, y1, lowerColors[col + 1], lowerAlpha[col + 1]);
                canvas.vertex(x0, y0, upperColors[col], upperAlpha[col]);
                canvas.vertex(x1, y1, lowerColors[col + 1], lowerAlpha[col + 1]);
                canvas.vertex(x0, y1, lowerColors[col], lowerAlpha[col]);
            }

            int[] colors = upperColors; upperColors = lowerColors; lowerColors = colors;
            float[] alpha = upperAlpha; upperAlpha = lowerAlpha; lowerAlpha = alpha;
        }

    }

    private static void sampleRow(EffectCanvas canvas, float y, float step, float scale, int[] colors, float[] alpha) {
        final float zoom = parameter(FIELD_SCALE);

        final float rotation = patternPhase(80), cs = cos(rotation), sn = sin(rotation);
        final float phaseX = patternPhase(81), phaseY = patternPhase(82);

        final float distortion = parameter(DISTORTION), sharpness = parameter(SHARPNESS);

        final float focusWidth = Math.min(15 * sharpness, 18 / (zoom * zoom));
        for (int col = 0; col < colors.length; col++) {
            final float x = Math.min(canvas.width, col * step) * scale * zoom;
            final float yLocal = (y - canvas.top) * scale * zoom;
            final float rotatedX = x * cs - yLocal * sn;
            final float rotatedY = x * sn + yLocal * cs;

            final double clock = canvas.time * 0.30;
            final float warp = sin(rotatedX * 0.045 + rotatedY * 0.075 + clock + phaseX) * 0.75f * distortion;
            float field = sin(rotatedX * 0.078 + warp + clock + phaseY) + sin(rotatedY * 0.10 - clock + phaseX + sin(rotatedX * 0.052 + phaseY) * 0.8 * distortion);

            final float focus = (float) Math.exp(-field * field * focusWidth);
            final float halo = (float) Math.exp(-field * field * 2.8);

            final float hue = 0.5f + 0.5f * sin(rotatedX * 0.026 + rotatedY * 0.041 - clock * 0.4);
            colors[col] = hue < 0.5f ? mix(color(0, 0xFF70CFC6), color(1, 0xFF9390DF), hue * 2) : mix(color(1, 0xFF9390DF), color(2, 0xFFE6BC8B), hue * 2 - 1);

            final float shimmer = 0.65f + 0.35f * noise(rotatedX * 0.05, rotatedY * 0.05 + clock * 0.2);
            alpha[col] = (focus * 0.34f + halo * 0.15f) * shimmer;
        }

    }

}