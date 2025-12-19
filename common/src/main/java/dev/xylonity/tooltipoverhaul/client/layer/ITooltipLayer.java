package dev.xylonity.tooltipoverhaul.client.layer;

import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import net.minecraft.world.phys.Vec2;

@FunctionalInterface
public interface ITooltipLayer {

    LayerDepth getLayerDepth();

    default void render(TooltipContext context, Vec2 position) {
        ;;
    }

    default void renderInternal(TooltipContext context) {
        render(context, context.getTooltipPosition());
    }

}
