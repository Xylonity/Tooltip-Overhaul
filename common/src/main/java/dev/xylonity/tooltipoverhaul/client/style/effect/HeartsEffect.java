package dev.xylonity.tooltipoverhaul.client.style.effect;

import dev.xylonity.tooltipoverhaul.client.style.effect.internal.AmbientEffect;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.*;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class HeartsEffect extends AmbientEffect {

    /// Heart pattern distributed in columns (same thing as the matrix effect)
    private static final String[][] HEARTS = {
            {".x.x.", "xxxxx", ".xxx.", "..x.."},
            {".xx.xx.", "xxxxxxx", "xxxxxxx", ".xxxxx.", "..xxx..", "...x..."}
    };

    @Override
    protected boolean clipToTooltip() {
        return true;
    }

    @Override
    protected float interiorVisibility() {
        return 0.75f;
    }

    @Override
    protected void draw(EffectCanvas canvas) {
        final int red = color(0, 0xFFFF4D6D), pink = color(1, 0xFFFF9EC4), shine = color(2, 0xFFFFFFFF);

        final float beat = cycle(canvas.time / 1.1);
        final float pulse = 0.85f + 0.15f * ((float) Math.exp(-Math.pow((beat - 0.05f) / 0.04f, 2)) + 0.6f * (float) Math.exp(-Math.pow((beat - 0.22f) / 0.04f, 2)));

        for (int i = 0; i < canvas.particleCount(7); i++) {
            final float period = 3.5f + seed(i, 1) * 1.5f;
            final double clock = canvas.time / period + seed(i, 2);
            final int event = (int) Math.floor(clock) * 13 + i;
            final float age = cycle(clock);

            // Heart pattern
            final String[] heart = HEARTS[seed(event, 3) < 0.3f ? 1 : 0];
            final int tint = seed(event, 4) < 0.5f ? red : pink, width = heart[0].length();

            // Pos
            final int x = (int) Math.floor(canvas.left + 3 + seed(event, 5) * (canvas.width - 6 - width) + sin(age * TAU * 1.2 + seed(i, 6) * TAU) * 2);
            final int y = (int) Math.floor(canvas.bottom - 2 - heart.length - age * canvas.height * 0.85f);

            if (age > 0.88f) {
                final float burst = (age - 0.88f) / (1 - 0.88f);

                final int middleX = x + width / 2;
                final int middleY = y + heart.length / 2;
                final int reach = 1 + Math.round(burst * 2);

                for (int corner = 0; corner < 4; corner++) {
                    final int sparkX = middleX + (corner % 2 == 0 ? -reach : reach), sparkY = middleY + (corner < 2 ? -reach : reach);
                    canvas.rectangle(sparkX, sparkY, sparkX + 1, sparkY + 1, tint, 0.8f * (1 - burst));
                }

                continue;
            }

            final float alpha = 0.85f * pulse * smooth(age / 0.1f);
            for (int row = 0; row < heart.length; row++) {
                for (int column = 0; column < width; column++) {
                    if (heart[row].charAt(column) == 'x') {
                        canvas.rectangle(x + column, y + row, x + column + 1, y + row + 1, row == 1 && column == 1 ? shine : tint, alpha);
                    }

                }

            }

        }

    }

}