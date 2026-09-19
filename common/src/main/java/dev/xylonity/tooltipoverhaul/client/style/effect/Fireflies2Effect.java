package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class Fireflies2Effect extends AmbientEffect {

    @Override
    protected boolean hasMaterial() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.60f;
    }

    private static Point flight(EffectCanvas canvas, int i, double seconds) {
        final float turbulence = parameter(TURBULENCE);
        final double wander = seed(i, 1) + flow(i * 4.3 + 37, seconds * 0.12) * 0.24 * turbulence;
        final float reach = Math.max(18, Math.min(38, Math.min(canvas.width, canvas.height) * 0.32f));

        float padding = -5 + flow(i * 2.7, seconds * 0.31) * reach * turbulence;
        padding = Math.max(-Math.min(canvas.width, canvas.height) * 0.32f, padding);

        final Point home = canvas.edge(wander, padding);

        final float x = home.x() + (flow(i * 3.1 + 11, seconds * 0.28) * 12 + sin(seconds * 0.91 + i) * 2.4f) * turbulence;
        final float y = home.y() + (flow(i * 2.3 + 19, seconds * 0.24) * 10 + cos(seconds * 0.73 + i * 2) * 2) * turbulence;
        return new Point(x, y);
    }

    private static float flash(EffectCanvas canvas, int i) {
        final float phase = cycle(canvas.time * parameter(TWINKLE_SPEED) / (3.4 + seed(i, 3) * 3.4) + seed(i, 4));
        float pulse = bell(clamp(phase / 0.25f)) + bell(clamp((phase - 0.30f) / 0.18f)) * 0.6f;
        return 1 - parameter(TWINKLE_DEPTH) + pulse * parameter(TWINKLE_DEPTH);
    }

    @Override
    protected void drawMaterial(EffectCanvas canvas) {
        for (int i = 0; i < canvas.particleCount(15); i++) {
            Point point = flight(canvas, i, canvas.time);
            canvas.dot(point.x(), point.y() - 0.3f, 0.7f + seed(i, 5) * 0.2f, color(0, 0xFF5F6141), 0.6f);
        }

    }

    @Override
    protected void draw(EffectCanvas canvas) {
        for (int i = 0; i < canvas.particleCount(15); i++) {
            final Point point = flight(canvas, i, canvas.time);
            final float pulse = flash(canvas, i);
            final float depth = seed(i, 5);
            final int color = mix(color(0, 0xFFCFDF83), color(1, 0xFFFFD08A), depth);

            canvas.glow(point.x(), point.y(), 3.2f + depth * 1.5f, color, pulse * 0.32f);

            final Point tail = flight(canvas, i, canvas.time - 0.15);
            canvas.line(tail.x(), tail.y(), point.x(), point.y(), 0.15f, 0.4f, color, 0, pulse * 0.35f);
            canvas.dot(point.x(), point.y(), 0.5f + depth * 0.3f, color(2, 0xFFFFE4A3), 0.05f + pulse * 0.9f);
        }

    }

}
