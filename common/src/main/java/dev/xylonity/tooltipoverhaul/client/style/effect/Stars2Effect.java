package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectRuntime;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class Stars2Effect extends AmbientEffect {

    private static final int[] DEFAULT_COLORS = {0xFFFFFFFF, 0xFFE3E8FF, 0xFFFFE9D2};
    private final int[] colors;

    public Stars2Effect() {
        this(DEFAULT_COLORS);
    }

    public Stars2Effect(int[] colors) {
        this.colors = colors == null || colors.length == 0 ? DEFAULT_COLORS : colors.clone();
    }

    @Override
    protected boolean clipToTooltip() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 1;
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final double density = EffectRuntime.density();
        for (int stream = 0; stream < Math.ceil(density); stream++) {
            drawBurst(canvas, stream, Math.min(1, density - stream));
        }

    }

    private void drawBurst(EffectCanvas canvas, int stream, double chance) {
        final double clock = canvas.time / 2.8 + stream * 0.618034;
        final int event = (int) Math.floor(clock) + stream * 100003;
        if (seed(event, 77) > chance) {
            return;
        }

        final float age = cycle(clock) * 2.8f;
        final float duration = (0.65f + seed(event, 1) * 0.2f) * parameter(BURST_DURATION);
        if (age >= duration) {
            return;
        }

        final float phase = age / duration;
        final float fade = smooth(phase / 0.09f) * (float) Math.pow(1 - phase, 1.15);

        final float size = (4 + sin(phase * Math.PI) * (20 + seed(event, 2) * 30)) * parameter(SIZE);

        final float x = canvas.left + canvas.width * (0.1f + seed(event, 3) * 0.8f);
        final float y = canvas.top + canvas.height * (0.1f + seed(event, 4) * 0.8f);

        final float rotation = seed(event, 5) * TAU * 0.5f + sin(age * 2.3f) * 0.08f;
        final int burstColor = color(Math.floorMod(event, 3), colors[Math.floorMod(event, colors.length)]);

        canvas.glow(x, y, 11, burstColor, fade * 0.30f);

        final int rays = (int) parameter(STAR_POINTS);
        for (int ray = 0; ray < rays; ray++) {
            final float angle = rotation + ray * TAU / rays;

            final float dx = cos(angle);
            final float dy = sin(angle);

            final float length = size * (ray % 2 == 0 ? 1 : 0.8f);

            canvas.line(x, y, x + dx * length, y + dy * length, 7, 0.2f, burstColor, fade * 0.32f, 0);
            canvas.line(x - dy * 0.7f, y + dx * 0.7f, x + dx * length, y + dy * length, 2.8f, 0, color(2, 0xFFFFAFCB), fade * 0.40f, 0);
            canvas.line(x + dy * 0.7f, y - dx * 0.7f, x + dx * length, y + dy * length, 2.8f, 0, color(1, 0xFFABCFFF), fade * 0.40f, 0);
            canvas.line(x, y, x + dx * length, y + dy * length, 2.6f, 0, burstColor, fade * 0.9f, 0);
        }

        canvas.glow(x, y, 1.5f, color(0, 0xFFFFFFFF), fade);
    }

}
