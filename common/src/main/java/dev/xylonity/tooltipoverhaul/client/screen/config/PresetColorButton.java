package dev.xylonity.tooltipoverhaul.client.screen.config;

import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.*;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.formatHex;

final class PresetColorButton extends AbstractWidget {

    private static final String CUSTOM = "custom";

    private final List<String> presets;
    private final int accent;
    private final ConfigModalHost modals;
    private final int screenWidth;
    private final int screenHeight;
    private String value;
    private float hoverAnimation;

    PresetColorButton(int x, int y, int width, int height, List<String> presets, String current, int accent, ConfigModalHost modals, int screenWidth, int screenHeight) {
        super(x, y, width, height, Component.empty());
        this.presets = List.copyOf(presets);
        this.accent = accent;
        this.modals = modals;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.value = current;
    }

    String getValue() {
        return value;
    }

    void setValue(String value) {
        this.value = value;
    }

    private boolean isPreset() {
        return presets.contains(value.trim().toLowerCase(Locale.ROOT));
    }

    private int currentArgb() {
        return isPreset() ? 0xFFFFFFFF : ConfigColorParser.parseColor(value.trim());
    }

    private static String presetLabel(String preset) {
        final String key = "tooltipoverhaul.config.frames.option." + preset;
        return I18n.exists(key) ? I18n.get(key) : prettify(preset);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible || button != 0 || !clicked(mouseX, mouseY)) {
            return false;
        }

        final List<SelectionPopup.Option> choices = new ArrayList<>();
        for (final String preset : presets) {
            choices.add(new SelectionPopup.Option(preset, presetLabel(preset), "", ItemStack.EMPTY));
        }

        choices.add(new SelectionPopup.Option(CUSTOM, I18n.get("tooltipoverhaul.config.custom_color"), isPreset() ? "" : value.trim(), ItemStack.EMPTY));

        final String selected = isPreset() ? value.trim().toLowerCase(Locale.ROOT) : CUSTOM;
        modals.open(new SelectionPopup(screenWidth, screenHeight, Component.translatable("tooltipoverhaul.config.frames.choose"), choices, selected::equals, this::select, false, accent));

        playDownSound(Minecraft.getInstance().getSoundManager());

        return true;
    }

    private void select(String selected) {
        if (!selected.equals(CUSTOM)) {
            value = selected;
            return;
        }

        modals.open(new ColorLevelPicker(getX(), getY() + getHeight(), HEADER_HEIGHT + 4, screenWidth, screenHeight, accent, currentArgb(), false, argb -> value = formatHex(argb, false)));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        final int x0 = getX();
        final int y0 = getY();
        final int width = getWidth();
        final int height = getHeight();
        final boolean hovering = mouseX >= x0 && mouseX < x0 + width && mouseY >= y0 && mouseY < y0 + height;
        hoverAnimation = hovering ? Math.min(1f, hoverAnimation + 0.15f) : Math.max(0f, hoverAnimation - 0.1f);
        drawCard(graphics, x0, y0, x0 + width, y0 + height, 0xFF18181A, buttonBorder(accent, hoverAnimation));

        final Minecraft client = Minecraft.getInstance();
        final boolean preset = isPreset();
        String text = preset ? presetLabel(value.trim().toLowerCase(Locale.ROOT)) : value.trim().toUpperCase(Locale.ROOT);
        final int iconGap = 3;
        final int swatchSpace = preset ? 0 : 12 + iconGap;
        final int maximumLabelWidth = width - ConfigIconButton.Icon.EXPAND.width - iconGap - swatchSpace - 12;
        if (client.font.width(text) > maximumLabelWidth) {
            final int ellipsisWidth = client.font.width("\u2026");
            text = client.font.plainSubstrByWidth(text, Math.max(4, maximumLabelWidth - ellipsisWidth)) + "\u2026";
        }

        final int contentWidth = swatchSpace + client.font.width(text) + iconGap + ConfigIconButton.Icon.EXPAND.width;
        int contentX = x0 + (width - contentWidth) / 2;
        if (!preset) {
            drawColorSwatch(graphics, contentX, y0 + (height - 12) / 2, 12, currentArgb(), accent, hovering, 0xFF);
            contentX += swatchSpace;
        }

        graphics.drawString(client.font, text, contentX, y0 + (height - client.font.lineHeight) / 2 + 1, 0xFFD0D0D0, false);
        ConfigIconButton.drawIcon(graphics, ConfigIconButton.Icon.EXPAND, contentX + client.font.width(text) + iconGap, y0 + (height - ConfigIconButton.Icon.EXPAND.height) / 2, 0xFFD0D0D0);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        ;;
    }

}
