package dev.xylonity.tooltipoverhaul.client.style.icon;

import com.mojang.math.Axis;
import dev.xylonity.tooltipoverhaul.client.layer.impl.IconLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.compat.modernfix.ModernFixCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec2;

public class DefaultIcon implements IconLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {

        float positionX = context.getTooltipPosition().x + context.getPaddingX() - 1;
        float positionY = context.getTooltipPosition().y + context.getPaddingY();

        context.push(() -> {

            context.translate(positionX + Constants.ICON_SIZE / 2f, positionY + Constants.ICON_SIZE / 2f, context.getLayerDepth().getZ());

            float time = (System.currentTimeMillis() % 3600000) / 10.0f;
            context.multiply(Axis.YP, time);

            context.scale(1.35f, 1.35f, 1.35f);

            if (ModernFixCompat.SHOULD_RETURN_ORIGINAL_RENDER) {
                ModernFixCompat.push();
                try {
                    RenderUtils.renderItem(context, Minecraft.getInstance().player, Minecraft.getInstance().level, context.getStack(), 0, 0, 0);
                }
                finally {
                    ModernFixCompat.pop();
                }
            }
            else {
                RenderUtils.renderItem(context, Minecraft.getInstance().player, Minecraft.getInstance().level, context.getStack(), 0, 0, 0);
            }

        });

    }

}