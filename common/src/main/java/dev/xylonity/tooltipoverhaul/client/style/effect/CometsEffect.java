package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class CometsEffect extends AmbientEffect {

    @Override
    protected boolean clipToTooltip() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.7f;
    }

    private static Point flight(EffectCanvas canvas, float progress, float dx, float dy, float travel, float lane, float curveSeed, float turbulence) {
        final float curve = sin(progress * Math.PI) * curveSeed * 24 * turbulence;
        final float along = (progress - 0.5f) * travel;
        return new Point(canvas.cx + dx * along - dy * (lane + curve), canvas.cy + dy * along + dx * (lane + curve));
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final float angle = parameter(DIRECTION) * TAU / 360;

        final float dx = cos(angle);
        final float dy = sin(angle);

        final float travel = Math.abs(dx) * canvas.width + Math.abs(dy) * canvas.height + 80;
        final float breadth = Math.abs(dy) * canvas.width + Math.abs(dx) * canvas.height;
        final float turbulence = parameter(TURBULENCE), glow = parameter(GLOW);

        final int cool = color(0, 0xFF83CEFF), violet = color(1, 0xFFC7A1F3), warm = color(2, 0xFFFFEACB);
        final Point[] path = new Point[49];
        final int[] colors = new int[49];

        for (int i = 0; i < canvas.particleCount(5); i++) {
            final double clock = canvas.time / (2.6 + seed(i, 3) * 2.2) + seed(i, 4);
            final int event = (int) Math.floor(clock) * 101 + i;

            final float phase = cycle(clock), fade = life(phase);
            final float lane = (seed(event, 1) - 0.5f) * breadth * 0.85f;
            final float curveSeed = seed(event, 2) - 0.5f;
            final float tail = (0.12f + seed(event, 5) * 0.08f) * parameter(TRAIL_LENGTH);

            final int tint = mix(cool, violet, seed(event, 6));
            if (tail > 0) {
                for (int knot = 0; knot < path.length; knot++) {
                    final float along = knot / 48f;
                    path[knot] = flight(canvas, Math.max(0, phase - (1 - along) * tail), dx, dy, travel, lane, curveSeed, turbulence);
                    colors[knot] = mix(tint, warm, along * along);
                }

                for (int pass = 0; pass < 2; pass++) {
                    final boolean halo = pass == 0;
                    canvas.ribbon(48, along -> {
                        final int knot = Math.round(along * 48);
                        Point point = path[knot];
                        return new Knot(point.x(), point.y(), (halo ? 3.4f : 0.8f) * along + 0.05f, colors[knot], fade * along * along * (halo ? 0.20f * glow : 0.75f));
                    });

                }

                for (int fragment = 0; fragment < 4; fragment++) {
                    final float age = cycle(canvas.time * 0.8 + seed(event * 4 + fragment, 7));
                    final Point point = flight(canvas, Math.max(0, phase - age * tail), dx, dy, travel, lane, curveSeed, turbulence);
                    canvas.mote(point.x(), point.y() + age * age * 5, 0.35f, tint, fade * bell(age) * 0.55f);
                }

            }

            final Point head = flight(canvas, phase, dx, dy, travel, lane, curveSeed, turbulence);
            canvas.glow(head.x(), head.y(), 6, tint, fade * 0.35f);
            canvas.sparkle(head.x(), head.y(), 2.1f, angle, warm, fade * 0.9f);
        }

    }

}
