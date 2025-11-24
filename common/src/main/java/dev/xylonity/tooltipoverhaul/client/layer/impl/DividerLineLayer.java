package dev.xylonity.tooltipoverhaul.client.layer.impl;

import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextUtils;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

public interface DividerLineLayer extends ITooltipLayer {

    @Override
    default void renderInternal(TooltipContext context) {
        context.push(() -> {
            context.translate(0, 0, getLayerDepth().getZ());
            context.setLayerDepth(getLayerDepth());

            boolean hasIcon = context.hasIcon();
            boolean hasRating = RenderUtils.hasRating(context);

            int y = (int) context.getTooltipPosition().y + context.getPaddingY();
            int x = (int) context.getTooltipPosition().x + context.getPaddingX();

            if (hasIcon) {
                int dividerLinetopPadding = Constants.getDividerLineTopPadding(context);
                y += Constants.getIconSize(context) + dividerLinetopPadding;
                if (dividerLinetopPadding > 1) {
                    y += Constants.getDividerLineHeight(context);
                }

            }
            else {
                int titleHeight = context.getComponents().get(0).getHeight();
                y += titleHeight;

                // If there is a rating text present, adds its height
                if (hasRating) {
                    Component rating = TextUtils.getRatingText(context);
                    y += ClientTooltipComponent.create(rating.getVisualOrderText()).getHeight();
                }

                y += Constants.getDividerLineTopPadding(context);

            }

            render(context, new Vec2(x, y));
        });

    }

    @Override
    default LayerDepth getLayerDepth() {
        return LayerDepth.DIVIDER_LINE;
    }

}
