package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class StarsEffect extends AmbientEffect {

    private static final int[] DEFAULT_COLORS = {0xFFFFE6BA, 0xFFDDEAFF, 0xFFE1CFF3};
    private final int[] colors;

    public StarsEffect() {
        this(DEFAULT_COLORS);
    }

    public StarsEffect(int[] colors) {
        this.colors = (colors == null || colors.length == 0) ? DEFAULT_COLORS : colors.clone();
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final int count = canvas.particleCount(46);
        for (int i = 0; i < count; i++) {
            final Point point = canvas.edge(seed(i, 1), -4 + seed(i, 2) * 19);

            final float x = point.x() + sin(canvas.time * 0.09 + i * 1.7) * 1.5f;
            final float y = point.y() + sin(canvas.time * 0.12 + i * 2.1) * 1.5f;

            final float depth = seed(i, 3);
            final float phase = cycle(canvas.time * parameter(TWINKLE_SPEED) / (3.5 + seed(i, 4) * 5) + seed(i, 5));
            final float pulse = 1 - parameter(TWINKLE_DEPTH) + bell(phase) * parameter(TWINKLE_DEPTH);

            final int color = color(i % 3, colors[i % colors.length]);

            final int tier = i % 9;
            if (tier >= 4) {
                canvas.mote(x, y, 0.25f + depth * 0.5f, color, 0.10f + pulse * 0.55f);
                continue;
            }

            final boolean large = tier == 0;

            float radius = large ? 2.5f + depth * 1.6f : 1.1f + depth * 1.2f;
            radius *= 0.88f + pulse * 0.12f;

            final float flare = large ? 0.18f + pulse * pulse * 0.72f : 0.12f + pulse * 0.62f;

            final int shape = (i / 3) % 3;

            final float rotation = seed(i, 6) * TAU + sin(canvas.time * 0.12 + i) * 0.1f;

            canvas.star(x, y, radius, shape == 0 ? 4 : shape == 1 ? 6 : 5, shape == 2 ? 0.43f : 0.22f, rotation, color, flare);

            if (large) {
                canvas.haze(x, y, radius * 1.6f, radius * 0.35f, color, pulse * pulse * 0.08f);
            }

        }

    }

}
