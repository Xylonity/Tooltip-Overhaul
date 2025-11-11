package dev.xylonity.tooltipoverhaul.client.style.icon.background;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.xylonity.tooltipoverhaul.client.layer.impl.IconBackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.render.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import net.minecraft.world.phys.Vec2;

public class GlowingIconBackground implements IconBackgroundLayer {

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

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int glowIntensity = (int) (100 + 50 * Math.sin(TooltipRenderer.COUNTER * 1.5));

        int color = ColorUtils.getDividerLineColor(context) & 0x00FFFFFF;

        int innerGlow = (Math.min(Math.max(glowIntensity + 30, 0), 255) << 24) | color;

        context.getGraphics().fill(x0, y0, x1, y1, context.getLayerDepth().getZ(), 0xC0181825);

        // Top border
        context.getGraphics().fill(x0 - 1, y0 - 1, x1 + 1, y0, context.getLayerDepth().getZ(), innerGlow);

        // Bottom border
        context.getGraphics().fill(x0 - 1, y1, x1 + 1, y1 + 1, context.getLayerDepth().getZ(), innerGlow);

        // Left border
        context.getGraphics().fill(x0 - 1, y0, x0, y1, context.getLayerDepth().getZ(), innerGlow);

        // Right border
        context.getGraphics().fill(x1, y0, x1 + 1, y1, context.getLayerDepth().getZ(), innerGlow);
    }

}