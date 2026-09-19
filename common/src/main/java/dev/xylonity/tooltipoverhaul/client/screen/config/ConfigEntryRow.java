package dev.xylonity.tooltipoverhaul.client.screen.config;

import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;
import dev.xylonity.tooltipoverhaul.config.wrapper.ConfigEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.buttonBorder;
import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.entryTranslationKey;
import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.playClickSound;
import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.prettify;
import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.translatedOrFallback;
import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.drawCard;
import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.drawColorSwatch;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.colorEntries;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.dimAccent;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.withAlpha;

final class ConfigEntryRow extends ConfigFormEntry {

    final Field field;
    final ConfigEntry meta;
    final String searchIndex;
    final Object defaultValue;
    final List<int[]> swatchHits = new ArrayList<>();

    static final int CATALOG_HIT = -3;

    boolean swatchHovered;

    private int resetX = Integer.MIN_VALUE;

    private final ConfigFieldBinding binding;
    private List<Component> cachedTooltip;

    ConfigEntryRow(Field field, ConfigEntry meta, Object snapshotValue, int accent, ConfigModalHost modals, int screenWidth, int screenHeight, Runnable beforeSubEditor) {
        this(field, meta, accent, new ConfigFieldBinding(field, meta, snapshotValue, accent, modals, screenWidth, screenHeight, beforeSubEditor));
    }

