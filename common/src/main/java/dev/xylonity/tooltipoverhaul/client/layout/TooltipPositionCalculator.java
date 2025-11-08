package dev.xylonity.tooltipoverhaul.client.layout;

import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class TooltipPositionCalculator {

    private final TooltipContext context;

    public TooltipPositionCalculator(TooltipContext context) {
        this.context = context;
    }

    public Vec2 calculate() {

        boolean isMainTooltip = context.isMainTooltip();

        int paddingX = context.getPaddingX();
        int paddingY = context.getPaddingY();
        int mouseX = context.getMouseX();
        int mouseY = context.getMouseY();
        int tooltipWidth = (int) context.getTooltipSize().x;
        int tooltipHeight = (int) context.getTooltipSize().y;
        int screenWidth = context.getScreenWidth();
        int screenHeight = context.getScreenHeight();

        // Initial position (with offset)
        float posX = mouseX + (isMainTooltip ? 12 : -12);
        float posY = mouseY - 12;

        if (isMainTooltip) {
            TooltipContext equippedContext = context.getOtherTooltipContext();
            boolean hasEquippedContext = equippedContext != null;

            // Just if the equipped stack is not available
            if (!hasEquippedContext) {
                // If it exceeds the right border, put the tooltip to the left
                // Not adding the padding here so the vanilla's wrapper doesn't flicker
                if (posX + tooltipWidth > screenWidth) {
                    posX = mouseX - tooltipWidth - 12;
                }

            }

            // Clamps to the right border of the screen (now taking into account the padding value),
            // so the tooltip doesn't exceed the screen limits
            if (posX + tooltipWidth + paddingX > screenWidth) {
                posX = screenWidth - tooltipWidth - paddingX;
            }

            // Clamps to the left border of the screen
            if (posX < paddingX) {
                posX = paddingX;
            }

            // If it exceeds the bottom border, increase the height
            if (posY + tooltipHeight + paddingY > screenHeight) {
                posY = screenHeight - tooltipHeight - paddingY;
            }

            // If it exceeds the top border, decrease the height
            if (posY < paddingY) {
                posY = paddingY;
            }

        }
        else {

            posX = posX - tooltipWidth;

        }

        return new Vec2(posX, posY);
    }

    /**
     * This method is used as a filter if the equipped stack is active, thus recalculating certain tooltip positions depending on
     * the length of both tooltips
     * @return the new size of the current context
     */
    public Vec2 adjustPosition() {

        Vec2 newPosition = context.getTooltipPosition();

        boolean isMainTooltip = context.isMainTooltip();

        int paddingX = context.getPaddingX();
        int paddingY = context.getPaddingY();
        int mouseY = context.getMouseY();
        int tooltipHeight = (int) context.getTooltipSize().y;
        int screenHeight = context.getScreenHeight();

        // Initial position (with offset)
        float posY = mouseY - 12;

        TooltipContext otherContext = context.getOtherTooltipContext();

        Vec2 otherContextPosition = otherContext != null ? otherContext.getTooltipPosition() : null;
        Vec2 otherContextSize = otherContext != null ? otherContext.getTooltipSize() : null;

        if (isMainTooltip) {

            if (otherContextPosition != null && otherContextSize != null) {
                if (otherContextPosition.x <= paddingX * 2) {
                    newPosition = new Vec2(otherContextPosition.x + otherContextSize.x + 12 * 2, newPosition.y);
                }
            }

            // Vertically aligns the tooltip under a certain margin. That is, if the main tooltip is too high or too
            // low, the equipable tooltip is aligned with respect to the margins of the screen
            int margin = 60;
            if (otherContextPosition != null && otherContextSize != null) {
                if (posY != otherContextPosition.y) {
                    int difference = (int) (posY - otherContextPosition.y);
                    difference = difference < 0 ? -difference : difference;

                    if (difference > margin) {
                        newPosition = new Vec2(newPosition.x, screenHeight / 2f - tooltipHeight / 2f);
                    }

                }

            }

        }
        else {

            // If it exceeds the bottom border, increase the height
            if (posY + tooltipHeight + paddingY > screenHeight) {
                posY = screenHeight - tooltipHeight - paddingY;
            }

            // If it exceeds the top border, decrease the height
            if (posY < paddingY) {
                posY = paddingY;
            }

            // Hooks the second tooltip to the main tooltip X position
            if (otherContextPosition != null && otherContextSize != null) {
                newPosition = new Vec2(0, context.getTooltipPosition().y).add(new Vec2(otherContextPosition.x - 24 - context.getTooltipSize().x, 0));
                newPosition = new Vec2(newPosition.x, otherContextPosition.y);

                if (newPosition.x < context.getPaddingX()) {
                    newPosition = new Vec2(context.getPaddingX() * 2, newPosition.y);
                }
            }

            // Vertically aligns the tooltip under a certain margin. That is, if the main tooltip is too high or too
            // low, the equipable tooltip is aligned with respect to the margins of the screen
            int margin = 60;
            if (otherContextPosition != null && otherContextSize != null) {
                if (posY != otherContextPosition.y) {
                    int difference = (int) (posY - otherContextPosition.y);
                    difference = difference < 0 ? -difference : difference;

                    if (difference > margin) {
                        newPosition = new Vec2(newPosition.x, screenHeight / 2f - tooltipHeight / 2f);
                    }

                }

            }

        }

        return newPosition;
    }

}