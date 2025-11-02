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
        return new Vec2(context.getMouseX(), context.getMouseY()).add(new Vec2(12, -12));
    }

}
