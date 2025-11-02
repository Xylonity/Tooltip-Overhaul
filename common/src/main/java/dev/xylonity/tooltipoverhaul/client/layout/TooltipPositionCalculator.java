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

        int mouseX = context.getMouseX();
        int mouseY = context.getMouseY();
        int sizeX = (int) context.getTooltipSize().x;
        int sizeY = (int) context.getTooltipSize().y;

        Vec2 position = new Vec2(mouseX, mouseY);
        position = position.add(new Vec2(12, -12));

        if (mouseX + sizeX + 14 > context.getScreenWidth()) {
            position = position.add(new Vec2(sizeX + 24, 0).negated());
        }

        return position;
    }

}
