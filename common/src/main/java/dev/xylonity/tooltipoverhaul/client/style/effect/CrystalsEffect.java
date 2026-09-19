package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class CrystalsEffect extends AmbientEffect {

    @Override
    protected boolean hasMaterial() {
        return true;
    }

    @Override
    protected void drawMaterial(EffectCanvas canvas) {
        crystals(canvas, true);
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        crystals(canvas, false);
        for (int index = 0; index < effectCount(16); index++) {
            final float phase = cycle(canvas.time * 0.065 + seed(index, 11));
            final Point point = canvas.edge(seed(index, 12), 3 + sin(phase * Math.PI) * 8);
            canvas.mote(point.x() + sin(canvas.time * 0.3 + index) * 2, point.y() - phase * 4, 0.35f + seed(index, 13) * 0.5f, color(2, 0xFFE2D4FF), life(phase) * 0.55f);
        }

    }

    private static void crystals(EffectCanvas canvas, boolean material) {
        final int count = canvas.particleCount(9);
        for (int index = 0; index < count; index++) {
            final Point point = canvas.edge(seed(index, 1), 2 + seed(index, 2) * 6);

            final float x = point.x() + flow(index, canvas.time * 0.12) * 3;
            final float y = point.y() + sin(canvas.time * (0.35 + seed(index, 3) * 0.15) + index * 2) * 2;
            final float length = 3.3f + seed(index, 4) * 3.5f;
            final float angle = (seed(index, 5) - 0.5f) * 1.2f + sin(canvas.time * 0.24 + index) * 0.18f;

            shard(canvas, x, y, length, angle, index, 1, material);

            if (index % 3 == 0) {
                shard(canvas, x + 2, y + length * 0.4f, length * 0.52f, angle + 0.55f, index + 17, 0.8f, material);
            }

        }

    }

    private static void shard(EffectCanvas canvas, float x, float y, float size, float angle, int index, float alpha, boolean material) {
        final float baseSize = size;

        size *= parameter(SIZE);

        final float turn = (float) canvas.time * 0.28f * parameter(ROTATION_SPEED) + index * 1.9f;
        final float breadth = size * (0.28f + 0.12f * cos(turn));

        size *= parameter(STRETCH);

        final float ridge = sin(turn) * breadth * 0.6f;

        final float dx = cos(angle);
        final float dy = sin(angle);

        final float[] sx = {0, breadth, breadth, 0, -breadth, -breadth};
        final float[] sy = {-size, -size * 0.38f, size * 0.40f, size, size * 0.38f, -size * 0.40f};
        final float glint = (float) Math.pow(0.5f + 0.5f * cos(turn - 0.7), 7);

        if (!material) {
            canvas.haze(x, y, baseSize * 1.5f, baseSize * 1.8f * parameter(STRETCH), color(0, 0xFFB5A5EA), alpha * (0.12f + glint * 0.12f));
        }

        final float mx = x + ridge * dx;
        final float my = y + ridge * dy;
        for (int side = 0; side < 6; side++) {
            int next = (side + 1) % 6;
            final float ax = x + sx[side] * dx - sy[side] * dy, ay = y + sx[side] * dy + sy[side] * dx;
            final float bx = x + sx[next] * dx - sy[next] * dy, by = y + sx[next] * dy + sy[next] * dx;

            final float light = 0.28f + 0.40f * Math.max(0, cos(turn + side * TAU / 6));

            if (material) {
                int color = mix(color(0, 0xFF543278), color(0, 0xFFA995DD), light);
                if (side % 3 == 1) {
                    color = mix(color(0, 0xFF354E80), color(1, 0xFF9FD2E0), light);
                }

                canvas.triangle(mx, my, ax, ay, bx, by, color, alpha * 0.98f, alpha * 0.88f, alpha * 0.93f);
            }
            else {
                final float facet = (float) Math.pow(Math.max(0, cos(turn + side * TAU / 6 - 0.6)), 8);
                canvas.triangle(mx, my, ax, ay, bx, by, color(2, 0xFFECE7FF), alpha * facet * 0.30f, alpha * facet * 0.06f, alpha * facet * 0.18f);
                canvas.line(ax, ay, bx, by, 0.22f, 0.22f, color(2, 0xFFDAD3FF), alpha * light * 0.28f, alpha * light * 0.28f);
            }

        }

        if (material) {
            // Outline
            for (int facet = 0; facet < 6; facet++) {
                final int next = (facet + 1) % 6;
                canvas.line(x + sx[facet] * dx - sy[facet] * dy, y + sx[facet] * dy + sy[facet] * dx,
                        x + sx[next] * dx - sy[next] * dy, y + sx[next] * dy + sy[next] * dx,
                        0.72f, 0.72f, color(0, 0xFF30283F), alpha * 0.8f, alpha * 0.8f);
            }

            return;
        }

        canvas.line(x + size * dy, y - size * dx, mx, my, 0.45f, 0.35f, color(2, 0xFFF2E6FF), alpha * 0.8f, alpha * 0.35f);
        canvas.star(x + size * dy, y - size * dx, 2.2f + glint * 1.2f, 4, 0.20f, angle + 0.2f, color(2, 0xFFFBF6FF), alpha * (0.16f + glint * 0.7f));
    }

}