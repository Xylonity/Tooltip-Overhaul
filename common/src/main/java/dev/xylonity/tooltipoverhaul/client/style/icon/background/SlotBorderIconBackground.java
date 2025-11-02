package dev.xylonity.tooltipoverhaul.client.style.icon.background;

import dev.xylonity.tooltipoverhaul.client.layer.impl.IconBackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class SlotBorderIconBackground implements IconBackgroundLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {

        int positionX = (int) context.getTooltipPosition().x - 1 + context.getPaddingX();
        int positionY = (int) context.getTooltipPosition().y + context.getPaddingY();

        int slotSizeX = positionX + Constants.ICON_SIZE;
        int slotSizeY = positionY + Constants.ICON_SIZE;

        int x0 = positionX;
        int y0 = positionY;
        int x1 = slotSizeX;
        int y1 = slotSizeY;

        // Background
        context.getGraphics().fill(x0, y0, x1, y1, context.getLayerDepth().getZ(), 0x903E3E3E);

        // Top border
        context.getGraphics().fill(x0, y0 - 1, x1, y0, context.getLayerDepth().getZ(), 0x905E5E5E);
        // Bottom border
        context.getGraphics().fill(x0, y1 + 1, x1, y1, context.getLayerDepth().getZ(), 0x905E5E5E);
        // Left border
        context.getGraphics().fill(x0 - 1, y0, x0, y1, context.getLayerDepth().getZ(), 0x905E5E5E);
        // Right border
        context.getGraphics().fill(x1 + 1, y0, x1, y1, context.getLayerDepth().getZ(), 0x905E5E5E);
    }

}