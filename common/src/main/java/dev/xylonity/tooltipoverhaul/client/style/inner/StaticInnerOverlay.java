package dev.xylonity.tooltipoverhaul.client.style.inner;

import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipFrame;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class StaticInnerOverlay implements ITooltipFrame {

    private final int color;

    public StaticInnerOverlay(int color) {
        this.color = color;
    }

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        int x0 = (int) pos.x - 3;
        int y0 = (int) pos.y - 3;
        int width = size.x + 6;
        int height = size.y + 6;

        // Top
        ctx.graphics().fill(x0, y0, x0 + width, y0 + 1, depth.getZ(), color);
        // Bottom
        ctx.graphics().fill(x0, y0 + height - 1, x0 + width, y0 + height, depth.getZ(), color);
        // Left
        ctx.graphics().fill(x0, y0, x0 + 1, y0 + height, depth.getZ(), color);
        // Right
        ctx.graphics().fill(x0 + width - 1, y0, x0 + width, y0 + height, depth.getZ(), color);
    }

}
