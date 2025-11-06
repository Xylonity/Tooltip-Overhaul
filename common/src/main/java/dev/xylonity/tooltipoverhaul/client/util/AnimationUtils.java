package dev.xylonity.tooltipoverhaul.client.util;

public class AnimationUtils {

    public static float easeOutCubic(float time) {
        float ease = 1f - time;
        return 1f - ease * ease * ease;
    }

    public static float easeOutQuint(float time) {
        float cubicPart = 1 - (float) Math.pow(1 - time, 3);
        return cubicPart * time + time * (1 - time);
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

}
