package dev.xylonity.tooltipoverhaul.client.layer.impl;

import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.style.TooltipStyle;
import dev.xylonity.tooltipoverhaul.util.TextAxis;
import dev.xylonity.tooltipoverhaul.util.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class IconBackgroundLayer implements ITooltipLayer {

    @Override
    public void render(TooltipContext ctx, Vec2 pos, Point size, TooltipStyle style, Component rarity, Font font, CustomFrameData customFrame) {

        if (Util.shouldDisableIcon(ctx.stack())) return;

        ctx.push(() -> {
            ctx.translate(0, 0, LayerDepth.BACKGROUND_TEXT.getZ());
            style.renderPreviewBack(LayerDepth.BACKGROUND_TEXT, ctx, pos.add(new Vec2(Util.getMainPanelPadding(ctx, TextAxis.X) / 2f - 2, Util.getMainPanelPadding(ctx, TextAxis.Y) - 2)), size);
        });
    }

}
