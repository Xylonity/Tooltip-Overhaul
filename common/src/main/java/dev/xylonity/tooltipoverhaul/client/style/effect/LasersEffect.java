package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class LasersEffect extends AmbientEffect {

    private static final float CHARGE = 0.14f;
    private static final float BEAM = 0.42f;

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
        final int red = color(0, 0xFFFF3B3B);
        final int orange = color(1, 0xFFFF9A3B);
        final int white = color(2, 0xFFFFFFFF);

        for (int i = 0; i < Math.max(1, effectCount(3)); i++) {
            final double clock = canvas.time / (1.5 + seed(i, 1) * 0.8) + seed(i, 2);
            final int event = (int) Math.floor(clock) * 53 + i;
            final float phase = cycle(clock);
            if (phase >= 1) {
                continue;
            }

            // Muzzle on one edge, target on the other
            final float turn = seed(event, 3);
            final Point muzzlePoint = canvas.edge(turn, 1);

            final float sweep = seed(event, 13) < 0.4f ? (seed(event, 14) - 0.5f) * 0.05f : 0;
            final float slide = phase < CHARGE ? 0 : Math.min(1, (phase - CHARGE) / (1 - CHARGE) / BEAM);

            final Point targetPoint = canvas.edge(turn + 0.38f + seed(event, 4) * 0.24f + sweep * slide, 1);
            final float dx = targetPoint.x() - muzzlePoint.x(), dy = targetPoint.y() - muzzlePoint.y();
            final float len = Math.max(0.001f, (float) Math.hypot(dx, dy));

            final float ux = dx / len;
            final float uy = dy / len;

            final int tint = seed(event, 5) < 0.6f ? red : orange;

            if (phase < CHARGE) {
                // Light gathers into the muzzle from around it and the lens brightens
                final float build = phase / CHARGE;
                canvas.glow(muzzlePoint.x(), muzzlePoint.y(), 6 * build, tint, build * 0.7f);
                canvas.glow(muzzlePoint.x(), muzzlePoint.y(), 2, white, build);
                for (int spark = 0; spark < 6; spark++) {
                    final float pull = cycle(canvas.time * 3 + seed(event * 6 + spark, 6));
                    final float angle = seed(event * 6 + spark, 7) * TAU;

                    canvas.mote(muzzlePoint.x() + cos(angle) * (1 - pull) * 8, muzzlePoint.y() + sin(angle) * (1 - pull) * 8, 0.4f, tint, pull * build);
                }

                continue;
            }

            final float age = (phase - CHARGE) / (1 - CHARGE);
            final float on = age < BEAM ? (0.8f + 0.2f * noise(event, canvas.time * 40)) * (1 - smooth((age - BEAM * 0.6f) / (BEAM * 0.4f)) * 0.35f) : 0;

            // Muzzle flash
            canvas.glow(muzzlePoint.x(), muzzlePoint.y(), 9, tint, on * 0.8f);
            canvas.glow(muzzlePoint.x(), muzzlePoint.y(), 3.5f, white, on);

            if (on > 0.01f) {
                canvas.line(muzzlePoint.x(), muzzlePoint.y(), targetPoint.x(), targetPoint.y(), 2.8f, 2.8f, tint, on * 0.35f, on * 0.35f);
                canvas.line(muzzlePoint.x(), muzzlePoint.y(), targetPoint.x(), targetPoint.y(), 1.1f, 1.1f, mix(tint, white, 0.35f), on * 0.85f, on * 0.85f);
                canvas.line(muzzlePoint.x(), muzzlePoint.y(), targetPoint.x(), targetPoint.y(), 0.35f, 0.35f, white, on, on);

                // Heat shimmer along the beam
                for (int shimmer = 0; shimmer < 5; shimmer++) {
                    final float along = cycle(canvas.time * 1.5 + seed(event * 5 + shimmer, 8));
                    canvas.glow(muzzlePoint.x() + ux * along * len, muzzlePoint.y() + uy * along * len, 3, tint, on * 0.3f);
                }

            }

            // Hot spot
            final float burnIn = Math.min(1, age / BEAM), scorch = (1 - smooth((age - BEAM) / (1 - BEAM))) * burnIn;
            canvas.glow(targetPoint.x(), targetPoint.y(), 11, tint, scorch * 0.8f + on * 0.4f);
            canvas.glow(targetPoint.x(), targetPoint.y(), 3, white, on);
            canvas.haze(targetPoint.x(), targetPoint.y(), 5, 2.5f, tint, scorch * 0.8f);

            for (int ricochet = 0; ricochet < 12; ricochet++) {
                final int spark = event * 12 + ricochet;
                final float born = seed(spark, 9) * BEAM * 0.8f;
                final float st = (age - born) / 0.32f;
                if (st <= 0 || st >= 1) {
                    continue;
                }

                final float angle = (float) Math.atan2(-uy, -ux) + (seed(spark, 10) - 0.5f) * 1.8f;
                final float speed = 10 + seed(spark, 11) * 18;
                canvas.mote(targetPoint.x() + cos(angle) * speed * st, targetPoint.y() + sin(angle) * speed * st + st * st * 14, 0.5f + seed(spark, 12) * 0.4f, mix(white, tint, st), (1 - st) * 0.95f);
            }

        }

    }

}