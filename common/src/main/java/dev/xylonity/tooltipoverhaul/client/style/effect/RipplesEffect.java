package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class RipplesEffect extends AmbientEffect {

    @Override
    protected boolean clipToTooltip() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.78f;
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final float reach = Math.max(32, Math.min(120, (float) Math.hypot(canvas.width, canvas.height) * 0.42f));
        for (int source = 0; source < effectCount(3); source++) {
            final double clock = canvas.time / 4.2 + source / 3.0;
            final float phase = cycle(clock);

            final int event = (int) Math.floor(clock) * 3 + source;

            final float x = canvas.left + canvas.width * (0.12f + seed(event, 1) * 0.76f);
            final float y = canvas.top + canvas.height * (0.12f + seed(event, 2) * 0.76f);

            final float extent = reach * (0.8f + seed(event, 3) * 0.35f) * parameter(EXPANSION);
            final float rotation = seed(event, 4) * TAU + phase * 0.5f;
            final float awakening = smooth(phase / 0.06f) * (1 - smooth(phase / 0.48f));

            canvas.glow(x, y, 13, color(1, 0xFF9981DD), awakening * 0.25f);
            canvas.sparkle(x, y, 3.5f, rotation, color(2, 0xFFE4D8FF), awakening * 0.78f);

            for (int wave = 0; wave < (int) parameter(WAVE_COUNT); wave++) {
                float age = (phase - wave * 0.13f) / (1 - wave * 0.13f);
                if (age <= 0 || age >= 1) {
                    continue;
                }

                final float radius = radius(age, extent);
                final float fade = smooth(age / 0.08f) * (float) Math.pow(1 - age, 1.3);
                final float strength = wave == 0 ? 1 : 0.48f;
                final int segments = Math.max(96, Math.min(224, (int) (radius * 4)));
                for (int pass = 0; pass < 2; pass++) {
                    final boolean halo = pass == 0;
                    canvas.ribbon(segments, along -> {
                        float angle = along * TAU;
                        final float wobbled = radius * (1 + sin(angle * 5 + rotation) * 0.009f);
                        final float iridescence = 0.5f + 0.5f * sin(angle * 2 - rotation + age * 3);
                        int color = iridescence < 0.5f ? mix(color(0, 0xFF8ACDDF), color(1, 0xFFAD91EE), iridescence * 2)
                                : mix(color(1, 0xFFAD91EE), color(2, 0xFFF0BBDD), iridescence * 2 - 1);
                        final float shimmer = 0.72f + 0.28f * cos(angle * 3 + rotation - age * 4);
                        return new Knot(x + cos(angle) * wobbled, y + sin(angle) * wobbled,
                                Math.min(wobbled * 0.4f, halo ? 5.5f : 1.15f), color, fade * strength * shimmer * (halo ? 0.20f : 0.72f));
                    });

                }

            }

            // Sparks that drift along the wave
            for (int spark = 0; spark < 5; spark++) {
                final float delay = 0.10f + seed(event * 5 + spark, 11) * 0.16f;
                final float age = (phase - delay) / 0.65f;
                if (age <= 0 || age >= 1) {
                    continue;
                }

                final float angle = rotation + spark * TAU / 5 + age * 0.20f;
                final float glintRadius = radius(phase, extent) + age * 3;
                final float alpha = bell(age) * (0.35f + seed(event * 5 + spark, 12) * 0.25f);
                canvas.sparkle(x + cos(angle) * glintRadius, y + sin(angle) * glintRadius, 1.7f + seed(spark, 13) * 1.1f, angle * 0.35f, color(2, 0xFFE6D9FF), alpha);
            }

        }

    }

    private static float radius(float age, float extent) {
        return 1 + (1 - (float) Math.pow(1 - age, 1.65)) * extent;
    }

}
