package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class CinderEffect extends AmbientEffect {

    @Override
    protected boolean clipToTooltip() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.80f;
    }

    private static Point ember(EffectCanvas canvas, int index, float age, float heightBlend) {
        final float source = canvas.left - 3 + seed(index, 4) * (canvas.width + 6);
        final float travel = (canvas.height + 26) * (0.55f + heightBlend * 0.35f + seed(index, 5) * (0.45f - heightBlend * 0.10f));
        final float x = source + flow(index * 2.1, age * 2.4 + seed(index, 6) * 9) * (4 + age * 16) * parameter(TURBULENCE);
        final float curl = sin(age * 8 + seed(index, 8) * TAU) * sin(age * Math.PI);
        return new Point(x + (age * age * 5 + curl * 2.5f) * parameter(TURBULENCE), canvas.bottom + 2 - age * travel);
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        for (int layer = 0; layer < 2; layer++) {
            final int layerIndex = layer;
            canvas.ribbon(96, along -> {
                final float convection = flow(along * 7 + layerIndex * 13, canvas.time * 0.38);
                return new Knot(canvas.left - 5 + along * (canvas.width + 10), canvas.bottom - 1 + convection * 3 - layerIndex * 2, 4 + noise(along * 11, canvas.time * 0.45) * 6,
                        layerIndex == 0 ? color(2, 0xFFEA4E21) : color(1, 0xFFFFA444), bell(along) * (layerIndex == 0 ? 0.23f : 0.12f));
            });

        }

        final float heightBlend = smooth((canvas.height - 74) / 100);
        final float flightScale = (float) Math.sqrt(Math.max(1, (canvas.height + 26) / 100));
        final float densityScale = (float) Math.sqrt(Math.max(1, canvas.height / 74) * Math.max(1, canvas.width / 220));
        final int count = Math.min(effectCount(220), Math.round(canvas.particleCount(58) * densityScale));

        for (int i = 0; i < count; i++) {
            final float phase = cycle(canvas.time / ((3.2 + seed(i, 2) * 4) * flightScale) + seed(i, 3));
            final float flicker = 0.72f + 0.28f * noise(i * 3.1, canvas.time * 3.2);
            final float fade = smooth(phase / 0.16f) * smooth((1 - phase) / (0.32f - heightBlend * 0.14f)) * (1 - phase * (0.7f - heightBlend * 0.32f)) * flicker;
            final Point point = ember(canvas, i, phase, heightBlend);
            final float size = (0.55f + seed(i, 7) * seed(i, 7) * 1.35f) * (1 - phase * (0.4f - heightBlend * 0.15f));
            final float warmUntil = 0.35f + heightBlend * 0.20f;
            final int emberColor = phase < warmUntil ? mix(color(0, 0xFFFFEDBB), color(1, 0xFFFFA34D), phase / warmUntil)
                    : mix(color(1, 0xFFFFA34D), color(2, 0xFF9C4840), smooth((phase - warmUntil) / (1 - warmUntil)));
            if (i % 3 == 0 && parameter(TRAIL_LENGTH) > 0) {
                final int index = i;
                canvas.ribbon(12, along -> {
                    final Point trailPoint = ember(canvas, index, Math.max(0, phase - (1 - along) * (0.045f + seed(index, 9) * 0.04f) * parameter(TRAIL_LENGTH) / flightScale), heightBlend);
                    return new Knot(trailPoint.x(), trailPoint.y(), size * (0.12f + along * 0.65f), mix(color(2, 0xFFBC4431), emberColor, along), fade * along * along * 0.55f);
                });

            }

            canvas.glow(point.x(), point.y(), size * 4.5f, color(1, 0xFFFF6B24), fade * 0.34f);

            if (i % 5 == 0) {
                canvas.star(point.x(), point.y(), size * 1.55f, 4, 0.64f, i + phase * 4, emberColor, fade * 0.85f);
            }
            else {
                canvas.mote(point.x(), point.y(), size, emberColor, fade);
            }

            canvas.glow(point.x(), point.y(), size * 0.7f, color(0, 0xFFFFF4DC), fade * (1 - smooth(phase / 0.6f)) * 0.6f);
        }

    }

}
