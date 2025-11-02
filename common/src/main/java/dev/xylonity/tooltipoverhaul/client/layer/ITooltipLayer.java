package dev.xylonity.tooltipoverhaul.client.layer;

import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

@FunctionalInterface
public interface ITooltipLayer {

    void render(TooltipContext context, Vec2 position);

    default void renderInternal(TooltipContext context) {
        render(context, context.getTooltipPosition());
    }

}
