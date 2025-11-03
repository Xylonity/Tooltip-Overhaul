package dev.xylonity.tooltipoverhaul.client.layer.impl;

import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import net.minecraft.world.phys.Vec2;

public interface DividerLineLayer extends ITooltipLayer {

    @Override
    default void renderInternal(TooltipContext context) {
        context.push(() -> {
            LayerDepth layerDepth = LayerDepth.DIVIDER_LINE;
            context.translate(0, 0, layerDepth.getZ());
            context.setLayerDepth(layerDepth);

            boolean hasIcon = context.hasIcon();
            boolean hasRating = RenderUtils.hasRating(context);

            int y = (int) context.getTooltipPosition().y + context.getPaddingY();
            int x = (int) context.getTooltipPosition().x + context.getPaddingX();

            if (hasIcon) {
                y += Constants.ICON_SIZE + Constants.SEPARATION_TITLE_ICON;
            }
            else {
                int extraY = context.getComponents().get(0).getHeight();
                if (hasRating) {
                    y += extraY * 2 + context.getPaddingY();
                }
                else {
                    y += extraY + context.getPaddingY();
                }

            }

            render(context, new Vec2(x, y));
        });

    }

}
