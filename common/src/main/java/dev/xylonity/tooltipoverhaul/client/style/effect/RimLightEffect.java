package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class RimLightEffect extends AmbientEffect {

    private final int color1;
    private final int color2;

    public RimLightEffect(int color1, int color2) {
        this.color1 = mix(color1, 0xFFD0DCE5, 0.76f);
        this.color2 = mix(color2, 0xFFE0D5DC, 0.76f);
    }

    @Override
    protected boolean hasMaterial() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.12f;
    }

    private Knot reflection(EffectCanvas canvas, float along, int patch, float width, float opacity) {
        final float span = (patch == 0 ? 0.30f : 0.25f) * parameter(ARC_LENGTH);
        final double turn = canvas.time * 0.018 + (patch == 0 ? 0.12 : 0.64) + (along - 0.5f) * span;
        final Point point = canvas.edge(turn, 2.0f);
        final float fade = bell(along);
        float outside = 1 - smooth(Math.min(Math.min(point.x() - canvas.left, canvas.right - point.x()), Math.min(point.y() - canvas.top, canvas.bottom - point.y())) / 3);
        final int tint = color(width < 2 ? 2 : patch, patch == 0 ? color1 : color2);
        return new Knot(point.x(), point.y(), width, tint, fade * opacity * outside);
    }

    @Override
    protected void drawMaterial(EffectCanvas canvas) {
        for (int patch = 0; patch < 2; patch++) {
            final int patchIndex = patch;
            canvas.ribbon(80, along -> reflection(canvas, along, patchIndex, 7.0f, patchIndex == 0 ? 0.28f : 0.23f));
        }

    }

    @Override
    protected void draw(EffectCanvas canvas) {
        for (int patch = 0; patch < 2; patch++) {
            final int patchIndex = patch;
            canvas.ribbon(80, along -> reflection(canvas, along, patchIndex, 4.0f, patchIndex == 0 ? 0.18f : 0.14f));
            canvas.ribbon(80, along -> reflection(canvas, along, patchIndex, 1.5f, patchIndex == 0 ? 0.64f : 0.48f));
        }

    }

}
