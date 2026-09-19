package dev.xylonity.tooltipoverhaul.client.util;

import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;

import java.util.Optional;
import java.util.Random;

public class AnimationUtils {

    public static float getSecondPanelRendererSpeed(TooltipContext context) {
        if (TooltipsConfig.REDUCED_MOTION) {
            return 0;
        }

        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getSecondPanelRendererSpeed).orElse(TooltipsConfig.SECOND_PANEL_RENDERER_SPEED);
    }

    public static float easeOutCubic(float time) {
        final float ease = 1f - time;
        return 1f - ease * ease * ease;
    }

    public static float easeOutQuint(float time) {
        final float cubicPart = 1 - (float) Math.pow(1 - time, 3);
        return cubicPart * time + time * (1 - time);
    }

    public static float easeInOutCubic(float progress) {
        return progress < 0.5f ? 4f * progress * progress * progress : 1f - (float) Math.pow(-2f * progress + 2f, 3) / 2f;
    }

    public static float easeOutQuad(float progress) {
        final float inv = 1f - progress;
        return 1f - inv * inv;
    }

    public static float easeInQuad(float progress) {
        return progress * progress;
    }

    public static float easeOutBack(float progress) {
        final float c1 = 1.70158f;
        final float c3 = c1 + 1f;
        final float inv = progress - 1f;
        return 1f + c3 * inv * inv * inv + c1 * inv * inv;
    }

    public static float smoothstep(float from, float to, float value) {
        final float step = clamp((value - from) / (to - from), 0f, 1f);
        return step * step * (3f - 2f * step);
    }

    public static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public static float dampBounce(float time, float decay, float height) {
        return (float) (Math.exp(-decay * time) * Math.sin(Math.PI * 3.0 * time)) * height;
    }

    public static float lerp(float from, float to, float progress) {
        return from + (to - from) * progress;
    }

    public static int randomBetween(Random random, int v1, int v2) {
        return v1 + random.nextInt(v2 - v1 + 1);
    }

    public static int clamp255(int value) {
        if (value < 0) {
            return 0;
        }

        return Math.min(value, 255);
    }

    public static float clamp01(float value) {
        if (value < 0f) {
            return 0f;
        }

        return Math.min(value, 1f);
    }

}
