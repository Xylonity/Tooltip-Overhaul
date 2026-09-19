package dev.xylonity.tooltipoverhaul.client.screen.config;

import com.mojang.blaze3d.platform.NativeImage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameLoader;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameManager;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameSource;
import dev.xylonity.tooltipoverhaul.client.frame.FrameTemplates;
import dev.xylonity.tooltipoverhaul.client.style.preview.PreviewPanelDecorations;
import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.*;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.*;
import static dev.xylonity.tooltipoverhaul.client.screen.config.FrameFieldSchema.*;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.*;

/**
 * Visual editor shared by user, mod and resourcepack custom frame values
 */
public class FrameEditorScreen extends AbstractConfigScreen {

    private static final int HEADER_HEIGHT = ConfigScreenStyle.HEADER_HEIGHT;
    private static final int FOOTER_HEIGHT = 32;
    private static final int MAX_LIST_COLORS = 3;

    private final FrameEditorDocument document;
    private final CustomFrameSource source;
    private final JsonObject root;
    private final List<JsonObject> frameEntries;
    private final List<JsonObject> templateEntries;
    private List<JsonObject> entries;
    private boolean editingTemplates;
    private String searchQuery = "";
    private String validationError = "";
    private int selected = -1;
    private boolean dirty = false;
    private long nextDraftSaveAt;
    private String draftError = "";
    private final boolean loadFailed;

    private final List<FieldRow> rows = new ArrayList<>();
    private final List<FieldRow> visibleRows = new ArrayList<>();
    private double formScroll = 0;
    private @Nullable ConfigNavigationList<JsonObject> entryList;
    private @Nullable SearchBox searchBox;
    private @Nullable EditBox focusedBox = null;

    private final ConfigThreePaneLayout panelLayout = new ConfigThreePaneLayout(10, 8, HEADER_HEIGHT + 6,
            FOOTER_HEIGHT + 26, FOOTER_HEIGHT + 4, 130, 96, 220, 140);

    private boolean draggingListSplitter = false;
    private boolean draggingPreviewSplitter = false;
    private @Nullable ConfigIconButton createEntryButton = null;
    private @Nullable ConfigIconButton cloneEntryButton = null;
    private @Nullable ConfigIconButton deleteEntryButton = null;
    private @Nullable ConfigIconButton libraryEntryButton = null;

    private final FramePreviewPanel previewPanel = new FramePreviewPanel(accent);

    // Scrollbar dragging
    private boolean draggingFormBar = false;
    private double barDragOffset = 0;

    // Field and color index the open picker writes into
    private @Nullable FieldRow pickerRow = null;
    private int pickerPartIndex = -1;

    // Field help tooltip
    private @Nullable FieldRow hoveredRow = null;

    private boolean swatchHintHovered;
    private long hoveredRowSince = 0;
    private int hoverMouseX;
    private int hoverMouseY;

    private long formRevealAt = -1L;
    private long savedFlashAt = 0;

    private static final String[] BOOL_OPTIONS = { "inherit", "true", "false" };

    public FrameEditorScreen(Screen parent, int accent) {
        this(parent, accent, CustomFrameLoader.discoverSources(Minecraft.getInstance().getResourceManager(), TooltipOverhaul.PLATFORM.getConfigPath()).get(0));
    }

    public FrameEditorScreen(Screen parent, int accent, CustomFrameSource source) {
        super(Component.literal("Tooltip Overhaul"), parent, accent);
        this.source = source;
        this.document = new FrameEditorDocument(source);
        this.root = document.root();
        this.frameEntries = document.frames();
        this.templateEntries = document.templates();
        this.entries = frameEntries;
        this.loadFailed = document.loadFailed();
        this.dirty = document.hasDraft();
        if (!entries.isEmpty()) {
            select(0);
        }

    }

    private void save() {
        if (loadFailed) {
            return;
        }

        String error = document.save();
        if (error != null) {
            validationError = error;
            return;
        }

        validationError = "";
        dirty = false;
        draftError = "";
        savedFlashAt = Util.getMillis();
        CustomFrameManager.reset();
        CustomFrameManager.initialize();
    }

    private void syncRoot() {
        document.syncRoot();
    }

    private JsonObject selectedEntry() {
        return selected >= 0 && selected < entries.size() ? entries.get(selected) : null;
    }

    private void extractTemplate() {
        if (editingTemplates || selectedEntry() == null) {
            return;
        }

        applyFocused();
        JsonObject entry = selectedEntry();
        String name = uniqueTemplateName("style");

        final JsonObject style = FrameTemplates.styleOf(entry);
        for (String key : new ArrayList<>(style.keySet())) {
            entry.remove(key);
        }

        style.addProperty("name", name);
        templateEntries.add(style);
        entry.addProperty("extends", name);

        markDirty();
        rebuildRows();
    }

    private String uniqueTemplateName(String base) {
        Set<String> names = new HashSet<>();
        for (JsonObject template : templateEntries) {
            if (template.has("name")) {
                names.add(template.get("name").getAsString());
            }

        }

        String name = base;
        for (int i = 2; names.contains(name); i++) {
            name = base + "_" + i;
        }

        return name;
    }

    private String[] templateOptions(JsonObject entry) {
        final List<String> names = new ArrayList<>(List.of("inherit"));
        for (JsonObject template : templateEntries) {
            if (template != entry && template.has("name")) {
                names.add(template.get("name").getAsString());
            }

        }

        if (entry.has("extends") && !names.contains(entry.get("extends").getAsString())) {
            names.add(entry.get("extends").getAsString());
        }

        return names.toArray(String[]::new);
    }

    private void moveEntry(int target) {
        if (selected < 0 || target == selected || target < 0 || target >= entries.size()) {
            return;
        }

        final JsonObject entry = entries.remove(selected);
        entries.add(target, entry);
        selected = target;
        if (!editingTemplates) {
            for (int i = 0; i < entries.size(); i++) {
                entries.get(i).addProperty("priority", entries.size() - i);
            }

        }

        markDirty();
    }

    private static int identityIndexOf(List<JsonObject> haystack, @Nullable JsonObject needle) {
        if (needle == null) {
            return -1;
        }

        for (int i = 0; i < haystack.size(); i++) {
            if (haystack.get(i) == needle) {
                return i;
            }

        }

        return -1;
    }

    private void refreshEntryList(boolean resetScroll) {
        if (entryList == null) {
            return;
        }

        entryList.setItems(entries);
        entryList.setReorderingEnabled(true);
        entryList.setSelectedIndex(selected);
        if (resetScroll) {
            entryList.resetScroll();
        }

    }

    private void select(int index) {
        selected = Mth.clamp(index, -1, entries.size() - 1);
        formRevealAt = -1L;
        formScroll = 0;
        focusedBox = null;
        modals.clear();
        previewPanel.resetViewAndAnimations();
        if (entryList != null) {
            entryList.setSelectedIndex(selected);
        }

        rebuildRows();
        refreshPreview();
    }

    private void markDirty() {
        dirty = true;
        refreshPreview();
    }

    private void refreshPreview() {
        syncRoot();
        validationError = previewPanel.refresh(root, selectedEntry());
    }

    private ItemStack resolveListStack(JsonObject entry) {
        return previewPanel.stackFor(entry);
    }

    private int listLeft() {
        return panelLayout.left();
    }

