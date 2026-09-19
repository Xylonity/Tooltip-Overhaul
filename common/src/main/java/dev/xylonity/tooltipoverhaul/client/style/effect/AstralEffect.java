package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectField;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class AstralEffect extends AtmosphericFieldEffect {

    @Override
    protected EffectField field() {
        return EffectField.ASTRAL;
    }

    @Override
    protected int sample(float fieldX, float fieldY, double time, float pixelSize) {
        final double clock = time * 0.28;
        float fold = fieldY - 0.52f + (sin(fieldX * 3.8 + clock * 0.75 + patternPhase(80)) * 0.30f + sin(fieldX * 7.2 - clock * 0.55) * 0.09f) * parameter(DISTORTION);
        float crossing = fieldX - 0.50f + (sin(fieldY * 3.6 - clock * 0.55 + 2 + patternPhase(81)) * 0.32f + sin(fieldY * 6 + clock * 0.45) * 0.07f) * parameter(DISTORTION);
        final float softness = Math.max(0.025f / parameter(SHARPNESS), pixelSize * 1.7f);
        final float blueHem = focus(fold, softness);
        final float goldHem = focus(crossing, softness * 1.3f);

        final float blueVeil = focus(fold, fold < 0 ? 0.18f : 0.055f);
        final float goldVeil = focus(crossing, crossing > 0 ? 0.22f : 0.07f);
        final float blue = (blueVeil * 0.23f + blueHem * 0.27f) * (0.65f + bell(fieldX) * 0.35f);
        final float gold = (goldVeil * 0.18f + goldHem * 0.20f) * (0.55f + bell(fieldY) * 0.45f);
        final int cool = mix(color(0, 0xFF659DCF), color(1, 0xFFB2A5E6), 0.5f + 0.5f * sin(fieldX * 3 - clock * 0.4));
        final int warm = mix(color(1, 0xFFD3A2C6), color(2, 0xFFE4C49A), 0.5f + 0.5f * sin(fieldY * 3 + clock * 0.3));

        int color = mix(cool, warm, gold / Math.max(0.001f, blue + gold));
        color = mix(color, color(1, 0xFFD8E8F1), blueHem * goldHem * 0.35f);

        final float rim = Math.max(blueHem, goldHem);
        return tint(color, blue + gold, rim * rim);
    }

    private static float focus(float distance, float width) {
        final float fraction = distance / width;
        return (float) Math.exp(-fraction * fraction);
    }

}