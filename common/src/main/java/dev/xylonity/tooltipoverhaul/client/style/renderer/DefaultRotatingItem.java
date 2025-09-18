package dev.xylonity.tooltipoverhaul.client.style.renderer;

import com.mojang.math.Axis;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipRotatingItem;
import dev.xylonity.tooltipoverhaul.compat.modernfix.ModernFixCompat;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.util.Util;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class DefaultRotatingItem implements ITooltipRotatingItem {

    private static final float BASE_SCALE = 2.75f;

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        this.renderDefault(depth, ctx, pos, size);
    }

    private void renderDefault(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        ctx.push(() -> {
            ctx.translate(0, 0, depth.getZ());

            ctx.translate(pos.x, pos.y, 0);

            ctx.multiply(Axis.YP, -(Util.calcRotY(8000d / TooltipsConfig.TIERED_ITEM_PREVIEW_ROTATING_SPEED)));
            ctx.multiply(Axis.ZP, -45);

            ctx.scale(BASE_SCALE, BASE_SCALE, BASE_SCALE);

            // Reverting the default renderItem pivot
            ctx.translate(-8f, -8f, -150f);

            ModernFixCompat.push();
            try {
                ctx.graphics().renderItem(ctx.stack(), 0, 0);
            }
            finally {
                ModernFixCompat.pop();
            }

        });

    }

}
