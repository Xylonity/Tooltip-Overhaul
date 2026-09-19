package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class MagicOrbsEffect extends AmbientEffect {

    private static Point orbit(EffectCanvas canvas, int index, double seconds) {
        final double angle = seconds * (0.25 + index * 0.025) + (index < 3 ? index * TAU / 3 : index * 2.399963f) + patternPhase(index + 80);
        final float radiusScale = (1 + sin(seconds * 0.37 + index * 2) * 0.07f) * parameter(ORBIT_RADIUS);
        return new Point(canvas.cx + cos(angle) * (canvas.width * 0.54f + 6) * radiusScale, canvas.cy + sin(angle) * (canvas.height * 0.54f + 6) * radiusScale + sin(angle * 3 + index) * 2);
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        for (int i = 0; i < effectCount(3); i++) {
            final int orb = i;
            final int color = i % 3 == 0 ? color(0, 0xFF8BDAEF) : i % 3 == 1 ? color(1, 0xFFC3A0EA) : color(2, 0xFF9EE0BD);
            for (int layer = 0; layer < 2 && parameter(TRAIL_LENGTH) > 0; layer++) {
                final boolean mist = layer == 0;
                canvas.ribbon(72, along -> {
                    final Point point = orbit(canvas, orb, canvas.time - (1 - along) * 3.4 * parameter(TRAIL_LENGTH));
                    final float taper = along * along;
                    return new Knot(point.x(), point.y(), (mist ? 4.5f : 0.9f) * taper + 0.15f, color, taper * (mist ? 0.23f : 0.72f));
                });

            }

            final Point head = orbit(canvas, i, canvas.time);
            final float breathe = 0.8f + 0.2f * sin(canvas.time * 1.5 + i);

            canvas.glow(head.x(), head.y(), 8, color, breathe * 0.35f);
            canvas.glow(head.x(), head.y(), 2.5f, color, breathe * 0.85f);

            canvas.mote(head.x(), head.y(), 1.1f, color(1, 0xFFEFFFFF), breathe);

            for (int j = 0; j < 9 && parameter(TRAIL_LENGTH) > 0; j++) {
                final float age = cycle(canvas.time * 0.35 + seed(j, i + 30));
                final Point point = orbit(canvas, i, canvas.time - age * 3.3 * parameter(TRAIL_LENGTH));
                final float drift = sin(j * 2.3 + age * 5) * age * 4;
                canvas.mote(point.x() + drift, point.y() + age * age * 4, 0.5f, color, life(age) * (1 - age) * 0.7f);
            }

        }

    }

}