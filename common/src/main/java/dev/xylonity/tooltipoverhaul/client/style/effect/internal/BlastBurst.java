package dev.xylonity.tooltipoverhaul.client.style.effect.internal;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;

public final class BlastBurst {

    public static void draw(EffectCanvas canvas, int event, float x, float y, float radius, float age, float trail, int warm, int cool, int hot, int fragments) {
        if (age < 0 || age >= 1) {
            return;
        }

        final float ignition = smooth(age / 0.018f);
        final float flash = ignition * (1 - smooth(age / 0.16f));

        canvas.glow(x, y, radius * 1.7f, warm, flash * 0.22f);
        canvas.glow(x, y, radius * 0.5f, hot, flash * 0.68f);
        fireball(canvas, event, x, y, radius, age, warm, cool, hot);

        for (int i = 0; i < 7; i++) {
            final int key = event * 31 + i;
            final float phase = (age - seed(key, 1) * 0.035f) / (0.48f + seed(key, 2) * 0.16f);
            if (phase <= 0 || phase >= 1) {
                continue;
            }

            final float angle = (i + seed(key, 3) * 0.65f) * TAU / 7;
            final float spread = 1 - (1 - phase) * (1 - phase);
            final float reach = radius * (0.45f + seed(key, 4) * 0.4f);
            final float bend = (seed(key, 5) - 0.5f) * radius * phase * 0.4f;

            final float px = x + cos(angle) * reach * spread - sin(angle) * bend;
            final float py = y + sin(angle) * reach * spread + cos(angle) * bend - phase * phase * radius * 0.35f;

            final float burn = smooth(phase / 0.07f) * (1 - smooth(phase));
            final int tint = mix(warm, cool, smooth(phase));
            final float puff = radius * (0.18f + spread * 0.28f) * (0.8f + seed(key, 6) * 0.4f);

            canvas.glow(px, py, puff * 1.6f, tint, burn * 0.25f);
            canvas.ribbon(9, progress -> {
                final float along = 0.3f + progress * 0.7f;
                return new Knot(x + (px - x) * along, y + (py - y) * along - sin(progress * Math.PI) * bend,
                        puff * (0.16f + 0.28f * sin(progress * Math.PI)), mix(hot, tint, progress * 0.7f + phase * 0.3f), burn * bell(progress) * 0.48f);
            });

        }

        for (int i = 0; i < fragments; i++) {
            final int key = event * 43 + i;
            final float phase = age / (0.45f + seed(key, 7) * 0.5f);
            if (phase >= 1) {
                continue;
            }

            final float angle = (i + seed(key, 8) * 0.8f) * TAU / fragments;
            final float speed = radius * (0.9f + seed(key, 9) * 1.7f);
            final float drag = phase * (1 - phase * 0.45f);

            final float px = x + cos(angle) * speed * drag;
            final float py = y + sin(angle) * speed * drag + phase * phase * radius * 0.65f;

            final float fade = smooth(phase / 0.035f) * (1 - phase) * (1 - phase);
            final int tint = mix(hot, mix(warm, cool, seed(key, 10)), smooth(phase / 0.35f));
            if (trail > 0) {
                canvas.ribbon(7, along -> {
                    final float past = Math.max(0, phase - (1 - along) * 0.13f * trail);
                    final float run = past * (1 - past * 0.45f);
                    return new Knot(x + cos(angle) * speed * run, y + sin(angle) * speed * run + past * past * radius * 0.65f, 0.08f + along * 0.48f, tint, fade * along * along * 0.75f);
                });
            }

            canvas.mote(px, py, 0.35f + seed(key, 11) * 0.35f, tint, fade * 0.85f);
        }

        final float residue = bell((age - 0.25f) / 0.75f);
        for (int i = 0; i < 5 && residue > 0.002f; i++) {
            final int key = event * 17 + i;
            final float angle = seed(key, 12) * TAU;

            final float px = x + cos(angle) * radius * 0.5f + flow(key, age * 2) * radius * 0.2f;
            final float py = y + sin(angle) * radius * 0.4f - age * radius * 0.7f;

            canvas.glow(px, py, radius * (0.3f + age * 0.35f), mix(cool, 0xFF777982, 0.65f), residue * 0.045f);
            canvas.mote(px, py, 0.3f, warm, residue * (0.35f + 0.2f * sin(age * 23 + i)));
        }

    }

