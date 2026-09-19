package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class SonarEffect extends AmbientEffect {

    private static final float SPEED = 38;

    @Override
    protected boolean clipToTooltip() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.68f;
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final float x = canvas.left + canvas.width * parameter(ORIGIN_X);
        final float y = canvas.top + canvas.height * parameter(ORIGIN_Y);

        float reach = (float) Math.hypot(Math.max(x - canvas.left, canvas.right - x), Math.max(y - canvas.top, canvas.bottom - y)) + 20;

        final float spacing = Math.max(42, reach / 8) * parameter(WAVE_SPACING);
        final float travel = cycle(canvas.time * SPEED / spacing) * spacing;

        final int waves = (int) Math.ceil(reach / spacing);
        final double heading = -0.7 + canvas.time * 0.32;

        canvas.haze(x, y, Math.max(48, canvas.width * 0.55f), Math.max(48, canvas.height * 0.7f), color(0, 0xFF246E91), 0.14f);
        canvas.haze(canvas.right, canvas.top, canvas.width * 0.65f, canvas.height * 0.8f, color(0, 0xFF304B83), 0.10f);

        for (int wave = 0; wave < waves; wave++) {
            final float radius = travel + wave * spacing;
            final float fade = smooth(radius / 12) * smooth((reach - radius) / Math.max(24, reach * 0.24f));
            if (fade < 0.002f) {
                continue;
            }

            final int segments = Math.max(96, Math.min(384, (int) (radius * 4)));
            for (int pass = 0; pass < 3; pass++) {
                final int layer = pass;
                final float ringRadius = Math.max(0.5f, radius - (layer == 2 ? 5 : 0));

                canvas.ribbon(segments, along -> {
                    final float angle = along * TAU;
                    final float crescent = (float) Math.pow(0.5 + 0.5 * cos(angle - heading + radius * 0.008), 3);
                    final float light = 0.38f + crescent * 0.62f;
                    final int color = mix(color(0, 0xFF538EBD), color(1, 0xFFA5EBDD), crescent);
                    return new Knot(x + cos(angle) * ringRadius, y + sin(angle) * ringRadius,
                            Math.min(ringRadius * 0.4f, layer == 0 ? 5.5f : layer == 1 ? 0.95f : 0.5f), color, fade * light * (layer == 0 ? 0.18f : layer == 1 ? 0.72f : 0.20f));
                });

            }

        }

        for (int i = 0; i < canvas.particleCount(24); i++) {
            final float px = canvas.left + (0.04f + seed(i, 21) * 0.92f) * canvas.width;
            final float py = canvas.top + (0.06f + seed(i, 22) * 0.88f) * canvas.height;

            final float distance = (float) Math.hypot(px - x, py - y);
            final float sinceWave = cycle((canvas.time * SPEED - distance) / spacing);
            final float pulse = smooth(sinceWave / 0.035f) * (1 - smooth(sinceWave / 0.48f));

            canvas.glow(px, py, 4.5f, color(1, 0xFF7DDBD2), pulse * 0.27f);
            canvas.dot(px, py, 0.45f + seed(i, 23) * 0.35f, color(2, 0xFFC8F7EA), 0.025f + pulse * 0.70f);

            if (i % 5 == 0) {
                canvas.arc(px, py, 1.5f + sinceWave * 7, 1.5f + sinceWave * 7, 0, TAU, 0.45f, color(1, 0xFF87C6D0), pulse * 0.18f);
            }

        }

        final float ping = 1 - smooth(travel / (spacing * 0.38f));
        canvas.glow(x, y, 6 + ping * 3, color(0, 0xFF66C9D4), 0.16f + ping * 0.22f);
        canvas.sparkle(x, y, 2.4f + ping * 0.7f, 0, color(2, 0xFFBCEDE6), 0.28f + ping * 0.32f);
    }

}
