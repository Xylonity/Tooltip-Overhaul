package dev.xylonity.tooltipoverhaul.client.screen.config;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.drawCard;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.*;

final class ConfigSlider extends AbstractWidget {

    private final double min;
    private final double max;
    private double value;
    private final boolean integerMode;
    private final int accent;
    private boolean dragging;
    private float activeAnimation;
    private boolean editing;
    private String editBuffer = "";

    ConfigSlider(int x, int y, int width, int height, double min, double max, double value, boolean integerMode, int accent) {
        super(x, y, width, height, Component.empty());
        this.min = min;
        this.max = max;
        this.value = Math.max(min, Math.min(max, value));
        this.integerMode = integerMode;
        this.accent = accent;
        setTooltip(Tooltip.create(Component.translatable("tooltipoverhaul.config.slider_hint")));
    }

    /**
     * Right click types a value directly
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && active && visible && clicked(mouseX, mouseY)) {
            if (!editing) {
                editing = true;
                editBuffer = formatCurrent();
            }

            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    double getValueRaw() {
        return integerMode ? Math.round(value) : value;
    }

    void setValueRaw(double value) {
        this.value = Math.max(min, Math.min(max, value));
    }

    private double fraction() {
        return max <= min ? 0 : (value - min) / (max - min);
    }

    private void updateFromMouse(double mouseX) {
        final double progress = Math.max(0, Math.min(1, (mouseX - getX()) / getWidth()));
        value = min + progress * (max - min);
        if (integerMode) {
            value = Math.round(value);
        }

    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (editing) {
            commitEdit();
        }
        else if (Screen.hasControlDown() && Screen.hasShiftDown()) {
            editing = true;
            editBuffer = formatCurrent();
        }
        else {
            dragging = true;
            updateFromMouse(mouseX);
        }

    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (dragging) {
            updateFromMouse(mouseX);
        }

    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        dragging = false;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused && editing) {
            commitEdit();
        }

    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (!editing) {
            return false;
        }

        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            commitEdit();
        }
        else if (key == GLFW.GLFW_KEY_ESCAPE) {
            editing = false;
        }
        else if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (!editBuffer.isEmpty()) {
                editBuffer = editBuffer.substring(0, editBuffer.length() - 1);
            }

        }
        else {
            return false;
        }

        return true;
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        if (!editing) {
            return false;
        }

        if (Character.isDigit(character) || character == '-' || !integerMode && (character == '.' || character == ',')) {
            editBuffer += character == ',' ? '.' : character;
        }

        return true;
    }

    private void commitEdit() {
        editing = false;
        try {
            setValueRaw(Double.parseDouble(editBuffer.trim()));
            if (integerMode) {
                value = Math.round(value);
            }

        }
        catch (NumberFormatException ignored) {
            ;;
        }

    }

    private String formatCurrent() {
        if (integerMode) {
            return String.valueOf((long) getValueRaw());
        }

        final double raw = getValueRaw();
        if (raw == Math.floor(raw) && !Double.isInfinite(raw)) {
            return String.valueOf((long) raw);
        }

        return String.format(Locale.ROOT, "%.6f", raw).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        final int x0 = getX();
        final int y0 = getY();
        final int width = getWidth();
        final int height = getHeight();
        final int[] channels = rgb(accent);
        final boolean hovered = mouseX >= x0 && mouseX < x0 + width && mouseY >= y0 && mouseY < y0 + height;
        activeAnimation = hovered || dragging ? Math.min(1f, activeAnimation + 0.15f) : Math.max(0f, activeAnimation - 0.1f);
        final Minecraft client = Minecraft.getInstance();

        if (editing) {
            drawCard(graphics, x0, y0, x0 + width, y0 + height, 0xFF161618, accent);
            String text = editBuffer + ((Util.getMillis() / 400L) % 2 == 0 ? "_" : "");
            graphics.drawString(client.font, text, x0 + (width - client.font.width(text)) / 2, y0 + (height - client.font.lineHeight) / 2 + 1, 0xFFE8E8E8, false);
            return;
        }

        final int border = mixRgb(0x3A3A3E, dimAccent(accent), activeAnimation);

        drawCard(graphics, x0, y0, x0 + width, y0 + height, 0xFF1A1A1C, 0xFF000000 | border);

        final int fillWidth = (int) (width * fraction());
        if (fillWidth > 1) {
            final float boost = 1f + activeAnimation * 0.4f;
            final int red = Math.min(255, (int) (channels[0] / 3f * boost));
            final int green = Math.min(255, (int) (channels[1] / 3f * boost));
            final int blue = Math.min(255, (int) (channels[2] / 3f * boost));
            graphics.fill(x0 + 1, y0 + 1, Math.min(x0 + fillWidth, x0 + width - 1), y0 + height - 1, 0xFF000000 | (red << 16) | (green << 8) | blue);
        }

        final int thumbX = x0 + Math.max(0, Math.min(width - 3, fillWidth - 1));
        graphics.fill(thumbX, y0, thumbX + 3, y0 + height, accent);

        if (activeAnimation > 0.05f) {
            final int glowAlpha = (int) (0x50 * activeAnimation);
            final int glow = (glowAlpha << 24) | (channels[0] << 16) | (channels[1] << 8) | channels[2];
            graphics.fill(thumbX - 1, y0 + 1, thumbX, y0 + height - 1, glow);
            graphics.fill(thumbX + 3, y0 + 1, thumbX + 4, y0 + height - 1, glow);
        }

        final String text = integerMode ? String.valueOf((long) getValueRaw()) : String.format("%.3f", getValueRaw());
        graphics.drawString(client.font, text, x0 + (width - client.font.width(text)) / 2, y0 + (height - client.font.lineHeight) / 2 + 1, 0xFFD0D0D0, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        ;;
    }

}