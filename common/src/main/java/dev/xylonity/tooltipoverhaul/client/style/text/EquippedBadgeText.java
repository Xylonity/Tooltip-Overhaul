package dev.xylonity.tooltipoverhaul.client.style.text;

import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipText;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

/**
 * Handles the rendering of the equipped text qualifier above the compared tooltip
 */
public class EquippedBadgeText implements ITooltipText {

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size, Component rarity, Font font) {
        ctx.push(() -> {
            ctx.translate(0, 0, depth.getZ());
            MutableComponent text = Component.translatable("tooltipoverhaul.equipped_badge_text");
            ctx.graphics().drawString(font, text, (int) pos.x + (size.x / 2) - font.width(text) / 2, (int) pos.y + 2, 0xA8A8A8, false);
        });

    }

}