    private ConfigEntryRow(Field field, ConfigEntry meta, int accent, ConfigFieldBinding binding) {
        super(label(field), description(field, meta), binding.widget(), field.getName(), accent, 30);
        this.field = field;
        this.meta = meta;
        this.binding = binding;
        this.defaultValue = binding.defaultValue();
        final String fallback = prettify(field.getName());
        this.searchIndex = (field.getName() + " " + fallback + " " + label.getString() + " " + meta.category() + " " + ConfigScreenStyle.categoryLabel(meta.category()) + " "
                + meta.comment() + " " + description).toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private static Component label(Field field) {
        final String key = entryTranslationKey(field);
        return I18n.exists(key) ? Component.translatable(key) : Component.literal(prettify(field.getName()));
    }

    private static String description(Field field, ConfigEntry meta) {
        return translatedOrFallback(entryTranslationKey(field) + ".description", meta.comment().trim());
    }

    String categoryKey() {
        return meta.category().trim();
    }

    boolean isFrameOverlay() {
        return field.getDeclaringClass() == TooltipsConfig.class && field.getName().equals("GLOBAL_FRAME_OVERLAY_LOCATION");
    }

    boolean matchesSearch(String query) {
        for (String chunk : query.split("\\s+")) {
            if (!chunk.isEmpty() && !searchIndex.contains(chunk)) {
                return false;
            }

        }

        return true;
    }

    @Override
    boolean isModified() {
        return binding.isModified();
    }

    @Override
    boolean requiresRestart() {
        return meta.requiresRestart();
    }

    List<Component> tooltipLines() {
        if (cachedTooltip != null) {
            return cachedTooltip;
        }

        final List<Component> lines = new ArrayList<>();
        final String note = translatedOrFallback(entryTranslationKey(field) + ".note", meta.note().trim());
        if (!note.isEmpty()) {
            lines.add(Component.literal(note));
        }

        if (defaultValue != null) {
            lines.add(Component.translatable("tooltipoverhaul.config.tooltip.default", formatValue(defaultValue)).withStyle(style -> style.withColor(0x7A7A7A)));
        }

        if (Double.isFinite(meta.min()) && Double.isFinite(meta.max())) {
            final boolean integral = field.getType() == int.class || field.getType() == long.class;
            final String minimum = integral ? String.valueOf((long) meta.min()) : trimDouble(meta.min());
            final String maximum = integral ? String.valueOf((long) meta.max()) : trimDouble(meta.max());
            lines.add(Component.translatable("tooltipoverhaul.config.tooltip.range", minimum, maximum).withStyle(style -> style.withColor(0x7A7A7A)));
        }

        if (widget instanceof ConfigSlider) {
            lines.add(Component.translatable("tooltipoverhaul.config.tooltip.slider_hint").withStyle(style -> style.withColor(0x5A5A5A)));
        }

        if (meta.requiresRestart()) {
            lines.add(Component.translatable("tooltipoverhaul.config.tooltip.requires_restart").withStyle(style -> style.withColor(0xFF8844)));
        }

        cachedTooltip = lines;

        return lines;
    }

    private static String formatValue(Object value) {
        if (value instanceof Double number) {
            return trimDouble(number);
        }

        if (value instanceof Float number) {
            return trimDouble(number);
        }

        if (value instanceof Enum<?> enumeration) {
            return prettify(enumeration.name());
        }

        return String.valueOf(value);
    }

    private static String trimDouble(double value) {
        return value == Math.floor(value) && !Double.isInfinite(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    void writeToField() {
        binding.writeToField();
    }

    void resetToDefault() {
        binding.resetToDefault();
    }

    @Override
    protected void renderAccessories(GuiGraphics graphics, int mouseX, int mouseY, int widgetX, int widgetY, int alpha, float textFade, float entrance, float hover) {
        swatchHits.clear();

        swatchHovered = false;
        int leftmost = widgetX;
        if (meta.color() && widget instanceof EditBox editBox && textFade > 0.5f && alpha >= 0x10) {
            int swatchX = widgetX - 16;
            final int swatchY = currentY + (height - 12) / 2;
            final List<String> parts = colorEntries(editBox.getValue());
            for (int partIndex = parts.size() - 1; partIndex >= 0; partIndex--) {
                final String part = parts.get(partIndex);
                if (!(part.startsWith("#") || part.startsWith("0x") || part.startsWith("0X"))) {
                    continue;
                }

                final boolean hovered = mouseX >= swatchX && mouseX < swatchX + 12 && mouseY >= swatchY && mouseY < swatchY + 12;
                swatchHovered |= hovered;
                drawColorSwatch(graphics, swatchX, swatchY, 12, ConfigColorParser.parseColor(part), accent, hovered, alpha);

                swatchHits.add(new int[]{swatchX, swatchY, partIndex, 12});

                leftmost = swatchX;
                swatchX -= 14;
            }

        }

        if (isFrameOverlay() && widget instanceof EditBox && textFade > 0.5f && alpha >= 0x10) {
            final int catalogX = widgetX - 22;
            final boolean hovered = mouseX >= catalogX && mouseX < catalogX + 18 && mouseY >= widgetY && mouseY < widgetY + 18;
            drawCard(graphics, catalogX, widgetY, catalogX + 18, widgetY + 18, withAlpha(hovered ? 0x26262A : 0x1A1A1C, alpha), withAlpha(buttonBorder(accent, hovered ? 1f : 0f), alpha));
            ConfigIconButton.drawIcon(graphics, ConfigIconButton.Icon.MENU, catalogX + (18 - ConfigIconButton.Icon.MENU.width) / 2, widgetY + (18 - ConfigIconButton.Icon.MENU.height) / 2, withAlpha(hovered ? accent : 0x808080, alpha));
            swatchHits.add(new int[]{catalogX, widgetY, CATALOG_HIT, 18});
            leftmost = catalogX;
        }

        resetX = leftmost - (leftmost == widgetX ? 24 : 22);
        if (hover <= 0.3f || defaultValue == null) {
            return;
        }

        final int resetY = currentY + (height - 18) / 2;
        final boolean resetHovered = mouseX >= resetX && mouseX < resetX + 18 && mouseY >= resetY && mouseY < resetY + 18;
        final int resetAlpha = (int) (0xFF * (hover - 0.3f) / 0.7f);
        drawCard(graphics, resetX, resetY, resetX + 18, resetY + 18, withAlpha(resetHovered ? 0x202024 : 0x1A1A1C, resetAlpha), withAlpha(resetHovered ? dimAccent(accent) : 0x2E2E32, resetAlpha));
        ConfigIconButton.drawIcon(graphics, ConfigIconButton.Icon.REPEAT, resetX + (18 - ConfigIconButton.Icon.REPEAT.width) / 2,
                resetY + (18 - ConfigIconButton.Icon.REPEAT.height) / 2, resetHovered ? withAlpha(accent & 0x00FFFFFF, resetAlpha) : withAlpha(0x8A8A8A, resetAlpha));
    }

    boolean clickedReset(double mouseX, double mouseY, int button, int rowLeft, int rowTop, int rowWidth) {
        if (button != 0 || hoverAnim <= 0.3f || defaultValue == null || widget == null) {
            return false;
        }

        final int resetY = rowTop + (height - 18) / 2;
        if (resetX != Integer.MIN_VALUE && mouseX >= resetX && mouseX < resetX + 18 && mouseY >= resetY && mouseY < resetY + 18) {
            resetToDefault();
            playClickSound();
            return true;
        }

        return false;
    }

}