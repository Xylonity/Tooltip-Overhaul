package dev.xylonity.tooltipoverhaul.client.screen.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameLoader;
import dev.xylonity.tooltipoverhaul.client.style.vignette.parser.VignetteEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.formatHex;

/**
 * Ordered vignette composition screen
 */
public final class VignetteEditorScreen extends AbstractFrameSubEditorScreen {

    private static final VignetteEntry DEFAULT_ENTRY = new VignetteEntry("circular", "top_left", 0x803B82F6, 0.75f, 0, 0);

    private final List<VignetteEntry> entries = new ArrayList<>();
    private final ConfigSlider[] sliders = new ConfigSlider[3];
    private final float[] previous = new float[3];
    private int selected = -1;
    private boolean combined = true;

    public VignetteEditorScreen(Screen parent, JsonObject root, JsonObject entry, ItemStack sample, int accent, Consumer<JsonObject> apply) {
        super(text("vignette_editor"), parent, root, entry, sample, accent, apply);
        readEntries();
    }

    private void readEntries() {
        entries.clear();
        for (String raw : CustomFrameLoader.parseFrame(root, draft).vignettes()) {
            final List<VignetteEntry> parsed = VignetteEntry.Parser.from(raw);
            if (!raw.isBlank() && parsed.isEmpty()) {
                throw new IllegalArgumentException("Invalid vignette: " + raw);
            }

            entries.addAll(parsed);
        }

        selected = entries.isEmpty() ? -1 : Mth.clamp(selected, 0, entries.size() - 1);
    }

    @Override
    protected int entryCount() {
        return entries.size();
    }

    @Override
    protected void addToolbarWidgets() {
        final int buttonWidth = (listWidth() - 8) / 3;
        final int buttonY = listBottom() + 4;
        addRenderableWidget(new ConfigIconButton(listLeft(), buttonY, buttonWidth, ConfigIconButton.Icon.ADD, text("vignette_add"), button -> {
            commitControls();
            entries.add(DEFAULT_ENTRY);
            selected = entries.size() - 1;
            saveEntries();
            buildControls();
            refreshNavigation();
        }, accent));
        addRenderableWidget(new ConfigIconButton(listLeft() + buttonWidth + 4, buttonY, buttonWidth, ConfigIconButton.Icon.COPY, text("vignette_copy"), button -> {
            if (selected < 0) {
                return;
            }

            commitControls();
            entries.add(selected + 1, entries.get(selected));
            selected++;
            saveEntries();
            buildControls();
            refreshNavigation();
        }, accent));
        addRenderableWidget(new ConfigIconButton(listLeft() + (buttonWidth + 4) * 2, buttonY, buttonWidth, ConfigIconButton.Icon.TRASH, text("vignette_delete"), button -> {
            if (selected < 0) {
                return;
            }

            entries.remove(selected);
            selected = Math.min(selected, entries.size() - 1);
            saveEntries();
            buildControls();
            refreshNavigation();
        }, accent));
        addRenderableWidget(new FlatButton(previewLeft() + 6, formTop() + 4, previewWidth() - 12, 18, text(combined ? "vignette_combined" : "vignette_solo"), button -> {
            commitControls();
            combined = !combined;
            button.setMessage(text(combined ? "vignette_combined" : "vignette_solo"));
        }, accent));

    }

    private ConfigOptionButton<String> choiceButton(int x, int width, List<String> options, String current, Consumer<String> change) {
        return new ConfigOptionButton<>(x, 0, width, 18, options, current, accent, value -> value, value -> text("option." + value).getString(), change, this::commitControls, modals, this.width, height);
    }

