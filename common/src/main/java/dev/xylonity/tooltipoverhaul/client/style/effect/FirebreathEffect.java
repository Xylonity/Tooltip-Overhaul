package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class FirebreathEffect extends AmbientEffect {

    private static final float CHARGE = 0.08f;
    private static final float CUT = 0.6f;
    private static final float GONE = 0.72f;

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
        final int red = color(0, 0xFFFF6A1F);
        final int gold = color(1, 0xFFFFC23A);
        final int pale = color(2, 0xFFFFF5C2);

        final int white = 0xFFFFFFFF;
        final float churn = parameter(TURBULENCE);

        for (int i = 0; i < Math.max(1, effectCount(1)); i++) {
            final double clock = canvas.time / (3.8 + seed(i, 1) * 1) + seed(i, 2);
            final int event = (int) Math.floor(clock) * 71 + i;
            final float phase = cycle(clock);
            final float dir = seed(event, 3) < 0.5f ? 1 : -1;

            final float ox = dir > 0 ? canvas.left - 2 : canvas.right + 2;
            final float oy = canvas.top + canvas.height * (0.3f + seed(event, 4) * 0.4f);

            final float span = canvas.width * 0.95f;
            final float sweep = (sin(canvas.time * 1.9 + seed(event, 5) * TAU) * 0.2f + flow(event, canvas.time * 0.7) * 0.12f) * churn;

            final float mouth = phase < CHARGE ? smooth(phase / CHARGE) : 1 - smooth((phase - CUT) / (GONE - CUT));
            canvas.glow(ox, oy, 16, red, mouth * 0.6f);
            canvas.glow(ox, oy, 7, gold, mouth * 0.9f);
            canvas.glow(ox, oy, 3, white, mouth);

            if (phase < CHARGE) {
                for (int spark = 0; spark < 8; spark++) {
                    final float pull = cycle(canvas.time * 2.5 + seed(event * 8 + spark, 6));
                    final float angle = seed(event * 8 + spark, 7) * TAU;
                    canvas.mote(ox + dir * (1 - pull) * 10 + cos(angle) * (1 - pull) * 6, oy + sin(angle) * (1 - pull) * 6, 0.4f, gold, pull * mouth);
                }

                continue;
            }

            if (phase >= GONE) {
                embers(canvas, event, ox, oy, dir, span, (phase - GONE) / (1 - GONE), red, gold);
                continue;
            }

            final float out = phase < CUT ? 1 - (float) Math.pow(1 - Math.min(1, (phase - CHARGE) / 0.14f), 3) : 1;
            final float root = phase < CUT ? 0 : smooth((phase - CUT) / (GONE - CUT));
            final float length = span * out;

            // Trail particles
            for (int puffIndex = 0; puffIndex < 72; puffIndex++) {
                final int puff = event * 53 + puffIndex;
                final float travel = cycle(canvas.time * (2.2 + seed(puff, 8) * 0.8) + seed(puff, 9));
                if (travel < root || travel * span > length + 4) {
                    continue;
                }

                final float along = travel * span;
                final float cone = (2.5f + travel * 12) * churn;
                final float side = (seed(puff, 10) - 0.5f) * cone + flow(puff * 0.6, travel * 4) * cone * 0.5f;
                final float x = ox + dir * along, y = oy + along * sweep + side - travel * travel * 4;
                final float radius = (2 + travel * 6) * (0.75f + seed(puff, 11) * 0.5f);
                final float heat = 1 - smooth((travel - 0.15f) / 0.75f);
                final int skin = heat > 0.6f ? mix(gold, pale, (heat - 0.6f) * 2) : mix(red, gold, heat / 0.6f);
                final float alpha = smooth(travel / 0.05f) * (1 - smooth((travel - 0.8f) / 0.2f)) * (0.55f + 0.2f * noise(puff, canvas.time * 8));
                canvas.haze(x, y, radius * 2.6f, radius * 2.2f, mix(red, skin, 0.5f), alpha * 0.55f);
                canvas.haze(x, y, radius * 1.3f, radius * 1.15f, skin, alpha * 0.9f);
            }

            canvas.ribbon(16, progress -> {
                final float travel = root + progress * (1 - root) * 0.6f;
                final float along = travel * length;
                final float roll = (noise(travel * 5 + event, canvas.time * 7) - 0.5f) * 4 * travel * churn;
                return new Knot(ox + dir * along, oy + along * sweep + roll, 0.8f + travel * 2, mix(gold, white, 0.75f),
                        0.6f * (1 - progress) * (1 - progress) * smooth((travel - root) / 0.05f + 0.2f));
            });

            // Cool floating particles at the tongue (end) of the trail
            for (int tongue = 0; tongue < 7; tongue++) {
                final float age = cycle(canvas.time * 1.5 + seed(event * 7 + tongue, 12));
                final float along = length * (0.85f + age * 0.35f);
                final float x = ox + dir * along, y = oy + along * sweep - age * age * 16 + flow(tongue * 3 + event, age * 3) * 6 * churn;
                canvas.haze(x, y, (6 + age * 7) * (0.8f + seed(tongue, 13) * 0.4f), (5 + age * 6), mix(gold, red, age), bell(age) * 0.65f * out);
            }

            canvas.haze(ox + dir * length * 0.5f, oy + length * 0.5f * sweep, length * 0.55f, 11 + length * 0.06f, red, 0.16f * out);
            embers(canvas, event, ox, oy, dir, span, 0, red, gold);
        }

    }

    private static void embers(EffectCanvas canvas, int event, float ox, float oy, float dir, float span, float after, int red, int gold) {
        for (int ember = 0; ember < 24; ember++) {
            final float age = cycle(canvas.time * (0.9 + seed(event * 22 + ember, 14) * 0.5) + seed(event * 22 + ember, 15));
            final float along = seed(event * 22 + ember, 16) * span;
            final float x = ox + dir * along + flow(ember * 1.7 + event, age * 2) * 6;
            final float y = oy - age * (8 + seed(event * 22 + ember, 17) * 16) + (seed(event * 22 + ember, 18) - 0.5f) * 12;
            canvas.mote(x, y, 0.35f + seed(ember, 19) * 0.4f, mix(gold, red, age), life(age) * (1 - after) * 0.9f);
        }

    }

}
