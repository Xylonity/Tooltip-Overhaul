package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class SearchlightsEffect extends AmbientEffect {

    private static final int SLIVERS = 22;

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
        final int pale = color(0, 0xFFDCE9FF), blue = color(1, 0xFFA8C4FF), white = color(2, 0xFFFFFFFF);
        final float halfAngle = 0.1f * parameter(BAND_WIDTH);
        final float length = (float) Math.hypot(canvas.width, canvas.height) * 1.3f;

        final int beams = Math.max(1, effectCount(2));

        final float[] sx = new float[beams];
        final float[] sy = new float[beams];
        final float[] aim = new float[beams];
        final float[] power = new float[beams];

        for (int beam = 0; beam < beams; beam++) {
            final float at = beams == 1 ? 0.5f : beam / (float) (beams - 1);

            sx[beam] = canvas.left + 4 + at * (canvas.width - 8);
            sy[beam] = canvas.bottom + 3;

            final float centre = -TAU * 0.25f + (0.5f - at) * 0.95f;
            final float swing = sin(canvas.time * (0.3 + seed(beam, 1) * 0.2) + seed(beam, 2) * TAU + patternPhase(3));

            aim[beam] = centre + swing * Math.abs(swing) * (0.6f + seed(beam, 3) * 0.25f);
            power[beam] = 0.85f + 0.15f * noise(beam * 7, canvas.time * 1.5);

            final int tone = mix(pale, blue, seed(beam, 4) * 0.7f);
            final float dx = cos(aim[beam]), dy = sin(aim[beam]);

            // Halo
            final float lx = sx[beam], ly = sy[beam], lampPower = power[beam];
            for (int layer = 0; layer < 2; layer++) {
                final boolean outer = layer == 0;
                canvas.ribbon(16, along -> new Knot(lx + dx * along * length, ly + dy * along * length,
                        (2 + along * length * halfAngle * (outer ? 2.6f : 1.4f)) + 0.1f, outer ? tone : mix(tone, white, 0.4f), (outer ? 0.12f : 0.2f) * lampPower * (1 - along * 0.75f)));
            }

            // Beam body
            for (int k = 0; k < SLIVERS; k++) {
                final float a0 = aim[beam] - halfAngle + 2 * halfAngle * k / SLIVERS;
                final float a1 = aim[beam] - halfAngle + 2 * halfAngle * (k + 1) / SLIVERS;
                final float across = (k + 0.5f) / SLIVERS * 2 - 1;
                final float profile = (float) Math.pow(1 - across * across, 1.4);
                canvas.triangle(sx[beam], sy[beam], sx[beam] + cos(a0) * length, sy[beam] + sin(a0) * length,
                        sx[beam] + cos(a1) * length, sy[beam] + sin(a1) * length, mix(tone, white, profile * 0.6f), 0.5f * profile * power[beam], 0, 0);
            }

            // Lamp
            canvas.haze(sx[beam], sy[beam], 16, 5, tone, 0.35f * power[beam]);
            canvas.glow(sx[beam], sy[beam], 8, tone, 0.7f * power[beam]);
            canvas.glow(sx[beam], sy[beam], 3.2f, white, power[beam]);
            canvas.star(sx[beam], sy[beam], 9, 4, 0.1f, aim[beam], white, 0.6f * power[beam]);
            canvas.glow(sx[beam] + dx * 5, sy[beam] + dy * 5, 4.5f, mix(tone, white, 0.5f), 0.5f * power[beam]);
        }

        // Dust drifting through the panel only visible when a beam catches it
        for (int i = 0; i < canvas.particleCount(34); i++) {
            final float x = canvas.left + seed(i, 10) * canvas.width + flow(i * 1.7, canvas.time * 0.06) * 8;
            final float y = canvas.top + seed(i, 11) * canvas.height + flow(i * 2.3 + 9, canvas.time * 0.05) * 6;
            float lit = 0;
            for (int beam = 0; beam < beams; beam++) {
                final float dx = x - sx[beam], dy = y - sy[beam];
                final float dist = (float) Math.hypot(dx, dy);

                float delta = (float) Math.atan2(dy, dx) - aim[beam];
                delta = (float) Math.atan2(sin(delta), cos(delta));

                final float inside = 1 - Math.min(1, Math.abs(delta) / halfAngle);
                lit = Math.max(lit, inside * inside * (1 - dist / length) * power[beam]);
            }

            final float twinkle = 0.6f + 0.4f * sin(canvas.time * (1 + seed(i, 12)) + seed(i, 13) * TAU);
            canvas.glow(x, y, 1.6f, white, lit * 0.5f);
            canvas.mote(x, y, 0.35f + seed(i, 14) * 0.35f, white, (0.04f + lit) * twinkle);
        }

    }

}