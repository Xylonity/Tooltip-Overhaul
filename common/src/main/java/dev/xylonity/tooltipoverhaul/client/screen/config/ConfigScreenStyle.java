package dev.xylonity.tooltipoverhaul.client.screen.config;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import dev.xylonity.tooltipoverhaul.config.wrapper.AutoConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import java.lang.reflect.Field;
import java.util.Locale;

import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.*;

final class ConfigScreenStyle {

    static final int HEADER_HEIGHT = 46;
    static final int DEFAULT_ACCENT = 0xFF4A9EFF;

    static void renderBackground(GuiGraphics graphics, int width, int height, int headerHeight, int accent) {
        graphics.fill(0, 0, width, height, 0xFF0E0E0E);

        final float pulse = 0.5f + 0.5f * (float) Math.sin(Util.getMillis() / 900.0);
        final float breath = 0.45f + 0.55f * pulse;
        int[] channels = rgb(accent);
        final int glowWidth = Math.min(220, width / 2);
        for (int x = 0; x < glowWidth; x += 2) {
            final float progress = (float) x / glowWidth;
            int alpha = (int) (0x2A * (1f - progress * progress) * breath);
            if (alpha > 0) {
                graphics.fill(x, 0, x + 2, headerHeight, (alpha << 24) | (channels[0] << 16) | (channels[1] << 8) | channels[2]);
            }

        }

        for (int i = 0; i < 24; i++) {
            int alpha = (int) (26 * ((float) i / 24) * ((float) i / 24));
            if (alpha > 0) {
                graphics.fill(0, height - 24 + i, width, height - 23 + i, alpha << 24);
            }

        }

    }

    static void renderBrandedHeader(GuiGraphics graphics, Font font, int width, String title, String description, long openedAt) {
        for (int i = 0; i < HEADER_HEIGHT; i++) {
            int alpha = Math.max(0, 60 - i * 2);
            if (alpha > 0) {
                graphics.fill(0, i, width, i + 1, alpha << 24);
            }

        }

        final String heading = title.toUpperCase(Locale.ROOT);
        final boolean hasDescription = !description.isEmpty();
        final int titleY = hasDescription ? 12 : 19;
        final long now = Util.getMillis();
        final float titleProgress = AnimationUtils.easeOutCubic(Mth.clamp((now - openedAt) / 240f, 0f, 1f));
        final float descriptionProgress = AnimationUtils.easeOutCubic(Mth.clamp((now - openedAt - 80) / 240f, 0f, 1f));

        final int titleAlpha = (int) (0xFF * titleProgress);
        if (titleAlpha >= 0x10) {
            graphics.drawString(font, heading, 16 - (int) ((1f - titleProgress) * 6f), titleY, withAlpha(0xFFFFFF, titleAlpha), false);
        }

        final int descriptionAlpha = (int) (0xFF * descriptionProgress);
        if (hasDescription && descriptionAlpha >= 0x10) {
            graphics.drawString(font, description, 16 - (int) ((1f - descriptionProgress) * 6f), titleY + font.lineHeight + 4, withAlpha(0x888888, descriptionAlpha), false);
        }

    }

    static void renderSectionHeader(GuiGraphics graphics, Font font, String section, int x, int y, int width, int accent) {
        renderSectionHeader(graphics, font, section, x, y, width, accent, 0xFF);
    }

    static void renderSectionHeader(GuiGraphics graphics, Font font, String section, int x, int y, int width, int accent, int alpha) {
        if (alpha < 0x10) {
            return;
        }

        final String label = section.toUpperCase(Locale.ROOT);
        final int textX = x + 12;
        final int lineY = y + 8;
        graphics.fill(x + 3, lineY, x + 8, lineY + 1, withAlpha(accent, Math.round(0xB0 * alpha / 255f)));
        graphics.drawString(font, label, textX, y + 4, withAlpha(0x585858, alpha), false);

        final int lineStart = textX + font.width(label) + 6;
        if (lineStart < x + width) {
            graphics.fill(lineStart, lineY, x + width, lineY + 1, (Math.round(0x20 * alpha / 255f) << 24) | 0xFFFFFF);
        }

    }

    static void drawNotchedBorder(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        graphics.fill(x0 + 2, y0, x1 - 2, y0 + 1, color);
        graphics.fill(x0 + 2, y1 - 1, x1 - 2, y1, color);
        graphics.fill(x0, y0 + 2, x0 + 1, y1 - 2, color);
        graphics.fill(x1 - 1, y0 + 2, x1, y1 - 2, color);
        graphics.fill(x0 + 1, y0 + 1, x0 + 2, y0 + 2, color);
        graphics.fill(x1 - 2, y0 + 1, x1 - 1, y0 + 2, color);
        graphics.fill(x0 + 1, y1 - 2, x0 + 2, y1 - 1, color);
        graphics.fill(x1 - 2, y1 - 2, x1 - 1, y1 - 1, color);
    }

