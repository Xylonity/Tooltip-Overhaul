package dev.xylonity.tooltipoverhaul.client.style.effect.internal;

import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.impl.EffectLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import net.minecraft.world.phys.Vec2;

import java.util.Map;

public final class EffectRuntime {

    private static EffectSettings current = new EffectSettings(1f, 1f, 1f, Map.of());

    private static int[] currentPalette = { 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF };

    public static EffectLayer wrap(String id, EffectLayer effect, EffectSettings settings) {
        return wrap(id, effect, settings, LayerDepth.EFFECT);
    }

    public static EffectLayer wrap(String id, EffectLayer effect, EffectSettings settings, LayerDepth depth) {
        final int[] defaults = EffectCatalog.palette(id);
        return new EffectLayer() {

            @Override
            public LayerDepth getLayerDepth() {
                return depth;
            }

            @Override
            public void render(TooltipContext context, Vec2 position) {
                final EffectSettings previous = current;
                final int[] previousPalette = currentPalette;
                current = settings.forEffect(id);
                currentPalette = defaults;
                try {
                    if (current.intensity() > 0) {
                        effect.render(context, position);
                    }

                }
                finally {
                    current = previous;
                    currentPalette = previousPalette;
                }

            }

        };
    }

    public static double seconds(TooltipContext context) {
        return animationSeconds(Math.max(0, System.currentTimeMillis() - context.getStartTime()) / 1000.0);
    }

    public static double animationSeconds(double elapsedSeconds) {
        return current.speed() == 0 ? 2 : Math.max(0, elapsedSeconds) * current.speed();
    }

    public static float value(EffectParameter parameter) {
        return current.value(parameter);
    }

    public static float intensity() {
        return current.intensity();
    }

    public static float density() {
        return current.density();
    }

    public static int alpha(int value) {
        return Math.round(value * intensity());
    }

    public static int color(int channel, int original) {
        if (current.colors().isEmpty()) {
            return saturate(original);
        }

        final int index = channel % 3, replacement = current.colors().get(index), base = currentPalette[index];
        final float shade = brightness(base) == 0 ? 1 : brightness(original) / brightness(base);

        final int red = Math.min(255, Math.round(((replacement >>> 16) & 255) * shade));
        final int green = Math.min(255, Math.round(((replacement >>> 8) & 255) * shade));
        final int blue = Math.min(255, Math.round((replacement & 255) * shade));

        return saturate((original & 0xFF000000) | (red << 16) | (green << 8) | blue);
    }

    private static int saturate(int color) {
        final float saturation = value(EffectParameter.SATURATION);
        if (saturation == 1f) {
            return color;
        }

        final float gray = brightness(color);

        final int red = Math.max(0, Math.min(255, Math.round(gray + (((color >>> 16) & 255) - gray) * saturation)));
        final int green = Math.max(0, Math.min(255, Math.round(gray + (((color >>> 8) & 255) - gray) * saturation)));
        final int blue = Math.max(0, Math.min(255, Math.round(gray + ((color & 255) - gray) * saturation)));

        return (color & 0xFF000000) | (red << 16) | (green << 8) | blue;
    }

    private static float brightness(int color) {
        return ((color >>> 16) & 255) * 0.2126f + ((color >>> 8) & 255) * 0.7152f + (color & 255) * 0.0722f;
    }

}
