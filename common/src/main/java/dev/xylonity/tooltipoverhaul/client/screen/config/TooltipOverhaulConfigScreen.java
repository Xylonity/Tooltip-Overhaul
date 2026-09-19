package dev.xylonity.tooltipoverhaul.client.screen.config;

import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import dev.xylonity.tooltipoverhaul.config.ConfigManager;
import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;
import dev.xylonity.tooltipoverhaul.config.wrapper.AutoConfig;
import dev.xylonity.tooltipoverhaul.config.wrapper.ConfigEntry;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.*;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.rgb;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.colorEntries;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.formatHex;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.withAlpha;

public class TooltipOverhaulConfigScreen extends AbstractConfigScreen {

    private final Class<?> configClass;
    private final AutoConfig meta;

    private ConfigPanel panel;
    private ConfigCategorySidebar sidebar;
    private SearchBox searchBox;

    private ConfigEntryRow pickerRow = null;
    private int pickerPartIndex = -1;

    private final Map<Field, Object> snapshot = new LinkedHashMap<>();

    // Preserved across resizes so the screen rebuilds in the same state
    private String selectedCategory = null;
    private String searchQuery = "";

    public TooltipOverhaulConfigScreen(Screen parent, Class<?> configClass) {
        super(Component.literal(resolveTitle(configClass)), parent, accentFor(configClass));
        this.configClass = configClass;
        this.meta = configClass.getAnnotation(AutoConfig.class);
        takeSnapshot();
    }

    private static int accentFor(Class<?> configClass) {
        final AutoConfig config = configClass.getAnnotation(AutoConfig.class);
        return config != null ? config.accentColor() : DEFAULT_ACCENT;
    }

    private void takeSnapshot() {
        for (Field field : configClass.getDeclaredFields()) {
            if (field.getAnnotation(ConfigEntry.class) == null) {
                continue;
            }

            field.setAccessible(true);
            try {
                snapshot.put(field, field.get(null));
            }
            catch (Exception ignored) {
                ;;
            }

        }

    }

    private void restoreSnapshot() {
        for (Map.Entry<Field, Object> entry : snapshot.entrySet()) {
            try {
                ConfigManager.setPrimitive(entry.getKey(), entry.getValue());
            }
            catch (Exception ignored) {
                ;;
            }

        }

    }

    @Override
    protected void initScreen() {
        final int panelTop = HEADER_HEIGHT + 6;
        final int panelBottom = this.height - 36;

        final List<Field> entryFields = new ArrayList<>();
        final Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        for (Field field : configClass.getDeclaredFields()) {
            final ConfigEntry entry = field.getAnnotation(ConfigEntry.class);
            if (entry == null) {
                continue;
            }

            field.setAccessible(true);
            entryFields.add(field);
            categoryCounts.merge(entry.category().trim(), 1, Integer::sum);
        }

        final boolean hasSidebar = categoryCounts.size() >= 2;
        final int sidebarWidth = hasSidebar ? Mth.clamp(this.width / 5, 84, 120) : 0;
        final int panelLeft = hasSidebar ? 10 + sidebarWidth + 6 : 16;

        this.panel = new ConfigPanel(panelLeft, panelTop, this.width - panelLeft - 10, Math.max(0, panelBottom - panelTop), accent, modals, width, height);
        this.panel.setSwatchClickListener(this::openColorPicker);

        if (hasSidebar) {
            this.sidebar = new ConfigCategorySidebar(10, panelTop, sidebarWidth,
                    Math.max(0, panelBottom - panelTop), accent, categoryCounts, entryFields.size(),
                    key -> {
                        selectedCategory = key;
                        panel.setCategory(key);
                    });

            this.sidebar.setSelectedKey(selectedCategory);
            this.addRenderableWidget(this.sidebar);
        }
        else {
            this.sidebar = null;
            this.selectedCategory = null;
        }

        this.addRenderableWidget(this.panel);

        final int searchWidth = Math.min(200, this.width / 3);
        final int searchX = this.width - searchWidth - 14;
        final int searchY = (HEADER_HEIGHT - 16) / 2 - 1;

        final int framesWidth = this.font.width(I18n.get("tooltipoverhaul.config.custom_frames")) + 16;
        addRenderableWidget(new FlatButton(searchX - framesWidth - 8, searchY, framesWidth, 16, Component.translatable("tooltipoverhaul.config.custom_frames"),
                button -> {
                        panel.applyToFields();
                        minecraft.setScreen(FrameSourceScreen.editorOrSelector(this, accent));
                    },
                accent));

        this.searchBox = new SearchBox(this.font, searchX, searchY, searchWidth, 16, accent);
        this.searchBox.setResponder(q -> {
            searchQuery = q;
            if (this.panel != null) {
                this.panel.setQuery(q);
            }

            if (this.sidebar != null) {
                this.sidebar.setDimmed(!q.isBlank());
            }

        });

        this.addRenderableWidget(this.searchBox);

        for (final Field field : entryFields) {
            this.panel.addEntry(field, field.getAnnotation(ConfigEntry.class), snapshot.get(field));
        }

        this.panel.setCategory(selectedCategory);
        if (!searchQuery.isEmpty()) {
            this.searchBox.setValue(searchQuery);
        }

        addCenteredFooterButtons(90, 8,
                new FooterAction(CommonComponents.GUI_DONE, () -> {
                    panel.applyToFields();
                    ConfigManager.save(configClass);
                    returnToParent();
                }, true, 60),
                new FooterAction(Component.translatable("tooltipoverhaul.config.cancel"), () -> {
                    restoreSnapshot();
                    returnToParent();
                }, false, 120),
                new FooterAction(Component.translatable("tooltipoverhaul.config.reset_all"), panel::resetAllToDefault, false, 180));
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        if (panel != null) {
            panel.applyToFields();
        }

        super.resize(minecraft, width, height);
    }

