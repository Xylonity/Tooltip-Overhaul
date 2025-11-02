package dev.xylonity.tooltipoverhaul.client.old.style;

import dev.xylonity.tooltipoverhaul.client.old.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.old.layer.bridge.*;
import dev.xylonity.tooltipoverhaul.client.old.style.renderer.DefaultArmorStand;
import dev.xylonity.tooltipoverhaul.client.old.style.renderer.DefaultIcon;
import dev.xylonity.tooltipoverhaul.client.old.style.renderer.DefaultRotatingItem;
import dev.xylonity.tooltipoverhaul.client.old.style.text.DefaultText;
import dev.xylonity.tooltipoverhaul.client.old.style.text.EquippedBadgeText;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import dev.xylonity.tooltipoverhaul.client.old.TooltipContext;
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
    private final ITooltipText equippedBadgeText;
    private final ITooltipDividerLine dividerLine;
    private final ITooltipArmorStand armorStand;
    private final ITooltipRotatingItem rotatingItem;

    public TooltipStyle(ITooltipPanel panel, ITooltipFrame innerFrame, ITooltipEffect effect, ITooltipDividerLine dividerLine, ITooltipPreviewBackground iconBackground) {
        this.effect = effect;
        this.panel = panel;
        this.frame = innerFrame;
        this.previewBackground = iconBackground;
        this.icon = new DefaultIcon();
        this.text = new DefaultText();
        this.equippedBadgeText = new EquippedBadgeText();
        this.dividerLine = dividerLine;
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

    public void renderEquippedBadgeText(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size, Component rarity, Font font) {
        equippedBadgeText.render(depth, ctx, pos, size, rarity, font);
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
