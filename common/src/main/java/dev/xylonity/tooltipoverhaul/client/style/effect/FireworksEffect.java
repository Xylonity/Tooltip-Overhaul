package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class FireworksEffect extends AmbientEffect {

    private static final int PEONY = 0;
    private static final int CHRYSANTHEMUM = 1;
    private static final int WILLOW = 2;
    private static final int CRACKLE = 3;

    private static final float CLIMB = 0.3f;

    @Override
    protected float interiorVisibility() {
        return 0.55f;
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final int[] palette = { color(0, 0xFFFF7AA8), color(1, 0xFF7AD7FF), color(2, 0xFFFFE08A) };
        final int white = 0xFFFFFFFF;
        final float trail = parameter(TRAIL_LENGTH);
        final float scale = 0.7f + Math.min(canvas.width, canvas.height) / 260f;

        for (int i = 0; i < Math.max(1, effectCount(3)); i++) {
            final double clock = canvas.time / (3.2 + seed(i, 1) * 1.0) + seed(i, 2);
            final int event = (int) Math.floor(clock) * 97 + i;

            final float phase = cycle(clock);
            final int kind = (int) (seed(event, 20) * 4) % 4;
            final int tint = palette[(int) (seed(event, 3) * 3) % 3];
            final int second = palette[(int) (seed(event, 3) * 3 + 1) % 3];

            final float bx = canvas.left + canvas.width * (0.15f + seed(event, 4) * 0.7f);
            final float by = canvas.top + canvas.height * (0.18f + seed(event, 5) * 0.42f);

            // Sizes spread from small pops to blooms wider than the panel is tall
            final float size = seed(event, 6);
            final float reach = (10 + (float) Math.pow(size, 1.4) * 24) * scale * (kind == CRACKLE ? 0.75f : 1);
            final float aspect = 0.8f + seed(event, 21) * 0.45f;

            final float burstLength = (kind == WILLOW ? 0.55f : kind == CRACKLE ? 0.38f : 0.45f) * parameter(BURST_DURATION);

            if (phase < CLIMB) {
                final float climb = 1 - (float) Math.pow(1 - phase / CLIMB, 1.7);
                final float bend = (seed(event, 7) - 0.5f) * 30, curve = (seed(event, 16) - 0.5f) * 24;
                final float waver = (2 + seed(event, 17) * 4) * (seed(event, 18) < 0.5f ? -1 : 1);
                final float rate = 5 + seed(event, 19) * 6;
                final Point head = rocket(canvas, bx, by, bend, curve, waver, rate, climb);

                canvas.ribbon(14, along -> {
                    final Point tail = rocket(canvas, bx, by, bend, curve, waver, rate, Math.max(0, climb - (1 - along) * 0.22f));
                    return new Knot(tail.x(), tail.y(), 0.4f + along * 0.9f, mix(tint, palette[2], 0.5f), along * along * 0.9f);
                });

                for (int spark = 0; spark < 6; spark++) {
                    final float age = cycle(canvas.time * 1.6 + seed(event * 5 + spark, 13));
                    final Point trailPoint = rocket(canvas, bx, by, bend, curve, waver, rate, Math.max(0, climb - age * 0.16f));
                    canvas.mote(trailPoint.x() + (seed(event * 5 + spark, 14) - 0.5f) * age * 5, trailPoint.y() + age * 4, 0.35f, palette[2], (1 - age) * 0.8f);
                }

                canvas.mote(head.x(), head.y(), 0.8f, white, 1);
                canvas.glow(head.x(), head.y(), 3.5f, tint, 0.4f);

                continue;
            }

            final float age = (phase - CLIMB) / burstLength;
            if (age >= 1) {
                continue;
            }

            canvas.glow(bx, by, reach * 1.1f, tint, bell(age / 0.25f) * (0.22f + size * 0.18f));
            canvas.glow(bx, by, 3 + reach * 0.15f, white, bell(age / 0.1f) * 0.28f);
            shell(canvas, event, kind, bx, by, reach, aspect, age, tint, second, white, palette[2], trail);
            if (seed(event, 22) < 0.45f && kind != CRACKLE) {
                final float inner = (age - 0.06f) / 0.7f;
                if (inner > 0 && inner < 1) {
                    shell(canvas, event + 31, PEONY, bx, by, reach * 0.42f, 1, inner, second, tint, white, palette[2], trail * 0.6f);
                }

            }

        }

        glitter(canvas, palette[2]);
    }

    private static void shell(EffectCanvas canvas, int event, int kind, float bx, float by, float reach, float aspect, float age, int tint, int second, int white, int gold, float trail) {
        final int sparks = Math.round((kind == CRACKLE ? 44 : kind == CHRYSANTHEMUM ? 22 : kind == WILLOW ? 28 : 34) * Math.max(0.45f, Math.min(1.1f, reach / 22)));
        final float dying = smooth((age - 0.55f) / 0.45f);
        final boolean recolour = seed(event, 23) < 0.4f;

        for (int k = 0; k < sparks; k++) {
            final int spark = event * 61 + k * 7;
            final float angle = k * TAU / sparks + (seed(spark, 8) - 0.5f) * (kind == CRACKLE ? 0.6f : 0.25f);

            final float speed = kind == PEONY ? 0.55f + seed(spark, 9) * 0.45f : kind == CRACKLE ? 0.15f + seed(spark, 9) * 0.85f : 0.35f + seed(spark, 9) * 0.65f;

            final float sag = (kind == WILLOW ? 16 + seed(spark, 10) * 10 : 3 + seed(spark, 10) * 9) * reach / 18;
            final float wander = flow(spark * 0.37, age * 2.5) * reach * 0.12f * age;
            final float x = bx + cos(angle) * reach * aspect * speed * spread(age) + wander;
            final float y = by + sin(angle) * reach * speed * spread(age) + age * age * sag + wander * 0.5f;

            final float flicker = kind == CRACKLE ? 0.4f + 0.6f * bell(cycle(canvas.time * 9 + seed(spark, 11))) : 1 - dying * (0.5f + 0.5f * sin(canvas.time * 22 + k * 1.7));

            final float alpha = (float) Math.pow(1 - age, kind == WILLOW ? 0.45 : 0.6) * flicker * smooth(age / 0.2f) * smooth(age / 0.2f);

            final int body = recolour ? mix(tint, second, smooth((age - 0.3f) / 0.3f)) : tint;
            final float tail = (kind == CHRYSANTHEMUM ? 0.9f : kind == WILLOW ? 0.7f : kind == CRACKLE ? 0.15f : 0.5f) * trail;

            if (tail > 0) {
                for (int layer = 0; layer < 2; layer++) {
                    final boolean halo = layer == 0;
                    canvas.ribbon(8, along -> {
                        final float past = Math.max(0, age - (1 - along) * tail);
                        final float drift = flow(spark * 0.37, past * 2.5) * reach * 0.12f * past;
                        return new Knot(bx + cos(angle) * reach * aspect * speed * spread(past) + drift, by + sin(angle) * reach * speed * spread(past) + past * past * sag + drift * 0.5f,
                                halo ? 2.2f : 0.2f + along * 0.6f, mix(white, body, Math.min(1, past * 3)), alpha * along * along * (halo ? 0.3f : 1));
                    });

                }

            }

            final int head = age < 0.12f ? mix(white, body, age / 0.12f) : seed(spark, 12) < 0.35f ? mix(body, gold, dying) : body;
            canvas.glow(x, y, kind == CRACKLE ? 1.6f : 2.4f, body, alpha * 0.45f);
            canvas.mote(x, y, (kind == CRACKLE ? 0.4f : 0.6f) + seed(spark, 13) * 0.4f, head, alpha);
        }

    }

    private static void glitter(EffectCanvas canvas, int gold) {
        for (int i = 0; i < canvas.particleCount(12); i++) {
            final float fall = cycle(canvas.time / (9 + seed(i, 30) * 8) + seed(i, 31));
            final float x = canvas.left + seed(i, 32) * canvas.width + flow(i * 1.9, canvas.time * 0.12) * 5;
            final float y = canvas.top - 4 + fall * (canvas.height + 8);
            final float blink = bell(cycle(canvas.time * (0.8 + seed(i, 33) * 0.9) + seed(i, 34)));
            canvas.mote(x, y, 0.4f + seed(i, 35) * 0.25f, gold, life(fall) * (0.2f + blink * 0.8f));
        }

    }

    private static Point rocket(EffectCanvas canvas, float bx, float by, float bend, float curve, float waver, float rate, float climb) {
        final float y0 = canvas.bottom + 4;
        final float arc = sin(climb * (float) Math.PI) * curve;
        final float wobble = sin(climb * rate) * waver * (1 - climb * 0.6f);
        return new Point(bx + bend * (1 - climb) * (1 - climb) + arc + wobble, y0 + (by - y0) * climb);
    }

    private static float spread(float age) {
        final float rest = 1 - age;
        return 1 - rest * rest * (float) Math.sqrt(rest);
    }

}
