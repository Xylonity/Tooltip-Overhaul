package dev.xylonity.tooltipoverhaul.client.style;

import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.*;
import dev.xylonity.tooltipoverhaul.client.style.background.preview.DefaultBackgroundPreview;
import dev.xylonity.tooltipoverhaul.client.style.divider.DefaultDividerLine;
import dev.xylonity.tooltipoverhaul.client.style.renderer.DefaultArmorStand;
import dev.xylonity.tooltipoverhaul.client.style.renderer.DefaultIcon;
import dev.xylonity.tooltipoverhaul.client.style.renderer.DefaultRotatingItem;
import dev.xylonity.tooltipoverhaul.client.style.text.DefaultText;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import net.minecraft.world.phys.Vec2;

import java.awt.Point;

/**
 * Main renderer bridge that handles abstraction calls from most of the layers to their respective renderers
 */
public class TooltipStyle {

    private final ITooltipEffect effect;
    private final ITooltipPanel panel;
    private final ITooltipFrame frame;
    private final ITooltipPreviewBackground previewBackground;
    private final ITooltipIcon icon;
    private final ITooltipText text;
    private final ITooltipDividerLine dividerLine;
    private final ITooltipArmorStand armorStand;
    private final ITooltipRotatingItem rotatingItem;

    public TooltipStyle(ITooltipPanel panel, ITooltipFrame innerFrame, ITooltipEffect effect) {
        this.effect = effect;
        this.panel = panel;
        this.frame = innerFrame;
        this.previewBackground = new DefaultBackgroundPreview();
        this.icon = new DefaultIcon();
        this.text = new DefaultText();
        this.dividerLine = new DefaultDividerLine();
        this.armorStand = new DefaultArmorStand();
        this.rotatingItem = new DefaultRotatingItem();
    }

    public void renderBack(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        panel.render(depth, ctx, pos, size);
    }

    public void renderPreviewBack(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        previewBackground.render(depth, ctx, pos, size);
    }

    public void renderIcon(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        icon.render(depth, ctx, pos, size);
    }

    public void renderText(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size, Component rarity, Font font) {
        text.render(depth, ctx, pos, size, rarity, font);
    }

    public void renderDividerLine(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        dividerLine.render(depth, ctx, pos, size);
    }

    public void renderInnerFrame(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        frame.render(depth, ctx, pos, size);
    }

    public void renderArmorStand(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        armorStand.render(depth, ctx, pos, size);
    }

    public void renderRotatingItem(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        rotatingItem.render(depth, ctx, pos, size);
    }

    public void renderEffect(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        if (effect != null) {
            effect.render(depth, ctx, pos, size);
        }

    }

}
