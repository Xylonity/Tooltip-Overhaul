package dev.xylonity.tooltipoverhaul.client.style.icon.background;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.xylonity.tooltipoverhaul.client.layer.impl.IconBackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.render.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipLayout;
import net.minecraft.world.phys.Vec2;

public class GlowingIconBackground implements IconBackgroundLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {

        final int positionX = (int) context.getTooltipPosition().x + TooltipLayout.iconX(context);
        final int positionY = (int) context.getTooltipPosition().y + TooltipLayout.iconY(context);

        final int slotSizeX = positionX + Constants.getIconSize(context);
        final int slotSizeY = positionY + Constants.getIconSize(context);

        final int x0 = positionX;
        final int y0 = positionY;
        final int x1 = slotSizeX;
        final int y1 = slotSizeY;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        final int glowIntensity = (int) (100 + 50 * Math.sin(TooltipRenderer.COUNTER * 1.5));

        final int color = ColorUtils.getIconBorderColor(context, ColorUtils.getDividerLineColor(context)) & 0x00FFFFFF;

        final int innerGlow = (Math.min(Math.max(glowIntensity + 30, 0), 255) << 24) | color;

        context.getGraphics().fill(x0, y0, x1, y1, ColorUtils.getIconBackgroundColor(context, 0x603E3E3E));

        IconBorder.render(context, x0, y0, x1, y1, innerGlow);
    }

}