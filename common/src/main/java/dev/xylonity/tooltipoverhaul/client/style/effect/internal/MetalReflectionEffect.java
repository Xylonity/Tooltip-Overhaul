package dev.xylonity.tooltipoverhaul.client.style.effect.internal;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public abstract class MetalReflectionEffect extends AmbientEffect {

    private static final float[] REFLECTION_SAMPLES = { -24, -22, -19, -16, -14, -12, -9, -5, -3, -1, 0, 1, 3, 5, 9, 15, 22, 24 };

    @Override
    protected boolean clipToTooltip() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 1;
    }

    protected double period() {
        return 4.6;
    }

    protected float duration() {
        return 1.84f;
    }

    protected boolean mirrored() {
        return false;
    }

    protected float bend(EffectCanvas canvas, float phase) {
        return 0;
    }

    private float reflectX(EffectCanvas canvas, float x) {
        return mirrored() ? canvas.left + canvas.right - x : x;
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final float elapsed = cycle(canvas.time / period()) * (float) period();
        final float activeDuration = duration() * parameter(DUTY_CYCLE) / 0.4f;
        if (elapsed >= activeDuration) {
            return;
        }

        final float phase = elapsed / activeDuration;
        final float margin = 38 * parameter(BAND_WIDTH);

        final float sweep = canvas.left - margin + smooth(phase) * (canvas.width + canvas.height * parameter(SLANT) + margin * 2);

        final float fade = life(phase);
        final float bend = bend(canvas, phase);

        // Thing diagonal shade
        for (float y = canvas.top; y < canvas.bottom; y += 2) {
            final float y1 = Math.min(canvas.bottom, y + 2);
            final float center0 = centerX(canvas, y, sweep, bend);
            final float center1 = centerX(canvas, y1, sweep, bend);
            for (int band = 0; band < REFLECTION_SAMPLES.length - 1; band++) {
                final float offset0 = REFLECTION_SAMPLES[band] * parameter(BAND_WIDTH);
                final float offset1 = REFLECTION_SAMPLES[band + 1] * parameter(BAND_WIDTH);

                final float x0 = Math.max(canvas.left, Math.min(canvas.right, center0 + offset0));
                final float x1 = Math.max(canvas.left, Math.min(canvas.right, center0 + offset1));
                final float x2 = Math.max(canvas.left, Math.min(canvas.right, center1 + offset0));
                final float x3 = Math.max(canvas.left, Math.min(canvas.right, center1 + offset1));
                if (x0 == x1 && x2 == x3) {
                    continue;
                }

                final float alpha0 = shine(x0 - center0) * fade;
                final float alpha1 = shine(x1 - center0) * fade;
                final float alpha2 = shine(x2 - center1) * fade;
                final float alpha3 = shine(x3 - center1) * fade;

                final int color = mix(color(0, 0xFFC0DAF5), color(1, 0xFFFFECCB), smooth((offset0 / parameter(BAND_WIDTH) + 15) / 30));

                canvas.triangle(reflectX(canvas, x0), y, reflectX(canvas, x1), y, reflectX(canvas, x3), y1, color, alpha0, alpha1, alpha3);
                canvas.triangle(reflectX(canvas, x0), y, reflectX(canvas, x3), y1, reflectX(canvas, x2), y1, color, alpha0, alpha3, alpha2);
            }

        }

        canvas.ribbon(160, along -> {
            final Point point = canvas.edge(along, 0);
            final float reflection = shine(reflectX(canvas, point.x()) - centerX(canvas, point.y(), sweep, bend));
            return new Knot(point.x(), point.y(), 1.2f, color(2, 0xFFFFF3DC), reflection * fade * 1.5f);
        });

        for (int i = 0; i < 2; i++) {
            final float y = i == 0 ? canvas.top : canvas.bottom;
            final float x = reflectX(canvas, centerX(canvas, y, sweep, bend));
            final float edgeFade = smooth((x - canvas.left) / 10) * smooth((canvas.right - x) / 10) * fade;
            canvas.haze(x, y, 10, 1.7f, color(2, 0xFFE8F3FF), edgeFade * 0.5f);
            canvas.sparkle(x, y, 5.2f, 0, color(2, 0xFFFFF7E5), edgeFade * 0.8f);
        }

    }

    private static float centerX(EffectCanvas canvas, float y, float sweep, float bend) {
        final float vertical = clamp((y - canvas.top) / canvas.height);
        return sweep - (y - canvas.top) * parameter(SLANT) - bend * 4 * vertical * (1 - vertical);
    }

    private static float shine(float distance) {
        distance /= parameter(BAND_WIDTH);
        return smooth(1 - Math.abs(distance) / 24) * 0.24f + smooth((10 - Math.abs(distance)) / 6) * 0.66f + smooth(1 - Math.abs(distance + 17) / 4) * 0.20f;
    }

}
