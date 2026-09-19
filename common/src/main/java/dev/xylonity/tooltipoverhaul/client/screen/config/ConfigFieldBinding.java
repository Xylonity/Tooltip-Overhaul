package dev.xylonity.tooltipoverhaul.client.screen.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipLayout;
import dev.xylonity.tooltipoverhaul.client.style.icon.animation.IconAnimation;
import dev.xylonity.tooltipoverhaul.client.style.preview.PreviewPanelDecorations;
import dev.xylonity.tooltipoverhaul.client.style.vignette.parser.VignetteEntry;
import dev.xylonity.tooltipoverhaul.config.ConfigManager;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.config.wrapper.ConfigEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.*;

final class ConfigFieldBinding {

    private final Field field;
    private final ConfigEntry meta;
    private final Object snapshotValue;
    private final Object defaultValue;
    private final int accent;
    private final ConfigModalHost modals;
    private final int screenWidth;
    private final int screenHeight;
    private final Runnable beforeSubEditor;
    private final AbstractWidget widget;

    ConfigFieldBinding(Field field, ConfigEntry meta, Object snapshotValue, int accent, ConfigModalHost modals, int screenWidth, int screenHeight, Runnable beforeSubEditor) {
        this.field = field;
        this.meta = meta;
        this.snapshotValue = snapshotValue;
        this.defaultValue = ConfigManager.getCodeDefault(field);
        this.accent = accent;
        this.modals = modals;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.beforeSubEditor = beforeSubEditor;
        this.widget = createWidget();
    }

    AbstractWidget widget() {
        return widget;
    }

    Object defaultValue() {
        return defaultValue;
    }

    private AbstractWidget createWidget() {
        final Minecraft minecraft = Minecraft.getInstance();
        final Class<?> type = field.getType();
        try {
            if (type == boolean.class) {
                return new ToggleButton(0, 0, 44, 18, field.getBoolean(null), accent);
            }

            if (meta.slider() && Double.isFinite(meta.min()) && Double.isFinite(meta.max())) {
                final boolean isInt = type == int.class || type == long.class;
                return new ConfigSlider(0, 0, 120, 18, meta.min(), meta.max(), ((Number) field.get(null)).doubleValue(), isInt, accent);
            }

            if (isTooltipLayout()) {
                return enumButton(100, TooltipLayout.Style.values(), TooltipLayout.Style.fromString((String) field.get(null)));
            }

            if (isIconAppearAnimation()) {
                return enumButton(120, IconAnimation.values(), IconAnimation.fromString((String) field.get(null)));
            }

            if (isPreviewPanelSideTriangles()) {
                return enumButton(100, PreviewPanelDecorations.SideTriangles.values(), PreviewPanelDecorations.SideTriangles.fromString((String) field.get(null)));
            }

            if (isPreviewPanelCornerType()) {
                return enumButton(100, PreviewPanelDecorations.CornerType.values(), PreviewPanelDecorations.CornerType.fromString((String) field.get(null)));
            }

            if (isPreviewPanelBackgroundCornerType()) {
                return enumButton(100, PreviewPanelDecorations.BackgroundCornerType.values(), PreviewPanelDecorations.BackgroundCornerType.fromString((String) field.get(null)));
            }

            if (type.isEnum()) {
                return enumButton(100, (Enum<?>[]) type.getEnumConstants(), (Enum<?>) field.get(null));
            }

            if (isEffects()) {
                return new FlatButton(0, 0, 120, 18, Component.translatable("tooltipoverhaul.config.frames.effect_editor"), button -> openEffectEditor(), accent);
            }

            if (isVignettes()) {
                return new FlatButton(0, 0, 120, 18, Component.translatable("tooltipoverhaul.config.frames.vignette_editor"), button -> openVignetteEditor(), accent);
            }

            if (meta.color() && meta.options().length > 0) {
                return new PresetColorButton(0, 0, 120, 18, Arrays.asList(meta.options()), String.valueOf(field.get(null)), accent, modals, screenWidth, screenHeight);
            }

            if (type == String.class && meta.options().length > 0) {
                return optionButton(120, Arrays.asList(meta.options()), optionOf(String.valueOf(field.get(null))));
            }

            final String value = String.valueOf(field.get(null));
            final StyledEditBox box = new StyledEditBox(minecraft.font, 0, 0, value.length() > 20 ? 200 : 120, 18, Component.empty(), accent);

            box.setMaxLength(512);
            box.setValue(value);
            box.setValidator(validatorFor(type));

            return box;
        }
        catch (Exception exception) {
            final EditBox box = new StyledEditBox(minecraft.font, 0, 0, 120, 18, Component.empty(), accent);
            box.setValue("");
            return box;
        }

    }