    private int listTop() {
        return panelLayout.top();
    }

    private int listBottom() {
        return panelLayout.leftBottom();
    }

    private int listWidth() {
        return panelLayout.leftWidth();
    }

    private int previewWidth() {
        return panelLayout.rightWidth();
    }

    private int previewLeft() {
        return panelLayout.rightLeft();
    }

    private int formLeft() {
        return panelLayout.centerLeft();
    }

    private int formRight() {
        return panelLayout.centerRight();
    }

    private int listSplitterX() {
        return panelLayout.leftSplitter();
    }

    private int previewSplitterX() {
        return panelLayout.rightSplitter();
    }

    private boolean overListSplitter(double mouseX, double mouseY) {
        return panelLayout.overLeftSplitter(mouseX, mouseY);
    }

    private boolean overPreviewSplitter(double mouseX, double mouseY) {
        return panelLayout.overRightSplitter(mouseX, mouseY);
    }

    private void clampPanelWidths() {
        panelLayout.resize(width, height);
    }

    private int formTop() {
        return panelLayout.top();
    }

    private int formBottom() {
        return panelLayout.contentBottom();
    }

    @Override
    protected void initScreen() {
        draggingListSplitter = false;
        draggingPreviewSplitter = false;
        clampPanelWidths();
        rebuildRows();
        entryList = createEntryList();

        addCenteredFooterButtons(92, 8,
                new FooterAction(Component.translatable("tooltipoverhaul.config.frames.save"), () -> {applyFocused();save();}, true),
                new FooterAction(Component.translatable("tooltipoverhaul.config.frames.back"), this::onClose, false)
        );

        final int manageY = listBottom() + 4;
        final int buttonWidth = Math.max(1, (listWidth() - 12) / 4);
        libraryEntryButton = addRenderableWidget(new ConfigIconButton(listLeft() + (buttonWidth + 4) * 3, manageY, buttonWidth, ConfigIconButton.Icon.TEMPLATES,
                Component.translatable("tooltipoverhaul.config.frames.templates_hint"), button -> {
            applyFocused();
            editingTemplates = !editingTemplates;
            entries = editingTemplates ? templateEntries : frameEntries;
            refreshEntryList(true);
            select(entries.isEmpty() ? -1 : 0);
        }, accent));
        libraryEntryButton.setTooltip(Tooltip.create(Component.translatable("tooltipoverhaul.config.frames.templates_hint")));

        createEntryButton = addRenderableWidget(new ConfigIconButton(listLeft(), manageY, buttonWidth, ConfigIconButton.Icon.ADD,
                Component.translatable("tooltipoverhaul.config.frames.create"), button -> {
            JsonObject entry = new JsonObject();
            if (editingTemplates) {
                entry.addProperty("name", uniqueTemplateName("style"));
            }
            else {
                entry.add("items", new JsonArray());
            }

            entries.add(entry);
            dirty = true;
            refreshEntryList(false);
            entryList.reveal();
            select(entries.size() - 1);
        }, accent));

        cloneEntryButton = addRenderableWidget(new ConfigIconButton(listLeft() + buttonWidth + 4, manageY, buttonWidth, ConfigIconButton.Icon.COPY,
                Component.translatable("tooltipoverhaul.config.frames.clone"), button -> {
            JsonObject entry = selectedEntry();
            if (entry != null) {
                final JsonObject copy = entry.deepCopy();
                copy.remove("id");
                if (editingTemplates) {
                    copy.addProperty("name", uniqueTemplateName(entry.get("name").getAsString()));
                }

                entries.add(selected + 1, copy);
                dirty = true;
                refreshEntryList(false);
                entryList.reveal();
                select(selected + 1);
            }

        }, accent));

        deleteEntryButton = addRenderableWidget(new ConfigIconButton(listLeft() + (buttonWidth + 4) * 2, manageY, buttonWidth, ConfigIconButton.Icon.TRASH,
                Component.translatable("tooltipoverhaul.config.frames.delete"), button -> {
            if (selected >= 0 && selected < entries.size()) {
                entries.remove(selected);
                dirty = true;
                refreshEntryList(false);
                select(Math.min(selected, entries.size() - 1));
            }

        }, accent));

        final int searchWidth = Math.min(200, this.width / 3);
        final int searchX = this.width - searchWidth - 14;
        final int searchY = (HEADER_HEIGHT - 16) / 2 - 1;
        searchBox = new SearchBox(font, searchX, searchY, searchWidth, 16, accent);
        searchBox.setResponder(query -> {
            searchQuery = query;
            rebuildVisibleRows();
            formScroll = 0;
            formRevealAt = -1L;
            hoveredRow = null;
        });

        addRenderableWidget(searchBox);

        if (!searchQuery.isEmpty()) {
            searchBox.setValue(searchQuery);
        }

        layoutEntryButtons();

    }

    private ConfigNavigationList<JsonObject> createEntryList() {
        ConfigNavigationList.Adapter<JsonObject> adapter = new ConfigNavigationList.Adapter<>() {

            @Override
            public Component label(JsonObject entry, int index) {
                final int sourceIndex = identityIndexOf(entries, entry);
                return Component.literal(labelFor(entry, sourceIndex >= 0 ? sourceIndex : index));
            }

            @Override
            public int rowHeight(JsonObject entry, int index, int contentWidth) {
                final int textWidth = Math.max(4, contentWidth - 42);
                int lines = Math.max(1, font.split(label(entry, index), textWidth).size());
                return Math.max(22, lines * font.lineHeight + Math.max(0, lines - 1) + 6);
            }

            @Override
            public int leadingWidth(JsonObject entry, int index) {
                return 20;
            }

            @Override
            public int trailingWidth(JsonObject entry, int index) {
                return 14;
            }

            @Override
            public boolean wrapLabel(JsonObject entry, int index) {
                return true;
            }

            @Override
            public void renderLeading(GuiGraphics graphics, JsonObject entry, int index, int x, int y, int rowHeight, int alpha, boolean selected, boolean hovered) {
                previewPanel.renderListIcon(graphics, entry, x, y + (rowHeight - 2 - 16) / 2, alpha / 255f);
            }

            @Override
            public void renderTrailing(GuiGraphics graphics, JsonObject entry, int index, int right, int y, int rowHeight, int alpha, boolean selected, boolean hovered) {
                ConfigIconButton.drawIcon(graphics, ConfigIconButton.Icon.ALIGN, right - ConfigIconButton.Icon.ALIGN.width, y + (rowHeight - ConfigIconButton.Icon.ALIGN.height) / 2, withAlpha(selected ? accent : 0x505050, alpha));
            }

        };
        ConfigNavigationList<JsonObject> list = new ConfigNavigationList<>(listLeft(), listTop(), listWidth(), listBottom() - listTop(), Component.literal(I18n.get("tooltipoverhaul.config.frames.entries").toUpperCase(Locale.ROOT)),
                accent, entries, adapter, index -> {
            applyFocused();
            if (index >= 0 && index < entries.size()) {
                select(index);
            }

        }, (from, target) -> {
            moveEntry(target);
            rebuildRows();
        });

        list.setReorderingEnabled(true);
        list.setSelectedIndex(selected);

        return list;
    }

