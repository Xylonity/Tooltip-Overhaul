package dev.xylonity.tooltipoverhaul.client.screen.config;

import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.buttonBorder;
import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.drawCard;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.*;

class FlatButton extends Button {

    private float hoverAnimation;
    private final int accent;
    private final boolean primary;
    private final long createdAt = Util.getMillis();
    private int entranceDelay;

    private int entranceAlpha = 0xFF;

    FlatButton(int x, int y, int width, int height, Component message, OnPress onPress, int accent) {
        this(x, y, width, height, message, onPress, accent, false);
    }

    FlatButton(int x, int y, int width, int height, Component message, OnPress onPress, int accent, boolean primary) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.accent = accent;
        this.primary = primary;
    }

    FlatButton withEntranceDelay(int delayMs) {
        entranceDelay = delayMs;
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        final float entrance = AnimationUtils.easeOutCubic(Mth.clamp((Util.getMillis() - createdAt - entranceDelay) / 180f, 0f, 1f));
        entranceAlpha = (int) (0xFF * entrance);
        if (entranceAlpha < 0x10) {
            return;
        }

        final int rise = (int) ((1f - entrance) * 4f);
        final int x0 = getX();
        final int y0 = getY() + rise;
        final int x1 = x0 + getWidth();
        final int y1 = y0 + getHeight();
        final boolean hovering = mouseX >= x0 && mouseX < x1 && mouseY >= y0 && mouseY < y1;
        hoverAnimation = hovering ? Math.min(1f, hoverAnimation + 0.15f) : Math.max(0f, hoverAnimation - 0.1f);

        if (primary) {
            final int[] channels = rgb(accent);
            final float boost = 0.32f + hoverAnimation * 0.18f;
            int background = (entranceAlpha << 24) | ((int) (channels[0] * boost) << 16) | ((int) (channels[1] * boost) << 8) | (int) (channels[2] * boost);
            drawCard(graphics, x0, y0, x1, y1, background, withAlpha(accent, entranceAlpha));
        }
        else {
            final float eased = AnimationUtils.smoothstep(0f, 1f, hoverAnimation);
            drawCard(graphics, x0, y0, x1, y1, (entranceAlpha << 24) | mixRgb(0x161618, 0x1E1E22, eased), withAlpha(buttonBorder(accent, hoverAnimation), entranceAlpha));
        }

        final Minecraft client = Minecraft.getInstance();
        final int textColor = !active ? 0x666666 : primary ? 0xFFFFFF : 0xD0D0D0;
        graphics.drawCenteredString(client.font, getMessage(), x0 + width / 2, y0 + (height - client.font.lineHeight) / 2 + 1, withAlpha(textColor, entranceAlpha));
    }

}