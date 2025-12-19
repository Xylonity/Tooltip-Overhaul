package dev.xylonity.tooltipoverhaul.client.style.icon.background;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.xylonity.tooltipoverhaul.client.layer.impl.IconBackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.render.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import net.minecraft.world.phys.Vec2;

public class FocusIconBackground implements IconBackgroundLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {

        int positionX = (int) context.getTooltipPosition().x - 1 + context.getPaddingX();
        int positionY = (int) context.getTooltipPosition().y + context.getPaddingY();

        int slotSizeX = positionX + Constants.getIconSize(context);
        int slotSizeY = positionY + Constants.getIconSize(context);

        int x0 = positionX;
        int y0 = positionY;
        int x1 = slotSizeX;
        int y1 = slotSizeY;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int color = (((int) (40 + 30 * Math.sin(TooltipRenderer.COUNTER * 2.5))) << 24) | 0xFFFFFF;

        // Background
        context.getGraphics().fill(x0, y0, x1, y1, 0x8018181C);

        // Top left
        context.getGraphics().fill(x0, y0 - 1, x0 + 5, y0, color);
        context.getGraphics().fill(x0 - 1, y0 - 1, x0, y0 + 5, color);

        // Bottom left
        context.getGraphics().fill(x0, y1 + 1, x0 + 5, y1, color);
        context.getGraphics().fill(x0 - 1, y1 - 5, x0, y1 + 1, color);

        // Top right
        context.getGraphics().fill(x1 - 5, y0 - 1, x1 + 1, y0, color);
        context.getGraphics().fill(x1 + 1, y0, x1, y0 + 5, color);

        // Bottom right
        context.getGraphics().fill(x1 - 5, y1, x1 + 1, y1 + 1, color);
        context.getGraphics().fill(x1 + 1, y1 - 5, x1, y1, color);
    }

}