package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.MetalReflectionEffect;

public class MetalShiningEffect extends MetalReflectionEffect {

    @Override
    protected double period() {
        return 3.1;
    }

    @Override
    protected float duration() {
        return 0.95f;
    }

    @Override
    protected boolean mirrored() {
        return true;
    }

}