    @Override
    protected void tickScreen() {
        if (panel != null) {
            panel.tickPanel();
        }

        super.tickScreen();
    }

    @Override
    protected boolean handleKeyPressed(int key, int scancode, int modifiers) {
        if (key == GLFW.GLFW_KEY_F && hasControlDown() && searchBox != null) {
            this.setFocused(searchBox);
            searchBox.setFocused(true);
            return true;
        }

        return super.handleKeyPressed(key, scancode, modifiers);
    }

    private void openColorPicker(ConfigEntryRow row, int partIndex, int x, int y) {
        if (!(row.widget instanceof EditBox box)) {
            return;
        }

        if (partIndex == ConfigEntryRow.CATALOG_HIT) {
            final String current = box.getValue().trim();
            modals.open(new SelectionPopup(this.width, this.height, Component.translatable("tooltipoverhaul.config.frames.frame_catalog"), FrameEditorScreen.frameCatalogOptions(current), current::equals, box::setValue, false, accent));
            playClickSound();
            return;
        }

        int initial = 0xFFFFFFFF;
        final List<String> parts = colorEntries(box.getValue());
        if (partIndex >= 0 && partIndex < parts.size()) {
            initial = ConfigColorParser.parseColor(parts.get(partIndex));
        }

        pickerRow = row;
        pickerPartIndex = partIndex;
        modals.open(new ColorLevelPicker(x, y, HEADER_HEIGHT + 4, this.width, this.height, accent, initial, false, argb -> applyPickedColor(pickerRow, pickerPartIndex, argb)));

        playClickSound();
    }

    private void applyPickedColor(ConfigEntryRow row, int partIndex, int argb) {
        if (row == null || !(row.widget instanceof EditBox box)) {
            return;
        }

        final List<String> parts = colorEntries(box.getValue());
        if (partIndex < 0 || partIndex >= parts.size()) {
            return;
        }

        parts.set(partIndex, formatHex(argb, false));
        box.setValue(String.join(", ", parts));
    }

    @Override
    public void onClose() {
        panel.applyToFields();
        ConfigManager.save(configClass);
        returnToParent();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderScreenShell(graphics, meta != null ? meta.description() : "");
        renderHeaderInfo(graphics);

        super.render(graphics, mouseX, mouseY, partialTick);

        renderModifiedCounter(graphics);

        if (panel != null) {
            panel.renderPendingTooltip(graphics, font, width, height);
        }

        renderModalLayer(graphics, mouseX, mouseY, partialTick);

    }

    private void renderHeaderInfo(GuiGraphics graphics) {
        if (panel == null) {
            return;
        }

        final String text;
        if (!searchQuery.isBlank()) {
            final int results = panel.visibleCount();
            text = I18n.get(results == 1 ? "tooltipoverhaul.config.result" : "tooltipoverhaul.config.results", results);
        }
        else {
            final int total = snapshot.size();
            text = I18n.get(total == 1 ? "tooltipoverhaul.config.option" : "tooltipoverhaul.config.options", total) + (meta != null ? " \u00b7 " + meta.file() + ".toml" : "");
        }

        final int alpha = (int) (0xFF * AnimationUtils.easeOutCubic(Mth.clamp((Util.getMillis() - openedAt - 120) / 240f, 0f, 1f)));
        if (alpha >= 0x10) {
            graphics.drawString(font, text, width - 14 - font.width(text), 34, withAlpha(0x4A4A4A, alpha), false);
        }

    }

    private void renderModifiedCounter(GuiGraphics graphics) {
        if (panel == null) {
            return;
        }

        final int modified = panel.countModified();
        if (modified <= 0) {
            return;
        }

        final String text = I18n.get(modified == 1 ? "tooltipoverhaul.config.change" : "tooltipoverhaul.config.changes", modified);
        final int textX = width - 14 - font.width(text);
        final int textY = height - 22;

        // Accent dot next to the pending changes counter
        final int[] ac = rgb(accent);
        graphics.fill(textX - 8, textY + 2, textX - 4, textY + 6, 0xFF000000 | (ac[0] << 16) | (ac[1] << 8) | ac[2]);

        graphics.drawString(font, text, textX, textY, withAlpha(accent, 0xE0), false);
    }

}
