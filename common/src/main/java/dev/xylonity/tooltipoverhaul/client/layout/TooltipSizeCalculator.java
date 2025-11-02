package dev.xylonity.tooltipoverhaul.client.layout;

import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.phys.Vec2;

import java.awt.*;
import java.util.List;

public class TooltipSizeCalculator {

    private final TooltipContext context;

    public TooltipSizeCalculator(TooltipContext context) {
        this.context = context;
    }

    public Vec2 calculate() {

        boolean hasIcon = context.hasIcon();
        boolean hasRating = RenderUtils.hasRating(context);
        boolean isEmptyTooltip = context.isEmptyTooltip();

        List<ClientTooltipComponent> components = context.getComponents();
        Font font = context.getFont();

        int paddingX = context.getPaddingX();
        int paddingY = context.getPaddingY();

        int width = components.get(0).getWidth(font) + paddingX * 2;
        int height = components.get(0).getHeight() + paddingY * 2;
        for (int i = 0; i < components.size(); i++) {
            if (i > 0) {
                height += components.get(i).getHeight();
            }

            width = Math.max(width, components.get(i).getWidth(font) + paddingX * 2);
        }

        if (hasRating) {
            width = Math.max(width, font.width(TextUtils.computeRatingText(context)) + paddingX * 2);
        }

        if (hasIcon) {
            width += Constants.ICON_SIZE + Constants.SEPARATION_TITLE_ICON;
            height += Constants.ICON_SIZE_PADDING;
        }

        return new Vec2(width, height);
    }

}
