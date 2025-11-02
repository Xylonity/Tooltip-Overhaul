package dev.xylonity.tooltipoverhaul.client.old.layer.impl;

import dev.xylonity.tooltipoverhaul.client.old.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.old.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.old.style.TooltipStyle;
import dev.xylonity.tooltipoverhaul.client.old.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameData;
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
