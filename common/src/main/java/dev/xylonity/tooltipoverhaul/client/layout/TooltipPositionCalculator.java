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

        int paddingX = context.getPaddingX();
        int paddingY = context.getPaddingY();
        int mouseX = context.getMouseX();
        int mouseY = context.getMouseY();
        int tooltipWidth = (int) context.getTooltipSize().x;
        int tooltipHeight = (int) context.getTooltipSize().y;
        int screenWidth = context.getScreenWidth();
        int screenHeight = context.getScreenHeight();

        // Initial position (with offset)
        float posX = mouseX + 12;
        float posY = mouseY - 12;

        // If it exceeds the right border, put the tooltip to the left
        // Not adding the padding here so the vanilla's wrapper doesn't flicker
        if (posX + tooltipWidth > screenWidth) {
            posX = mouseX - tooltipWidth - 12;
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

        return new Vec2(posX, posY);
    }

}