    private ConfigOptionButton<Enum<?>> enumButton(int width, Enum<?>[] values, Enum<?> current) {
        return new ConfigOptionButton<>(0, 0, width, 18, Arrays.asList(values), current, accent, value -> value.name().toLowerCase(Locale.ROOT), value -> {
            String key = "tooltipoverhaul.config.frames.option." + value.name().toLowerCase(Locale.ROOT);
            return I18n.exists(key) ? I18n.get(key) : prettify(value.name());
        }, value -> {
            ;;
        }, () -> {
            ;;
        }, modals, screenWidth, screenHeight);

    }

    private ConfigOptionButton<String> optionButton(int width, List<String> options, String current) {
        return new ConfigOptionButton<>(0, 0, width, 18, options, current, accent, value -> value, value -> {
            String key = "tooltipoverhaul.config.frames.option." + value;
            return I18n.exists(key) ? I18n.get(key) : prettify(value);
        }, value -> {
            ;;
        }, () -> {
            ;;
        }, modals, screenWidth, screenHeight);

    }

    private String optionOf(String raw) {
        final String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (final String option : meta.options()) {
            if (option.equals(normalized)) {
                return option;
            }

        }

        return meta.options()[0];
    }

    private void openEffectEditor() {
        final Minecraft minecraft = Minecraft.getInstance();
        final Screen parent = minecraft.screen;
        beforeSubEditor.run();

        final JsonObject entry = new JsonObject();
        entry.addProperty("specialEffect", TooltipsConfig.EFFECTS);
        minecraft.setScreen(new EffectEditorScreen(parent, entry, accent, draft -> TooltipsConfig.EFFECTS = draft.has("specialEffect") ? draft.get("specialEffect").getAsString() : ""));
    }

    private void openVignetteEditor() {
        final Minecraft minecraft = Minecraft.getInstance();
        final Screen parent = minecraft.screen;
        beforeSubEditor.run();

        final JsonArray vignettes = new JsonArray();
        for (final VignetteEntry vignette : VignetteEntry.Parser.from(TooltipsConfig.VIGNETTES)) {
            vignettes.add(vignette.serialize());
        }

        final JsonObject entry = new JsonObject();
        entry.add("vignettes", vignettes);
        minecraft.setScreen(new VignetteEditorScreen(parent, new JsonObject(), entry, ItemStack.EMPTY, accent, draft -> {
            final List<String> raw = new ArrayList<>();
            if (draft.has("vignettes")) {
                for (final JsonElement element : draft.getAsJsonArray("vignettes")) {
                    raw.add(element.getAsString());
                }

            }

            TooltipsConfig.VIGNETTES = String.join(", ", raw);
        }));
    }

    private static Predicate<String> validatorFor(Class<?> type) {
        if (type == int.class) {
            return raw -> raw.isBlank() || parseIntSafe(raw, Integer.MIN_VALUE) != Integer.MIN_VALUE || raw.trim().equals(String.valueOf(Integer.MIN_VALUE));
        }

        if (type == long.class) {
            return raw -> raw.isBlank() || parseLongSafe(raw, Long.MIN_VALUE) != Long.MIN_VALUE || raw.trim().equals(String.valueOf(Long.MIN_VALUE));
        }

        if (type == float.class || type == double.class) {
            return raw -> {
                if (raw.isBlank()) {
                    return true;
                }

                try {
                    Double.parseDouble(raw.trim());
                    return true;
                }
                catch (NumberFormatException exception) {
                    return false;
                }

            };

        }

        return raw -> true;
    }

