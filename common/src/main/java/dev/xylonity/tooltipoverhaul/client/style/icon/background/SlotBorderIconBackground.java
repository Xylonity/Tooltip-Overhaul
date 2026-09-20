package dev.xylonity.tooltipoverhaul.client.style.icon.background;

import dev.xylonity.tooltipoverhaul.client.layer.impl.IconBackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipLayout;
import net.minecraft.world.phys.Vec2;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;

public class SlotBorderIconBackground implements IconBackgroundLayer {

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

        final int border = ColorUtils.getIconBorderColor(context, 0x905E5E5E);

        // Background
        context.getGraphics().fill(x0, y0, x1, y1, ColorUtils.getIconBackgroundColor(context, 0x903E3E3E));

        IconBorder.render(context, x0, y0, x1, y1, border);
    }

}