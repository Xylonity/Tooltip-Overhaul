package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class EchoEffect extends AmbientEffect {

    @Override
    protected void draw(EffectCanvas canvas) {
        for (int wave = 0; wave < effectCount(4); wave++) {
            final float phase = cycle(canvas.time / 5.8 + (wave < 4 ? wave * 0.25 : seed(wave, 11)));
            final float fade = bell(phase) * (1 - phase) * 1.5f;
            final float expansion = 1 + (1 - (1 - phase) * (1 - phase)) * 19 * parameter(EXPANSION);
            final int index = wave;
            for (int layer = 0; layer < 2; layer++) {
                final boolean mist = layer == 0;
                canvas.ribbon(160, along -> {
                    final float disturbance = sin(along * TAU * 3 + canvas.time * 0.4 + patternPhase(80)) * phase * 1.3f * parameter(DISTORTION);
                    final Point point = canvas.edge(along, expansion + disturbance);
                    final float modulation = 0.2f + 0.8f * smooth(0.5f + 0.5f * sin(along * TAU * 2 - canvas.time * 0.5 + index + patternPhase(81)));
                    final int color = mix(color(0, 0xFFB6A0E4), color(1, 0xFF8ECFE4), 0.5f + 0.5f * sin(along * TAU + canvas.time * 0.15));
                    return new Knot(point.x(), point.y(), mist ? 3 + phase * 2 : 0.7f,
                            mist ? color : color(2, mix(0xFFB6A0E4, 0xFF8ECFE4, 0.5f + 0.5f * sin(along * TAU + canvas.time * 0.15))), fade * modulation * (mist ? 0.20f : 0.50f));
                });

            }

        }

    }

}
