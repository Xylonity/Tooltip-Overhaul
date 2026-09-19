package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectField;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

abstract class AtmosphericFieldEffect extends AmbientEffect {

    private static final float WORLD_SCALE = 1f / 144f;

    @Override
    protected final void draw(final EffectCanvas canvas) {
        final EffectField.Grid grid = field().grid(canvas.width, canvas.height);
        final int columns = grid.columns();
        final int rows = grid.rows();
        final float pixelSize = Math.max(canvas.width / columns, canvas.height / rows) * WORLD_SCALE * parameter(FIELD_SCALE);
        int[] upper = new int[columns + 1];
        int[] lower = new int[columns + 1];

        sampleRow(canvas, 0, pixelSize, upper);

        for (int row = 0; row < rows; row++) {
            final float v0 = row / (float) rows;
            final float v1 = (row + 1f) / rows;
            final float y0 = canvas.top + v0 * canvas.height;
            final float y1 = canvas.top + v1 * canvas.height;

            sampleRow(canvas, v1, pixelSize, lower);

            for (int column = 0; column < columns; column++) {
                final float x0 = canvas.left + canvas.width * column / columns;
                final float x1 = canvas.left + canvas.width * (column + 1) / columns;
                canvas.vertex(x0, y0, upper[column], 1);
                canvas.vertex(x1, y0, upper[column + 1], 1);
                canvas.vertex(x1, y1, lower[column + 1], 1);
                canvas.vertex(x0, y0, upper[column], 1);
                canvas.vertex(x1, y1, lower[column + 1], 1);
                canvas.vertex(x0, y1, lower[column], 1);
            }

            final int[] swap = upper;
            upper = lower;
            lower = swap;
        }

        drawAccents(canvas);
    }

    private void sampleRow(EffectCanvas canvas, float vertical, float pixelSize, int[] colors) {
        final float zoom = parameter(FIELD_SCALE);
        final float fieldY = 0.5f + (vertical - 0.5f) * canvas.height * WORLD_SCALE * zoom;
        for (int column = 0; column < colors.length; column++) {
            final float x = canvas.width * column / (colors.length - 1);
            final float fieldX = 0.5f + (x - canvas.width * 0.5f) * WORLD_SCALE * zoom;
            colors[column] = sample(fieldX, fieldY, canvas.time, pixelSize);
        }

    }

    protected abstract int sample(float fieldX, float fieldY, double time, float pixelSize);

    @Override
    protected final void drawAfterField(EffectCanvas canvas) {
        drawAccents(canvas);
    }

    protected void drawAccents(EffectCanvas canvas) {
        ;;
    }

    protected static int tint(int color, float alpha, float rim) {
        return (color & 0x00FFFFFF) | (Math.round(veilOpacity(color, alpha, rim) * 255) << 24);
    }

    @Override
    protected final boolean clipToTooltip() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.8f;
    }

}
