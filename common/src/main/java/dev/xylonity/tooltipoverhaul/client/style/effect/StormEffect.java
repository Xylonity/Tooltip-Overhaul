package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class StormEffect extends AmbientEffect {

    private static final int SEGMENTS = 32;

    @Override
    protected boolean clipToTooltip() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.5f;
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final int cool = color(0, 0xFF7FB6FF), violet = color(1, 0xFFC9A8FF), hot = color(2, 0xFFF3F8FF);
        final float jag = parameter(TURBULENCE);

        final int forks = Math.round(parameter(FORKS));

        final boolean horizontal = canvas.width > canvas.height * 2.6f;

        final float[] boltX = new float[SEGMENTS + 1];
        final float[] boltY = new float[SEGMENTS + 1];
        final float[] forkX = new float[SEGMENTS + 1];
        final float[] forkY = new float[SEGMENTS + 1];

        for (int i = 0; i < effectCount(3); i++) {
            final double clock = canvas.time / (1.8 + seed(i, 1) * 2.8) + seed(i, 2);
            final int event = (int) Math.floor(clock) * 131 + i;

            final float phase = cycle(clock);

            // Three return strokes in quick succession
            final float strike = bell(phase / 0.030f) + 0.70f * bell((phase - 0.045f) / 0.022f) + 0.40f * bell((phase - 0.080f) / 0.030f);
            final float afterglow = Math.max(0, 1 - phase / 0.34f);

            // The stepped leader
            if (phase > 0.9f) {
                leader(canvas, ((int) Math.floor(clock) + 1) * 131 + i, (phase - 0.9f) / 0.1f, horizontal, jag, cool, violet, hot, boltX, boltY);
            }

            if (strike + afterglow * afterglow < 0.005f) {
                continue;
            }

            final int tint = mix(cool, violet, seed(event, 3));
            final float[] ends = endpoints(canvas, event, horizontal);

            final float x0 = ends[0];
            final float y0 = ends[1];
            final float x1 = ends[2];
            final float y1 = ends[3];

            final float span = (float) Math.hypot(x1 - x0, y1 - y0);

            carve(boltX, boltY, x0, y0, x1, y1, event, jag);
            channel(canvas, boltX, boltY, tint, hot, strike, afterglow, 1, false);

            for (int fork = 0; fork < forks; fork++) {
                final int key = event * 7 + fork * 29;
                final int knot = 5 + (int) (seed(key, 6) * (SEGMENTS - 11));
                final float heading = (float) Math.atan2(boltY[knot + 2] - boltY[knot - 2], boltX[knot + 2] - boltX[knot - 2]);
                final float turn = (seed(key, 7) < 0.5f ? -1 : 1) * (0.45f + seed(key, 8) * 0.5f);
                final float reach = (0.20f + seed(key, 9) * 0.3f) * span;
                carve(forkX, forkY, boltX[knot], boltY[knot], boltX[knot] + cos(heading + turn) * reach, boltY[knot] + sin(heading + turn) * reach, key, jag);
                channel(canvas, forkX, forkY, tint, hot, strike * 0.55f, afterglow * 0.4f, 0.55f, true);
            }

            // The discharge washes the whole panel
            canvas.glow(boltX[SEGMENTS / 2], boltY[SEGMENTS / 2], Math.max(canvas.width, canvas.height) * 0.75f, tint, strike * 0.16f + afterglow * afterglow * 0.05f);
            canvas.glow(boltX[0], boltY[0], 11, mix(tint, hot, 0.5f), strike * 0.5f);
            canvas.glow(boltX[SEGMENTS], boltY[SEGMENTS], 11, mix(tint, hot, 0.5f), strike * 0.5f);

            impact(canvas, boltX[SEGMENTS], boltY[SEGMENTS], horizontal, event, phase, strike, tint, hot);

            // Debris
            for (int spark = 0; spark < 8; spark++) {
                final int key = event * 9 + spark * 53;
                final float age = (phase - 0.02f - seed(key, 10) * 0.05f) / 0.3f;
                if (age <= 0 || age >= 1) {
                    continue;
                }

                final int knot = 3 + (int) (seed(key, 11) * (SEGMENTS - 6));
                final float angle = seed(key, 13) * TAU;
                final float spread = age * (5 + seed(key, 12) * 10);
                canvas.mote(boltX[knot] + cos(angle) * spread, boltY[knot] + sin(angle) * spread + age * age * 6, 0.45f + seed(key, 14) * 0.35f, mix(hot, tint, age), bell(age) * 0.7f);
            }

        }

        clouds(canvas, cool, violet, hot);
        crackle(canvas, mix(cool, violet, 0.5f), hot);
        charge(canvas, mix(cool, violet, 0.35f), hot);
    }

    /**
     * Where a stroke enters and leaves the panel
     */
    private static float[] endpoints(EffectCanvas canvas, int event, boolean horizontal) {
        if (horizontal) {
            return new float[]{canvas.left - 8, canvas.top + canvas.height * (0.18f + seed(event, 4) * 0.64f), canvas.right + 8, canvas.top + canvas.height * (0.18f + seed(event, 5) * 0.64f)};
        }

        return new float[]{canvas.left + canvas.width * (0.14f + seed(event, 4) * 0.72f), canvas.top - 8, canvas.left + canvas.width * (0.14f + seed(event, 5) * 0.72f), canvas.bottom + 8};
    }

    private static void leader(EffectCanvas canvas, int event, float progress, boolean horizontal, float jag, int cool, int violet, int hot, float[] xs, float[] ys) {
        final float[] ends = endpoints(canvas, event, horizontal);

        carve(xs, ys, ends[0], ends[1], ends[2], ends[3], event, jag);

        final float reach = (float) Math.floor(smooth(progress) * SEGMENTS / 2) * 2 / SEGMENTS;
        if (reach <= 0) {
            return;
        }

        final int tint = mix(cool, violet, seed(event, 3));
        final float glow = 0.35f + 0.25f * noise(event, canvas.time * 20);
        canvas.ribbon(SEGMENTS, span -> {
            final float along = span * reach;
            final int knot = Math.min(SEGMENTS, Math.round(along * SEGMENTS));
            return new Knot(xs[knot], ys[knot], 0.55f, mix(tint, hot, 0.3f), glow * (0.35f + 0.65f * span));
        });

        final int tip = Math.min(SEGMENTS, Math.round(reach * SEGMENTS));

        canvas.glow(xs[tip], ys[tip], 4, tint, glow * 0.6f);
        canvas.mote(xs[tip], ys[tip], 0.55f, hot, glow * 1.4f);
    }

    private static void impact(EffectCanvas canvas, float x, float y, boolean horizontal, int event, float phase, float strike, int tint, int hot) {
        canvas.glow(x, y, 18, tint, strike * 0.3f);

        for (int spark = 0; spark < 7; spark++) {
            final int key = event * 11 + spark * 71;
            final float age = (phase - seed(key, 15) * 0.03f) / (0.22f + seed(key, 16) * 0.12f);
            if (age <= 0 || age >= 1) {
                continue;
            }

            final float angle = horizontal ? (float) Math.PI + (seed(key, 17) - 0.5f) * 1.6f : -(float) Math.PI / 2 + (seed(key, 17) - 0.5f) * 1.9f;
            final float speed = 9 + seed(key, 18) * 14;
            final float run = age * (1 - age * 0.35f);

            final float px = x + cos(angle) * speed * run;
            final float py = y + sin(angle) * speed * run + (horizontal ? 0 : age * age * 12);

            final float fade = smooth(age / 0.04f) * (1 - age) * (1 - age);

            canvas.line(x + cos(angle) * speed * Math.max(0, run - 0.12f), y + sin(angle) * speed * Math.max(0, run - 0.12f) + (horizontal ? 0 : Math.max(0, age - 0.12f) * Math.max(0, age - 0.12f) * 12),
                    px, py, 0.05f, 0.5f, tint, 0, fade * 0.7f);
            canvas.mote(px, py, 0.4f + seed(key, 19) * 0.3f, mix(hot, tint, age), fade);
        }

    }

    /**
     * Lightning flickering above
     */
    private static void clouds(EffectCanvas canvas, int cool, int violet, int hot) {
        final int lumps = effectCount(5);
        for (int k = 0; k < lumps; k++) {
            final float x = canvas.left + canvas.width * (0.08f + 0.84f * k / Math.max(1, lumps - 1)) + flow(k * 3.1, canvas.time * 0.05) * canvas.width * 0.04f;
            final float y = canvas.top + 2 + seed(k, 40) * 3;

            final float rx = canvas.width * (0.26f + seed(k, 41) * 0.1f), ry = 15 + seed(k, 42) * 6;
            final double clock = canvas.time / (2.4 + seed(k, 43) * 3.6) + seed(k, 44);
            final float phase = cycle(clock);

            final float flash = bell(phase / 0.05f) + 0.55f * bell((phase - 0.075f) / 0.045f);
            final int tint = mix(cool, violet, 0.35f + seed(k, 45) * 0.5f);

            canvas.haze(x, y, rx, ry, tint, 0.075f + flash * 0.3f);
            if (flash > 0.01f) {
                canvas.haze(x + (seed(k, 46) - 0.5f) * rx * 0.5f, y + 1, rx * 0.4f, ry * 0.8f, mix(tint, hot, 0.6f), flash * 0.4f);
            }

        }

    }

    private static void carve(float[] xs, float[] ys, float x0, float y0, float x1, float y1, int event, float jag) {
        xs[0] = x0;
        ys[0] = y0;
        xs[SEGMENTS] = x1;
        ys[SEGMENTS] = y1;

        final float length = Math.max(0.001f, (float) Math.hypot(x1 - x0, y1 - y0));
        final float nx = -(y1 - y0) / length;
        final float ny = (x1 - x0) / length;

        for (int step = SEGMENTS; step > 1; step >>= 1) {
            final float amplitude = length * 0.17f * jag * (float) Math.pow(step / (float) SEGMENTS, 0.72);
            for (int i = step >> 1; i < SEGMENTS; i += step) {
                final int before = i - (step >> 1), after = i + (step >> 1);
                final float offset = (seed(event + i * 7919, 3) - 0.5f) * amplitude;
                xs[i] = (xs[before] + xs[after]) * 0.5f + nx * offset;
                ys[i] = (ys[before] + ys[after]) * 0.5f + ny * offset;
            }

        }

    }

    /**
     * Halo
     */
    private static void channel(EffectCanvas canvas, float[] xs, float[] ys, int tint, int hot, float strike, float afterglow, float scale, boolean taper) {
        final float cooling = afterglow * afterglow;
        trace(canvas, xs, ys, 3.1f * scale, tint, strike * 0.38f + cooling * 0.2f, taper);
        trace(canvas, xs, ys, 1.25f * scale, mix(tint, hot, 0.4f), strike * 0.75f + cooling * afterglow * 0.22f, taper);
        trace(canvas, xs, ys, 0.4f * scale, hot, strike * 0.95f, taper);
    }

    private static void trace(EffectCanvas canvas, float[] xs, float[] ys, float width, int color, float alpha, boolean taper) {
        if (alpha < 0.003f) {
            return;
        }

        canvas.ribbon(SEGMENTS, along -> {
            final int knot = Math.round(along * SEGMENTS);
            return new Knot(xs[knot], ys[knot], width, color, taper ? alpha * smooth((1 - along) / 0.3f) : alpha);
        });

    }

    /**
     * Short filaments snapping along the rim
     */
    private static void crackle(EffectCanvas canvas, int tint, int hot) {
        for (int i = 0; i < effectCount(8); i++) {
            final double clock = canvas.time / (0.6 + seed(i, 20) * 0.9) + seed(i, 21);
            final int event = (int) Math.floor(clock) * 37 + i;
            final float live = bell(cycle(clock) / 0.3f);
            if (live < 0.01f) {
                continue;
            }

            final float turn = seed(event, 22);
            final float extent = (9 + seed(event, 23) * 11) / Math.max(24, 2 * (canvas.width + canvas.height));
            final float height = 2 + seed(event, 24) * 3;
            for (int layer = 0; layer < 2; layer++) {
                final boolean halo = layer == 0;
                canvas.ribbon(20, progress -> {
                    final float along = turn + extent * progress;
                    final Point rim = canvas.edge(along, -1);
                    final Point before = canvas.edge(along - 0.001f, -1), after = canvas.edge(along + 0.001f, -1);

                    final float dx = after.x() - before.x();
                    final float dy = after.y() - before.y();

                    final float length = Math.max(0.001f, (float) Math.hypot(dx, dy));
                    final float arch = sin(progress * Math.PI) * (height + sin(progress * 3 * Math.PI + seed(event, 25) * TAU) * 0.65f);
                    return new Knot(rim.x() - dy / length * arch, rim.y() + dx / length * arch, (halo ? 1.5f : 0.38f) * (0.35f + 0.65f * sin(progress * Math.PI)),
                            halo ? tint : mix(tint, hot, 0.8f), live * (halo ? 0.28f : 0.8f) * (0.4f + 0.6f * bell(progress)));
                });

            }

            final Point start = canvas.edge(turn, -1), end = canvas.edge(turn + extent, -1);
            canvas.mote(start.x(), start.y(), 0.4f, hot, live * 0.65f);
            canvas.mote(end.x(), end.y(), 0.4f, hot, live * 0.65f);
        }

    }

    private static void charge(EffectCanvas canvas, int tint, int hot) {
        for (int i = 0; i < canvas.particleCount(11); i++) {
            final float blink = cycle(canvas.time / (1.4 + seed(i, 30) * 2.2) + seed(i, 31));
            final float alive = bell(blink / 0.2f);
            if (alive < 0.01f) {
                continue;
            }

            final float x = canvas.left + seed(i, 32) * canvas.width + flow(i * 1.7, canvas.time * 0.2) * 5;
            final float y = canvas.top + seed(i, 33) * canvas.height + flow(i * 2.9 + 13, canvas.time * 0.18) * 4;
            canvas.glow(x, y, 2.6f, tint, alive * 0.45f);
            canvas.mote(x, y, 0.4f + seed(i, 34) * 0.3f, hot, alive * 0.8f);
        }

    }

}