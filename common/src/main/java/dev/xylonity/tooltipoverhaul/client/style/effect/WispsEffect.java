package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

/**
 * Will o' the wisps painting wisps
 */
public class WispsEffect extends AmbientEffect {

    @Override
    protected boolean clipToTooltip() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.55f;
    }

    private static Point path(EffectCanvas canvas, int index, double time) {
        final float x = canvas.cx + (canvas.width * 0.5f + 3) * sin(flow(index * 3.1 + 5, time * 0.28 + seed(index, 1) * 9) * 2.2);
        final float y = canvas.cy + (canvas.height * 0.5f + 1) * sin(flow(index * 5.7 + 11, time * 0.33 + seed(index, 2) * 9) * 2.2);
        return new Point(x, y);
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final int teal = color(0, 0xFF3DF5DA), blue = color(1, 0xFF6AA0FF), white = color(2, 0xFFEAFFFB);
        final float trail = parameter(TRAIL_LENGTH);

        mist(canvas, mix(teal, blue, 0.5f));

        for (int i = 0; i < canvas.particleCount(5); i++) {
            final int tint = mix(teal, blue, seed(i, 3) * 0.8f);
            final Point head = path(canvas, i, canvas.time);

            final float pulse = 0.8f + 0.2f * sin(canvas.time * 2.3 + seed(i, 4) * TAU);
            final float flicker = 0.7f + 0.3f * noise(i * 3.3, canvas.time * 7);

            if (trail > 0) {
                final int index = i;
                for (int layer = 0; layer < 2; layer++) {
                    final boolean halo = layer == 0;
                    canvas.ribbon(20, along -> {
                        final Point point = path(canvas, index, canvas.time - (1 - along) * 1.4 * trail);
                        return new Knot(point.x(), point.y(), halo ? 3.2f + along * 1.6f : 0.2f + along * 1.5f, halo ? tint : mix(tint, white, along * 0.5f), along * along * (halo ? 0.4f : 0.9f) * pulse);
                    });

                }

            }

            canvas.glow(head.x(), head.y(), 13, tint, pulse * 0.75f);
            canvas.glow(head.x(), head.y(), 5, tint, pulse * 0.8f);
            canvas.dot(head.x(), head.y(), 2.1f, mix(tint, white, 0.12f), pulse);

            final Point back = path(canvas, i, canvas.time - 0.15);

            final float lean = (head.x() - back.x()) * 0.5f;
            final float reach = (5 + 2.5f * flicker) * pulse;

            canvas.ribbon(7, along -> new Knot(head.x() - lean * along + sin(canvas.time * 9 + along * 5) * along * 1.3f, head.y() - along * reach, 2 * (1 - along * 0.85f), mix(white, tint, along), (1 - along * along) * flicker));
            canvas.mote(head.x(), head.y() - 0.8f, 1.1f, white, flicker);

            for (int spark = 0; spark < 4; spark++) {
                final int key = i * 4 + spark;
                final float age = cycle(canvas.time / (0.9 + seed(key, 5) * 0.6) + seed(key, 6));
                final Point origin = path(canvas, i, canvas.time - age * 0.7);
                canvas.mote(origin.x() + (seed(key, 7) - 0.5f) * 6 * age + flow(key * 1.3, age * 4) * 2, origin.y() - age * (7 + seed(key, 8) * 7), 0.55f, mix(white, tint, age), bell(age) * 0.9f);
            }

        }

        for (int i = 0; i < canvas.particleCount(9); i++) {
            final float blink = bell(cycle(canvas.time / (2 + seed(i, 20) * 2) + seed(i, 21)));
            final float x = canvas.left + seed(i, 22) * canvas.width + flow(i * 2.1, canvas.time * 0.15) * 5;
            final float y = canvas.top + seed(i, 23) * canvas.height + flow(i * 3.7 + 9, canvas.time * 0.12) * 4;
            canvas.mote(x, y, 0.4f + seed(i, 24) * 0.25f, mix(teal, blue, seed(i, 25)), blink * 0.8f);
        }

    }

    private static void mist(EffectCanvas canvas, int tint) {
        for (int layer = 0; layer < 2; layer++) {
            final int layerIndex = layer;
            canvas.ribbon(64, along -> {
                final float lift = noise(along * 6 + layerIndex * 9, canvas.time * 0.25 + layerIndex) * 5;
                return new Knot(canvas.left - 6 + along * (canvas.width + 12), canvas.bottom - 2 - lift - layerIndex * 1.5f, 4 + noise(along * 9 + 3, canvas.time * 0.3) * 5, tint, bell(along) * (layerIndex == 0 ? 0.26f : 0.14f));
            });

        }

    }

}
