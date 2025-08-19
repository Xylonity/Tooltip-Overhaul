package dev.xylonity.tooltipoverhaul.client.style;

import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipEffect;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipFrame;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipPanel;
import dev.xylonity.tooltipoverhaul.client.style.background.DefaultPanel;
import dev.xylonity.tooltipoverhaul.client.style.inner.GlintInnerOverlay;

public class TooltipStyleBuilder {
    private final ITooltipPanel panel = new DefaultPanel();
    private ITooltipFrame frame = new GlintInnerOverlay(0xFFEFEFEF, 0xFF8A8A8A, 0x0, 5, 3);
    private ITooltipEffect effect = null;

    public TooltipStyleBuilder innerOverlay(ITooltipFrame frame) {
        this.frame = frame;
        return this;
    }

    public TooltipStyleBuilder addEffect(ITooltipEffect effect) {
        this.effect = effect;
        return this;
    }

    // Particles are temporary disabled

    public TooltipStyle build() {
        return new TooltipStyle(panel, frame, effect);
    }

}