    private boolean isTooltipLayout() {
        return field.getDeclaringClass() == TooltipsConfig.class && field.getName().equals("TOOLTIP_LAYOUT");
    }

    private boolean isIconAppearAnimation() {
        return field.getDeclaringClass() == TooltipsConfig.class && field.getName().equals("ICON_APPEAR_ANIMATION");
    }

    private boolean isPreviewPanelSideTriangles() {
        return field.getDeclaringClass() == TooltipsConfig.class && field.getName().equals("PREVIEW_PANEL_SIDE_TRIANGLES");
    }

    private boolean isPreviewPanelCornerType() {
        return field.getDeclaringClass() == TooltipsConfig.class && field.getName().equals("PREVIEW_PANEL_CORNER_TYPE");
    }

    private boolean isPreviewPanelBackgroundCornerType() {
        return field.getDeclaringClass() == TooltipsConfig.class && field.getName().equals("PREVIEW_PANEL_BACKGROUND_CORNER_TYPE");
    }

    private boolean isEffects() {
        return field.getDeclaringClass() == TooltipsConfig.class && field.getName().equals("EFFECTS");
    }

    private boolean isVignettes() {
        return field.getDeclaringClass() == TooltipsConfig.class && field.getName().equals("VIGNETTES");
    }

    private Object currentValue() {
        final Class<?> type = field.getType();
        if (widget instanceof ToggleButton toggleButton) {
            return toggleButton.isToggled();
        }

        if (widget instanceof ConfigSlider configSlider) {
            return configSlider.getValueRaw();
        }

        if (widget instanceof ConfigOptionButton<?> optionButton) {
            Object current = optionButton.getCurrent();
            return type == String.class && current instanceof Enum<?> value
                    ? value.name().toLowerCase(Locale.ROOT) : current;
        }

        if (widget instanceof PresetColorButton presetButton) {
            return presetButton.getValue();
        }

        if (widget instanceof FlatButton) {
            try {
                return field.get(null);
            }
            catch (Exception exception) {
                return null;
            }

        }

        if (widget instanceof EditBox box) {
            final String raw = box.getValue().trim();
            if (type == String.class) {
                return box.getValue();
            }

            if (raw.isEmpty()) {
                return null;
            }

            try {
                if (type == int.class) {
                    return Integer.decode(raw.startsWith("#") ? "0x" + raw.substring(1) : raw);
                }

                if (type == long.class) {
                    return Long.decode(raw.startsWith("#") ? "0x" + raw.substring(1) : raw);
                }

                if (type == float.class) {
                    return Float.parseFloat(raw);
                }

                if (type == double.class) {
                    return Double.parseDouble(raw);
                }

            }
            catch (NumberFormatException exception) {
                return null;
            }

        }

        return null;
    }

    boolean isModified() {
        if (snapshotValue == null) {
            return false;
        }

        final Object current = currentValue();
        if (current == null) {
            return false;
        }

        if (current instanceof Number number && snapshotValue instanceof Number snapNumber) {
            return Math.abs(number.doubleValue() - snapNumber.doubleValue()) > 1e-6;
        }

        return !current.equals(snapshotValue);
    }

