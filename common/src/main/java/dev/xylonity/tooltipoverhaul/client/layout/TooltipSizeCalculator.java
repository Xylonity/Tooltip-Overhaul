package dev.xylonity.tooltipoverhaul.client.layout;

import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

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

        // Offset to the right if the icon is active
        int iconOffset = hasIcon ? (Constants.getIconSize(context) + Constants.getIconTitleSeparation(context)) : 0;

        // Computes approximated width and height using the component amount
        int width = components.get(0).getWidth(font) + paddingX * 2 + iconOffset;
        int height = components.get(0).getHeight() + paddingY * 2;
        for (int i = 0; i < components.size(); i++) {
            if (i > 0) {
                height += components.get(i).getHeight();
                width = Math.max(width, components.get(i).getWidth(font) + paddingX * 2);
            }

        }

        // Computes the new width if the rating is larger than the tooltip's width and adds extra height in case the icon is not present
        if (hasRating) {
            Component rating = TextUtils.getRatingText(context);
            width = Math.max(width, font.width(rating) + paddingX * 2 + iconOffset);

            if (!hasIcon) {
                height += ClientTooltipComponent.create(rating.getVisualOrderText()).getHeight();
            }
        }

        // If the tooltip has an icon active, subtracts the title component height (which is approximately 10)
        if (hasIcon) {
            height += Constants.getIconSize(context) - 10;
        }

        //
        if (hasDividerLine && components.size() > 1) {
            if (hasIcon) {
                height += Constants.getDividerLineFullPadding(context);
            }
            else {
                height += Constants.getDividerLineFullPadding(context);
            }

        }

        return new Vec2(width, height);
    }

    public Vec2 adjustSize() {

        Vec2 newSize = context.getTooltipSize();

        boolean isMainTooltip = context.isMainTooltip();
        TooltipContext otherContext = context.getOtherTooltipContext();

        Vec2 otherContextPosition = otherContext != null ? otherContext.getTooltipPosition() : null;
        Vec2 otherContextSize = otherContext != null ? otherContext.getTooltipSize() : null;

        if (isMainTooltip) {
            if (otherContextPosition != null && otherContextSize != null) {
                if (otherContextPosition.x == context.getPaddingX() && context.getTooltipPosition().x + context.getTooltipSize().x > context.getScreenWidth()) {
                    newSize = new Vec2(context.getScreenWidth() - context.getPaddingX() - context.getTooltipPosition().x, newSize.y);
                }
            }

        }
        else {

        }

        return newSize;
    }

}
