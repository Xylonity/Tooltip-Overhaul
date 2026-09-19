package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class SteelShiningEffect extends AmbientEffect {

    private static final double PERIOD = 3.8;
    private static final float DURATION = 1.6f;
    private static final float HALF_WIDTH = 0.78f;
    private static final float INTENSITY = 1.5f;
    private static final float[] SAMPLES = {-1, -0.85f, -0.70f, -0.55f, -0.40f, -0.20f,
            0, 0.20f, 0.40f, 0.55f, 0.70f, 0.85f, 1};

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
        final float elapsed = cycle(canvas.time / PERIOD) * (float) PERIOD;
        final float duration = Math.min((float) PERIOD, DURATION * parameter(DUTY_CYCLE) / 0.4f);
        if (elapsed >= duration) {
            return;
        }

        final float phase = elapsed / duration;
        final float progress = smooth(phase);
        final float fade = smooth(phase / 0.16f) * smooth((1 - phase) / 0.20f);
        final float halfWidth = HALF_WIDTH * parameter(BAND_WIDTH);
        final float sweep = -halfWidth + progress * (TAU * 0.25f + halfWidth * 2);
        final float growth = smooth(phase / 0.28f);

        Point pivot = new Point(canvas.left + canvas.width * (0.32f - progress * 0.52f), canvas.top - Math.max(6, canvas.height * 0.20f));
        float reach = (float) Math.hypot((canvas.right - pivot.x()) / canvas.width, (canvas.bottom - pivot.y()) / canvas.height) + 0.08f;

        final int segments = Math.max(64, Math.min(240, (int) (Math.max(canvas.width, canvas.height) / 2)));

        for (int row = 0; row < segments; row++) {
            final float r0 = reach * row / segments, r1 = reach * (row + 1) / segments;
            final float alpha0 = radialFade(r0, growth) * fade, alpha1 = radialFade(r1, growth) * fade;
            if (Math.max(alpha0, alpha1) < 0.002f) {
                continue;
            }

            for (int band = 0; band < SAMPLES.length - 1; band++) {
                final float d0 = SAMPLES[band], d1 = SAMPLES[band + 1];
                point(canvas, pivot, r0, sweep, d0, alpha0);
                point(canvas, pivot, r1, sweep, d0, alpha1);
                point(canvas, pivot, r1, sweep, d1, alpha1);
                point(canvas, pivot, r0, sweep, d0, alpha0);
                point(canvas, pivot, r1, sweep, d1, alpha1);
                point(canvas, pivot, r0, sweep, d1, alpha0);
            }

        }

        canvas.ribbon(160, along -> {
            final Point point = canvas.edge(along, 0);
            final float x = (point.x() - pivot.x()) / canvas.width, y = (point.y() - pivot.y()) / canvas.height;
            final float radius = (float) Math.hypot(x, y);
            final float distance = ((float) Math.atan2(y, x) - sweep) / halfWidth;
            return new Knot(point.x(), point.y(), 1.2f, color(2, 0xFFE6F3FF), reflection(distance) * radialFade(radius, growth) * fade * 1.25f);
        });

    }

    private static float radialFade(float radius, float growth) {
        return smooth((radius - (1 - growth) * 0.92f) / 0.20f);
    }

    private static void point(EffectCanvas canvas, Point pivot, float radius, float angle, float distance, float alpha) {
        final float theta = angle + distance * HALF_WIDTH * parameter(BAND_WIDTH);
        int color = mix(color(0, 0xFF94B8D8), color(1, 0xFFD5E3EF), smooth((distance + 1) * 0.5f));
        color = mix(color, color(2, 0xFFF3F8FF), smooth(1 - Math.abs(distance) / 0.70f));
        canvas.vertex(pivot.x() + cos(theta) * radius * canvas.width, pivot.y() + sin(theta) * radius * canvas.height, color, reflection(distance) * alpha);
    }

    private static float reflection(float distance) {
        return (smooth(1 - Math.abs(distance)) * 0.30f + smooth(1 - Math.abs(distance) / 0.70f) * 0.34f) * INTENSITY;
    }

}
