package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class EruptionEffect extends AmbientEffect {

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
        final int red = color(0, 0xFFFF7A2A), amber = color(1, 0xFFFFD36B), hot = color(2, 0xFFFFF4C8);
        final int white = 0xFFFFFFFF;
        final float scatter = parameter(TURBULENCE), trail = parameter(TRAIL_LENGTH);
        final float gravity = 60 + canvas.height * 0.4f;

        for (int ventIndex = 0; ventIndex < Math.max(1, effectCount(2)); ventIndex++) {
            final double clock = canvas.time / (2.6 + seed(ventIndex, 8) * 1.2) + seed(ventIndex, 9);
            final int event = (int) Math.floor(clock) * 53 + ventIndex;

            final float age = cycle(clock);
            final float seconds = age * (2.6f + seed(ventIndex, 8) * 1.2f);
            final int side = (int) (seed(event, 1) * 3) % 3;

            final float vx;
            final float vy;
            final float nx;
            final float ny;
            if (side == 0) {
                vx = canvas.left + canvas.width * (0.2f + seed(event, 2) * 0.6f);
                vy = canvas.bottom + 1;
                nx = 0;
                ny = -1;
            }
            else {
                vx = side == 1 ? canvas.left - 1 : canvas.right + 1;
                vy = canvas.top + canvas.height * (0.15f + seed(event, 2) * 0.55f);
                nx = side == 1 ? 1 : -1;
                ny = 0;
            }

            final float aimBase = (float) Math.atan2(ny, nx);
            final float open = smooth(age / 0.12f) * (1 - smooth((age - 0.7f) / 0.3f));
            final float throb = (0.75f + 0.25f * noise(event, canvas.time * 2.5)) * open;

            // Paints the ground around the eruption
            canvas.ribbon(28, progress -> {
                final float along = (progress - 0.5f) * 36;
                return new Knot(vx + along * -ny, vy + along * nx, 1.5f + 5 * bell(progress), red, 0.35f * throb);
            });

            canvas.haze(vx + nx * 6, vy + ny * 6, 8 + Math.abs(ny) * 3, 8 + Math.abs(nx) * 3, red, 0.24f * throb);
            canvas.haze(vx, vy, 4.5f + Math.abs(ny) * 7, 4.5f + Math.abs(nx) * 7, red, 0.5f * throb);
            canvas.haze(vx, vy, 2.6f + Math.abs(ny) * 3.4f, 2.6f + Math.abs(nx) * 3.4f, amber, 0.9f * throb);
            canvas.glow(vx + nx, vy + ny, 3.5f, white, throb);

            // Ember column
            for (int i = 0; i < canvas.particleCount(30); i++) {
                final float life = cycle(canvas.time / (1.3 + seed(i, 3) * 1.1) + seed(i, 4));
                final float spreadOut = (seed(i, 5) - 0.5f) * 2 * life * (6 + seed(i, 6) * 10) * scatter + flow(i * 1.3, life * 3) * 4;
                final float reach = life * (canvas.height * 0.4f + seed(i, 7) * canvas.height * 0.35f);
                final float x = vx + nx * reach + -ny * spreadOut;
                final float y = vy + ny * reach + nx * spreadOut + life * life * 8;

                canvas.glow(x, y, 2.2f, red, life(life) * 0.45f * open);
                canvas.mote(x, y, 0.5f + seed(i, 8) * 0.5f, mix(hot, red, life), life(life) * (1 - life * 0.4f) * open);
            }

            // The burst
            canvas.glow(vx, vy, 22, amber, bell((age - 0.1f) / 0.08f) * 0.9f);
            canvas.glow(vx, vy, 8, white, bell((age - 0.1f) / 0.05f));

            final float since = seconds - 0.1f * (2.6f + seed(ventIndex, 8) * 1.2f);
            for (int k = 0; k < 16; k++) {
                final int bomb = event * 29 + k * 3;
                final float aim = aimBase + (seed(bomb, 10) - 0.5f) * 1.3f * scatter;
                final float launch = (30 + seed(bomb, 11) * 34) * (0.6f + canvas.height / 120f);
                final float ux = cos(aim) * launch, uy = sin(aim) * launch;

                final float drop = canvas.bottom + 1 - vy;
                final float land = (-uy + (float) Math.sqrt(Math.max(0, uy * uy + 2 * gravity * drop))) / gravity;
                final float flight = since - seed(bomb, 12) * 0.12f;

                if (flight < 0 || flight > land + 0.35f) {
                    continue;
                }

                final float bx = vx + ux * flight;
                final float by = vy + uy * flight + 0.5f * gravity * flight * flight;

                final float size = 0.8f + seed(bomb, 13) * 0.9f;
                final float cooling = Math.min(1, flight / Math.max(0.01f, land));
                final int skin = mix(mix(white, amber, Math.min(1, flight * 4)), red, cooling * 0.8f);

                if (flight <= land) {
                    if (trail > 0) {
                        for (int layer = 0; layer < 2; layer++) {
                            final boolean halo = layer == 0;
                            canvas.ribbon(8, along -> {
                                final float past = Math.max(0, flight - (1 - along) * 0.22f * trail);
                                return new Knot(vx + ux * past, vy + uy * past + 0.5f * gravity * past * past,
                                        (halo ? 3 : 1) * size * along + 0.05f, halo ? red : mix(amber, red, cooling), (halo ? 0.35f : 0.9f) * along * along);
                            });
                        }

                    }

                    canvas.glow(bx, by, 3.5f * size, red, 0.55f);
                    canvas.glow(bx, by, 1.4f * size, amber, 0.85f);
                    canvas.mote(bx, by, size, skin, 1);
                }
                else {
                    // Droplets where the particles hit the ground
                    final float splash = (flight - land) / 0.35f;
                    final float gx = vx + ux * land, gy = canvas.bottom + 1;
                    canvas.glow(gx, gy, 5 * size + splash * 4, amber, (1 - splash) * 0.7f);
                    for (int droplet = 0; droplet < 3; droplet++) {
                        final float sideways = (seed(bomb + droplet, 14) - 0.5f) * 2;
                        canvas.mote(gx + sideways * splash * 6, gy - bell(splash) * (3 + seed(bomb + droplet, 15) * 4), 0.4f, amber, (1 - splash) * 0.9f);
                    }

                }

            }

        }

    }

}