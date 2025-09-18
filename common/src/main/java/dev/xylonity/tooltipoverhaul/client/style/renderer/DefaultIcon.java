package dev.xylonity.tooltipoverhaul.client.style.renderer;

import com.mojang.math.Axis;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipIcon;
import dev.xylonity.tooltipoverhaul.compat.modernfix.ModernFixCompat;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class DefaultIcon implements ITooltipIcon {

    private static final float ANIMATION_DURATION = 0.6f;

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        // Para el hovered del tooltip, queremos el look GUI exacto.
        // El mixin ya desactiva el culling de ModernFix SOLO aquí.
        this.renderDefault(depth, ctx, pos, size);
    }

    private void renderDefault(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        ctx.push(() -> {
            ctx.translate(0, 0, depth.getZ());

            ctx.translate((pos.x + TooltipRenderer.PADDING_X + 4) + 8, (pos.y + TooltipRenderer.PADDING_Y + 4) + 8, 0);
            ctx.translate(0, 0, 150);

            float progress = Math.min(TooltipRenderer.ELAPSED / ANIMATION_DURATION, 1.0f);

            // Scales from 0 to default scale during the first ms of the anim
            if (TooltipRenderer.ELAPSED < ANIMATION_DURATION) {
                float scale = easeOutQuint(progress);
                ctx.scale(scale * 1.5f, scale * 1.5f, scale * 1.5f);

                ctx.multiply(Axis.YP, (float) ((TooltipRenderer.ELAPSED * 5) * 360 / 6.0 % 360));
            }
            else {
                ctx.scale(1.5f, 1.5f, 1.5f);

                float elapsed = TooltipRenderer.ELAPSED - ANIMATION_DURATION;

                // Extra rotation from the anim above
                float finalRot = (float) ((ANIMATION_DURATION * 5) * 360 / 6.0 % 360);

                ctx.multiply(Axis.YP, (float) (finalRot + (elapsed * 360 / 6.0) % 360));
            }

            // Reverting the default renderItem pivot
            ctx.translate(-8, -8, -150);

            ModernFixCompat.push();
            try {
                ctx.graphics().renderItem(ctx.stack(), 0, 0);
            }
            finally {
                ModernFixCompat.pop();
            }

        });

    }

    private static float easeOutQuint(float t) {
        float cubicPart = 1 - (float) Math.pow(1 - t, 3);
        return cubicPart * t + t * (1 - t);
    }

}