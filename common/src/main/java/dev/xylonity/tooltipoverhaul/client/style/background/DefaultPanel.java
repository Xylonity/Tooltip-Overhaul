package dev.xylonity.tooltipoverhaul.client.style.background;

import dev.xylonity.tooltipoverhaul.client.Palette;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipPanel;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class DefaultPanel implements ITooltipPanel {

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        int x0 = (int) pos.x - 3;
        int y0 = (int) pos.y - 3;
        int x1 = (int) pos.x + size.x + 3;
        int y1 = (int) pos.y + size.y + 3;

        // Background
        ctx.graphics().fill(x0, y0, x1, y1, depth.getZ(), Palette.PANEL_BG);

        // Top border
        ctx.graphics().fill(x0, y0 - 1, x1, y0, depth.getZ(), Palette.PANEL_BG);
        // Bottom border
        ctx.graphics().fill(x0, y1 + 1, x1, y1, depth.getZ(), Palette.PANEL_BG);
        // Left border
        ctx.graphics().fill(x0 - 1, y0, x0, y1, depth.getZ(), Palette.PANEL_BG);
        // Right border
        ctx.graphics().fill(x1 + 1, y0, x1, y1, depth.getZ(), Palette.PANEL_BG);
    }

}