package dev.xylonity.tooltipoverhaul.client.style.renderer;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipIcon;
import dev.xylonity.tooltipoverhaul.compat.modernfix.ModernFixCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class DefaultIcon implements ITooltipIcon {

    private static final float ANIMATION_DURATION = 0.6f;

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        if (ModernFixCompat.STATIC_RENDERING_ENABLED) {
            this.renderStatic(depth, ctx, pos, size);
        } else {
            this.renderDefault(depth, ctx, pos, size);
        }

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
            } else {
                ctx.scale(1.5f, 1.5f, 1.5f);

                float elapsed = TooltipRenderer.ELAPSED - ANIMATION_DURATION;

                // Extra rotation from the anim above
                float finalRot = (float) ((ANIMATION_DURATION * 5) * 360 / 6.0 % 360);

                ctx.multiply(Axis.YP, (float) (finalRot + (elapsed * 360 / 6.0) % 360));
            }

            // Reverting the default renderItem pivot
            ctx.translate(-8, -8, -150);
            ctx.graphics().renderItem(ctx.stack(), 0, 0);
        });

    }

    private void renderStatic(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        ctx.push(() -> {
            ctx.translate(0, 0, depth.getZ());

            ctx.translate((pos.x + TooltipRenderer.PADDING_X + 12), (pos.y + TooltipRenderer.PADDING_Y + 12), 0);

            float time = Math.min(TooltipRenderer.ELAPSED / ANIMATION_DURATION, 1.0f);
            float scale = TooltipRenderer.ELAPSED < ANIMATION_DURATION ? easeOutQuint(time) * 1.5f : 1.5f;

            float angle;
            if (TooltipRenderer.ELAPSED < ANIMATION_DURATION) {
                angle = (float) (((TooltipRenderer.ELAPSED * 5.0) * 360.0 / 6.0) % 360.0);
            } else {
                float normal = TooltipRenderer.ELAPSED - ANIMATION_DURATION;
                float rot = (float) (((ANIMATION_DURATION * 5.0) * 360.0 / 6.0) % 360.0);
                angle = (float) ((rot + (normal * 360.0 / 6.0)) % 360.0);
            }

            ctx.multiply(Axis.YP, angle);

            ctx.scale(1.0f, -1.0f, 1.0f);
            ctx.scale(20.0f * scale, 20.0f * scale, 20.0f * scale);

            Minecraft minecraft = Minecraft.getInstance();
            BakedModel model = minecraft.getItemRenderer().getModel(ctx.stack(), null, null, 0);

            if (ctx.stack().getItem() instanceof BlockItem && model.isGui3d()) {
                ctx.multiply(Axis.XP, 30f);
                ctx.multiply(Axis.YP, -45f);
            }

            Lighting.setupForFlatItems();
            Minecraft.getInstance().getItemRenderer().renderStatic(ctx.stack(), ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ctx.graphics().pose(), ctx.graphics().bufferSource(), null, 0);
            //Lighting.setupForFlatItems();
        });
    }

    private static float easeOutQuint(float t) {
        float cubicPart = 1 - (float) Math.pow(1 - t, 3);
        return cubicPart * t + t * (1 - t);
    }

}