package dev.xylonity.tooltipoverhaul.client.layer.impl;

import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.style.TooltipStyle;
import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class ArmorStandLayer implements ITooltipLayer {

    @Override
    public void render(TooltipContext ctx, Vec2 pos, Point size, TooltipStyle style, Component rarity, Font font, CustomFrameData customFrame) {

        if (!TooltipsConfig.ARMOR_ITEMS_RENDERER) return;
        if (!(ctx.stack().getItem() instanceof ArmorItem)) return;

        Vec2 finalPos = pos.add(new Vec2(-30 + TooltipsConfig.SECOND_PANEL_X, 56 + TooltipsConfig.SECOND_PANEL_Y));
        ctx.push(() -> {
            ctx.translate(0, 0, LayerDepth.BACKGROUND_RENDERS.getZ());
            style.renderArmorStand(LayerDepth.BACKGROUND_RENDERS, ctx, finalPos, size);
        });
    }

}
