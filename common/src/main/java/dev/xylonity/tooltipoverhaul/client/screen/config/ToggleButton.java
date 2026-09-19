package dev.xylonity.tooltipoverhaul.client.screen.config;

import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.drawRoundedCard;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.*;

final class ToggleButton extends AbstractWidget {

    private boolean toggled;
    private float animation;
    private final int accent;

    ToggleButton(int x, int y, int width, int height, boolean initial, int accent) {
        super(x, y, width, height, Component.empty());
        toggled = initial;
        animation = initial ? 1f : 0f;
        this.accent = accent;
    }

    boolean isToggled() {
        return toggled;
    }

    void setToggled(boolean toggled) {
        this.toggled = toggled;
        animation = toggled ? 1f : 0f;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        toggled = !toggled;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        animation = toggled ? Math.min(1f, animation + 0.2f) : Math.max(0f, animation - 0.2f);
        final float eased = AnimationUtils.smoothstep(0f, 1f, animation);
        final int x0 = getX();
        final int y0 = getY();
        final int width = getWidth();
        final int height = getHeight();
        final int[] channels = rgb(accent);
        final int dark = 0xFF000000 | ((channels[0] / 4) << 16) | ((channels[1] / 4) << 8) | channels[2] / 4;

        drawRoundedCard(graphics, x0, y0, x0 + width, y0 + height, ColorUtils.lerpColor(0xFF2A2A2E, dark, eased), ColorUtils.lerpColor(0xFF3A3A3E, 0xFF000000 | dimAccent(accent), eased));

        final int padding = 3;
        final int knobWidth = 14;
        final int knobX = x0 + padding + (int) ((width - knobWidth - padding * 2) * eased);
        final int knob = ColorUtils.lerpColor(0xFF555555, accent, eased);
        graphics.fill(knobX + 1, y0 + padding, knobX + knobWidth - 1, y0 + height - padding, knob);
        graphics.fill(knobX, y0 + padding + 1, knobX + knobWidth, y0 + height - padding - 1, knob);

        if (eased > 0.5f) {
            final int glowAlpha = (int) (0x30 * (eased - 0.5f) * 2f);
            final int glow = (glowAlpha << 24) | (channels[0] << 16) | (channels[1] << 8) | channels[2];

            graphics.fill(knobX - 1, y0 + padding + 1, knobX, y0 + height - padding - 1, glow);
            graphics.fill(knobX + knobWidth, y0 + padding + 1, knobX + knobWidth + 1, y0 + height - padding - 1, glow);
        }

        final Minecraft client = Minecraft.getInstance();
        final int textY = y0 + (height - client.font.lineHeight) / 2 + 1;
        if (eased > 0.6f) {
            int alpha = (int) (0xFF * (eased - 0.6f) / 0.4f);
            if (alpha >= 0x10) {
                graphics.drawString(client.font, "ON", x0 + 6, textY, withAlpha(accent, alpha), false);
            }

        }
        else if (eased < 0.4f) {
            final int alpha = (int) (0xFF * (0.4f - eased) / 0.4f);
            if (alpha >= 0x10) {
                final String text = "OFF";
                graphics.drawString(client.font, text, x0 + width - 5 - client.font.width(text), textY, withAlpha(0x666666, alpha), false);
            }

        }

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        ;;
    }

}
