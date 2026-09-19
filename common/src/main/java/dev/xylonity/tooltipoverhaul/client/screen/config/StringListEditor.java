package dev.xylonity.tooltipoverhaul.client.screen.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.*;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.dimAccent;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.withAlpha;

final class StringListEditor implements ConfigModal {

    private static final int ROW_HEIGHT = 18;
    private static final int PADDING = 8;
    private static final int MAX_VISIBLE = 8;

    private final Minecraft client = Minecraft.getInstance();
    private final Component title;
    private final List<String> values = new ArrayList<>();
    private final StyledEditBox addBox;
    private final Consumer<String> update;
    private final IconRenderer iconRenderer;
    private final int screenHeight;
    private final int accent;
    private final int x;
    private final int anchorY;
    private final int width = 230;
    private int scroll;
    private boolean closed;

    StringListEditor(int screenWidth, int screenHeight, int anchorX, int anchorY, int accent, Component title, String initial, IconRenderer iconRenderer, Consumer<String> update) {
        this.screenHeight = screenHeight;
        this.anchorY = anchorY;
        this.accent = accent;
        this.title = title;
        this.iconRenderer = iconRenderer;
        this.update = update;
        this.x = Mth.clamp(anchorX - width - 6, 4, screenWidth - width - 4);
        for (String part : initial.split(",")) {
            if (!part.trim().isEmpty()) {
                values.add(part.trim());
            }

        }

        addBox = new StyledEditBox(client.font, 0, 0, width - PADDING * 2, 16, Component.empty(), accent);
        addBox.setMaxLength(256);
        addBox.setFocused(true);
    }

    private int visibleRows() {
        return Math.max(1, Math.min(values.size(), MAX_VISIBLE));
    }

    private int height() {
        return PADDING + 13 + visibleRows() * ROW_HEIGHT + 6 + 16 + PADDING;
    }

    private int top() {
        final int minimum = ConfigScreenStyle.HEADER_HEIGHT + 4;
        return Mth.clamp(anchorY - height() / 2, minimum, Math.max(minimum, screenHeight - height() - 4));
    }

    private int rowsTop() {
        return top() + PADDING + 13;
    }

    private void clampScroll() {
        scroll = Mth.clamp(scroll, 0, Math.max(0, values.size() - MAX_VISIBLE));
    }

    private void sync() {
        update.accept(String.join(", ", values));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int y0 = top();
        final int y1 = y0 + height();

        graphics.pose().pushPose();

        graphics.pose().translate(0, 0, 400);
        drawCard(graphics, x, y0, x + width, y1, 0xF8101012, withAlpha(dimAccent(accent), 0xFF));
        graphics.drawString(client.font, title, x + PADDING, y0 + 6, 0xFF888888, false);

        if (values.isEmpty()) {
            graphics.drawString(client.font, I18n.get("tooltipoverhaul.config.frames.list.empty"), x + PADDING, rowsTop() + (ROW_HEIGHT - client.font.lineHeight) / 2, 0xFF555555, false);
        }

        clampScroll();

        for (int i = scroll; i < Math.min(values.size(), scroll + MAX_VISIBLE); i++) {
            int rowY = rowsTop() + (i - scroll) * ROW_HEIGHT;
            boolean hovered = mouseX >= x + 4 && mouseX < x + width - 4 && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            if (hovered) {
                graphics.fill(x + 4, rowY, x + width - 4, rowY + ROW_HEIGHT, 0xFF1B1B1F);
            }

            String value = values.get(i);
            boolean known = iconRenderer == null;
            if (iconRenderer != null) {
                known = iconRenderer.render(graphics, value, x + PADDING, rowY + 1);
                if (!known) {
                    graphics.drawString(client.font, "?", x + PADDING + 5, rowY + 5, 0xFFCC8040, false);
                }

            }
            else {
                graphics.fill(x + PADDING + 6, rowY + 8, x + PADDING + 9, rowY + 11, 0xFF585858);
            }

            graphics.drawString(client.font, client.font.plainSubstrByWidth(value, width - PADDING * 2 - 32), x + PADDING + 20, rowY + (ROW_HEIGHT - client.font.lineHeight) / 2, known ? 0xFFD0D0D0 : 0xFFCC8040, false);
            if (hovered) {
                boolean overDelete = mouseX >= x + width - PADDING - 12 && mouseX < x + width - PADDING;
                graphics.drawString(client.font, "\u00d7", x + width - PADDING - 8, rowY + (ROW_HEIGHT - client.font.lineHeight) / 2 + 2, overDelete ? 0xFFE06060 : 0xFF915050, false);
            }

        }

        if (values.size() > MAX_VISIBLE) {
            final int trackTop = rowsTop();
            final int trackHeight = MAX_VISIBLE * ROW_HEIGHT;
            ConfigScroll.render(graphics, x + width, ConfigScroll.fromContent(trackTop, trackTop + trackHeight, values.size() * ROW_HEIGHT, scroll * ROW_HEIGHT), accent, mouseX, mouseY, false);
        }

        addBox.setX(x + PADDING);
        addBox.setY(y1 - PADDING - 16);
        addBox.render(graphics, mouseX, mouseY, partialTick);
        if (addBox.getValue().isEmpty()) {
            graphics.drawString(client.font, I18n.get("tooltipoverhaul.config.frames.list.add_hint"), x + PADDING + 11, y1 - PADDING - 12, 0xFF4A4A52, false);
        }

        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        final int y0 = top();
        if (mouseX < x || mouseX >= x + width || mouseY < y0 || mouseY >= y0 + height()) {
            closed = true;
            return true;
        }

        if (button != 0) {
            return true;
        }

        for (int i = scroll; i < Math.min(values.size(), scroll + MAX_VISIBLE); i++) {
            final int rowY = rowsTop() + (i - scroll) * ROW_HEIGHT;
            if (mouseX >= x + width - PADDING - 12 && mouseX < x + width - PADDING && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                values.remove(i);
                clampScroll();
                sync();
                playClickSound();
                return true;
            }

        }

        addBox.mouseClicked(mouseX, mouseY, button);
        addBox.setFocused(true);

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll -= (int) Math.signum(delta);
        clampScroll();
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            closed = true;
            return true;
        }

        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            final String value = addBox.getValue().trim();
            if (value.isEmpty()) {
                closed = true;
            }
            else {
                values.add(value);
                addBox.setValue("");
                scroll = Math.max(0, values.size() - MAX_VISIBLE);
                sync();
                playClickSound();
            }

            return true;
        }

        return addBox.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        addBox.charTyped(character, modifiers);
        return true;
    }

    @Override
    public void tick() {
        ;;
    }

    @Override
    public boolean closed() {
        return closed;
    }

    @FunctionalInterface
    interface IconRenderer {
        boolean render(GuiGraphics graphics, String value, int x, int y);
    }

}