package dev.xylonity.tooltipoverhaul.client.layer.impl;

import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.style.TooltipStyle;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class EffectLayer implements ITooltipLayer {

    @Override
    public void render(TooltipContext ctx, Vec2 pos, Point size, TooltipStyle style, Component rarity, Font font, CustomFrameData customFrame) {
        ctx.push(() -> {
            ctx.translate(0, 0, LayerDepth.BACKGROUND_EFFECT.getZ());
            style.renderEffect(LayerDepth.BACKGROUND_EFFECT, ctx, pos, size);
        });
    }

}