    public static void shockwave(EffectCanvas canvas, float x, float y, float size, float elapsed, float duration, int warm, int hot) {
        final float phase = elapsed / duration;
        if (phase <= 0 || phase >= 1) {
            return;
        }

        final float run = 1 - (1 - phase) * (1 - phase) * (1 - phase) * (1 - phase);
        final float radius = size * (0.12f + run * 0.93f) * parameter(EffectParameter.SIZE);
        final float sigma = Math.min(radius * 0.28f, (0.7f + phase * 0.62f) * parameter(EffectParameter.THICKNESS));
        final float alpha = smooth(phase / 0.07f) * (1 - phase) * (1 - phase) * 0.42f;

        final int tint = mix(warm, hot, 0.55f);

        final int segments = Math.max(64, Math.min(192, (int) (radius * 5)));

        for (int k = 0; k < segments; k++) {
            final float from = k * TAU / segments;
            final float to = (k + 1) * TAU / segments;
            for (int band = 0; band < 16; band++) {
                final float v0 = -3 + band * 6f / 16;
                final float v1 = -3 + (band + 1) * 6f / 16;

                final float r0 = radius + v0 * sigma;
                final float r1 = radius + v1 * sigma;

                final float a0 = alpha * gaussian(v0);
                final float a1 = alpha * gaussian(v1);

                canvas.triangle(x + cos(from) * r0, y + sin(from) * r0 * 0.94f,
                        x + cos(from) * r1, y + sin(from) * r1 * 0.94f,
                        x + cos(to) * r1, y + sin(to) * r1 * 0.94f, tint, a0, a1, a1);
                canvas.triangle(x + cos(from) * r0, y + sin(from) * r0 * 0.94f,
                        x + cos(to) * r1, y + sin(to) * r1 * 0.94f,
                        x + cos(to) * r0, y + sin(to) * r0 * 0.94f, tint, a0, a1, a0);
            }

        }

    }

    private static float gaussian(float x) {
        return Math.max(0, ((float) Math.exp(-0.5f * x * x) - 0.011109f) / 0.988891f);
    }

    private static void fireball(EffectCanvas canvas, int event, float x, float y, float radius, float age, int warm, int cool, int hot) {
        final float burn = smooth(age / 0.025f) * (1 - smooth((age - 0.08f) / 0.55f));
        if (burn < 0.002f) {
            return;
        }

        final float expansion = 1 - (float) Math.exp(-age * 10);
        final float size = radius * (0.2f + expansion * 0.85f) * parameter(EffectParameter.SIZE);

        final float centerY = y - age * age * radius * 0.65f;

        final float rotation = seed(event, 20) * TAU;

        final int core = mix(warm, hot, 0.5f * (1 - smooth(age / 0.4f)));
        final int skin = mix(warm, cool, smooth(age / 0.65f));
        for (int segment = 0; segment < 32; segment++) {
            final float from = segment * TAU / 32, to = (segment + 1) * TAU / 32;
            final float ra = size * contour(from, rotation, age), rb = size * contour(to, rotation, age);
            for (int ring = 0; ring < 4; ring++) {
                final float inner = ring / 4f, outer = (ring + 1) / 4f;

                final float ai = burn * (1 - smooth(inner)) * 0.95f;
                final float ao = burn * (1 - smooth(outer)) * 0.95f;

                final int ci = mix(core, skin, inner), co = mix(core, skin, outer);

                canvas.vertex(x + cos(from) * ra * inner, centerY + sin(from) * ra * inner, ci, ai);
                canvas.vertex(x + cos(from) * ra * outer, centerY + sin(from) * ra * outer, co, ao);
                canvas.vertex(x + cos(to) * rb * outer, centerY + sin(to) * rb * outer, co, ao);
                if (ring > 0) {
                    canvas.vertex(x + cos(from) * ra * inner, centerY + sin(from) * ra * inner, ci, ai);
                    canvas.vertex(x + cos(to) * rb * outer, centerY + sin(to) * rb * outer, co, ao);
                    canvas.vertex(x + cos(to) * rb * inner, centerY + sin(to) * rb * inner, ci, ai);
                }

            }

        }

    }

    private static float contour(float angle, float rotation, float age) {
        return 1 + 0.15f * sin(angle * 3 + rotation + age * 2) + 0.1f * sin(angle * 5 - rotation - age * 3) + 0.06f * cos(angle * 7 + rotation);
    }

}