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
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class SecondBackgroundLayer implements ITooltipLayer {

    @Override
    public void render(TooltipContext ctx, Vec2 pos, Point size, TooltipStyle style, Component rarity, Font font, CustomFrameData customFrame) {

        if (!TooltipsConfig.ARMOR_ITEMS_RENDERER && !TooltipsConfig.TIERED_ITEMS_RENDERER) return;
        if (!(ctx.stack().getItem() instanceof ArmorItem) && !(ctx.stack().getItem() instanceof TieredItem)) return;

        Point sec = new Point(30, 60);
        Vec2 p = pos.add(new Vec2(-45 + TooltipsConfig.SECOND_PANEL_X, TooltipsConfig.SECOND_PANEL_Y));
        ctx.push(() -> {
            ctx.translate(0, 0, LayerDepth.BACKGROUND.getZ());
            style.renderBack(LayerDepth.BACKGROUND, ctx, p, sec);
        });
    }

}
