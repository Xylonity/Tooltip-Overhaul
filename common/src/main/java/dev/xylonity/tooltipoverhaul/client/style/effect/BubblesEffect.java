package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class BubblesEffect extends AmbientEffect {

    @Override
    protected float interiorVisibility() {
        return 0.85f;
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final int count = canvas.particleCount(21);
        for (int index = 0; index < count; index++) {
            final float depth = seed(index, 8);
            final float phase = cycle(canvas.time / (6 + seed(index, 1) * 6) + seed(index, 2));
            final float fade = life(phase);
            final float baseRadius = (1.7f + depth * depth * 4.8f) * (0.9f + phase * 0.1f);
            final float radius = baseRadius * parameter(SIZE);

            float x = canvas.left + (index + seed(index, 3)) / count * canvas.width;
            x += flow(index * 1.7, canvas.time * 0.20) * 4 * parameter(TURBULENCE);

            float y = canvas.top + canvas.height * (0.30f + seed(index, 4) * 0.70f) + radius;
            y -= phase * (8 + seed(index, 5) * canvas.height * 0.45f) * parameter(TRAVEL);

            final float reflectionAngle = 2.15f + (seed(index, 9) - 0.5f) * 0.35f;
            final float stretch = (1 + sin(canvas.time * 0.7 + index) * 0.025f) * parameter(STRETCH);

            canvas.haze(x - radius * 0.18f, y + radius * 0.15f, baseRadius, baseRadius * stretch, color(0, 0xFF80ABCB), fade * 0.08f);

            final float px = x;
            final float py = y;
            final int particleIndex = index;

            canvas.ribbon(48, along -> {
                final double angle = along * TAU;
                final float iridescence = 0.5f + 0.5f * sin(angle * 2 + particleIndex + canvas.time * 0.18);
                final int tint = iridescence < 0.5f ? mix(color(0, 0xFF7ACDDB), color(1, 0xFFD8A7E8), iridescence * 2) : mix(color(1, 0xFFD8A7E8), color(2, 0xFFF5DFAD), iridescence * 2 - 1);
                final float specular = (float) Math.pow(Math.max(0, cos(angle + reflectionAngle)), 6);
                return new Knot(px + cos(angle) * radius, py + sin(angle) * radius * stretch, 0.4f + depth * 0.28f, tint, fade * (0.22f + 0.65f * specular));
            });

            // Light source
            canvas.ribbon(24, along -> {
                final float angle = -reflectionAngle - 0.52f + along * 1.04f;
                return new Knot(px + cos(angle) * radius * 0.83f, py + sin(angle) * radius * 0.83f * stretch, Math.min(0.48f, radius * 0.13f), color(2, 0xFFEFFFFF), fade * bell(along) * 0.72f);
            });

            canvas.ribbon(18, along -> {
                final float angle = -reflectionAngle + TAU * 0.5f - 0.38f + along * 0.76f;
                return new Knot(px + cos(angle) * radius * 0.92f, py + sin(angle) * radius * 0.92f * stretch, Math.min(0.35f, radius * 0.10f), color(2, 0xFFFFDAEC), fade * bell(along) * 0.26f);
            });

        }

    }

}