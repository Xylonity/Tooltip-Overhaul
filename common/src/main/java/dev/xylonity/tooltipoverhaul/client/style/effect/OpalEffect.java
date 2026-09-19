package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectField;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class OpalEffect extends AmbientEffect {

    @Override
    protected EffectField field() {
        return EffectField.OPAL;
    }

    @Override
    protected boolean clipToTooltip() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.80f;
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final EffectField.Grid grid = field().grid(canvas.width, canvas.height);
        final float step = grid.step(), scale = grid.scale();
        final int columns = grid.columns(), rows = grid.rows();

        int[] upperColors = new int[columns + 1];
        int[] lowerColors = new int[columns + 1];
        float[] upperAlpha = new float[columns + 1];
        float[] lowerAlpha = new float[columns + 1];

        sampleRow(canvas, canvas.top, step, scale, upperColors, upperAlpha);

        for (int row = 0; row < rows; row++) {
            final float y0 = canvas.top + row * step, y1 = Math.min(canvas.bottom, canvas.top + (row + 1) * step);

            sampleRow(canvas, y1, step, scale, lowerColors, lowerAlpha);

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
        final double clock = canvas.time * 0.22;
        final float zoom = parameter(FIELD_SCALE), distortion = parameter(DISTORTION);
        for (int col = 0; col < colors.length; col++) {
            final float fieldX = Math.min(canvas.width, col * step) * scale * 0.026f * zoom;
            final float fieldY = (y - canvas.top) * scale * 0.034f * zoom;
            float wx = fieldX + sin(fieldY * 0.9 + clock * 0.6) * 0.65f * distortion;
            wx += (noise(fieldX * 0.7 + clock * 0.25, fieldY * 0.8 + 7) - 0.5f) * 0.8f * distortion;

            float wy = fieldY + sin(fieldX * 0.72 - clock * 0.5) * 0.55f * distortion;
            wy += (noise(fieldX * 0.5 + 13, fieldY * 0.9 + clock * 0.2) - 0.5f) * 0.65f * distortion;

            float cloud = noise(wx * 0.8 + 41 + clock * 0.12, wy * 0.8 + 17 - clock * 0.1) * 0.7f + noise(wx * 1.6 + 93, wy * 1.6 + 51 + clock * 0.13) * 0.3f;
            final float contour = cloud - 0.52f + sin(wx * 1.1 + wy * 0.6 + clock * 0.4) * 0.07f;

            final float vein = (float) Math.exp(-contour * contour * 260 * parameter(SHARPNESS));
            final float veil = smooth((cloud - 0.26f) / 0.42f);

            final float hue = 0.5f + 0.5f * sin(wx * 0.85 + wy * 0.55 - clock * 0.3);
            int tint = hue < 0.5f ? mix(color(0, 0xFF77BBB5), color(1, 0xFFA69BD3), hue * 2) : mix(color(1, 0xFFA69BD3), color(2, 0xFFD5B596), hue * 2 - 1);

            colors[col] = mix(tint, color(0, 0xFF7F88A9), veil * 0.25f);
            alpha[col] = veilOpacity(colors[col], (veil * 0.14f + vein * 0.30f) * (0.75f + cloud * 0.25f), vein * vein * vein);
        }

    }

}