    private void layoutEntryButtons() {
        if (createEntryButton == null || cloneEntryButton == null || deleteEntryButton == null || libraryEntryButton == null) {
            return;
        }

        final int buttonWidth = Math.max(1, (listWidth() - 12) / 4);
        final int y = listBottom() + 4;
        final ConfigIconButton[] buttons = { createEntryButton, cloneEntryButton, deleteEntryButton, libraryEntryButton };
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setX(listLeft() + (buttonWidth + 4) * i);
            buttons[i].setY(y);
            buttons[i].setWidth(buttonWidth);
        }

    }

    private final class FieldRow extends ConfigFormEntry {

        final @Nullable FieldSpec spec;
        final @Nullable String section;
        final String searchIndex;

        final @Nullable JsonObject entry;
        @Nullable ConfigIconButton replay;

        final List<int[]> swatchHits = new ArrayList<>();

        FieldRow(String section) {
            this.spec = null;
            this.section = section;
            this.entry = null;
            this.searchIndex = normalizeSearch(section + " " + I18n.get("tooltipoverhaul.config.frames.section." + section));
            initialize(Component.empty(), "", null, section, FrameEditorScreen.this.accent, 20);
        }

        FieldRow(FieldSpec spec, JsonObject entry, String containingSection) {
            super(Component.literal(fieldLabel(spec.key())), fieldDescription(spec.key()), null, spec.key(), FrameEditorScreen.this.accent, 32);
            this.spec = spec;
            this.section = null;
            this.entry = entry;
            this.searchIndex = normalizeSearch(spec.key() + " " + fieldLabel(spec.key()) + " "
                    + fieldDescription(spec.key()) + " " + containingSection + " " + I18n.get("tooltipoverhaul.config.frames.section." + containingSection));

            if (spec.key().equals("createTemplate")) {
                this.widget = new FlatButton(0, 0, widgetWidth(), 18, Component.translatable("tooltipoverhaul.config.frames.extract_template"), button -> extractTemplate(), accent);
            }
            else if (spec.key().equals("specialEffect")) {
                this.widget = new FlatButton(0, 0, widgetWidth(), 18, Component.translatable("tooltipoverhaul.config.frames.effect_editor"), button -> {
                    applyFocused();
                    syncRoot();
                    try {
                        minecraft.setScreen(new EffectEditorScreen(FrameEditorScreen.this, root, entry, previewPanel.selectedStack(), accent, draft -> {
                            for (String key : List.of("specialEffect", "effectSettings")) {
                                if (draft.has(key)) {
                                    entry.add(key, draft.get(key).deepCopy());
                                }
                                else {
                                    entry.remove(key);
                                }

                            }

                            markDirty();
                        }));

                    }
                    catch (RuntimeException exception) {
                        validationError = exception.getMessage();
                    }

                }, accent);
            }
            else if (spec.key().equals("vignettes")) {
                this.widget = new FlatButton(0, 0, widgetWidth(), 18, Component.translatable("tooltipoverhaul.config.frames.vignette_editor"), button -> {
                    applyFocused();
                    syncRoot();
                    try {
                        minecraft.setScreen(new VignetteEditorScreen(FrameEditorScreen.this, root, entry, previewPanel.selectedStack(), accent, draft -> {
                            if (draft.has("vignettes")) {
                                entry.add("vignettes", draft.get("vignettes").deepCopy());
                            }
                            else {
                                entry.remove("vignettes");
                            }

                            markDirty();
                        }));

                    }
                    catch (RuntimeException exception) {
                        validationError = exception.getMessage();
                    }

                }, accent);
            }
            else if (spec.kind() == Kind.BOOL || spec.kind() == Kind.CHOICE) {
                final String[] options = spec.key().equals("extends") ? templateOptions(entry) : spec.kind() == Kind.BOOL ? BOOL_OPTIONS : withInherit(spec.options());
                this.widget = new ConfigOptionButton<>(0, 0, widgetWidth(), 18,
                        Arrays.asList(options), readChoice(entry, spec, options), accent,
                        value -> value, FrameEditorScreen::optionLabel,
                        value -> writeChoice(entry, spec, value), () -> { }, modals,
                        FrameEditorScreen.this.width, FrameEditorScreen.this.height);
            }
            else {
                final StyledEditBox box = new StyledEditBox(minecraft.font, 0, 0, widgetWidth(), 18, Component.empty(), accent);
                box.setMaxLength(spec.kind() == Kind.LIST ? 32767 : 1024);
                box.setValue(readText(entry, spec));
                box.setResponder(value -> writeText(entry, spec, value));
                this.widget = box;
            }

            if (spec.key().equals("tooltipAppearAnimation") || spec.key().equals("tooltipDisappearAnimation") || spec.key().equals("iconAppearAnimation")) {
                replay = new ConfigIconButton(0, 0, 18, ConfigIconButton.Icon.PLAY,
                        Component.translatable("tooltipoverhaul.config.frames.replay_animation"), button -> {
                    applyFocused();
                    if (spec.key().equals("iconAppearAnimation")) {
                        previewPanel.replayIconAnimation();
                    }
                    else if (spec.key().equals("tooltipDisappearAnimation")) {
                        previewPanel.replayTooltipDisappearAnimation();
                    }
                    else {
                        previewPanel.replayTooltipAnimation();
                    }

                }, accent);

            }

        }

        @Override
        protected void prepareWidgetWidth(int rowWidth) {
            if (widget instanceof StyledEditBox box) {
                box.setBaseWidth(widgetWidth());
            }
            else if (widget != null) {
                widget.setWidth(widgetWidth());
            }

        }

        @Override
        protected int expandedWidgetWidth(int rowWidth) {
            return rowWidth - 16;
        }

        @Override
        boolean isModified() {
            return spec != null && entry != null && entry.has(spec.key());
        }

        @Override
        protected void renderAccessories(GuiGraphics graphics, int mouseX, int mouseY, int widgetX, int widgetY, int alpha, float textFade, float entrance, float hover) {
            if (replay != null) {
                replay.setX(widgetX - 22);
                replay.setY(widgetY);
                replay.setAlpha(entrance);
                replay.render(graphics, mouseX, mouseY, 0);
            }

            renderRowSwatches(graphics, this, mouseX, mouseY, textFade * entrance);
            renderRowReset(graphics, this, mouseX, mouseY, widgetX, hover, entrance);
        }

        int widgetWidth() {
            final int formW = formRight() - formLeft();
            final int available = Math.max(36, formW - 52);
            return Math.min(available, Math.min(130, Math.max(88, (int) (formW / 3.5f))));
        }

        boolean matchesSearch(String query) {
            for (String term : normalizeSearch(query).split("\\s+")) {
                if (!term.isEmpty() && !searchIndex.contains(term)) {
                    return false;
                }

            }

            return true;
        }

    }

    private static String normalizeSearch(String value) {
        return value.toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private static String fieldDescription(String key) {
        final String descriptionKey = "tooltipoverhaul.config.frames.field." + key + ".desc";
        return I18n.exists(descriptionKey) ? I18n.get(descriptionKey) : "";
    }

    private static String[] withInherit(String[] options) {
        final String[] all = new String[options.length + 1];
        all[0] = "inherit";
        System.arraycopy(options, 0, all, 1, options.length);
        return all;
    }

    static String fieldLabel(String key) {
        final String langKey = "tooltipoverhaul.config.frames.field." + key;
        if (I18n.exists(langKey)) {
            return I18n.get(langKey);
        }

        return prettify(key.replaceAll("([a-z0-9])([A-Z])", "$1 $2").toLowerCase(Locale.ROOT));
    }

    private static String optionLabel(String value) {
        String key = "tooltipoverhaul.config.frames.option." + value;
        return I18n.exists(key) ? I18n.get(key) : prettify(value);
    }

    private String readChoice(JsonObject entry, FieldSpec spec, String[] options) {
        if (!entry.has(spec.key()) || !entry.get(spec.key()).isJsonPrimitive()) {
            return options[0];
        }

        final String raw = spec.key().equals("extends") ? entry.get(spec.key()).getAsString().trim() : entry.get(spec.key()).getAsString().trim().toLowerCase(Locale.ROOT);
        if (spec.key().equals("previewPanelSideTriangles")) {
            return PreviewPanelDecorations.SideTriangles.fromString(raw).name().toLowerCase(Locale.ROOT);
        }

        for (String option : options) {
            if (option.equals(raw)) {
                return option;
            }

        }

        return options[0];
    }

    private void writeChoice(JsonObject entry, FieldSpec spec, String value) {
        if ("inherit".equals(value)) {
            entry.remove(spec.key());
        }
        else if (spec.kind() == Kind.BOOL) {
            entry.addProperty(spec.key(), Boolean.parseBoolean(value));
        }
        else {
            entry.addProperty(spec.key(), value);
        }

        markDirty();
        refreshInheritedText();
    }

    private String readText(JsonObject entry, FieldSpec spec) {
        return FrameFieldDefaults.read(root, entry, spec.key(), resolveListStack(entry));
    }

    private void writeText(JsonObject entry, FieldSpec spec, String raw) {
        final String value = raw.trim();
        if (editingTemplates && spec.key().equals("name") && entry.has("name") && !value.isEmpty()) {
            final String old = entry.get("name").getAsString();
            for (List<JsonObject> group : List.of(frameEntries, templateEntries)) {
                for (JsonObject dependent : group) {
                    if (dependent.has("extends") && dependent.get("extends").getAsString().equals(old)) {
                        dependent.addProperty("extends", value);
                    }

                }

            }

        }

        if (value.isEmpty()) {
            entry.remove(spec.key());
            markDirty();
            return;
        }

        switch (spec.kind()) {
            case LIST, COLOR_LIST -> {
                final JsonArray array = new JsonArray();
                for (String part : value.split(",")) {
                    if (!part.trim().isEmpty()) {
                        array.add(part.trim());
                    }

                }

                entry.add(spec.key(), array);
            }

            case TUPLE_LIST -> {
                final JsonArray array = new JsonArray();
                for (String part : value.split("(?<=\\))\\s*[,;]\\s*")) {
                    if (!part.trim().isEmpty()) {
                        array.add(part.trim());
                    }

                }

                entry.add(spec.key(), array);
            }

            case INT -> {
                try {
                    entry.addProperty(spec.key(), Integer.parseInt(value));
                }
                catch (NumberFormatException ignored) {
                    return;
                }

            }

            case FLOAT -> {
                try {
                    entry.addProperty(spec.key(), Float.parseFloat(value));
                }
                catch (NumberFormatException ignored) {
                    return;
                }

            }

            case COLOR_INT -> {
                if (value.startsWith("#") || value.startsWith("0x") || value.startsWith("0X")) {
                    entry.addProperty(spec.key(), ConfigColorParser.parseColor(value));
                }
                else {
                    return;
                }

            }

            default -> entry.addProperty(spec.key(), raw);
        }

        markDirty();
    }

    private void rebuildRows() {
        syncRoot();
        rows.clear();
        visibleRows.clear();

        final JsonObject entry = selectedEntry();
        if (entry == null) {
            return;
        }

        String currentSection = "";
        for (Object item : SECTIONS_AND_FIELDS) {
            if (item instanceof String section) {
                if (editingTemplates && section.equals("matching")) {
                    continue;
                }

                currentSection = section;
                rows.add(new FieldRow(section));
            }
            else if (item instanceof FieldSpec spec) {
                if (editingTemplates && Set.of("items", "tags", "namespace", "rarity", "priority", "createTemplate").contains(spec.key())) {
                    continue;
                }

                rows.add(new FieldRow(spec, entry, currentSection));
            }

        }

        rebuildVisibleRows();

    }

    private void rebuildVisibleRows() {
        visibleRows.clear();
        if (searchQuery.isBlank()) {
            visibleRows.addAll(rows);
            return;
        }

        FieldRow sectionRow = null;
        boolean sectionMatches = false;
        boolean sectionAdded = false;
        for (FieldRow row : rows) {
            if (row.section != null) {
                sectionRow = row;
                sectionMatches = row.matchesSearch(searchQuery);
                sectionAdded = false;
                continue;
            }

            if (!sectionMatches && !row.matchesSearch(searchQuery)) {
                hideRow(row);
                continue;
            }

            if (sectionRow != null && !sectionAdded) {
                visibleRows.add(sectionRow);
                sectionAdded = true;
            }

            visibleRows.add(row);
        }

        for (FieldRow row : rows) {
            if (!visibleRows.contains(row)) {
                hideRow(row);
            }

        }

    }

    private static void hideRow(FieldRow row) {
        row.swatchHits.clear();
        if (row.widget != null) {
            row.widget.setY(-1000);
            if (row.replay != null) {
                row.replay.setY(-1000);
            }

        }

    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        restoreGuiRenderState(graphics);
        clampPanelWidths();
        layoutEntryButtons();
        final String sourceLabel = source.primary() ? I18n.get("tooltipoverhaul.config.frames.subtitle") : source.location().toString();
        renderScreenShell(graphics, font.plainSubstrByWidth(sourceLabel,
                Math.max(60, (searchBox == null ? width : searchBox.getX()) - 30)));

        renderHeader(graphics);
        renderList(graphics, mouseX, mouseY, partialTick);
        renderForm(graphics, mouseX, mouseY, partialTick);

        restoreGuiRenderState(graphics);
        renderPreview(graphics, mouseX, mouseY);
        restoreGuiRenderState(graphics);
        renderPanelSplitters(graphics, mouseX, mouseY);

        super.render(graphics, mouseX, mouseY, partialTick);

        renderFieldTooltip(graphics);

        renderModalLayer(graphics, mouseX, mouseY, partialTick);

    }

    private void renderPanelSplitters(GuiGraphics graphics, int mouseX, int mouseY) {
        renderPanelSplitter(graphics, listSplitterX(), draggingListSplitter || overListSplitter(mouseX, mouseY));
        renderPanelSplitter(graphics, previewSplitterX(), draggingPreviewSplitter || overPreviewSplitter(mouseX, mouseY));
    }

    private void renderPanelSplitter(GuiGraphics graphics, int x, boolean active) {
        final int color = active ? withAlpha(dimAccent(accent), 0xD0) : 0x302E2E32;
        graphics.fill(x, formTop() + 5, x + 1, formBottom() - 5, color);

        if (active) {
            final int middleY = (formTop() + formBottom()) / 2;
            final ConfigIconButton.Icon icon = ConfigIconButton.Icon.RESIZE_PANEL;
            ConfigIconButton.drawIcon(graphics, icon, x - (icon.width - 1) / 2, middleY - icon.height / 2, color);
        }

    }

    private void renderHeader(GuiGraphics graphics) {
        final int tagY = HEADER_HEIGHT - font.lineHeight + 1;
        if (dirty) {
            final String tag = !draftError.isEmpty() ? I18n.get("tooltipoverhaul.config.frames.draft_error") : I18n.get("tooltipoverhaul.config.frames." + (document.hasDraft() ? "draft_saved" : "unsaved"));
            graphics.drawString(font, tag, this.width - font.width(tag) - 14, tagY, 0xFFFFB040, false);
            return;
        }

        final float fade = savedFlashAt > 0 ? 1f - Mth.clamp((Util.getMillis() - savedFlashAt) / 1600f, 0f, 1f) : 0f;
        if (fade > 0.06f) {
            final String tag = I18n.get("tooltipoverhaul.config.frames.saved");
            graphics.drawString(font, tag, this.width - font.width(tag) - 14, tagY, withAlpha(0x5FCB6A, (int) (0xFF * fade)), false);
        }

    }

    private void renderList(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (entryList == null) {
            return;
        }

        entryList.setBounds(listLeft(), listTop(), listWidth(), listBottom() - listTop());
        entryList.render(graphics, mouseX, mouseY, partialTick);
    }

    private String labelFor(JsonObject entry, int index) {
        if (entry.has("name") && entry.get("name").isJsonPrimitive()) {
            final String name = entry.get("name").getAsString().trim();
            if (!name.isEmpty()) {
                return name;
            }

        }

        return I18n.get("tooltipoverhaul.config.frames.entry", index + 1);
    }

    private void renderForm(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        swatchHintHovered = false;
        final int x0 = formLeft();
        final int y0 = formTop();
        final int x1 = formRight();
        final int y1 = formBottom();

        drawCard(graphics, x0, y0, x1, y1, 0xE6111113, 0xFF232327);

        if (selectedEntry() == null) {
            final String empty = I18n.get(loadFailed ? "tooltipoverhaul.config.frames.load_failed" : "tooltipoverhaul.config.frames.no_selection");
            graphics.drawString(font, empty, x0 + (x1 - x0 - font.width(empty)) / 2, (y0 + y1) / 2 - 4, 0xFF555555, false);
            return;
        }

        if (visibleRows.isEmpty()) {
            final String empty = I18n.get("tooltipoverhaul.config.empty_search", searchQuery);
            graphics.drawString(font, empty, x0 + (x1 - x0 - font.width(empty)) / 2, (y0 + y1) / 2 - 4, 0xFF555555, false);
            return;
        }

        graphics.enableScissor(x0, y0 + 2, x1, y1 - 2);

        final boolean mouseInForm = mouseX >= x0 && mouseX < x1 - 10 && mouseY >= y0 && mouseY < y1;
        FieldRow hoverCandidate = null;
        final long now = Util.getMillis();
        if (formRevealAt < 0L) {
            formRevealAt = now;
        }

        int y = y0 + 6 - (int) formScroll;
        for (int rowIndex = 0; rowIndex < visibleRows.size(); rowIndex++) {
            y += rowGapBefore(rowIndex);

            final FieldRow row = visibleRows.get(rowIndex);

            row.currentY = y;

            final int baseRowLeft = x0 + 6;
            final int baseRowRight = x1 - 8;
            if (row.widget != null) {
                row.recalcHeight(baseRowRight - baseRowLeft);
            }

            if (y + row.height >= y0 && y <= y1) {
                final float entrance = AnimationUtils.easeOutCubic(Mth.clamp((now - formRevealAt - Math.min(rowIndex, 12) * 35L) / 170f, 0f, 1f));
                final int slide = Math.round((1f - entrance) * 16f);
                final int entranceAlpha = Math.round(0xFF * entrance);
                if (row.section != null) {
                    final String label = I18n.get("tooltipoverhaul.config.frames.section." + row.section);
                    renderSectionHeader(graphics, font, label, baseRowLeft + slide, y, baseRowRight - baseRowLeft, accent, entranceAlpha);
                }
                else if (row.spec != null && row.widget != null) {
                    final boolean rowHovered = mouseInForm && mouseX >= baseRowLeft && mouseX < baseRowRight && mouseY >= y && mouseY < y + row.height;
                    row.render(graphics, rowIndex, y, baseRowLeft, baseRowRight - baseRowLeft, mouseX, mouseY, rowHovered, entrance, partialTick);

                    if (rowHovered && mouseX < row.widget.getX() - 20) {
                        hoverCandidate = row;
                    }

                }

            }
            else {
                row.swatchHits.clear();
                if (row.widget != null) {
                    row.widget.setY(-1000);
                    if (row.replay != null) {
                        row.replay.setY(-1000);
                    }

                }

            }

            y += row.height;
        }

        graphics.disableScissor();

        if (hoverCandidate != hoveredRow) {
            hoveredRow = hoverCandidate;
            hoveredRowSince = Util.getMillis();
        }

        if (hoveredRow != null) {
            hoverMouseX = mouseX;
            hoverMouseY = mouseY;
        }

        renderScrollbar(graphics, formBarGeometry(), x1, mouseX, mouseY);
    }

    private void renderRowReset(GuiGraphics graphics, FieldRow row, int mouseX, int mouseY, int widgetX, float hover, float entrance) {
        if (row.spec == null || row.widget == null || hover <= 0.3f || !row.isModified() || entrance <= 0f) {
            return;
        }

        int leftmost = row.replay != null ? widgetX - 22 : widgetX;
        for (int[] hit : row.swatchHits) {
            leftmost = Math.min(leftmost, hit[0]);
        }

        final int resetX = leftmost - 22;
        final int resetY = row.widget.getY() + (row.widget.getHeight() - 18) / 2;
        final boolean hovered = mouseX >= resetX && mouseX < resetX + 18 && mouseY >= resetY && mouseY < resetY + 18;
        final int alpha = (int) (0xFF * entrance * (hover - 0.3f) / 0.7f);
        drawCard(graphics, resetX, resetY, resetX + 18, resetY + 18, withAlpha(hovered ? 0x202024 : 0x1A1A1C, alpha), withAlpha(hovered ? dimAccent(accent) : 0x2E2E32, alpha));
        ConfigIconButton.drawIcon(graphics, ConfigIconButton.Icon.REPEAT, resetX + (18 - ConfigIconButton.Icon.REPEAT.width) / 2,
                resetY + (18 - ConfigIconButton.Icon.REPEAT.height) / 2, hovered ? withAlpha(accent & 0x00FFFFFF, alpha) : withAlpha(0x8A8A8A, alpha));
        row.swatchHits.add(new int[] { resetX, resetY, -4, 18 });
    }

    private void resetRow(FieldRow row) {
        if (row.spec == null || row.entry == null) {
            return;
        }

        if (row.widget instanceof EditBox box && focusedBox == box) {
            box.setFocused(false);
            focusedBox = null;
        }

        row.entry.remove(row.spec.key());
        if (row.widget instanceof ConfigOptionButton<?> options) {
            options.setCurrent("inherit");
        }

        markDirty();
        refreshInheritedText();
    }

    private void renderRowSwatches(GuiGraphics graphics, FieldRow row, int mouseX, int mouseY, float textFade) {
        row.swatchHits.clear();
        if (row.spec == null || row.widget == null || textFade < 0.5f) {
            return;
        }

        final Kind kind = row.spec.kind();

        if ("texture".equals(row.spec.key()) && row.widget instanceof EditBox) {
            final int catalogX = row.widget.getX() - 22;
            final int catalogY = row.widget.getY();
            final boolean hovered = mouseX >= catalogX && mouseX < catalogX + 18 && mouseY >= catalogY && mouseY < catalogY + 18;
            final int iconColor = hovered ? accent : 0xFF808080;

            graphics.fill(catalogX, catalogY, catalogX + 18, catalogY + 18, hovered ? 0xFF26262A : 0xFF1A1A1C);
            drawNotchedBorder(graphics, catalogX, catalogY, catalogX + 18, catalogY + 18, buttonBorder(accent, hovered ? 1f : 0f));
            ConfigIconButton.drawIcon(graphics, ConfigIconButton.Icon.MENU, catalogX + (18 - ConfigIconButton.Icon.MENU.width) / 2, catalogY + (18 - ConfigIconButton.Icon.MENU.height) / 2, iconColor);
            row.swatchHits.add(new int[] { catalogX, catalogY, -3, 18 });
            return;
        }

        // Lists fields to specify multiple matching items in a better way
        if (kind == Kind.LIST && row.widget instanceof EditBox) {
            final int listX = row.widget.getX() - 22;
            final int listY = row.widget.getY();
            final boolean hovered = mouseX >= listX && mouseX < listX + 18 && mouseY >= listY && mouseY < listY + 18;

            graphics.fill(listX, listY, listX + 18, listY + 18, hovered ? 0xFF26262A : 0xFF1A1A1C);
            drawNotchedBorder(graphics, listX, listY, listX + 18, listY + 18, buttonBorder(accent, hovered ? 1f : 0f));
            ConfigIconButton.drawIcon(graphics, ConfigIconButton.Icon.MENU, listX + (18 - ConfigIconButton.Icon.MENU.width) / 2, listY + (18 - ConfigIconButton.Icon.MENU.height) / 2, hovered ? accent : 0xFF808080);

            row.swatchHits.add(new int[] { listX, listY, -2, 18 });

            return;
        }

        if ((kind != Kind.COLOR && kind != Kind.COLOR_INT && kind != Kind.COLOR_LIST) || !(row.widget instanceof EditBox box)) {
            return;
        }

        final List<String> parts = colorParts(box.getValue(), kind);
        int swatchX = row.widget.getX() - 16;
        final int swatchY = row.widget.getY() + 3;

        // Color append
        if ((kind == Kind.COLOR_LIST && parts.size() < MAX_LIST_COLORS) || parts.isEmpty()) {
            final boolean hovered = mouseX >= swatchX && mouseX < swatchX + 12 && mouseY >= swatchY && mouseY < swatchY + 12;
            graphics.fill(swatchX, swatchY, swatchX + 12, swatchY + 12, hovered ? 0xFF26262A : 0xFF1A1A1C);
            drawNotchedBorder(graphics, swatchX, swatchY, swatchX + 12, swatchY + 12, buttonBorder(accent, hovered ? 1f : 0f));
            graphics.drawString(font, "+", swatchX + 4, swatchY + 2, hovered ? accent : 0xFF808080, false);
            row.swatchHits.add(new int[] { swatchX, swatchY, -1, 12 });
            swatchX -= 14;
        }

        for (int i = parts.size() - 1; i >= 0; i--) {
            final boolean hovered = mouseX >= swatchX && mouseX < swatchX + 12 && mouseY >= swatchY && mouseY < swatchY + 12;
            swatchHintHovered |= hovered;
            drawColorSwatch(graphics, swatchX, swatchY, 12, ConfigColorParser.parseColor(parts.get(i)), accent, hovered, 0xFF);
            row.swatchHits.add(new int[] { swatchX, swatchY, i, 12 });
            swatchX -= 14;
        }

    }

    private static List<String> colorParts(String raw, Kind kind) {
        return kind == Kind.COLOR_LIST ? colorTokens(raw) : raw.isBlank() ? new ArrayList<>() : new ArrayList<>(List.of(raw.trim()));
    }

    private int totalFormHeight() {
        int total = 12;
        for (int rowIndex = 0; rowIndex < visibleRows.size(); rowIndex++) {
            final FieldRow row = visibleRows.get(rowIndex);
            if (row.widget != null) {
                row.recalcHeight(Math.max(1, formRight() - formLeft() - 14));
            }

            total += rowGapBefore(rowIndex) + row.height;
        }

        return total;
    }

    private int rowGapBefore(int rowIndex) {
        return rowIndex > 0 && visibleRows.get(rowIndex - 1).section == null ? ConfigFormEntry.ROW_GAP : 0;
    }

    private @Nullable ConfigScroll.Geometry formBarGeometry() {
        return selectedEntry() == null ? null : ConfigScroll.fromContent(formTop(), formBottom(), totalFormHeight(), formScroll);
    }

    private void renderScrollbar(GuiGraphics graphics, @Nullable ConfigScroll.Geometry bar, int rightEdge, int mouseX, int mouseY) {
        ConfigScroll.render(graphics, rightEdge, bar, accent, mouseX, mouseY, draggingFormBar);
    }

    private double maxFormScroll() {
        return Math.max(0, totalFormHeight() - (formBottom() - formTop()));
    }

    private void renderPreview(GuiGraphics graphics, int mouseX, int mouseY) {
        previewPanel.setBounds(previewLeft(), formTop(), previewWidth(), formBottom() - formTop());
        previewPanel.render(graphics, font, width, height, mouseX, mouseY, validationError);
    }

    private void renderFieldTooltip(GuiGraphics graphics) {
        if (hoveredRow == null || hoveredRow.spec == null || modals.isOpen() || previewPanel.isPanning() || draggingFormBar || entryList != null && entryList.isDraggingEntry() || draggingListSplitter || draggingPreviewSplitter) {
            return;
        }

        final int maxWidth = 200;
        if (swatchHintHovered) {
            ConfigTooltipRenderer.render(graphics, font, List.of(new ConfigTooltipRenderer.Section(font.split(Component.translatable("tooltipoverhaul.config.swatch_hint"), maxWidth), 0xFFFFFF)),
                    hoverMouseX, hoverMouseY, width, height, HEADER_HEIGHT + 4, accent, 1f);
            return;
        }

        if (Util.getMillis() - hoveredRowSince < 400) {
            return;
        }

        final String key = hoveredRow.spec.key();
        final List<FormattedCharSequence> lines = new ArrayList<>(font.split(Component.translatable("tooltipoverhaul.config.frames.tooltip.key", key).withStyle(style -> style.withColor(0x70707A)), maxWidth));

        if (hoveredRow.entry != null && hoveredRow.widget instanceof EditBox) {
            if (hoveredRow.isModified()) {
                final JsonObject withoutOverride = hoveredRow.entry.deepCopy();
                withoutOverride.remove(key);
                final String fallback = FrameFieldDefaults.read(root, withoutOverride, key, resolveListStack(hoveredRow.entry));
                lines.addAll(font.split(fallback.isBlank() ? Component.translatable("tooltipoverhaul.config.frames.tooltip.overridden").withStyle(style -> style.withColor(0xC4C4C4))
                        : Component.translatable("tooltipoverhaul.config.frames.tooltip.default", fallback).withStyle(style -> style.withColor(0xC4C4C4)), maxWidth));
            }
            else {
                lines.addAll(font.split(Component.translatable("tooltipoverhaul.config.frames.tooltip.using_default").withStyle(style -> style.withColor(0x8C8C8C)), maxWidth));
            }

        }

        final float fade = AnimationUtils.easeOutQuad(Mth.clamp((Util.getMillis() - hoveredRowSince - 400) / 120f, 0f, 1f));
        ConfigTooltipRenderer.render(graphics, font, List.of(new ConfigTooltipRenderer.Section(lines, 0xFFFFFF)),
                hoverMouseX, hoverMouseY, width, height, HEADER_HEIGHT + 4, accent, fade);
    }

    @Override
    protected boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && overListSplitter(mouseX, mouseY)) {
            applyFocused();
            draggingListSplitter = true;
            hoveredRow = null;
            return true;
        }

        if (button == 0 && overPreviewSplitter(mouseX, mouseY)) {
            applyFocused();
            draggingPreviewSplitter = true;
            hoveredRow = null;
            return true;
        }

        if (previewPanel.contains(mouseX, mouseY)) {
            applyFocused();
            return previewPanel.mouseClicked(mouseX, mouseY, button);
        }

        if (entryList != null && entryList.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Form scrollbar
        final ConfigScroll.Geometry formBar = formBarGeometry();
        if (formBar != null && button == 0 && mouseX >= formRight() - 10 && mouseX < formRight() && mouseY >= formBar.top() && mouseY < formBar.bottom()) {
            if (mouseY >= formBar.thumbTop() && mouseY < formBar.thumbTop() + formBar.thumbHeight()) {
                draggingFormBar = true;
                barDragOffset = mouseY - formBar.thumbTop();
            }
            else {
                formScroll = ConfigScroll.valueFromMouse(mouseY, formBar, maxFormScroll());
            }

            return true;
        }

        // Form
        if (mouseX >= formLeft() && mouseX < formRight() && mouseY >= formTop() && mouseY < formBottom()) {
            for (FieldRow row : visibleRows) {
                for (int[] hit : row.swatchHits) {
                    if (mouseX >= hit[0] && mouseX < hit[0] + hit[3]
                            && mouseY >= hit[1] && mouseY < hit[1] + hit[3] && button == 0) {
                        if (hit[2] == -4) {
                            resetRow(row);
                        }
                        else if (hit[2] == -2) {
                            openListEditor(row, (int) mouseX, (int) mouseY);
                        }
                        else if (hit[2] == -3) {
                            openFrameCatalog(row);
                        }
                        else {
                            openPicker(row, hit[2], (int) mouseX, (int) mouseY);
                        }

                        playClickSound();
                        return true;
                    }

                }

            }

            for (FieldRow row : visibleRows) {
                if (row.widget == null) {
                    continue;
                }

                if (row.replay != null && row.replay.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }

                if (row.widget.mouseClicked(mouseX, mouseY, button)) {
                    if (row.widget instanceof EditBox box) {
                        if (focusedBox != null && focusedBox != box) {
                            focusedBox.setFocused(false);
                        }

                        focusedBox = box;
                        box.setFocused(true);
                    }
                    else {
                        applyFocused();
                    }

                    return true;
                }

            }

            applyFocused();
            return true;
        }

        applyFocused();

        return super.handleMouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected boolean handleMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingListSplitter && button == 0) {
            panelLayout.resizeLeft(mouseX);
            layoutEntryButtons();
            formScroll = Mth.clamp(formScroll, 0, maxFormScroll());
            return true;
        }

        if (draggingPreviewSplitter && button == 0) {
            panelLayout.resizeRight(mouseX);
            previewPanel.setBounds(previewLeft(), formTop(), previewWidth(), formBottom() - formTop());
            previewPanel.clampPan();
            formScroll = Mth.clamp(formScroll, 0, maxFormScroll());
            return true;
        }

        if (entryList != null && entryList.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }

        if (previewPanel.mouseDragged(dragX, dragY)) {
            return true;
        }

        if (draggingFormBar) {
            final ConfigScroll.Geometry bar = formBarGeometry();
            if (bar != null) {
                formScroll = ConfigScroll.valueFromDrag(mouseY, barDragOffset, bar, maxFormScroll());

            }

            return true;
        }

        return super.handleMouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected boolean handleMouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && (draggingListSplitter || draggingPreviewSplitter)) {
            draggingListSplitter = false;
            draggingPreviewSplitter = false;
            return true;
        }

        if (entryList != null && entryList.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }

        previewPanel.mouseReleased();
        draggingFormBar = false;

        return super.handleMouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected boolean handleMouseScrolled(double mouseX, double mouseY, double delta) {
        if (previewPanel.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }

        if (entryList != null && entryList.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }

        if (mouseX >= formLeft() && mouseX < formRight()) {
            formScroll = Mth.clamp(formScroll - delta * 18, 0, maxFormScroll());
            return true;
        }

        return super.handleMouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected boolean handleKeyPressed(int key, int scancode, int modifiers) {
        if (entryList != null && entryList.isDraggingEntry() && key == GLFW.GLFW_KEY_ESCAPE) {
            entryList.cancelDrag();
            return true;
        }

        if (key == GLFW.GLFW_KEY_F && hasControlDown() && searchBox != null) {
            applyFocused();
            setFocused(searchBox);
            searchBox.setFocused(true);
            return true;
        }

        if (hasControlDown() && key == GLFW.GLFW_KEY_S) {
            applyFocused();
            save();
            return true;
        }

        if (hasAltDown() && (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN)) {
            moveEntry(selected + (key == GLFW.GLFW_KEY_UP ? -1 : 1));
            rebuildRows();
            if (entryList != null) {
                entryList.setSelectedIndex(selected);
                entryList.revealSelected();
            }

            return true;
        }

        if (focusedBox != null && focusedBox.keyPressed(key, scancode, modifiers)) {
            return true;
        }

        if (focusedBox != null && focusedBox.isFocused() && key != GLFW.GLFW_KEY_ESCAPE) {
            return true;
        }

        return super.handleKeyPressed(key, scancode, modifiers);
    }

    @Override
    protected boolean handleCharTyped(char chr, int modifiers) {
        if (focusedBox != null && focusedBox.charTyped(chr, modifiers)) {
            return true;
        }

        return super.handleCharTyped(chr, modifiers);
    }

    @Override
    protected void tickBeforeModals() {
        if (entryList != null) {
            entryList.tickDrag();
        }

    }

    @Override
    protected void tickScreen() {
        if (dirty && Util.getMillis() >= nextDraftSaveAt) {
            preserveDraft();
            nextDraftSaveAt = Util.getMillis() + 2000;
        }

        if (searchBox != null) {
            searchBox.tick();
        }

        if (focusedBox != null) {
            focusedBox.tick();
        }

        super.tickScreen();
    }

    private void applyFocused() {
        if (searchBox != null && searchBox.isFocused()) {
            searchBox.setFocused(false);
            if (getFocused() == searchBox) {
                setFocused(null);
            }

        }

        if (focusedBox != null) {
            focusedBox.setFocused(false);
            focusedBox = null;
            refreshInheritedText();
        }

    }

    private void refreshInheritedText() {
        final JsonObject entry = selectedEntry();
        if (entry == null) {
            return;
        }

        for (FieldRow row : rows) {
            if (row.spec == null || entry.has(row.spec.key()) || !(row.widget instanceof EditBox box) || box.isFocused()) {
                continue;
            }

            box.setResponder(null);
            box.setValue(readText(entry, row.spec));
            box.setResponder(value -> writeText(entry, row.spec, value));
        }

    }

    @Override
    public void onClose() {
        applyFocused();
        if (!preserveDraft()) {
            return;
        }

        previewPanel.close();
        returnToParent();
    }

    private boolean preserveDraft() {
        if (!dirty || loadFailed) {
            return true;
        }

        final String error = document.saveDraft();
        draftError = error == null ? "" : error;
        return error == null;
    }

    @Override
    public void removed() {
        applyFocused();
        preserveDraft();
        previewPanel.close();
        super.removed();
    }

    void selectPreviewItem(ItemStack stack) {
        previewPanel.selectStack(stack);
    }

    private void openListEditor(FieldRow row, int anchorX, int anchorY) {
        if (row.spec == null || !(row.widget instanceof EditBox)) {
            return;
        }

        applyFocused();
        modals.clear();
        if (("items".equals(row.spec.key()) || "enchantments".equals(row.spec.key())) && row.widget instanceof EditBox box) {
            final boolean enchantments = "enchantments".equals(row.spec.key());
            final Set<String> values = new LinkedHashSet<>();
            for (String part : box.getValue().split(",")) {
                if (part.isBlank()) {
                    continue;
                }

                ResourceLocation id = ResourceLocation.tryParse(part.trim());
                final String value = id != null ? id.toString() : part.trim();
                if (!values.contains(value)) {
                    values.add(value);
                }

            }

            List<SelectionPopup.Option> options = new ArrayList<>();
            if (enchantments) {
                for (final Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
                    String id = BuiltInRegistries.ENCHANTMENT.getKey(enchantment).toString();
                    ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, 1));
                    options.add(new SelectionPopup.Option(id, Component.translatable(enchantment.getDescriptionId()).getString(), id, book));
                }

            }
            else for (Item item : BuiltInRegistries.ITEM) {
                if (item == Items.AIR) {
                    continue;
                }

                final String id = BuiltInRegistries.ITEM.getKey(item).toString();
                final ItemStack stack = new ItemStack(item);
                String name;
                try {
                    name = stack.getHoverName().getString();
                }
                catch (RuntimeException ignored) {
                    name = id;
                }

                options.add(new SelectionPopup.Option(id, name, id, stack));
            }

            final Set<String> known = new HashSet<>();
            for (final SelectionPopup.Option option : options) known.add(option.value());
            for (String value : values) if (!known.contains(value)) options.add(new SelectionPopup.Option(value, value, value, ItemStack.EMPTY));
            options.sort(Comparator.comparing(SelectionPopup.Option::label, String.CASE_INSENSITIVE_ORDER).thenComparing(SelectionPopup.Option::value));
            modals.open(new SelectionPopup(width, height, Component.translatable("tooltipoverhaul.config.frames." + (enchantments ? "enchantment_catalog" : "item_catalog")),
                    options, values::contains, value -> {
                if (!values.remove(value)) {
                    values.add(value);
                }

                box.setValue(String.join(", ", values));
            }, true, accent));

            return;
        }

        final EditBox box = (EditBox) row.widget;
        modals.open(new StringListEditor(width, height, anchorX, anchorY, accent, Component.literal(fieldLabel(row.spec.key())), box.getValue(), null, box::setValue));
    }

    private void openFrameCatalog(FieldRow row) {
        if (row.spec == null || !(row.widget instanceof EditBox box)) {
            return;
        }

        applyFocused();
        modals.clear();

        final String current = box.getValue().trim();
        modals.open(new SelectionPopup(width, height,
                Component.translatable("tooltipoverhaul.config.frames.frame_catalog"), frameCatalogOptions(current), current::equals, box::setValue, false, accent));
    }

    static List<SelectionPopup.Option> frameCatalogOptions(String current) {
        final List<SelectionPopup.Option> options = new ArrayList<>();
        final Map<ResourceLocation, Resource> textures = Minecraft.getInstance().getResourceManager()
                .listResources("textures/overlay", location -> location.getPath().endsWith(".png"));
        for (Map.Entry<ResourceLocation, Resource> entry : textures.entrySet()) {
            final ResourceLocation location = entry.getKey();
            SelectionPopup.FramePreview preview = null;
            try (InputStream stream = entry.getValue().open(); NativeImage image = NativeImage.read(stream)) {
                if (image.getWidth() >= Constants.getOverlayFrameDimension()
                        && image.getHeight() >= Constants.getOverlayFrameDimension()) {
                    preview = new SelectionPopup.FramePreview(location, image.getWidth(), image.getHeight());
                }

            }
            catch (Exception exception) {
                TooltipOverhaul.LOGGER.warn("Frame editor could not preview {}: {}", location, exception.getMessage());
            }

            String filename = location.getPath().substring(location.getPath().lastIndexOf('/') + 1);
            if (filename.endsWith(".png")) {
                filename = filename.substring(0, filename.length() - 4);
            }

            if (filename.endsWith("_frame")) {
                filename = filename.substring(0, filename.length() - 6);
            }

            options.add(new SelectionPopup.Option(location.toString(), prettify(filename), location.toString(), new ItemStack(Items.DIAMOND_SWORD), preview));
        }

        if (!current.isEmpty() && options.stream().noneMatch(option -> option.value().equals(current))) {
            options.add(new SelectionPopup.Option(current, current, current, ItemStack.EMPTY));
        }

        options.sort(Comparator.comparing(SelectionPopup.Option::label, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(SelectionPopup.Option::value));

        return options;
    }

    private void openPicker(FieldRow row, int partIndex, int anchorX, int anchorY) {
        if (row.spec == null || !(row.widget instanceof EditBox box)) {
            return;
        }

        applyFocused();

        int initial = 0xFFFFFFFF;
        if (partIndex >= 0) {
            final List<String> parts = colorParts(box.getValue(), row.spec.kind());
            if (partIndex < parts.size()) {
                initial = ConfigColorParser.parseColor(parts.get(partIndex));
            }

        }

        pickerRow = row;
        pickerPartIndex = partIndex;
        modals.open(new ColorLevelPicker(anchorX, anchorY, HEADER_HEIGHT + 4, this.width, this.height, accent, initial, row.spec.kind() == Kind.COLOR_INT,
                argb -> pickerPartIndex = applyPickedColor(pickerRow, pickerPartIndex, argb)));
    }

    private int applyPickedColor(FieldRow row, int partIndex, int argb) {
        if (row.spec == null || !(row.widget instanceof EditBox box)) {
            return partIndex;
        }

        final Kind kind = row.spec.kind();
        final String formatted = formatHex(argb, kind == Kind.COLOR_INT);

        if (kind != Kind.COLOR_LIST) {
            box.setValue(formatted);
            return 0;
        }

        final List<String> parts = colorParts(box.getValue(), kind);
        int index = partIndex;
        if (index < 0 || index >= parts.size()) {
            if (parts.size() < MAX_LIST_COLORS) {
                parts.add(formatted);
                index = parts.size() - 1;
            }
            else {
                index = parts.size() - 1;
                parts.set(index, formatted);
            }

        }
        else {
            parts.set(index, formatted);
        }

        box.setValue(String.join(", ", parts));

        return index;
    }

}