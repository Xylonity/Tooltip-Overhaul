package dev.xylonity.tooltipoverhaul.client.style.icon.animation;

import com.mojang.math.Axis;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;

import java.util.HashMap;
import java.util.Map;

public class IconAnimationFactory {

    private static final Map<IconAnimation, IIconAnimation> ANIMATIONS = new HashMap<>();

    static {
        ANIMATIONS.put(IconAnimation.ZOOM, IconAnimationFactory::animateZoom);
        ANIMATIONS.put(IconAnimation.ROTATE, IconAnimationFactory::animateRotate);
        ANIMATIONS.put(IconAnimation.ROTATE_FAST, IconAnimationFactory::animateRotateFast);
        ANIMATIONS.put(IconAnimation.ROTATE_ZOOM, IconAnimationFactory::animateRotateZoom);
        ANIMATIONS.put(IconAnimation.ZOOM_SNAP, IconAnimationFactory::animateZoomSnap);
        ANIMATIONS.put(IconAnimation.SKEW, IconAnimationFactory::animateSkew);
        ANIMATIONS.put(IconAnimation.VIBRATION, IconAnimationFactory::animateVibration);
        ANIMATIONS.put(IconAnimation.TILT_WAVE, IconAnimationFactory::animateTiltWave);
        ANIMATIONS.put(IconAnimation.FLIP, IconAnimationFactory::animateFlip);
        ANIMATIONS.put(IconAnimation.PENDULUM, IconAnimationFactory::animatePendulum);
        ANIMATIONS.put(IconAnimation.BOUNCE, IconAnimationFactory::animateBounce);
        ANIMATIONS.put(IconAnimation.GO_DOWN, IconAnimationFactory::animateGoDown);
        ANIMATIONS.put(IconAnimation.PULSE, IconAnimationFactory::animatePulse);
        ANIMATIONS.put(IconAnimation.FAN_IN, IconAnimationFactory::animateFanIn);
        ANIMATIONS.put(IconAnimation.HOVER_POP, IconAnimationFactory::animateHoverPop);
        ANIMATIONS.put(IconAnimation.BARREL_ROLL, IconAnimationFactory::animateBarrelRoll);
    }

    public static IIconAnimation get(IconAnimation animation) {
        return ANIMATIONS.getOrDefault(animation, (context, progress, scale) -> context.scale(scale, scale, scale));
    }

    private static void animateZoom(TooltipContext context, float progress, float scale) {
        float easedProgress = AnimationUtils.easeOutQuint(progress);
        float animationScale = easedProgress * scale;
        context.scale(animationScale, animationScale, animationScale);
    }

    private static void animateRotate(TooltipContext context, float progress, float scale) {
        context.multiply(Axis.YP, 180f * progress);
        context.scale(scale, scale, scale);
    }

    private static void animateRotateFast(TooltipContext context, float progress, float scale) {
        context.multiply(Axis.YP, 360f * progress);
        context.scale(scale, scale, scale);
    }

    private static void animateRotateZoom(TooltipContext context, float progress, float scale) {
        context.multiply(Axis.YP, 180f * progress);
        animateZoom(context, progress, scale);
    }

    private static void animateZoomSnap(TooltipContext context, float progress, float scale) {
        float abs;
        if (progress < 0.72f) {
            abs = 1.10f + 0.70f * AnimationUtils.easeOutCubic(progress / 0.72f);
        } else {
            abs = 1.80f - (0.30f * AnimationUtils.smoothstep(0.0f, 1.0f, (progress - 0.72f) / 0.28f));
        }

        float animationScale = scaled(abs, scale);

        context.multiply(Axis.YP, 4.0f * (1.0f - progress));
        context.scale(animationScale, animationScale, animationScale);
    }

    private static void animateSkew(TooltipContext context, float progress, float scale) {
        float tilt = 18.0f * (1.0f - progress);

        context.multiply(Axis.XP, tilt);
        context.multiply(Axis.YP, -tilt * 0.6f);

        float undershoot = 0.12f * (1f - progress);
        float overshoot = 0.20f * (float) Math.sin(Math.PI * progress);
        float animationScale = scale * (1.0f - undershoot + overshoot);

        context.scale(animationScale, animationScale, animationScale);
    }

