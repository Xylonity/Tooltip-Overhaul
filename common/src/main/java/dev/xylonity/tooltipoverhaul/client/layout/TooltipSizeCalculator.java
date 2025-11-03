package dev.xylonity.tooltipoverhaul.client.layout;

import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
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
        boolean hasDividerLine = context.hasDividerLine();

        List<ClientTooltipComponent> components = context.getComponents();
        Font font = context.getFont();

        int paddingX = context.getPaddingX();
        int paddingY = context.getPaddingY();

        int iconOffset = hasIcon ? (Constants.ICON_SIZE + Constants.SEPARATION_TITLE_ICON) : 0;

        int width = components.get(0).getWidth(font) + paddingX * 2 + iconOffset;
        int height = components.get(0).getHeight() + paddingY * 2;
        for (int i = 0; i < components.size(); i++) {
            if (i > 0) {
                height += components.get(i).getHeight();
                width = Math.max(width, components.get(i).getWidth(font) + paddingX * 2);
            }

        }

        if (hasRating) {
            Component rating = TextUtils.computeRatingText(context);
            width = Math.max(width, font.width(rating) + paddingX * 2 + iconOffset);

            if (!hasIcon) {
                height += ClientTooltipComponent.create(rating.getVisualOrderText()).getHeight();
            }
        }

        if (hasIcon) {
            height += Constants.ICON_SIZE_PADDING;
        }

        if (hasDividerLine && components.size() > 1) {
            height += Constants.DIVIDER_LINE_FULL_PADDING;
        }

        return new Vec2(width, height);
    }

    /**
     * Calculates the header height (title + rating + icon + divider)
     */
    public int calculateHeaderHeight() {

        int height = 0;
        boolean hasIcon = context.hasIcon();
        boolean hasDividerLine = context.hasDividerLine();
        boolean hasRating = RenderUtils.hasRating(context);

        ClientTooltipComponent title = context.getComponents().get(0);
        height += title.getHeight();

        // TODO: calculate using the actual rating height (or maybe use the title height here)
        if (hasRating) {
            height += 10;
        }
        else if (hasIcon) {
            height += title.getHeight();
        }

        if (hasIcon) {
            height += Constants.SEPARATION_TITLE_ICON;
        }

        if (hasDividerLine && context.getComponents().size() > 1) {
            height += Constants.DIVIDER_LINE_FULL_PADDING;
        }

        return height;
    }

    /**
     * Calculates the height for the viewport scrollable content
     */
    public int calculateScrollableContentHeight() {
        int height = 0;
        for (int i = 1; i < context.getComponents().size(); i++) {
            height += context.getComponents().get(i).getHeight();
        }

        return height;
    }

}
