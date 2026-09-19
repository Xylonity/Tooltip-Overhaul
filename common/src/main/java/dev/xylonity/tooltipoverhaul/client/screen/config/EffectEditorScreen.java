package dev.xylonity.tooltipoverhaul.client.screen.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.xylonity.tooltipoverhaul.client.frame.FrameTemplates;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCatalog;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectSettings;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.*;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.*;

/**
 * Effect composition screen
 */
public final class EffectEditorScreen extends AbstractFrameSubEditorScreen {

    private static final int BLUE = 0xFF70B7FF;
    private static final String[] SETTINGS = { "speed", "intensity", "density" };
    private static final EffectParameter[] PARAMETERS = EffectParameter.values();

    private final List<String> active = new ArrayList<>();
    private final ConfigSlider[] sliders = new ConfigSlider[SETTINGS.length + PARAMETERS.length];
    private final float[] previous = new float[sliders.length];
    private String selected = EffectCatalog.IDS.get(0);
    private boolean combined;
    private int[] palette;
    private final boolean settings;

    public EffectEditorScreen(Screen parent, JsonObject root, JsonObject entry, ItemStack sample, int accent, Consumer<JsonObject> apply) {
        this(parent, root, entry, sample, accent, apply, true);
    }

    public EffectEditorScreen(Screen parent, JsonObject entry, int accent, Consumer<JsonObject> apply) {
        this(parent, new JsonObject(), entry, ItemStack.EMPTY, accent, apply, false);
    }

    private EffectEditorScreen(Screen parent, JsonObject root, JsonObject entry, ItemStack sample, int accent, Consumer<JsonObject> apply, boolean settings) {
        super(text("effect_editor"), parent, root, entry, sample, accent, apply);
        this.settings = settings;
        final JsonObject resolved = FrameTemplates.resolve(this.root, this.draft);
        String effects = resolved.has("specialEffect") ? resolved.get("specialEffect").getAsString() : TooltipsConfig.EFFECTS;
        for (String id : effects.split("[;,]")) {
            String key = EffectCatalog.canonical(id);
            if (!key.isEmpty() && !key.equals("none") && !active.contains(key)) {
                active.add(key);
            }

        }

        for (String id : active) {
            if (EffectCatalog.IDS.contains(id)) {
                selected = id;
                break;
            }

        }

    }

    @Override
    protected int entryCount() {
        return EffectCatalog.IDS.size();
    }

    @Override
    protected Component subtitle() {
        return text("effect_subtitle");
    }

    @Override
    protected void addToolbarWidgets() {
        int x = previewLeft();
        int width = previewWidth();
        addRenderableWidget(new FlatButton(x + 6, formTop() + 4, width - 12, 18, text(combined ? "effect_combined" : "effect_solo"),
                button -> {
                        syncControls();
                        combined = !combined;
                        rebuildScreen();
        }, accent));
    }

