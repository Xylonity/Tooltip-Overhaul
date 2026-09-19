package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class ShieldEffect extends AmbientEffect {

    private static final float FLIGHT = 0.22f;
    private static final int RINGS = 3;

    @Override
    protected boolean clipToTooltip() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.6f;
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final int teal = color(0, 0xFF5FE8D8), pale = color(1, 0xFFB8FFF4), white = color(2, 0xFFFFFFFF);
        final float cell = 5.5f * parameter(SIZE);

        // Some hexagons breath slightly
        for (int i = 0; i < canvas.particleCount(9); i++) {
            final float shimmer = bell(cycle(canvas.time / (2 + seed(i, 1) * 3) + seed(i, 2)));
            final float x = canvas.left + seed(i, 3) * canvas.width, y = canvas.top + seed(i, 4) * canvas.height;
            hex(canvas, snapX(canvas, x, y, cell), snapY(canvas, x, y, cell), cell, teal, shimmer * 0.12f, shimmer * 0.03f);
        }

        for (int i = 0; i < effectCount(3); i++) {
            final double clock = canvas.time / (1.6 + seed(i, 5) * 0.9) + seed(i, 6);
            final int event = (int) Math.floor(clock) * 61 + i;
            final float phase = cycle(clock);

            final float ix = canvas.left + canvas.width * (0.12f + seed(event, 7) * 0.76f);
            final float iy = canvas.top + canvas.height * (0.15f + seed(event, 8) * 0.7f);

            final float hx = snapX(canvas, ix, iy, cell), hy = snapY(canvas, ix, iy, cell);

            final float from = seed(event, 9) * TAU;

            final float dx = cos(from), dy = sin(from);
            final float travel = (float) Math.hypot(canvas.width, canvas.height);

            final int tint = mix(teal, pale, seed(event, 10));

            // Bullet
            if (phase < FLIGHT) {
                final float approach = phase / FLIGHT;
                final float run = approach * approach;
                final float bx = hx + dx * (1 - run) * travel, by = hy + dy * (1 - run) * travel;
                canvas.line(bx + dx * 9, by + dy * 9, bx, by, 0.2f, 1.1f, white, 0, 0.95f);
                canvas.glow(bx, by, 4, tint, 0.7f);
                canvas.mote(bx, by, 0.9f, white, 1);
                continue;
            }

            final float age = (phase - FLIGHT) / (1 - FLIGHT);

            // Contact
            canvas.glow(hx, hy, cell * 4, tint, bell(age / 0.15f) * 0.5f);
            canvas.glow(hx, hy, cell * 1.4f, white, bell(age / 0.06f));

            for (int spark = 0; spark < 7; spark++) {
                final float skid = age / 0.35f;
                if (skid >= 1) {
                    break;
                }

                final float angle = seed(event * 7 + spark, 11) * TAU;
                final float distance = (1 - (1 - skid) * (1 - skid)) * cell * (2 + seed(event * 7 + spark, 12) * 2.5f);
                canvas.mote(hx + cos(angle) * distance, hy + sin(angle) * distance, 0.45f, white, (1 - skid) * 0.9f);
            }

            // Hexagon pattern wave
            for (int hexColumn = -RINGS; hexColumn <= RINGS; hexColumn++) {
                for (int hexRow = Math.max(-RINGS, -hexColumn - RINGS); hexRow <= Math.min(RINGS, -hexColumn + RINGS); hexRow++) {
                    final float cx = hx + cell * 1.5f * hexColumn;
                    final float cy = hy + cell * 1.7320508f * (hexRow + hexColumn * 0.5f);
                    final int ring = Math.max(Math.abs(hexColumn), Math.max(Math.abs(hexRow), Math.abs(-hexColumn - hexRow)));

                    final float lag = ring * 0.14f + seed(event * 50 + hexColumn * 7 + hexRow, 13) * 0.05f;
                    final float local = age - lag;

                    final float lit = smooth(local / 0.045f) * (1 - smooth(local / 0.4f)) * (1 - ring * 0.2f);
                    if (lit < 0.01f) {
                        continue;
                    }

                    hex(canvas, cx, cy, cell, ring == 0 ? mix(tint, white, 0.7f) : tint, lit, lit * (ring == 0 ? 0.24f : 0.12f));
                }

            }

        }

    }

    private static float snapX(EffectCanvas canvas, float x, float y, float cell) {
        return canvas.cx + Math.round((x - canvas.cx) / (cell * 1.5f)) * cell * 1.5f;
    }

    private static float snapY(EffectCanvas canvas, float x, float y, float cell) {
        final int hexColumn = Math.round((x - canvas.cx) / (cell * 1.5f));
        final float row = cell * 1.7320508f;
        return canvas.cy + (Math.round((y - canvas.cy) / row - hexColumn * 0.5f) + hexColumn * 0.5f) * row;
    }

    private static void hex(EffectCanvas canvas, float x, float y, float cell, int tint, float edge, float fill) {
        final float hexRadius = cell * 0.9f;
        for (int k = 0; k < 6; k++) {
            final float a0 = k * TAU / 6, a1 = (k + 1) * TAU / 6;
            final float x0 = x + cos(a0) * hexRadius, y0 = y + sin(a0) * hexRadius, x1 = x + cos(a1) * hexRadius, y1 = y + sin(a1) * hexRadius;
            if (fill > 0.003f) {
                canvas.triangle(x, y, x0, y0, x1, y1, tint, fill, fill * 0.4f, fill * 0.4f);
            }

            canvas.line(x0, y0, x1, y1, 0.45f, 0.45f, tint, edge, edge);
        }

    }

}
