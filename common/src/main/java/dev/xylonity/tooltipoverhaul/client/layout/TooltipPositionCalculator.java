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

        int extraMargin = 2;

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
        if (posX + tooltipWidth + extraMargin > screenWidth) {
            posX = mouseX - tooltipWidth - 12;
        }

        // If it exceeds the left border, put the tooltip to the right
        if (posX < extraMargin) {
            posX = extraMargin;
        }

        // If it exceeds the bottom border, increase the height
        if (posY + tooltipHeight + extraMargin > screenHeight) {
            posY = screenHeight - tooltipHeight - extraMargin;
        }

        // If it exceeds the top border, decrease the height
        if (posY < extraMargin) {
            posY = extraMargin;
        }

        return new Vec2(posX, posY);
    }

}
