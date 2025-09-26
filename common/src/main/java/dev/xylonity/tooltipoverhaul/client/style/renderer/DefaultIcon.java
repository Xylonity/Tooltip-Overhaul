package dev.xylonity.tooltipoverhaul.client.style.renderer;

import com.mojang.math.Axis;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipIcon;
import dev.xylonity.tooltipoverhaul.compat.modernfix.ModernFixCompat;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.util.Util;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class DefaultIcon implements ITooltipIcon {

    private static final float ANIMATION_DURATION = 0.6f;
    private static float SCALE = TooltipsConfig.ICON_SIZE;

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        this.renderDefault(depth, ctx, pos, size);
    }

    private void renderDefault(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {

        if (ctx.data().isPresent()) {
            SCALE = ctx.data().get().getIconSize();
        }

        ctx.push(() -> {
            ctx.translate(0, 0, depth.getZ());

            ctx.translate((pos.x + TooltipRenderer.PADDING_X + 4) + 8, (pos.y + TooltipRenderer.PADDING_Y + 4) + 8, 0);
            ctx.translate(0, 0, 150);

            float progress = Math.min(TooltipRenderer.ELAPSED / ANIMATION_DURATION, 1.0f);

            String animationName = Util.getIconAppearAnimation(ctx);

            if (animationName.equals("rotate") || animationName.equals("rotate_zoom")) {
                ctx.multiply(Axis.YP, 180);
            }

            // Scales from 0 to default scale during the first ms of the anim
            if (TooltipRenderer.ELAPSED < ANIMATION_DURATION) {
                switch (animationName) {
                    case "zoom" -> {
                        scale(ctx, progress);
                    }
                    case "rotate" -> {
                        ctx.scale(SCALE, SCALE, SCALE);
                        rotate(ctx, 1);
                    }
                    case "rotate_fast" -> {
                        ctx.scale(SCALE, SCALE, SCALE);
                        rotate(ctx, 2);
                    }
                    case "rotate_zoom" -> {
                        scale(ctx, progress);
                        rotate(ctx, 1);
                    }
                    case "zoom_snap" -> {
                        float abs;
                        if (progress < 0.72f) {
                            abs = 1.10f + 0.70f * easeOutCubic(progress / 0.72f);
                        }
                        else {
                            abs = 1.80f - (0.30f * smoothstep(0.0f, 1.0f, (progress - 0.72f) / 0.28f));
                        }
                        float scale = scaled(abs);

                        ctx.multiply(Axis.YP, 4.0f * (1.0f - progress));

                        ctx.scale(scale, scale, scale);
                    }
                    case "skew" -> {
                        float tilt = 18.0f * (1.0f - progress);

                        ctx.multiply(Axis.XP, tilt);
                        ctx.multiply(Axis.YP, -tilt * 0.6f);

                        float undershoot = 0.12f * (1f - progress);
                        float overshoot  = 0.20f * (float) Math.sin(Math.PI * progress);
                        float scale = SCALE * (1.0f - undershoot + overshoot);

                        ctx.scale(scale, scale, scale);
                    }
                    case "vibration" -> {
                        float decay = (1.0f - progress);

                        ctx.translate(1.2f * decay * (float) Math.sin(progress * 30.0f), 1.0f * decay * (float) Math.cos(progress * 27.0f), 0);

                        float abs = 1.40f + 0.10f * (1.0f - progress);
                        float scale = scaled(abs);

                        ctx.scale(scale, scale, scale);
                    }
                    case "tilt_wave" -> {
                        float amp = 16.0f * (1f - progress);
                        float waves = (float) Math.sin(progress * Math.PI * 6.0f);

                        ctx.multiply(Axis.ZP, amp * waves);
                        ctx.multiply(Axis.XP, 0.6f * amp * waves);

                        ctx.scale(SCALE, SCALE, SCALE);
                    }
                    case "flip" -> {
                        ctx.multiply(Axis.XP, (1f - (float) (1.0 - Math.pow(1.0 - progress, 2.2))) * 90.0f);

                        float undershoot = 0.12f * (1f - progress);
                        float overshoot  = 0.22f * (float) Math.sin(Math.PI * progress);
                        float scale = SCALE * (1f - undershoot + overshoot);

                        ctx.scale(scale, scale, scale);
                    }
                    case "pendulum" -> {
                        ctx.translate(0, -8, 0);

                        ctx.multiply(Axis.ZP, 28f * (float) Math.cos(progress * Math.PI * 3f) * (1f - progress));

                        ctx.translate(0, 8, 0);

                        ctx.scale(SCALE, SCALE, SCALE);
                    }
                    case "bounce" -> {
                        ctx.translate(0, dampBounce(progress, 3f, 8f), 0);

                        float abs = 1.45f + 0.05f * (float) Math.sin(progress * Math.PI);
                        float scale = scaled(abs);

                        ctx.scale(scale, scale, scale);
                    }
                    case "go_down" -> {
                        ctx.translate(0, -20f * (1f - easeOutCubic(progress)), 0);

                        ctx.multiply(Axis.ZP, (1f - progress) * -12f);

                        float scale = SCALE - 0.08f * (1f - progress);

                        ctx.scale(scale, scale, scale);
                    }
                    case "pulse" -> {
                        float pulses = (float) Math.sin(progress * Math.PI * 4.0f);
                        float abs = 1.50f + (0.25f * (1f - progress)) * pulses;
                        float scale = scaled(abs);

                        ctx.multiply(Axis.XP, 5f * pulses * (1f - progress));

                        ctx.scale(scale, scale, scale);
                    }
                    case "fan_in" -> {
                        ctx.multiply(Axis.YP, 32f * (float) Math.sin(progress * Math.PI * 5f) * (1f - progress));

                        float undershoot = 0.12f * (1f - progress);
                        float overshoot  = 0.18f * (float) Math.sin(Math.PI * progress);
                        float scale = SCALE * (1.0f - undershoot + overshoot);

                        ctx.scale(scale, scale, scale);
                    }
                    case "hover_pop" -> {
                        ctx.translate(0, -(6.0f * (1.0f - easeOutCubic(progress))), 0);

                        float scale = 1.35f + 0.20f * smoothstep(0.6f, 1.0f, progress);

                        ctx.multiply(Axis.XP, 6.0f * (1.0f - progress));

                        ctx.scale(scale, scale, scale);
                    }
                    case "barrel_roll" -> {
                        ctx.multiply(Axis.ZP, 360.0f * easeOutCubic(progress));

                        float abs = 0.90f + 0.60f * easeOutCubic(progress);
                        float scale = scaled(abs);

                        ctx.scale(scale, scale, scale);
                    }
                    default -> {
                        ctx.scale(SCALE, SCALE, SCALE);
                    }
                }

            }
            else {
                ctx.scale(SCALE, SCALE, SCALE);

                float elapsed = TooltipRenderer.ELAPSED - ANIMATION_DURATION;

                ctx.multiply(Axis.YP, (float) (computeFinalDegrees(animationName, ANIMATION_DURATION) + (elapsed * (360 / (6.0 / Util.getIconRotatingSpeed(ctx)))) % 360));
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

    private static float computeFinalDegrees(String anim, float duration) {
        int factor = switch (anim) {
            case "rotate_fast" -> 10;
            case "rotate", "rotate_zoom" -> 5;
            default -> 0;
        };

        return (duration * factor * (360 / 6f)) % 360;
    }

    private static float smoothstep(float a, float b, float x) {
        float step = clamp((x - a) / (b - a), 0f, 1f);
        return step * step * (3f - 2f * step);
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static float easeOutCubic(float time) {
        float ease = 1f - time;
        return 1f - ease * ease * ease;
    }

    private static float dampBounce(float time, float decay, float height) {
        return (float) (Math.exp(-decay * time) * Math.sin(((float) (Math.PI * 3.0)) * time)) * height;
    }

    private static void scale(TooltipContext ctx, float progress) {
        float scale = easeOutQuint(progress);
        ctx.scale(scale * SCALE, scale * SCALE, scale * SCALE);
    }

    private static void rotate(TooltipContext ctx, int times) {
        ctx.multiply(Axis.YP, (float) ((TooltipRenderer.ELAPSED * (5 * times)) * 360 / 6.0 % 360));
    }

    private static float easeOutQuint(float t) {
        float cubicPart = 1 - (float) Math.pow(1 - t, 3);
        return cubicPart * t + t * (1 - t);
    }

    private static float scaled(float value) {
        return SCALE * (value / 1.5f);
    }

}