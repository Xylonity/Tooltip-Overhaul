package dev.xylonity.tooltipoverhaul.client.layer.bridge;

import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import java.awt.Point;

import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import net.minecraft.world.phys.Vec2;

@FunctionalInterface
public interface ITooltipFrame {
    void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size);
}
