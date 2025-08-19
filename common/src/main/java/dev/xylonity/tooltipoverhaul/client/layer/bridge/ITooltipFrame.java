package dev.xylonity.tooltipoverhaul.client.layer.bridge;

import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

@FunctionalInterface
public interface ITooltipFrame {
    void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size);
}