    @Override
    protected void buildControls() {
        clearControls();
        Arrays.fill(sliders, null);

        EffectSettings values = EffectSettings.parse(FrameTemplates.resolve(root, draft)).forEffect(selected, false);
        palette = values.colors().isEmpty() ? EffectCatalog.palette(selected) : values.colors().stream().mapToInt(Integer::intValue).toArray();

        final int x = formLeft() + 8;
        final int width = formWidth() - 22;
        addControl(new FlatButton(x, 0, width - 54, 18, text(active.contains(selected) ? "effect_disable" : "effect_enable"), button -> {
            syncControls();
            if (!active.remove(selected)) {
                active.add(selected);
            }

            saveActive();
            buildControls();
        }, accent, active.contains(selected)), Component.literal(effectName(selected)), "specialEffect");

        FlatButton earlier = new FlatButton(x + width - 50, 0, 23, 18, Component.literal("\u2190"), button -> reorder(-1), accent);
        FlatButton later = new FlatButton(x + width - 23, 0, 23, 18, Component.literal("\u2192"), button -> reorder(1), accent);

        final int index = active.indexOf(selected);

        earlier.active = index > 0;
        later.active = index >= 0 && index < active.size() - 1;

        earlier.setTooltip(Tooltip.create(text("effect_earlier")));
        later.setTooltip(Tooltip.create(text("effect_later")));

        addControl(earlier, text("effect_earlier"), "effectOrderEarlier");
        addControl(later, text("effect_later"), "effectOrderLater");

        if (!settings) {
            return;
        }

        final float[] initial = {values.speed(), values.intensity(), values.density()};
        final float[] maximum = {EffectSettings.MAX_SPEED, 1, EffectSettings.MAX_DENSITY};
        for (int i = 0; i < sliders.length; i++) {
            final EffectParameter parameter = i < SETTINGS.length ? null : PARAMETERS[i - SETTINGS.length];
            if (parameter != null && !parameter.supports(selected)) {
                continue;
            }

            previous[i] = parameter == null ? initial[i] : values.value(parameter);
            sliders[i] = new ConfigSlider(x + width / 2, 0, width / 2, 18, parameter == null ? 0 : parameter.minimum(),
                    parameter == null ? maximum[i] : parameter.maximum(), previous[i], parameter != null && parameter.integer(), accent);

            final String key = settingKey(i);
            sliders[i].setMessage(text("effect_" + key));
            String camelKey = parameter != null ? "effectSettings." + selected + "." + key : switch (i) {
                case 0 -> "effectSpeed";
                case 1 -> "effectIntensity";
                default -> "effectDensity";
            };

            final String descriptionKey = parameter == null
                    ? "tooltipoverhaul.config.frames.field." + camelKey + ".desc"
                    : "tooltipoverhaul.config.frames.effect_" + key + ".desc";

            final int slot = i;
            final float fallback = parameter == null ? 1f : parameter.defaultValue();
            addControl(sliders[i], text("effect_" + key),
                    I18n.exists(descriptionKey) ? I18n.get(descriptionKey) : "",
                    camelKey, () -> {sliders[slot].setValueRaw(fallback);syncControls();
            });

        }

        for (int i = 0; i < 3; i++) {
            final int channel = i;
            final StyledEditBox colorBox = new StyledEditBox(font, x + width / 2, 0, width / 2, 18, Component.empty(), accent);
            colorBox.setValue(formatHex(0xFF000000 | palette[i], false));
            colorBox.setResponder(value -> {
                if (!isHexColor(value)) {
                    return;
                }

                syncControls();

                palette[channel] = 0xFF000000 | ConfigColorParser.parseColor(value) & 0xFFFFFF;
                writePalette();
            });
            final Component label = Component.translatable("tooltipoverhaul.config.frames.effect_color", i + 1);
            addColorControl(colorBox, label, "effectSettings." + selected + ".colors[" + i + "]", () -> 0xFF000000 | palette[channel], (x0, y0) -> {
                syncControls();
                modals.open(new ColorLevelPicker(x0, y0, HEADER_HEIGHT - 2, this.width, height, accent, palette[channel], false, false, argb -> {
                    palette[channel] = 0xFF000000 | argb & 0xFFFFFF;
                    writePalette();
                    colorBox.setValue(formatHex(0xFF000000 | palette[channel], false));
                }));

            }, () -> {

                syncControls();
                palette[channel] = EffectCatalog.palette(selected)[channel];
                writePalette();
                colorBox.setValue(formatHex(0xFF000000 | palette[channel], false));
            });

        }

        addControl(new FlatButton(x, 0, width, 18, text("effect_inherit"), button -> {
            if (draft.has("effectSettings")) {
                draft.getAsJsonObject("effectSettings").remove(selected);
                if (draft.getAsJsonObject("effectSettings").size() == 0) {
                    draft.remove("effectSettings");
                }

            }

            buildControls();
        }, accent), text("effect_inherit"), "effectSettings." + selected);

    }

    private static String settingKey(int index) {
        return index < SETTINGS.length ? SETTINGS[index] : PARAMETERS[index - SETTINGS.length].key();
    }

