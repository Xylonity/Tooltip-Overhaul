package dev.xylonity.tooltipoverhaul.client.old.layer.bridge;

import dev.xylonity.tooltipoverhaul.client.old.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.old.layer.LayerDepth;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

@FunctionalInterface
public interface ITooltipArmorStand {
    void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size);
}
