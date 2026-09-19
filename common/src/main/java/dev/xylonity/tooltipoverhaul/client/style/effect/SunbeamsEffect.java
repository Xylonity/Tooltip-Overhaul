package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class SunbeamsEffect extends AmbientEffect {

    @Override
    protected boolean clipToTooltip() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.72f;
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final int warm = color(0, 0xFFFFEFC2);
        final int amber = color(1, 0xFFFFD08A);
        final int white = color(2, 0xFFFFFDF0);

        final float axis = (float) Math.hypot(parameter(SLANT), 1);

        final float dx = parameter(SLANT) / axis;
        final float dy = 1 / axis;

        final float spread = Math.abs(dy) * canvas.width + Math.abs(dx) * canvas.height;
        final float travel = Math.abs(dx) * canvas.width + Math.abs(dy) * canvas.height + 32;

        final int count = Math.max(1, effectCount(4));
        final float[] offset = new float[count];
        final float[] half = new float[count];
        final float[] level = new float[count];

        final int[] tint = new int[count];
        for (int beam = 0; beam < count; beam++) {
            offset[beam] = ((beam + 0.5f) / count - 0.5f) * spread * 0.92f + (seed(beam, 1) - 0.5f) * spread * 0.14f + sin(canvas.time * (0.05 + seed(beam, 2) * 0.05) + seed(beam, 3) * TAU) * spread * 0.1f;
            half[beam] = (3.4f + seed(beam, 4) * seed(beam, 4) * 11f) * parameter(BAND_WIDTH);
            level[beam] = (0.4f + seed(beam, 8) * 0.6f) * (0.45f + 0.55f * (0.5f + 0.5f * sin(canvas.time * (0.16 + seed(beam, 5) * 0.18) + seed(beam, 6) * TAU)));
            tint[beam] = mix(warm, amber, 0.3f + seed(beam, 7) * 0.55f);
        }

        for (int index = 0; index < count; index++) {
            final float sx = canvas.cx - dy * offset[index] - dx * travel * 0.5f;
            final float sy = canvas.cy + dx * offset[index] - dy * travel * 0.5f;
            final int beam = index;
            for (int layer = 0; layer < 3; layer++) {
                final float width = half[index] * (layer == 0 ? 1f : layer == 1 ? 0.52f : 0.17f);
                final float strength = (layer == 0 ? 0.085f : layer == 1 ? 0.115f : 0.15f) * level[index];
                final int color = layer == 2 ? mix(tint[index], white, 0.55f) : tint[index];
                canvas.ribbon(20, along -> new Knot(sx + dx * travel * along, sy + dy * travel * along, width, color,
                        strength * (0.8f + 0.2f * noise(along * 6 + beam * 13, canvas.time * 0.22)) * (1 - along * 0.45f)));
            }

            final float entry = (canvas.top - sy) / (dy * travel);
            if (entry > 0 && entry < 1) {
                canvas.glow(sx + dx * travel * entry, canvas.top, half[index] * 3.2f, mix(tint[index], white, 0.4f), level[index] * 0.18f);
            }

        }

        for (int i = 0; i < canvas.particleCount(26); i++) {
            final float sink = cycle(canvas.time / (30 + seed(i, 10) * 26) + seed(i, 11));
            final float x = canvas.left + seed(i, 12) * canvas.width + flow(i * 2.3, canvas.time * 0.05) * 6;
            final float y = canvas.top - 4 + cycle(seed(i, 13) + sink) * (canvas.height + 8);
            float lit = 0;
            for (int beam = 0; beam < count; beam++) {
                final float distance = ((x - canvas.cx) * -dy + (y - canvas.cy) * dx - offset[beam]) / half[beam];
                lit = Math.max(lit, (float) Math.exp(-distance * distance * 1.5) * level[beam]);
            }

            final float twinkle = 0.4f + 0.6f * (0.5f + 0.5f * sin(canvas.time * (0.9 + seed(i, 14)) + seed(i, 15) * TAU));
            canvas.mote(x, y, 0.32f + seed(i, 16) * 0.3f, mix(white, warm, lit * 0.6f), (0.07f + lit * 0.85f) * twinkle);
        }

    }

}