package dev.xylonity.tooltipoverhaul.client.layer.impl;

import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.style.TooltipStyle;
import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameManager;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

import java.awt.*;
import java.util.Optional;

public class OverlayLayer implements ITooltipLayer {

    @Override
    public void render(TooltipContext ctx, Vec2 pos, Point size, TooltipStyle style, Component rarity, Font font, CustomFrameData customFrame) {
        ctx.push(() -> {
            ctx.translate(0, 0, LayerDepth.BACKGROUND_OVERLAY.getZ());
            Optional.ofNullable(customFrame).ifPresent(frame -> CustomFrameManager.renderCustomFrame(ctx, frame, pos, size));
        });
    }

}
