package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class TimeSpiralEffect extends AmbientEffect {

    @Override
    protected boolean clipToTooltip() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.48f;
    }

    private static Knot coil(EffectCanvas canvas, float along, int strand, boolean halo) {
        final Point point = canvas.edge(canvas.time * 0.065 + strand * 0.5 + patternPhase(strand + 80) / TAU + along * 0.92 * parameter(TWIST), -1.4f);
        final float contraction = 1 - smooth(along) * 0.58f;

        final float scale = Math.max(1, Math.min(2.2f, (float) Math.sqrt((canvas.width + canvas.height) / 184f)));
        final float fade = smooth(along / 0.10f) * smooth((1 - along) / 0.32f);

        final float breathing = 0.8f + 0.2f * sin(along * 5 - canvas.time * 0.7 + strand);

        final int color = mix(color(0, 0xFF81B7C5), color(1, 0xFFDFD9C7), along * 0.7f);
        return new Knot(canvas.cx + (point.x() - canvas.cx) * contraction, canvas.cy + (point.y() - canvas.cy) * contraction, (halo ? 2.6f : 0.65f) * scale, color, fade * breathing * (halo ? 0.13f : 0.66f));
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final int segments = Math.max(160, Math.min(320, (int) (canvas.width + canvas.height)));
        for (int strand = 0; strand < 2; strand++) {
            final int strandIndex = strand;
            canvas.ribbon(segments, along -> coil(canvas, along, strandIndex, true));
            canvas.ribbon(segments, along -> coil(canvas, along, strandIndex, false));
        }

        for (int i = 0; i < canvas.particleCount(14); i++) {
            final float along = cycle(canvas.time * 0.055 + seed(i, 1));
            final Knot knot = coil(canvas, along, i % 2, false);
            canvas.dot(knot.x(), knot.y(), 0.4f + seed(i, 2) * 0.3f, color(2, 0xFFE1EBE7), knot.alpha() * 0.8f);
        }

    }

}