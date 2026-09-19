package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectField;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class AuroraEffect extends AtmosphericFieldEffect {

    @Override
    protected EffectField field() {
        return EffectField.AURORA;
    }

    @Override
    protected int sample(float fieldX, float fieldY, double time, float pixelSize) {
        final double clock = time * 0.23;
        float fold = (sin(fieldX * 8 + clock + patternPhase(80) + sin(fieldX * 3 - clock * 0.4)) * 0.12f + sin(fieldX * 17 - clock * 0.7 + patternPhase(81)) * 0.025f) * parameter(DISTORTION);
        final float hem = 0.68f + fold;
        final float distance = hem - fieldY;
        float curtain = smooth((distance + 0.045f) / 0.10f) * (float) Math.exp(-Math.max(0, distance) * 5.5f / parameter(CURTAIN_HEIGHT));

        final float rays = 0.58f + noise(fieldX * 38 + sin(fieldY * 3 + clock) * 0.6, clock * 0.45 + 17) * 0.42f;

        final float softness = Math.max(0.033f, pixelSize * 1.5f);
        final float edge = (float) Math.exp(-distance * distance / (softness * softness));
        final float second = fieldY - (0.26f - fold * 0.55f);
        final float veil = (float) Math.exp(-second * second * 85) * (0.5f + 0.5f * sin(fieldX * 6 - clock));

        int color = mix(color(0, 0xFF6CDFB8), color(1, 0xFF947ADC), smooth(distance / 0.40f));
        color = mix(color, color(2, 0xFFB1EAD5), edge * 0.36f);

        final float ends = 0.65f + bell(fieldX) * 0.35f;
        return tint(color, (curtain * rays * 0.30f + edge * 0.17f + veil * 0.09f) * ends, edge * edge);
    }

    @Override
    protected void drawAccents(EffectCanvas canvas) {
        for (int index = 0; index < canvas.particleCount(12); index++) {
            final float x = canvas.left + seed(index, 61) * canvas.width;
            final float y = canvas.top + seed(index, 62) * canvas.height * 0.55f;
            final float glint = (float) Math.pow(0.5 + 0.5 * sin(canvas.time * 0.8 + index * 2.3), 4);
            canvas.mote(x, y, 0.45f + seed(index, 63) * 0.25f, color(2, 0xFFD0F1EA), 0.06f + glint * 0.32f);
        }

    }

}