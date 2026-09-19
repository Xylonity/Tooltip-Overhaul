package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.BlastBurst;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class MissilesEffect extends AmbientEffect {

    private static final float FLIGHT = 0.56f;

    @Override
    protected float interiorVisibility() {
        return 0.55f;
    }

    private static float scale(float progress) {
        return 1.15f - 0.5f * smooth(Math.max(0, (progress - 0.35f) / 0.65f));
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final int exhaust = color(0, 0xFFFFC994);
        final int flame = color(1, 0xFFDA8967);
        final int smoke = color(2, 0xFFB9BBC4);
        final int hot = mix(exhaust, 0xFFFFFFFF, 0.78f);

        final float trail = parameter(TRAIL_LENGTH);

        for (int i = 0; i < effectCount(3); i++) {
            final double clock = canvas.time / (3.0 + seed(i, 2) * 1.1) + seed(i, 3);
            for (int previous = 1; previous >= 0; previous--) {
                final int event = ((int) Math.floor(clock) - previous) * 83 + i;
                final float phase = cycle(clock) + previous, elapsed = phase / FLIGHT;
                if (previous > 0 && elapsed >= 1 + 0.48f * trail) {
                    continue;
                }

                final Flight flight = new Flight(canvas.left + canvas.width * (0.1f + seed(event, 4) * 0.8f), canvas.bottom + 4,
                        canvas.left + canvas.width * (0.15f + seed(event, 5) * 0.7f), canvas.top + canvas.height * (0.15f + seed(event, 6) * 0.45f),
                        canvas.height * (0.42f + seed(event, 7) * 0.2f), (1.6f + seed(event, 8) * 1.8f) * parameter(TURBULENCE),
                        5 + seed(event, 9) * 3, event);

                // Smoke trail
                if (trail > 0) {
                    final float lifetime = 0.48f * trail;
                    final float start = Math.max(0, elapsed - lifetime), end = Math.min(1, elapsed);
                    if (end > start) {
                        for (int layer = 0; layer < 2; layer++) {
                            final boolean halo = layer == 0;
                            canvas.ribbon(48, along -> {
                                final float emitted = start + (end - start) * along;
                                final float age = (elapsed - emitted) / lifetime;
                                final Point point = flight.at(emitted);
                                final float drift = age * age;
                                final float x = point.x() + flow(event * 1.7, emitted * 5) * drift * 3;
                                final float y = point.y() - drift * 4;
                                final float fade = (1 - age) * (1 - age) * smooth(emitted / 0.035f);
                                final float width = (halo ? 1.5f + age * 4.5f : 0.35f + age * 1.65f) * scale(emitted);
                                return new Knot(x, y, width, mix(exhaust, smoke, smooth(age / 0.3f)), fade * (halo ? 0.15f : 0.32f));
                            });

                        }

                    }

                }

                if (elapsed < 1) {
                    final Point at = flight.at(elapsed);
                    final Point before = flight.at(Math.max(0, elapsed - 0.006f));
                    final Point after = flight.at(Math.min(1, elapsed + 0.006f));

                    final float dx = after.x() - before.x();
                    final float dy = after.y() - before.y();

                    final float length = Math.max(0.001f, (float) Math.hypot(dx, dy));

                    final float ux = dx / length;
                    final float uy = dy / length;
                    final float size = scale(elapsed) * parameter(SIZE);

                    final float flare = 0.85f + 0.15f * noise(event, canvas.time * 12);
                    final float alpha = smooth(elapsed / 0.025f);

                    canvas.line(at.x() - ux * 8 * size * flare, at.y() - uy * 8 * size * flare, at.x() - ux * 1.5f * size, at.y() - uy * 1.5f * size, 0.05f, 0.95f * size, flame, 0, alpha * 0.72f);
                    canvas.line(at.x() - ux * 4 * size, at.y() - uy * 4 * size, at.x(), at.y(), 0.3f * size, 0.65f * size, hot, alpha * 0.5f, alpha);
                    canvas.glow(at.x() - ux * 2 * size, at.y() - uy * 2 * size, 3.5f * scale(elapsed), exhaust, alpha * 0.38f);

                    canvas.mote(at.x(), at.y(), 0.65f * scale(elapsed), hot, alpha);
                }
                else {
                    final Point before = flight.at(0.97f);
                    final float dx = flight.tx() - before.x(), dy = flight.ty() - before.y();
                    final float length = Math.max(0.001f, (float) Math.hypot(dx, dy));
                    impact(canvas, event, flight.tx(), flight.ty(), dx / length, dy / length, (15 + seed(event, 10) * 6) * (0.85f + Math.min(canvas.width, canvas.height) / 260f),
                            (phase - FLIGHT) / (1 - FLIGHT), (float) (3.0 + seed(i, 2) * 1.1) * (1 - FLIGHT), trail, exhaust, flame, hot, smoke);
                }

            }

        }

    }

    private static void impact(EffectCanvas canvas, int event, float x, float y, float ux, float uy, float radius, float age, float seconds, float trail, int warm, int cool, int hot, int smoke) {
        if (age < 0 || age >= 1) {
            return;
        }

        final float size = parameter(SIZE);

        // Particles
        final float flash = smooth(age / 0.015f) * (1 - smooth(age / 0.13f));
        if (flash > 0.002f) {
            final float reach = 1 - (1 - smooth(age / 0.13f)) * (1 - smooth(age / 0.13f));

            canvas.glow(x, y, radius * (0.5f + reach * 0.6f) * size, hot, flash * 0.55f);
            canvas.dot(x, y, radius * 0.16f * (1 + reach), 0xFFFFFFFF, flash * 0.9f);

            for (int ray = 0; ray < 7; ray++) {
                final float angle = seed(event, 30) * TAU + ray * TAU / 7 + seed(event * 7 + ray, 31) * 0.5f;
                final float length = radius * (0.7f + seed(event * 7 + ray, 32) * 0.7f) * (0.3f + reach * 0.7f) * size;
                canvas.line(x + cos(angle) * radius * 0.1f, y + sin(angle) * radius * 0.1f, x + cos(angle) * length, y + sin(angle) * length, 0.9f, 0.05f, hot, flash * 0.8f, 0);
            }

        }

        BlastBurst.shockwave(canvas, x, y, radius * 1.1f, age * seconds, 0.24f, warm, hot);
        BlastBurst.draw(canvas, event, x, y, radius, age, trail, warm, cool, hot, 12);

        // Sparks
        for (int i = 0; i < 9; i++) {
            final int key = event * 53 + i;
            final float phase = age / (0.4f + seed(key, 40) * 0.45f);
            if (phase >= 1) {
                continue;
            }

            final float heading = (float) Math.atan2(uy, ux) + (seed(key, 41) - 0.5f) * 1.5f;
            final float speed = radius * (1.6f + seed(key, 42) * 1.6f) * size;
            final float run = phase * (1 - phase * 0.4f);

            final float px = x + cos(heading) * speed * run;
            final float py = y + sin(heading) * speed * run + phase * phase * radius * 1.1f;

            final float fade = smooth(phase / 0.03f) * (1 - phase) * (1 - phase);
            final int tint = mix(hot, warm, smooth(phase / 0.4f));

            canvas.ribbon(6, along -> {
                final float past = Math.max(0, phase - (1 - along) * 0.1f);
                final float back = past * (1 - past * 0.4f);
                return new Knot(x + cos(heading) * speed * back, y + sin(heading) * speed * back + past * past * radius * 1.1f, 0.1f + along * 0.55f, tint, fade * along * along * 0.85f);
            });

            canvas.mote(px, py, 0.45f + seed(key, 43) * 0.3f, hot, fade);
        }

        // Delayed pops
        for (int k = 0; k < 2; k++) {
            final int key = event * 61 + k;
            final float phase = (age - 0.16f - k * 0.13f - seed(key, 50) * 0.08f) / 0.11f;
            if (phase <= 0 || phase >= 1) {
                continue;
            }

            final float angle = seed(key, 51) * TAU, distance = radius * (0.25f + seed(key, 52) * 0.35f);

            final float px = x + cos(angle) * distance;
            final float py = y + sin(angle) * distance - age * radius * 0.3f;

            final float pop = smooth(phase / 0.15f) * (1 - smooth(phase));

            canvas.glow(px, py, radius * 0.45f * (0.6f + phase), warm, pop * 0.4f);
            canvas.dot(px, py, radius * 0.08f * (1 + phase), hot, pop * 0.9f);

            for (int mote = 0; mote < 4; mote++) {
                final float popAngle = seed(key * 5 + mote, 53) * TAU;
                final float popOffset = radius * (0.1f + phase * 0.45f);
                canvas.mote(px + cos(popAngle) * popOffset, py + sin(popAngle) * popOffset + phase * phase * radius * 0.2f, 0.35f, hot, pop * 0.8f);
            }

        }

        final float after = smooth((age - 0.08f) / 0.15f) * (1 - smooth((age - 0.3f) / 0.7f));

        canvas.glow(x, y + radius * 0.05f, radius * 0.7f, mix(warm, cool, 0.5f), after * 0.14f);

        for (int i = 0; i < 3; i++) {
            final int key = event * 71 + i;
            final float phase = (age - 0.12f - seed(key, 60) * 0.12f) / 0.8f;
            if (phase <= 0 || phase >= 1) {
                continue;
            }

            final float lift = 1 - (1 - phase) * (1 - phase);
            final float px = x + (seed(key, 61) - 0.5f) * radius * 0.7f + flow(key, phase * 2) * radius * 0.35f * phase;
            final float py = y - lift * radius * (0.9f + seed(key, 62) * 0.5f) + seed(key, 63) * radius * 0.2f;
            final float puff = radius * (0.35f + lift * 0.55f) * (0.8f + seed(key, 64) * 0.4f);

            final float alpha = smooth(phase / 0.12f) * (1 - phase) * (1 - phase) * (1 - phase) * 0.09f;
            canvas.glow(px, py, puff, mix(smoke, cool, 0.5f * (1 - phase) * (1 - phase)), alpha);
        }

    }

    private record Flight(
            float sx,
            float sy,
            float tx,
            float ty,
            float loft,
            float weave,
            float rate,
            int event
    ) {

        Point at(float time) {
            final float progress = (float) Math.pow(clamp(time), 1.18), remaining = 1 - progress;
            final float dx = tx - sx;
            final float x1 = sx + dx * 0.08f, y1 = sy - loft;
            final float x2 = tx - dx * 0.2f, y2 = ty + loft * 0.18f;
            final float x = remaining * remaining * remaining * sx + 3 * remaining * remaining * progress * x1 + 3 * remaining * progress * progress * x2 + progress * progress * progress * tx;
            final float y = remaining * remaining * remaining * sy + 3 * remaining * remaining * progress * y1 + 3 * remaining * progress * progress * y2 + progress * progress * progress * ty;
            final float vx = 3 * remaining * remaining * (x1 - sx) + 6 * remaining * progress * (x2 - x1) + 3 * progress * progress * (tx - x2);
            final float vy = 3 * remaining * remaining * (y1 - sy) + 6 * remaining * progress * (y2 - y1) + 3 * progress * progress * (ty - y2);
            final float length = Math.max(0.001f, (float) Math.hypot(vx, vy));
            final float wobble = sin(progress * rate + seed(event, 1) * TAU) * weave * bell(progress);
            return new Point(x - vy / length * wobble, y + vx / length * wobble);
        }

    }

}