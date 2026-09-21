package dev.xylonity.tooltipoverhaul.client.screen.config;

import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.buttonBorder;
import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.drawCard;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.mixRgb;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.rgb;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.withAlpha;

/**
 * Section navigation wrapoped inside the main panel from the custom frames screens
 */
final class ConfigSectionTabs {

    private static final int CHIP_HEIGHT = 14;
    private static final int CHIP_PADDING = 6;
    private static final int GAP = 3;

    private final int accent;
    private final List<Tab> tabs = new ArrayList<>();
    private @Nullable String selected = null;
    private boolean hidden;
    private int height;

    ConfigSectionTabs(int accent) {
        this.accent = accent;
    }

    void setTabs(List<Tab> updated) {
        final List<Float> hovers = new ArrayList<>();
        for (Tab tab : updated) {
            float hover = 0f;
            for (Tab previous : tabs) {
                if (Objects.equals(previous.key, tab.key)) {
                    hover = previous.hover;
                }

            }

            hovers.add(hover);
        }

        tabs.clear();

        for (int i = 0; i < updated.size(); i++) {
            final Tab tab = updated.get(i);
            tab.hover = hovers.get(i);
            tabs.add(tab);
        }

        if (selected != null && tabs.stream().noneMatch(tab -> selected.equals(tab.key))) {
            selected = null;
        }

    }

    @Nullable
    String selected() {
        return selected;
    }

    void select(@Nullable String key) {
        selected = key != null && tabs.stream().anyMatch(tab -> key.equals(tab.key)) ? key : null;
    }

    void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    boolean visible() {
        return !hidden && !tabs.isEmpty();
    }

    boolean cycle(int direction) {
        if (!visible() || tabs.isEmpty()) {
            return false;
        }

        int index = 0;
        for (int i = 0; i < tabs.size(); i++) {
            if (Objects.equals(tabs.get(i).key, selected)) {
                index = i;
                break;
            }

        }

        selected = tabs.get(Math.floorMod(index + direction, tabs.size())).key;

        return true;
    }

    int height() {
        return height;
    }

    void layout(int left, int top, int width) {
        if (!visible() || width <= 0) {
            height = 0;
            return;
        }

        final Font font = Minecraft.getInstance().font;
        int x = left;
        int y = top;
        for (Tab tab : tabs) {
            final int chipWidth = font.width(tab.label) + CHIP_PADDING * 2;
            if (x > left && x + chipWidth > left + width) {
                x = left;
                y += CHIP_HEIGHT + GAP;
            }

            tab.x = x;
            tab.y = y;
            tab.width = chipWidth;
            x += chipWidth + GAP;
        }

        height = y + CHIP_HEIGHT - top;
    }

    void render(GuiGraphics graphics, int mouseX, int mouseY, int alpha) {
        if (!visible() || alpha < 0x10) {
            return;
        }

        final Font font = Minecraft.getInstance().font;
        final int[] channels = rgb(accent);
        for (final Tab tab : tabs) {
            final boolean active = Objects.equals(tab.key, selected);
            final boolean hovered = tab.contains(mouseX, mouseY);
            tab.hover = hovered ? Math.min(1f, tab.hover + 0.15f) : Math.max(0f, tab.hover - 0.1f);

            final int chipAlpha = alpha;
            final int x1 = tab.x + tab.width;
            final int y1 = tab.y + CHIP_HEIGHT;
            if (active) {
                final float boost = 0.32f + tab.hover * 0.18f;
                final int background = (chipAlpha << 24) | ((int) (channels[0] * boost) << 16) | ((int) (channels[1] * boost) << 8) | (int) (channels[2] * boost);
                drawCard(graphics, tab.x, tab.y, x1, y1, background, withAlpha(accent, chipAlpha));
            }
            else {
                final float eased = AnimationUtils.smoothstep(0f, 1f, tab.hover);
                drawCard(graphics, tab.x, tab.y, x1, y1, (chipAlpha << 24) | mixRgb(0x161618, 0x1E1E22, eased), withAlpha(buttonBorder(accent, tab.hover), chipAlpha));
            }

            final int textColor = active ? 0xFFFFFF : mixRgb(0x8A8A8A, 0xD0D0D0, tab.hover);
            graphics.drawString(font, tab.label, tab.x + CHIP_PADDING, tab.y + (CHIP_HEIGHT - font.lineHeight) / 2 + 1, withAlpha(textColor, chipAlpha), false);
        }

    }

    boolean mouseClicked(double mouseX, double mouseY) {
        if (!visible()) {
            return false;
        }

        for (Tab tab : tabs) {
            if (tab.contains(mouseX, mouseY)) {
                selected = tab.key;
                return true;
            }

        }

        return false;
    }

    static final class Tab {

        final @Nullable String key;
        final String label;
        int x;
        int y;
        int width;
        float hover;

        Tab(@Nullable String key, String label) {
            this.key = key;
            this.label = label;
        }

        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + CHIP_HEIGHT;
        }

    }

}