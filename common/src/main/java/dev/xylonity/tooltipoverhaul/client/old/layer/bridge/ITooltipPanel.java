package dev.xylonity.tooltipoverhaul.client.old.layer.bridge;

import dev.xylonity.tooltipoverhaul.client.old.TooltipContext;
import java.awt.Point;

import dev.xylonity.tooltipoverhaul.client.old.layer.LayerDepth;
import net.minecraft.world.phys.Vec2;

@FunctionalInterface
public interface ITooltipPanel {
    void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size);
}