    static void drawCard(GuiGraphics graphics, int x0, int y0, int x1, int y1, int background, int border) {
        graphics.fill(x0 + 1, y0 + 1, x1 - 1, y1 - 1, background);
        drawNotchedBorder(graphics, x0, y0, x1, y1, border);
    }

    static void drawColorSwatch(GuiGraphics graphics, int x, int y, int size, int argb, int accent, boolean hovered, int alpha) {
        final int border = buttonBorder(accent, hovered ? 1f : 0f);
        graphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, (alpha << 24) | (argb & 0x00FFFFFF));
        drawNotchedBorder(graphics, x, y, x + size, y + size, (alpha << 24) | (border & 0x00FFFFFF));

        final int red = (argb >>> 16) & 255, green = (argb >>> 8) & 255, blue = argb & 255;
        final boolean bright = red * 299 + green * 587 + blue * 114 > 128 * 1000;
        final int arrow = (Math.round(alpha * (hovered ? 1f : 0.8f)) << 24) | (bright ? 0x000000 : 0xFFFFFF);
        final int ax = x + size - 5, ay = y + size - 5;

        graphics.fill(ax, ay, ax + 3, ay + 1, arrow);
        graphics.fill(ax + 1, ay + 1, ax + 2, ay + 2, arrow);
    }

    static int buttonBorder(int accent, float hover) {
        return 0xFF000000 | mixRgb(0x3A3A3E, accent, AnimationUtils.smoothstep(0f, 1f, hover));
    }

    static void restoreGuiRenderState(GuiGraphics graphics) {
        graphics.flush();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);

        RenderSystem.enableDepthTest();
    }

    static void drawRoundedCard(GuiGraphics graphics, int x0, int y0, int x1, int y1, int background, int border) {
        graphics.fill(x0 + 2, y0, x1 - 2, y0 + 1, border);
        graphics.fill(x0 + 1, y0 + 1, x1 - 1, y0 + 2, border);
        graphics.fill(x0, y0 + 2, x0 + 1, y1 - 2, border);
        graphics.fill(x1 - 1, y0 + 2, x1, y1 - 2, border);
        graphics.fill(x0 + 1, y1 - 2, x1 - 1, y1 - 1, border);
        graphics.fill(x0 + 2, y1 - 1, x1 - 2, y1, border);
        graphics.fill(x0 + 2, y0 + 1, x1 - 2, y1 - 1, background);
        graphics.fill(x0 + 1, y0 + 2, x1 - 1, y1 - 2, background);
    }

    static void drawCardHoverFx(GuiGraphics graphics, int left, int top, int width, int height, float animation, int accent, int seed) {
        if (animation <= 0.1f || height < 12) {
            return;
        }

        final int[] channels = rgb(accent);
        final long time = Util.getMillis();
        for (int i = 0; i < 7; i++) {
            final long period = 1700L + Math.floorMod((long) seed * 31L + i * 197L, 900L);
            final long offset = Math.floorMod((long) seed * 137L + i * 311L, period);
            final float phase = Math.floorMod(time + offset, period) / (float) period;
            final float horizontal = Math.floorMod((long) seed * 53L + i * 419L, 1000L) / 1000f;
            final float sway = (float) Math.sin(phase * Math.PI * 2.0 + i * 1.3) * 2f;
            final int x = (int) (left + 5 + horizontal * (width - 10) + sway);
            final int y = (int) (top + height - 4 - phase * (height - 8));
            final int alpha = (int) (Math.sin(phase * Math.PI) * animation * 0x66);
            if (alpha < 6) {
                continue;
            }

            final int size = i % 3 == 0 ? 2 : 1;
            graphics.fill(x, y, x + size, y + size, (alpha << 24) | (channels[0] << 16) | (channels[1] << 8) | channels[2]);
        }

    }

    static String resolveTitle(Class<?> type) {
        final AutoConfig config = type.getAnnotation(AutoConfig.class);
        if (config != null && !config.title().isEmpty()) {
            return config.title();
        }

        return config != null ? prettify(config.file()) : type.getSimpleName();
    }

    static String prettify(String name) {
        final String[] parts = name.replace('_', ' ').replace('-', ' ').split("\\s+");
        final StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }

            if (!result.isEmpty()) {
                result.append(' ');
            }

            result.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                result.append(part.substring(1).toLowerCase(Locale.ROOT));
            }

        }

        return result.toString();
    }

    static String entryTranslationKey(Field field) {
        return "tooltipoverhaul.config.entry." + field.getName().toLowerCase(Locale.ROOT);
    }

    static String categoryLabel(String category) {
        final String normalized = category.trim().toLowerCase(Locale.ROOT);
        final String name = normalized.isEmpty() ? "general" : normalized;
        final String key = "tooltipoverhaul.config.category." + name;
        return I18n.exists(key) ? I18n.get(key) : prettify(name);
    }

    static String translatedOrFallback(String key, String fallback) {
        return I18n.exists(key) ? I18n.get(key) : fallback;
    }

    static void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
    }

}
