package dev.xylonity.tooltipoverhaul.client.old.layer.impl;

import dev.xylonity.tooltipoverhaul.client.old.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.old.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.old.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.old.style.TooltipStyle;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class EquippedBadgeInnerFrameLayer implements ITooltipLayer {

    @Override
    public void render(TooltipContext ctx, Vec2 pos, Point size, TooltipStyle style, Component rarity, Font font, CustomFrameData customFrame) {
        Point sec = new Point(size.x, 10);
        ctx.push(() -> {
            ctx.translate(0, 0, LayerDepth.BACKGROUND_INNER_FRAME.getZ());
            style.renderInnerFrame(LayerDepth.BACKGROUND_INNER_FRAME, ctx, pos.add(new Vec2(0, -23)), sec);
        });
    }

}
