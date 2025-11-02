package dev.xylonity.tooltipoverhaul.client.old.layer.bridge;

import dev.xylonity.tooltipoverhaul.client.old.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.old.layer.LayerDepth;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

@FunctionalInterface
public interface ITooltipText {
    void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size, Component rarity, Font font);
}