    private static void animateVibration(TooltipContext context, float progress, float scale) {
        float decay = (1.0f - progress);

        context.translate(1.2f * decay * (float) Math.sin(progress * 30f), 1f * decay * (float) Math.cos(progress * 27f), 0);

        float abs = 1.4f + 0.1f * (1f - progress);
        float animationScale = scaled(abs, scale);

        context.scale(animationScale, animationScale, animationScale);
    }

    private static void animateTiltWave(TooltipContext context, float progress, float scale) {
        float amp = 16f * (1f - progress);
        float waves = (float) Math.sin(progress * Math.PI * 6f);

        context.multiply(Axis.ZP, amp * waves);
        context.multiply(Axis.XP, 0.6f * amp * waves);

        context.scale(scale, scale, scale);
    }

    private static void animateFlip(TooltipContext context, float progress, float scale) {
        context.multiply(Axis.XP, (1f - (float) (1f - Math.pow(1f - progress, 2.2))) * 90f);

        float undershoot = 0.12f * (1f - progress);
        float overshoot = 0.22f * (float) Math.sin(Math.PI * progress);
        float animationScale = scale * (1f - undershoot + overshoot);

        context.scale(animationScale, animationScale, animationScale);
    }

    private static void animatePendulum(TooltipContext context, float progress, float scale) {
        context.translate(0, -8, 0);
        context.multiply(Axis.ZP, 28f * (float) Math.cos(progress * Math.PI * 3f) * (1f - progress));
        context.translate(0, 8, 0);

        context.scale(scale, scale, scale);
    }

    private static void animateBounce(TooltipContext context, float progress, float scale) {
        context.translate(0, AnimationUtils.dampBounce(progress, 3f, 8f), 0);

        float abs = 1.45f + 0.05f * (float) Math.sin(progress * Math.PI);
        float animationScale = scaled(abs, scale);

        context.scale(animationScale, animationScale, animationScale);
    }

    private static void animateGoDown(TooltipContext context, float progress, float scale) {
        context.translate(0, -20f * (1f - AnimationUtils.easeOutCubic(progress)), 0);
        context.multiply(Axis.ZP, (1f - progress) * -12f);

        float animationScale = scale - 0.08f * (1f - progress);
        context.scale(animationScale, animationScale, animationScale);
    }

    private static void animatePulse(TooltipContext context, float progress, float scale) {
        float pulses = (float) Math.sin(progress * Math.PI * 4.0f);
        float abs = 1.5f + (0.25f * (1f - progress)) * pulses;
        float animationScale = scaled(abs, scale);

        context.multiply(Axis.XP, 5f * pulses * (1f - progress));
        context.scale(animationScale, animationScale, animationScale);
    }

    private static void animateFanIn(TooltipContext context, float progress, float scale) {
        context.multiply(Axis.YP, 32f * (float) Math.sin(progress * Math.PI * 5f) * (1f - progress));

        float undershoot = 0.12f * (1f - progress);
        float overshoot = 0.18f * (float) Math.sin(Math.PI * progress);
        float animationScale = scale * (1f - undershoot + overshoot);

        context.scale(animationScale, animationScale, animationScale);
    }

    private static void animateHoverPop(TooltipContext context, float progress, float scale) {
        context.translate(0, -(6.0f * (1f - AnimationUtils.easeOutCubic(progress))), 0);

        float animationScale = 1.35f + 0.2f * AnimationUtils.smoothstep(0.6f, 1f, progress);
        context.multiply(Axis.XP, 6f * (1f - progress));

        context.scale(animationScale, animationScale, animationScale);
    }

    private static void animateBarrelRoll(TooltipContext context, float progress, float scale) {
        context.multiply(Axis.ZP, 360.0f * AnimationUtils.easeOutCubic(progress));

        float abs = 0.9f + 0.6f * AnimationUtils.easeOutCubic(progress);
        float animationScale = scaled(abs, scale);

        context.scale(animationScale, animationScale, animationScale);
    }

    private static float scaled(float value, float baseScale) {
        return baseScale * (value / 1.5f);
    }

}