    private void writePalette() {
        if (Arrays.equals(palette, EffectCatalog.palette(selected))) {
            if (draft.has("effectSettings") && draft.getAsJsonObject("effectSettings").has(selected)) {
                effectValues().remove("colors");
            }

            return;
        }

        final JsonArray colors = new JsonArray();
        for (int color : palette) {
            colors.add(formatHex(0xFF000000 | color, false));
        }

        effectValues().add("colors", colors);
    }

    private JsonObject effectValues() {
        if (!draft.has("effectSettings")) {
            draft.add("effectSettings", new JsonObject());
        }

        final JsonObject settings = draft.getAsJsonObject("effectSettings");
        if (!settings.has(selected)) {
            settings.add(selected, new JsonObject());
        }

        return settings.getAsJsonObject(selected);
    }

    private void saveActive() {
        draft.addProperty("specialEffect", active.isEmpty() ? "none" : String.join(", ", active));
    }

    private void reorder(int delta) {
        syncControls();
        final int from = active.indexOf(selected);
        final int to = from + delta;
        if (from < 0 || to < 0 || to >= active.size()) {
            return;
        }

        active.remove(from);
        active.add(to, selected);
        saveActive();
        buildControls();
    }

    @Override
    protected void syncControls() {
        for (int i = 0; i < sliders.length; i++) {
            if (sliders[i] == null) {
                continue;
            }

            final float value = (float) sliders[i].getValueRaw();
            if (Math.abs(value - previous[i]) < 0.0001f) {
                continue;
            }

            previous[i] = value;
            effectValues().addProperty(settingKey(i), value);
        }

    }

    private String effectName(String id) {
        final String translation = "tooltipoverhaul.effect." + id;
        if (I18n.exists(translation)) {
            return Component.translatable(translation).getString();
        }

        return prettify(id);
    }

    @Override
    protected Component navigationTitle() {
        return Component.translatable("tooltipoverhaul.config.frames.effects_active", active.size());
    }

    @Override
    protected int selectedEntryIndex() {
        return EffectCatalog.IDS.indexOf(selected);
    }

    @Override
    protected Component entryLabel(int index) {
        return Component.literal(effectName(EffectCatalog.IDS.get(index)));
    }

    @Override
    protected int entryLeadingWidth(int index) {
        return 14;
    }

    @Override
    protected int entryTrailingWidth(int index) {
        String id = EffectCatalog.IDS.get(index);
        return active.contains(id) ? font.width(Integer.toString(active.indexOf(id) + 1)) + 3 : 0;
    }

    @Override
    protected void renderEntryLeading(GuiGraphics graphics, int index, int x, int y, int rowHeight, int alpha, boolean selected, boolean hovered) {
        if (active.contains(EffectCatalog.IDS.get(index))) {
            ConfigIconButton.drawDiamond(graphics, x + 5, y + (rowHeight - 2) / 2, withAlpha(BLUE, alpha));
        }
        else {
            graphics.fill(x + 3, y + rowHeight / 2 - 1, x + 6, y + rowHeight / 2 + 2, withAlpha(0x494952, alpha));
        }

    }

    @Override
    protected void renderEntryTrailing(GuiGraphics graphics, int index, int right, int y, int rowHeight, int alpha, boolean selected, boolean hovered) {
        final String id = EffectCatalog.IDS.get(index);
        if (active.contains(id)) {
            final String order = Integer.toString(active.indexOf(id) + 1);
            graphics.drawString(font, order, right - font.width(order), y + (rowHeight - 2 - font.lineHeight) / 2 + 1, withAlpha(0x527BA8, alpha), false);
        }

    }

    @Override
    protected void selectEntry(int index) {
        syncControls();
        selected = EffectCatalog.IDS.get(index);
        buildControls();
    }

    @Override
    protected void configurePreview(JsonObject previewEntry) {
        previewEntry.addProperty("specialEffect", combined ? active.isEmpty() ? "none" : String.join(", ", active) : selected);
    }

}
