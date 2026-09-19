package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.BlastBurst;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class BlastsEffect extends AmbientEffect {

    @Override
    protected float interiorVisibility() {
        return 0.48f;
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final int gold = color(0, 0xFFF3B27C);
        final int red = color(1, 0xFFD67C58);
        final int pale = color(2, 0xFFFFEAD0);
        final float duration = parameter(BURST_DURATION);

        for (int i = 0; i < effectCount(2); i++) {
            final double period = (2.6 + seed(i, 1) * 1.2) * Math.max(1, duration);
            final double clock = canvas.time / period + seed(i, 2);
            final int event = (int) Math.floor(clock) * 67 + i;
            final float elapsed = cycle(clock) * (float) period;
            final float chargeTime = 0.36f + seed(event, 6) * 0.1f;
            final float age = (elapsed - chargeTime) / ((float) period * 0.72f * Math.min(1, duration));

            final float x = canvas.left + canvas.width * (0.15f + seed(event, 3) * 0.7f);
            final float y = canvas.top + canvas.height * (0.25f + seed(event, 4) * 0.55f);
            final float radius = (18 + seed(event, 5) * 12) * (0.85f + Math.min(canvas.width, canvas.height) / 260f);

            charge(canvas, event, x, y, radius, elapsed, chargeTime, gold, pale);

            if (age >= 0) {
                BlastBurst.shockwave(canvas, x, y, radius, elapsed - chargeTime, 0.30f, gold, pale);
                BlastBurst.draw(canvas, event, x, y, radius, age, parameter(TRAIL_LENGTH), gold, red, pale, 18);
            }

        }

    }

    private static void charge(EffectCanvas canvas, int event, float x, float y, float radius, float elapsed, float duration, int warm, int hot) {
        if (elapsed >= duration + 0.13f) {
            return;
        }

        final float phase = clamp(elapsed / duration);
        final float light = smooth(phase) * (1 - smooth((elapsed - duration) / 0.13f));

        canvas.glow(x, y, 5 + phase * 6, warm, light * 0.34f);
        canvas.glow(x, y, 1.2f + phase * 2, hot, light * 0.8f);
        canvas.sparkle(x, y, 0.8f + phase * 2.1f, seed(event, 7) * TAU, hot, light * 0.65f);

        for (int k = 0; k < 6; k++) {
            final float angle = seed(event * 7 + k, 8) * TAU + phase * 0.8f;
            final float distance = radius * (0.06f + (1 - phase) * (1 - phase) * 0.7f) * parameter(SIZE);
            canvas.mote(x + cos(angle) * distance, y + sin(angle) * distance, 0.4f, warm, bell(phase) * 0.65f);
        }

    }

}
