package dev.xylonity.tooltipoverhaul.client.old.style;
import dev.xylonity.tooltipoverhaul.client.old.layer.bridge.*;
import dev.xylonity.tooltipoverhaul.client.old.style.background.DefaultPanel;
import dev.xylonity.tooltipoverhaul.client.old.style.background.icon.SlotBorderIconBackground;
import dev.xylonity.tooltipoverhaul.client.old.style.divider.DefaultDividerLine;
import dev.xylonity.tooltipoverhaul.client.old.style.inner.GlintInnerOverlay;

public class TooltipStyleBuilder {
    private final ITooltipPanel panel = new DefaultPanel();
    private ITooltipFrame frame = new GlintInnerOverlay(0xFFEFEFEF, 0xFF8A8A8A, 0x0, 5, 3);
    private ITooltipEffect effect = null;
    private ITooltipDividerLine dividerLine = new DefaultDividerLine();
    private ITooltipPreviewBackground iconBackground = new SlotBorderIconBackground();

    public TooltipStyleBuilder innerOverlay(ITooltipFrame frame) {
        this.frame = frame;
        return this;
    }

    public TooltipStyleBuilder addEffect(ITooltipEffect effect) {
        this.effect = effect;
        return this;
    }

    public TooltipStyleBuilder addDividerLine(ITooltipDividerLine dividerLine) {
        this.dividerLine = dividerLine;
        return this;
    }

    public TooltipStyleBuilder addIconBackground(ITooltipPreviewBackground iconBackground) {
        this.iconBackground = iconBackground;
        return this;
    }

    // Particles are temporary disabled

    public TooltipStyle build() {
        return new TooltipStyle(panel, frame, effect, dividerLine, iconBackground);
    }

}