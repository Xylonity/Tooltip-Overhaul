package dev.xylonity.tooltipoverhaul.client.util;

import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;

import java.util.Optional;
import java.util.Random;

public class AnimationUtils {

    public static float getSecondPanelRendererSpeed(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getSecondPanelRendererSpeed).orElse(TooltipsConfig.SECOND_PANEL_RENDERER_SPEED);
    }

    public static float easeOutCubic(float time) {
        float ease = 1f - time;
        return 1f - ease * ease * ease;
    }

    public static float easeOutQuint(float time) {
        float cubicPart = 1 - (float) Math.pow(1 - time, 3);
        return cubicPart * time + time * (1 - time);
    }

    public static float easeInOutCubic(float t) {
        return t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
    }

    public static float smoothstep(float a, float b, float x) {
        float step = clamp((x - a) / (b - a), 0f, 1f);
        return step * step * (3f - 2f * step);
    }

    public static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public static float dampBounce(float time, float decay, float height) {
        return (float) (Math.exp(-decay * time) * Math.sin(Math.PI * 3.0 * time)) * height;
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
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

    public static float clamp01(float v) {
        if (v < 0f) {
            return 0f;
        }

        return Math.min(v, 1f);
    }

}
