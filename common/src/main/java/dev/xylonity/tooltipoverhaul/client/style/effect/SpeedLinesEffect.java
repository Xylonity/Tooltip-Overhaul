package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class SpeedLinesEffect extends AmbientEffect {

    @Override
    protected boolean clipToTooltip() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.75f;
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final int count = canvas.particleCount(28);
        for (int i = 0; i < count; i++) {
            final float depth = seed(i, 1);
            final float phase = cycle(canvas.time / (1.0 + (1 - depth) * 1.8) + seed(i, 2));

            final float fade = life(phase);

            final float x = canvas.right + 25 - phase * (canvas.width + 55);
            final float y = canvas.top + 2 + seed(i, 3) * Math.max(1, canvas.height - 4);

            final float length = (6 + depth * 23) * parameter(TRAIL_LENGTH);
            final int color = mix(color(0, 0xFF719BC7), color(1, 0xFFE2F4FF), depth);

            final int index = i;

            canvas.ribbon(18, along -> {
                final float px = x + (1 - along) * length;
                float bend = flow(px / 65, canvas.time * 0.25 + index % 2) * 3 * parameter(TURBULENCE);
                return new Knot(px, y + bend - (1 - along) * 1.4f,
                        (0.3f + depth * 0.45f) * sin(along * Math.PI), color, fade * smooth(along / 0.7f) * smooth((1 - along) / 0.13f) * (0.3f + depth * 0.5f));
            });

            if (depth > 0.7f) {
                final float bend = flow(x / 65, canvas.time * 0.25 + i % 2) * 3 * parameter(TURBULENCE);
                canvas.haze(x + length * 0.3f, y + bend, length * 0.65f, 1.8f, color(2, mix(0xFF719BC7, 0xFFE2F4FF, depth)), fade * 0.10f);
            }

        }

    }

}