    @Override
    protected void buildControls() {
        clearControls();
        Arrays.fill(sliders, null);

        int x = formLeft() + 8;
        final int width = formWidth() - 22;
        final int fieldX = x + width / 2;
        if (selected >= 0) {
            VignetteEntry entry = entries.get(selected);
            addControl(choiceButton(fieldX, width / 2, VignetteEntry.TYPES, entry.type(), value -> {
                commitControls();
                VignetteEntry old = entries.get(selected);
                replace(new VignetteEntry(value, old.position(), old.color(), old.radius(), old.extraPositionX(), old.extraPositionY()));
            }), text("vignette_type"), "vignettes[" + selected + "].type", () -> resetField(0));
            addControl(choiceButton(fieldX, width / 2, VignetteEntry.POSITIONS, entry.position(), value -> {
                commitControls();
                VignetteEntry old = entries.get(selected);
                replace(new VignetteEntry(old.type(), value, old.color(), old.radius(), old.extraPositionX(), old.extraPositionY()));
            }), text("vignette_position"), "vignettes[" + selected + "].position", () -> resetField(1));
            final StyledEditBox colorBox = new StyledEditBox(font, fieldX, 0, width / 2, 18, Component.empty(), accent);
            colorBox.setValue(formatHex(entry.color(), true));
            colorBox.setResponder(value -> {
                if (!isHexColor(value)) {
                    return;
                }

                VignetteEntry old = entries.get(selected);
                replace(new VignetteEntry(old.type(), old.position(), ConfigColorParser.parseColor(value), old.radius(), old.extraPositionX(), old.extraPositionY()));
            });
            addColorControl(colorBox, text("vignette_color"), "vignettes[" + selected + "].color",
                    () -> entries.get(selected).color(), (x0, y0) -> {
                commitControls();
                modals.open(new ColorLevelPicker(x0, y0, ConfigScreenStyle.HEADER_HEIGHT - 2,
                        this.width, height, accent, entries.get(selected).color(), true, true, color -> {
                    VignetteEntry old = entries.get(selected);
                    replace(new VignetteEntry(old.type(), old.position(), color, old.radius(), old.extraPositionX(), old.extraPositionY()));
                    colorBox.setValue(formatHex(color, true));
                }));
            }, () -> resetField(2));

            final float[] values = {entry.radius(), entry.extraPositionX(), entry.extraPositionY()};
            final float[] minimum = {0, -200, -200};
            final float[] maximum = {3, 200, 200};
            for (int i = 0; i < sliders.length; i++) {
                previous[i] = values[i];
                sliders[i] = new ConfigSlider(fieldX, 0, width / 2, 18, Math.min(minimum[i], values[i]), Math.max(maximum[i], values[i]), values[i], i > 0, accent);
                final String[] labels = {"radius", "offset_x", "offset_y"};
                final String[] keys = {"radius", "extraPositionX", "extraPositionY"};
                final int field = 3 + i;
                addControl(sliders[i], text("vignette_" + labels[i]), "vignettes[" + selected + "]." + keys[i], () -> resetField(field));
            }

        }

        addControl(new FlatButton(x, 0, width, 18, text("vignette_inherit"), button -> {
            draft.remove("vignettes");
            readEntries();
            buildControls();
            refreshNavigation();
        }, accent), text("vignette_inherit"), "vignettes");

    }

    private void resetField(int field) {
        if (selected < 0) {
            return;
        }

        commitControls();
        VignetteEntry old = entries.get(selected);
        final VignetteEntry fresh = DEFAULT_ENTRY;
        replace(new VignetteEntry(field == 0 ? fresh.type() : old.type(), field == 1 ? fresh.position() : old.position(),
                field == 2 ? fresh.color() : old.color(), field == 3 ? fresh.radius() : old.radius(),
                field == 4 ? fresh.extraPositionX() : old.extraPositionX(), field == 5 ? fresh.extraPositionY() : old.extraPositionY()));
        buildControls();
        refreshNavigation();
    }

    private void saveEntries() {
        JsonArray array = new JsonArray();
        for (VignetteEntry entry : entries) {
            array.add(entry.serialize());
        }

        draft.add("vignettes", array);
    }

    private void replace(VignetteEntry entry) {
        entries.set(selected, entry);
        saveEntries();
    }

    @Override
    protected void syncControls() {
        if (selected < 0 || sliders[0] == null) {
            return;
        }

        final float radius = (float) sliders[0].getValueRaw();
        final float x = (float) sliders[1].getValueRaw();
        final float y = (float) sliders[2].getValueRaw();
        if (radius == previous[0] && x == previous[1] && y == previous[2]) {
            return;
        }

        previous[0] = radius;
        previous[1] = x;
        previous[2] = y;
        final VignetteEntry old = entries.get(selected);
        replace(new VignetteEntry(old.type(), old.position(), old.color(), radius, Math.round(x), Math.round(y)));
    }

    @Override
    protected int selectedEntryIndex() {
        return selected;
    }

    @Override
    protected Component entryLabel(int index) {
        final VignetteEntry entry = entries.get(index);
        return Component.literal((index + 1) + ". " + text("option." + entry.type()).getString());
    }

    @Override
    protected Component entryDescription(int index) {
        return text("option." + entries.get(index).position());
    }

    @Override
    protected int entryLeadingWidth(int index) {
        return 14;
    }

    @Override
    protected void renderEntryLeading(GuiGraphics graphics, int index, int x, int y, int rowHeight, int alpha, boolean selected, boolean hovered) {
        final int color = entries.get(index).color();
        graphics.fill(x + 1, y + (rowHeight - 10) / 2, x + 11, y + (rowHeight - 10) / 2 + 10, (alpha << 24) | (color & 0x00FFFFFF));
    }

    @Override
    protected void selectEntry(int index) {
        commitControls();
        selected = index;
        buildControls();
    }

    @Override
    protected void configurePreview(JsonObject previewEntry) {
        if (!combined && selected >= 0) {
            final JsonArray array = new JsonArray();
            array.add(entries.get(selected).serialize());
            previewEntry.add("vignettes", array);
        }

    }

}
