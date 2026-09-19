package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectField;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class FluoriteEffect extends AtmosphericFieldEffect {

    @Override
    protected EffectField field() {
        return EffectField.FLUORITE;
    }

    @Override
    protected int sample(float fieldX, float fieldY, double time, float pixelSize) {
        final double clock = time * 0.24;
        final float distortion = parameter(DISTORTION);

        final float phaseX = patternPhase(80), phaseY = patternPhase(81);

        final float warpX = fieldX * 4.2f + sin(fieldY * 4.6 + clock * 0.65 + phaseX) * 0.72f * distortion;
        final float warpY = fieldY * 4.0f + sin(fieldX * 4.1 - clock * 0.55 + phaseY) * 0.68f * distortion;

        final float firstWave = warpX + warpY * 0.55f + (float) clock * 0.65f;
        final float secondWave = warpY - warpX * 0.45f - (float) clock * 0.50f;

        final float surface = sin(firstWave) * 0.65f + cos(secondWave) * 0.50f;

        final float dxdy = cos(fieldY * 4.6 + clock * 0.65 + phaseX) * 0.72f * 4.6f * distortion;
        final float dydx = cos(fieldX * 4.1 - clock * 0.55 + phaseY) * 0.68f * 4.1f * distortion;

        float du = cos(firstWave) * 0.65f * (4.2f + dydx * 0.55f) - sin(secondWave) * 0.50f * (dydx - 4.2f * 0.45f);
        float dv = cos(firstWave) * 0.65f * (dxdy + 4.0f * 0.55f) - sin(secondWave) * 0.50f * (4.0f - dxdy * 0.45f);

        final float normal = (float) Math.sqrt(du * du + dv * dv + 6.25f);
        final float reflection = clamp((du * -0.38f + dv * -0.28f + 2.5f * 0.88f) / normal);

        final float shine = (float) Math.pow(reflection, 5 * parameter(SHARPNESS));
        final float sheen = smooth((reflection - 0.28f) / 0.65f);

        final float hue = 0.5f + 0.5f * sin(surface * 2.6 + fieldX * 1.3 - fieldY * 0.8 - clock * 0.30);
        int color = hue < 0.5f ? mix(color(0, 0xFF5ABBB5), color(1, 0xFF879CDB), hue * 2) : mix(color(1, 0xFF879CDB), color(2, 0xFFD9A6BF), hue * 2 - 1);
        color = mix(color, color(2, 0xFFE5DFC9), shine * 0.62f);

        final float pools = smooth((surface + 0.55f) / 1.4f);
        return tint(color, 0.055f + pools * 0.075f + sheen * 0.15f + shine * 0.30f, shine * shine * shine);
    }

}