    void writeToField() {
        try {
            final Class<?> type = field.getType();
            if (type == boolean.class && widget instanceof ToggleButton toggleButton) {
                field.setBoolean(null, toggleButton.isToggled()); return;
            }

            if (widget instanceof ConfigSlider configSlider) {
                final double valueRaw = configSlider.getValueRaw();
                if (type == int.class) {
                    field.setInt(null, (int) valueRaw);
                }
                else if (type == long.class) {
                    field.setLong(null, (long) valueRaw);
                }
                else if (type == float.class) {
                    field.setFloat(null, (float) valueRaw);
                }
                else {
                    field.setDouble(null, valueRaw);
                }

                return;
            }

            if (widget instanceof ConfigOptionButton<?> || widget instanceof PresetColorButton) {
                field.set(null, currentValue()); return;
            }

            if (widget instanceof EditBox box) {
                final String raw = box.getValue().trim();
                if (type == String.class) {
                    field.set(null, box.getValue());
                    return;
                }

                if (raw.isEmpty()) {
                    return;
                }

                if (type == int.class) {
                    field.setInt(null, parseIntSafe(raw, field.getInt(null)));
                }
                else if (type == long.class) {
                    field.setLong(null, parseLongSafe(raw, field.getLong(null)));
                }
                else if (type == float.class) {
                    try {
                        field.setFloat(null, Float.parseFloat(raw));
                    }
                    catch (NumberFormatException ignored) {
                        ;;
                    }

                }
                else if (type == double.class) {
                    try {
                        field.setDouble(null, Double.parseDouble(raw));
                    }
                    catch (NumberFormatException ignored) {
                        ;;
                    }

                }

            }

        }
        catch (Exception ignored) {
            ;;
        }

    }

    void resetToDefault() {
        if (defaultValue == null) {
            return;
        }

        try {
            if (widget instanceof ToggleButton toggleButton && defaultValue instanceof Boolean bool) {
                toggleButton.setToggled(bool);
            }
            else if (widget instanceof ConfigSlider configSlider && defaultValue instanceof Number number) {
                configSlider.setValueRaw(number.doubleValue());
            }
            else if (widget instanceof ConfigOptionButton<?> optionButton && isTooltipLayout()) {
                optionButton.setCurrent(TooltipLayout.Style.fromString(String.valueOf(defaultValue)));
            }
            else if (widget instanceof ConfigOptionButton<?> optionButton && isIconAppearAnimation()) {
                optionButton.setCurrent(IconAnimation.fromString(String.valueOf(defaultValue)));
            }
            else if (widget instanceof ConfigOptionButton<?> optionButton && isPreviewPanelSideTriangles()) {
                optionButton.setCurrent(PreviewPanelDecorations.SideTriangles.fromString(String.valueOf(defaultValue)));
            }
            else if (widget instanceof ConfigOptionButton<?> optionButton && isPreviewPanelCornerType()) {
                optionButton.setCurrent(PreviewPanelDecorations.CornerType.fromString(String.valueOf(defaultValue)));
            }
            else if (widget instanceof ConfigOptionButton<?> optionButton && isPreviewPanelBackgroundCornerType()) {
                optionButton.setCurrent(PreviewPanelDecorations.BackgroundCornerType.fromString(String.valueOf(defaultValue)));
            }
            else if (widget instanceof ConfigOptionButton<?> optionButton && defaultValue instanceof Enum<?> enumType) {
                optionButton.setCurrent(enumType);
            }
            else if (widget instanceof ConfigOptionButton<?> optionButton) {
                optionButton.setCurrent(optionOf(String.valueOf(defaultValue)));
            }
            else if (widget instanceof PresetColorButton presetButton) {
                presetButton.setValue(String.valueOf(defaultValue));
            }
            else if (widget instanceof FlatButton) {
                field.set(null, defaultValue);
            }
            else if (widget instanceof EditBox box) {
                box.setValue(String.valueOf(defaultValue));
            }

        }
        catch (Exception ignored) {
            ;;
        }

    }

    private static int parseIntSafe(String safe, int fallback) {
        try {
            safe = safe.trim();
            if (safe.startsWith("#")) {
                safe = "0x" + safe.substring(1);
            }

            if (safe.startsWith("0x") || safe.startsWith("0X")) {
                return Integer.decode(safe);
            }

            return Integer.parseInt(safe);
        }
        catch (Exception exception) {
            return fallback;
        }

    }

    private static long parseLongSafe(String safe, long fallback) {
        try {
            safe = safe.trim();
            if (safe.startsWith("#")) {
                safe = "0x" + safe.substring(1);
            }

            if (safe.startsWith("0x") || safe.startsWith("0X")) {
                return Long.decode(safe);
            }

            return Long.parseLong(safe);
        }
        catch (Exception exception) {
            return fallback;
        }

    }

}