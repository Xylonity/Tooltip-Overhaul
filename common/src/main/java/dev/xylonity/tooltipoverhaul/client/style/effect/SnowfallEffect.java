package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class SnowfallEffect extends AmbientEffect {

    @Override
    protected boolean hasMaterial() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.65f;
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        for (int i = 0; i < canvas.particleCount(47); i += 6) {
            float phase = phase(canvas, i);
            Point point = position(canvas, i, phase);
            final float flash = cycle(canvas.time / (3.6 + seed(i, 7) * 2.8) + seed(i, 8));
            final float pulse = bell(clamp(flash / 0.42f));
            final float alpha = life(phase) * pulse * (0.55f + seed(i, 1) * 0.30f);
            if (alpha < 0.002f) {
                continue;
            }

            float radius = 1.6f + seed(i, 1) * 1.1f;
            canvas.glow(point.x(), point.y(), radius * 3, color(2, 0xFFA9CEEF), alpha * 0.22f);
            canvas.sparkle(point.x(), point.y(), radius, rotation(canvas, i), color(1, 0xFFEEF7FF), alpha);
        }

    }

    private static float phase(EffectCanvas canvas, int i) {
        return cycle(canvas.time / (9 + seed(i, 2) * 10) + seed(i, 3));
    }

    private static Point position(EffectCanvas canvas, int i, float phase) {
        float x = canvas.left - 7 + seed(i, 4) * (canvas.width + 14);
        x += (sin(phase * 5 + i * 1.7) * (3 + seed(i, 1) * 5) + flow(i, canvas.time * 0.16) * 5) * parameter(TURBULENCE);
        return new Point(x, canvas.top - 8 + phase * (canvas.height + 18));
    }

    private static float rotation(EffectCanvas canvas, int i) {
        return i + (float) canvas.time * (0.12f + seed(i, 1) * 0.12f) * parameter(ROTATION_SPEED);
    }

    @Override
    protected void drawMaterial(EffectCanvas canvas) {
        final int count = canvas.particleCount(47);
        for (int i = 0; i < count; i++) {
            final float depth = seed(i, 1);
            final float phase = phase(canvas, i);

            final Point point = position(canvas, i, phase);

            final float x = point.x();
            final float y = point.y();

            final float inset = Math.min(Math.min(x - canvas.left, canvas.right - x), Math.min(y - canvas.top, canvas.bottom - y));
            final float fade = life(phase) * (0.4f + depth * 0.35f) * (1 - smooth(inset / 9) * 0.45f);

            final int color = mix(color(0, 0xFFBDCADA), color(1, 0xFFF2F3F5), depth);

            if (i % 3 == 0) {
                final float twinkle = 0.82f + 0.18f * sin(canvas.time * 1.1 + i * 2.7);
                flake(canvas, x, y, 1.3f + depth * 1.05f, rotation(canvas, i), color, fade * twinkle);
            }
            else {
                canvas.dot(x, y, 0.4f + depth * depth * 0.9f, color, fade);
            }

        }

    }

    private static void flake(EffectCanvas canvas, float x, float y, float radius, float rotation, int color, float alpha) {
        final int vertices = 8;
        radius *= parameter(SIZE);
        for (int i = 0; i < vertices; i++) {
            final float from = rotation + TAU * i / vertices, to = rotation + TAU * (i + 1) / vertices;
            final float ra = i % 2 == 1 ? radius * 0.24f : radius;
            final float rb = i % 2 == 0 ? radius * 0.24f : radius;
            canvas.triangle(x, y, x + cos(from) * ra, y + sin(from) * ra, x + cos(to) * rb, y + sin(to) * rb, color, alpha, alpha * 0.65f, alpha * 0.65f);
        }

    }

}
