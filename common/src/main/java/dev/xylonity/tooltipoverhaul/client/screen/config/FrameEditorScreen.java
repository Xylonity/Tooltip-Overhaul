package dev.xylonity.tooltipoverhaul.client.screen.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameLoader;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameManager;
import dev.xylonity.tooltipoverhaul.client.render.TooltipAnimationState;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.render.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import dev.xylonity.tooltipoverhaul.client.util.PositionUtils;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextAxis;
import dev.xylonity.tooltipoverhaul.client.util.TextUtils;
import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import org.lwjgl.glfw.GLFW;

import org.jetbrains.annotations.Nullable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static dev.xylonity.tooltipoverhaul.client.screen.config.TooltipOverhaulConfigScreen.*;

/**
 * Visual editor for the TO's custom frames json file
 */
public class FrameEditorScreen extends Screen {

    private final Screen parent;
    private final int accent;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final int HEADER_HEIGHT = 40;
    private static final int FOOTER_HEIGHT = 32;
    private static final int LIST_WIDTH = 130;
    private static final int LIST_ROW_HEIGHT = 22;

    private static final int MAX_LIST_COLORS = 3;

    private final Path file;
    private JsonObject root = new JsonObject();
    private final List<JsonObject> entries = new ArrayList<>();
    private int selected = -1;
    private boolean dirty = false;
    private boolean loadFailed = false;

    private final List<FieldRow> rows = new ArrayList<>();
    private double formScroll = 0;
    private double listScroll = 0;
    private @Nullable EditBox focusedBox = null;

    private final List<ItemStack> previewStacks = new ArrayList<>();
    private double panX = 0;
    private double panY = 0;
    private double previewZoom = 1.0;
    private boolean panning = false;
    private @Nullable CustomFrameData previewData = null;
    private boolean previewBroken = false;

    /**
     * Measured footprint of one previewed tooltip
     */
    private record PreviewBounds(
            float width,
            float height,
            float insetLeft
    ) {
        ;;
    }

    // Mosaic layout cache
    private final List<PreviewBounds> previewSizes = new ArrayList<>();
    private float previewContentW = 0;
    private float previewContentH = 0;
    private int previewTotalItems = 0;

    private static final int PREVIEW_CAP = 12;

    // Scrollbar dragging
    private boolean draggingFormBar = false;
    private boolean draggingListBar = false;
    private double barDragOffset = 0;

    private @Nullable ColorLevelPicker picker = null;
    private @Nullable ListEditor listEditor = null;

    // Field and color index the open picker writes into
    private @Nullable FieldRow pickerRow = null;
    private int pickerPartIndex = -1;

    // Field help tooltip
    private @Nullable FieldRow hoveredRow = null;
    private long hoveredRowSince = 0;
    private int hoverMouseX;
    private int hoverMouseY;

    private final long openedAt = Util.getMillis();
    private long selectedAt = Util.getMillis();
    private long savedFlashAt = 0;

    // Items that cannot render due to certain circumstances (like a null level)
    private final Set<Item> brokenRenderItems = new HashSet<>();

    private final List<String> unresolvedIds = new ArrayList<>();

    private static final PreviewBounds PLACEHOLDER_BOUNDS = new PreviewBounds(150, 46, 0);

    private static final String[] BOOL_OPTIONS = { "inherit", "true", "false" };

    private final Minecraft minecraft = Minecraft.getInstance();

    public FrameEditorScreen(Screen parent, int accent) {
        super(Component.literal("Custom Frames"));
        this.parent = parent;
        this.accent = accent;
        this.file = CustomFrameLoader.getUserConfigFile(TooltipOverhaul.PLATFORM.getConfigPath());
        load();
    }

    private void load() {
        entries.clear();
        try {
            if (Files.exists(file)) {
                root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            }
        }
        catch (Exception exception) {
            TooltipOverhaul.LOGGER.error("Frame editor could not read {}: {}", file, exception.getMessage());
            loadFailed = true;
            return;
        }

        if (root.has("frames") && root.get("frames").isJsonArray()) {
            for (JsonElement element : root.getAsJsonArray("frames")) {
                if (element.isJsonObject()) {
                    entries.add(element.getAsJsonObject());
                }

            }

        }

        if (!entries.isEmpty()) {
            select(0);
        }

    }

    private void save() {
        JsonArray array = new JsonArray();
        for (JsonObject entry : entries) {
            array.add(entry);
        }

        root.add("frames", array);

        try {
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
        }
        catch (Exception exception) {
            TooltipOverhaul.LOGGER.error("Frame editor could not write {}: {}", file, exception.getMessage());
            return;
        }

        dirty = false;
        savedFlashAt = Util.getMillis();

        // Hot reload, same thing as /tooltipoverhaul reload
        CustomFrameManager.reset();
        CustomFrameManager.initialize();
    }

    private JsonObject selectedEntry() {
        return selected >= 0 && selected < entries.size() ? entries.get(selected) : null;
    }

    private void select(int index) {
        selected = Mth.clamp(index, -1, entries.size() - 1);
        selectedAt = Util.getMillis();
        formScroll = 0;
        focusedBox = null;
        picker = null;
        listEditor = null;
        panX = 0;
        panY = 0;
        previewZoom = 1.0;
        rebuildRows();
        refreshPreview();
    }

    private void markDirty() {
        dirty = true;
        refreshPreview();
    }

    private void refreshPreview() {
        JsonObject entry = selectedEntry();
        previewBroken = false;
        previewData = null;
        previewStacks.clear();
        if (entry == null) {
            return;
        }

        try {
            previewData = CustomFrameLoader.parseFrame(entry);
        }
        catch (Exception exception) {
            previewBroken = true;
        }

        resolvePreviewStacks(entry);

        previewSizes.clear();
        for (int i = 0; i < previewStacks.size(); i++) {
            previewSizes.add(null);
        }

    }

    private void flagBroken(ItemStack stack, Throwable throwable) {
        if (brokenRenderItems.add(stack.getItem())) {
            TooltipOverhaul.LOGGER.warn("Frame editor cannot preview {} on this screen: {}", BuiltInRegistries.ITEM.getKey(stack.getItem()), throwable.toString());
        }

    }

    private void resolvePreviewStacks(JsonObject entry) {
        previewTotalItems = 0;
        unresolvedIds.clear();
        if (entry.has("items") && entry.get("items").isJsonArray()) {
            for (JsonElement element : entry.getAsJsonArray("items")) {
                if (!element.isJsonPrimitive()) continue;

                final String raw = element.getAsString().trim();
                if (raw.isEmpty()) continue;

                ResourceLocation id = ResourceLocation.tryParse(raw);
                if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
                    unresolvedIds.add(raw);
                    continue;
                }

                previewTotalItems++;
                if (previewStacks.size() < PREVIEW_CAP) {
                    previewStacks.add(new ItemStack(BuiltInRegistries.ITEM.get(id)));
                }

            }

        }

        if (previewStacks.isEmpty()) {
            previewStacks.add(new ItemStack(Items.DIAMOND_SWORD));
        }

    }

    private ItemStack resolveListStack(JsonObject entry) {
        if (entry.has("items") && entry.get("items").isJsonArray()) {
            for (JsonElement element : entry.getAsJsonArray("items")) {
                if (!element.isJsonPrimitive()) {
                    continue;
                }

                ResourceLocation id = ResourceLocation.tryParse(element.getAsString().trim());
                if (id != null && BuiltInRegistries.ITEM.containsKey(id)) {
                    return new ItemStack(BuiltInRegistries.ITEM.get(id));
                }

            }

        }

        return new ItemStack(Items.DIAMOND_SWORD);
    }

    private void drawItemSafe(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (brokenRenderItems.contains(stack.getItem())) {
            graphics.renderItem(new ItemStack(Items.PAPER), x, y);
            return;
        }

        try {
            graphics.renderItem(stack, x, y);
        }
        catch (Throwable throwable) {
            flagBroken(stack, throwable);
        }

    }

    private int listLeft() {
        return 10;
    }

    private int listTop() {
        return HEADER_HEIGHT + 6;
    }

    private int listBottom() {
        return this.height - FOOTER_HEIGHT - 26;
    }

    private int previewWidth() {
        return Mth.clamp((int) (this.width * 0.36f), 200, 300);
    }

    private int previewLeft() {
        return this.width - previewWidth() - 10;
    }

    private int formLeft() {
        return listLeft() + LIST_WIDTH + 8;
    }

    private int formRight() {
        return previewLeft() - 8;
    }

    private int formTop() {
        return HEADER_HEIGHT + 6;
    }

    private int formBottom() {
        return this.height - FOOTER_HEIGHT - 4;
    }

    @Override
    protected void init() {
        picker = null;
        listEditor = null;
        rebuildRows();

        final int buttonHeight = 18;
        final int buttonY = this.height - 26;

        addRenderableWidget(new FlatButton(this.width / 2 - 96, buttonY, 92, buttonHeight,
                Component.translatable("tooltipoverhaul.config.frames.save"),
                button -> { applyFocused(); save(); },
                accent, true));

        addRenderableWidget(new FlatButton(this.width / 2 + 4, buttonY, 92, buttonHeight,
                Component.translatable("tooltipoverhaul.config.frames.back"),
                button -> onClose(),
                accent));

        // Entry management buttons under the list
        final int manageY = listBottom() + 4;
        final int third = (LIST_WIDTH - 8) / 3;

        addRenderableWidget(new FlatButton(listLeft(), manageY, third, 18,
                Component.literal("+"), button -> {
            JsonObject entry = new JsonObject();
            entry.add("items", new JsonArray());
            entries.add(entry);
            dirty = true;
            select(entries.size() - 1);
        }, accent, true));

        addRenderableWidget(new FlatButton(listLeft() + third + 4, manageY, third, 18,
                Component.literal("\u29c9"), button -> {
            JsonObject entry = selectedEntry();
            if (entry != null) {
                entries.add(selected + 1, entry.deepCopy());
                dirty = true;
                select(selected + 1);
            }
        }, accent));

        addRenderableWidget(new FlatButton(listLeft() + (third + 4) * 2, manageY, third, 18,
                Component.literal("\u2212"), button -> {
            if (selected >= 0 && selected < entries.size()) {
                entries.remove(selected);
                dirty = true;
                select(Math.min(selected, entries.size() - 1));
            }
        }, accent));

    }

    private enum Kind {
        STRING,
        INT,
        FLOAT,
        COLOR,
        COLOR_INT,
        LIST,
        TUPLE_LIST,
        COLOR_LIST,
        BOOL,
        CHOICE
    }

    private record FieldSpec(
            String key,
            Kind kind,
            String... options
    ) {
        ;;
    }

    private static final List<Object> SECTIONS_AND_FIELDS = List.of(
            "entry",
            new FieldSpec("name", Kind.STRING),
            "matching",
            new FieldSpec("items", Kind.LIST),
            new FieldSpec("tags", Kind.LIST),
            new FieldSpec("namespace", Kind.STRING),
            new FieldSpec("rarity", Kind.LIST),
            "appearance",
            new FieldSpec("texture", Kind.STRING),
            new FieldSpec("backgroundColor", Kind.COLOR_INT),
            new FieldSpec("borderType", Kind.CHOICE, "gradient", "glint", "static", "auto_gradient", "auto_glint", "auto_static"),
            new FieldSpec("innerFrameCornerType", Kind.CHOICE, "default", "rounded", "bevel", "inner", "cut", "thick", "bracket", "block", "notch", "weld", "gem"),
            new FieldSpec("backgroundCornerType", Kind.CHOICE, "default", "square", "rounded", "notch"),
            new FieldSpec("gradientType", Kind.CHOICE, "common", "uncommon", "rare", "epic", "legendary", "chaos", "custom_rarity", "custom"),
            new FieldSpec("gradientColors", Kind.COLOR_LIST),
            "icon",
            new FieldSpec("disableIcon", Kind.BOOL),
            new FieldSpec("iconBackgroundType", Kind.CHOICE, "focus", "void", "slot", "slot_border", "glow"),
            new FieldSpec("iconAppearAnimation", Kind.CHOICE, "none", "zoom", "rotate", "rotate_fast", "rotate_zoom", "zoom_snap", "skew", "vibration", "tilt_wave", "flip", "pendulum", "bounce", "go_down", "pulse", "fan_in", "hover_pop", "barrel_roll"),
            new FieldSpec("iconSize", Kind.FLOAT),
            new FieldSpec("iconRotatingSpeed", Kind.FLOAT),
            "text",
            new FieldSpec("titleAlignment", Kind.CHOICE, "left", "middle", "right"),
            new FieldSpec("showRating", Kind.BOOL),
            new FieldSpec("itemRating", Kind.STRING),
            new FieldSpec("colorItemRating", Kind.COLOR),
            new FieldSpec("ratingAlignment", Kind.CHOICE, "left", "middle", "right"),
            "layout",
            new FieldSpec("tooltipPositionX", Kind.INT),
            new FieldSpec("tooltipPositionY", Kind.INT),
            new FieldSpec("mainPanelPaddingX", Kind.INT),
            new FieldSpec("mainPanelPaddingY", Kind.INT),
            "divider",
            new FieldSpec("disableDividerLine", Kind.BOOL),
            new FieldSpec("dividerLineType", Kind.CHOICE, "gradient", "static", "linear"),
            new FieldSpec("dividerLineColor", Kind.COLOR),
            new FieldSpec("dividerLineTopPadding", Kind.INT),
            new FieldSpec("dividerLineBottomPadding", Kind.INT),
            "preview",
            new FieldSpec("showSecondPanel", Kind.BOOL),
            new FieldSpec("previewPanelModel", Kind.CHOICE, "armor_stand", "player_skin"),
            new FieldSpec("usePlayerSkinInPreview", Kind.BOOL),
            new FieldSpec("secondPanelX", Kind.INT),
            new FieldSpec("secondPanelY", Kind.INT),
            new FieldSpec("secondPanelSizeX", Kind.INT),
            new FieldSpec("secondPanelSizeY", Kind.INT),
            new FieldSpec("secondPanelRendererSpeed", Kind.FLOAT),
            "effects",
            new FieldSpec("specialEffect", Kind.STRING),
            new FieldSpec("particles", Kind.STRING),
            new FieldSpec("vignettes", Kind.TUPLE_LIST),
            "animation",
            new FieldSpec("tooltipAppearAnimation", Kind.CHOICE, "none", "fade", "pop", "rise", "unfold", "zoom", "slide", "swing", "emerge", "squash", "card", "shake"),
            new FieldSpec("tooltipAnimationDuration", Kind.FLOAT),
            "misc",
            new FieldSpec("showShadow", Kind.BOOL),
            new FieldSpec("disableScrolling", Kind.BOOL),
            new FieldSpec("disableTooltip", Kind.BOOL)
    );

    private final class FieldRow {

        final @Nullable FieldSpec spec;
        final @Nullable String section;
        final @Nullable AbstractWidget widget;
        final int height;

        int currentY = 0;

        final List<int[]> swatchHits = new ArrayList<>();

        FieldRow(String section) {
            this.spec = null;
            this.section = section;
            this.widget = null;
            this.height = 20;
        }

        FieldRow(FieldSpec spec, JsonObject entry) {
            this.spec = spec;
            this.section = null;
            this.height = 24;

            if (spec.kind() == Kind.BOOL || spec.kind() == Kind.CHOICE) {
                final String[] options = spec.kind() == Kind.BOOL ? BOOL_OPTIONS : withInherit(spec.options());
                this.widget = new StringCycleButton(0, 0, widgetWidth(), 18, options, readChoice(entry, spec, options), accent, value -> writeChoice(entry, spec, value));
            }
            else {
                final StyledEditBox box = new StyledEditBox(minecraft.font, 0, 0, widgetWidth(), 18, Component.empty(), accent);
                box.setMaxLength(1024);
                box.setValue(readText(entry, spec));
                box.setResponder(value -> writeText(entry, spec, value));
                this.widget = box;
            }

        }

        int widgetWidth() {
            final int formW = formRight() - formLeft();
            if (spec != null && (spec.kind() == Kind.BOOL || spec.kind() == Kind.CHOICE)) {
                return Math.min(150, Math.max(100, formW / 3));
            }

            return Math.min(220, Math.max(120, (int) (formW * 0.42f)));
        }

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

    private String readChoice(JsonObject entry, FieldSpec spec, String[] options) {
        if (!entry.has(spec.key()) || !entry.get(spec.key()).isJsonPrimitive()) {
            return options[0];
        }

        final String raw = entry.get(spec.key()).getAsString().trim().toLowerCase();
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
    }

    private String readText(JsonObject entry, FieldSpec spec) {
        if (!entry.has(spec.key()) || entry.get(spec.key()).isJsonNull()) {
            return "";
        }

        final JsonElement element = entry.get(spec.key());
        switch (spec.kind()) {
            case LIST, TUPLE_LIST, COLOR_LIST -> {
                if (element.isJsonArray()) {
                    final StringBuilder joined = new StringBuilder();
                    for (JsonElement item : element.getAsJsonArray()) {
                        if (!item.isJsonPrimitive()) {
                            continue;
                        }
                        if (!joined.isEmpty()) {
                            joined.append(", ");
                        }

                        joined.append(item.getAsString());
                    }

                    return joined.toString();
                }

                return element.isJsonPrimitive() ? element.getAsString() : "";
            }
            case COLOR_INT -> {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                    return String.format("#%08X", element.getAsInt());
                }

                return element.isJsonPrimitive() ? element.getAsString() : "";
            }
            default -> {
                return element.isJsonPrimitive() ? element.getAsString() : "";
            }

        }

    }

    private void writeText(JsonObject entry, FieldSpec spec, String raw) {
        final String value = raw.trim();
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
        rows.clear();

        final JsonObject entry = selectedEntry();
        if (entry == null) {
            return;
        }

        for (Object item : SECTIONS_AND_FIELDS) {
            if (item instanceof String section) {
                rows.add(new FieldRow(section));
            }
            else if (item instanceof FieldSpec spec) {
                rows.add(new FieldRow(spec, entry));
            }

        }

    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // blur
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (minecraft != null && minecraft.level == null) {
            renderPanorama(graphics, partialTick);
        }
        renderMenuBackground(graphics);

        renderHeader(graphics);
        renderList(graphics, mouseX, mouseY);
        renderForm(graphics, mouseX, mouseY, partialTick);
        renderPreview(graphics, mouseX, mouseY);

        super.render(graphics, mouseX, mouseY, partialTick);

        renderFieldTooltip(graphics);

        if (listEditor != null) {
            listEditor.render(graphics, mouseX, mouseY);
        }

        if (picker != null) {
            picker.render(graphics, mouseX, mouseY);
        }

    }

    private void renderHeader(GuiGraphics graphics) {
        final float t = AnimationUtils.easeOutCubic(Mth.clamp((Util.getMillis() - openedAt) / 240f, 0f, 1f));

        graphics.fill(0, 0, this.width, HEADER_HEIGHT, 0xE60F0F11);
        graphics.fill(0, HEADER_HEIGHT, this.width, HEADER_HEIGHT + 1, withAlpha(dimAccent(accent), (int) (0xFF * t)));

        final int barH = (int) ((HEADER_HEIGHT - 16) * t);
        graphics.fill(12, (HEADER_HEIGHT - barH) / 2, 15, (HEADER_HEIGHT + barH) / 2, accent);

        graphics.drawString(font, I18n.get("tooltipoverhaul.config.custom_frames"), 23, (HEADER_HEIGHT - font.lineHeight) / 2 - 4, 0xFFFFFFFF, false);
        graphics.drawString(font, I18n.get("tooltipoverhaul.config.frames.subtitle"), 23, (HEADER_HEIGHT - font.lineHeight) / 2 + 6, 0xFF888888, false);

        final int tagY = (HEADER_HEIGHT - font.lineHeight) / 2 + 1;
        if (dirty) {
            final String tag = I18n.get("tooltipoverhaul.config.frames.unsaved");
            graphics.drawString(font, tag, this.width - font.width(tag) - 14, tagY, 0xFFFFB040, false);
        }
        else if (savedFlashAt > 0) {
            // Short confirmation after a save
            final float fade = 1f - Mth.clamp((Util.getMillis() - savedFlashAt) / 1600f, 0f, 1f);
            final int alpha = (int) (0xFF * fade);
            if (alpha >= 0x10) {
                final String tag = I18n.get("tooltipoverhaul.config.frames.saved");
                graphics.drawString(font, tag, this.width - font.width(tag) - 14, tagY, withAlpha(0x5FCB6A, alpha), false);
            }

        }

    }

    private void renderList(GuiGraphics graphics, int mouseX, int mouseY) {
        final int x0 = listLeft();
        final int y0 = listTop();
        final int x1 = x0 + LIST_WIDTH;
        final int y1 = listBottom();

        drawCard(graphics, x0, y0, x1, y1, 0xE6111113, 0xFF232327);

        graphics.enableScissor(x0, y0 + 2, x1, y1 - 2);

        int y = y0 + 4 - (int) listScroll;
        for (int i = 0; i < entries.size(); i++) {
            if (y + LIST_ROW_HEIGHT >= y0 && y <= y1) {
                final boolean isSelected = i == selected;
                final boolean hovered = mouseX >= x0 + 3 && mouseX < x1 - 3 && mouseY >= y && mouseY < y + LIST_ROW_HEIGHT - 2 && mouseY >= y0 && mouseY < y1;

                final int bg = isSelected ? withAlpha(dimAccent(accent), 0x50) : hovered ? 0xFF1B1B1F : 0x00000000;
                if (bg != 0) {
                    graphics.fill(x0 + 3, y, x1 - 3, y + LIST_ROW_HEIGHT - 2, bg);
                }

                if (isSelected) {
                    graphics.fill(x0 + 3, y, x0 + 5, y + LIST_ROW_HEIGHT - 2, accent);
                }

                final JsonObject entry = entries.get(i);
                drawItemSafe(graphics, resolveListStack(entry), x0 + 8, y + 2);

                String label = labelFor(entry, i);
                if (font.width(label) > LIST_WIDTH - 38) {
                    label = font.plainSubstrByWidth(label, LIST_WIDTH - 44) + "\u2026";
                }

                graphics.drawString(font, label, x0 + 28, y + (LIST_ROW_HEIGHT - font.lineHeight) / 2, isSelected ? 0xFFFFFFFF : 0xFFB0B0B0, false);
            }

            y += LIST_ROW_HEIGHT;
        }

        if (entries.isEmpty()) {
            final String empty = I18n.get("tooltipoverhaul.config.empty");
            graphics.drawString(font, empty, x0 + (LIST_WIDTH - font.width(empty)) / 2, (y0 + y1) / 2 - 4, 0xFF555555, false);
        }

        graphics.disableScissor();

        renderScrollbar(graphics, listBarGeometry(), x1);
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

        graphics.enableScissor(x0, y0 + 2, x1, y1 - 2);

        final boolean mouseInForm = mouseX >= x0 && mouseX < x1 - 10 && mouseY >= y0 && mouseY < y1;
        FieldRow hoverCandidate = null;

        int y = y0 + 6 - (int) formScroll;
        for (FieldRow row : rows) {
            row.currentY = y;

            if (y + row.height >= y0 && y <= y1) {
                if (row.section != null) {
                    final String label = I18n.get("tooltipoverhaul.config.frames.section." + row.section);
                    graphics.drawString(font, label, x0 + 10, y + 6, 0xFF585858, false);
                    final int lineX = x0 + 14 + font.width(label);
                    graphics.fill(lineX, y + 9, x1 - 10, y + 10, 0xFF232327);
                }
                else if (row.spec != null && row.widget != null) {
                    float textFade = 1f;
                    int expandShift = 0;
                    if (row.widget instanceof StyledEditBox box) {
                        row.widget.setWidth(box.layoutWidth(x1 - x0 - 22));
                        textFade = 1f - box.expandProgress();
                        expandShift = Math.round(4 * box.expandProgress());
                    }

                    final int labelAlpha = (int) (0xFF * textFade);
                    if (labelAlpha >= 0x10) {
                        graphics.drawString(font, fieldLabel(row.spec.key()), x0 + 10, y + (row.height - font.lineHeight) / 2, withAlpha(0xC4C4C4, labelAlpha), false);
                    }

                    row.widget.setX(x1 - row.widget.getWidth() - 10 - expandShift);
                    row.widget.setY(y + (row.height - 18) / 2);
                    row.widget.render(graphics, mouseX, mouseY, partialTick);

                    renderRowSwatches(graphics, row, mouseX, mouseY, textFade);

                    if (mouseInForm && mouseY >= y && mouseY < y + row.height && mouseX < row.widget.getX() - 20) {
                        hoverCandidate = row;
                    }

                }

            }
            else {
                row.swatchHits.clear();
                if (row.widget != null) {
                    row.widget.setY(-1000);
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

        renderScrollbar(graphics, formBarGeometry(), x1);
    }

    private void renderRowSwatches(GuiGraphics graphics, FieldRow row, int mouseX, int mouseY, float textFade) {
        row.swatchHits.clear();
        if (row.spec == null || row.widget == null || textFade < 0.5f) {
            return;
        }

        final Kind kind = row.spec.kind();

        // List fields to specify multiple matching items in a better way
        if (kind == Kind.LIST && row.widget instanceof EditBox) {
            final int listX = row.widget.getX() - 16;
            final int listY = row.widget.getY() + 3;
            final boolean hovered = mouseX >= listX && mouseX < listX + 12 && mouseY >= listY && mouseY < listY + 12;

            graphics.fill(listX, listY, listX + 12, listY + 12, hovered ? 0xFF26262A : 0xFF1A1A1C);
            drawNotchedBorder(graphics, listX, listY, listX + 12, listY + 12, hovered ? dimAccent(accent) | 0xFF000000 : 0xFF2E2E32);
            for (int line = 0; line < 3; line++) {
                graphics.fill(listX + 3, listY + 3 + line * 3, listX + 9, listY + 4 + line * 3, hovered ? accent : 0xFF808080);
            }

            row.swatchHits.add(new int[] { listX, listY, -2 });
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
            drawNotchedBorder(graphics, swatchX, swatchY, swatchX + 12, swatchY + 12, hovered ? dimAccent(accent) | 0xFF000000 : 0xFF2E2E32);
            graphics.drawString(font, "+", swatchX + 4, swatchY + 2, hovered ? accent : 0xFF808080, false);
            row.swatchHits.add(new int[] { swatchX, swatchY, -1 });
            swatchX -= 14;
        }

        for (int i = parts.size() - 1; i >= 0; i--) {
            final boolean hovered = mouseX >= swatchX && mouseX < swatchX + 12 && mouseY >= swatchY && mouseY < swatchY + 12;
            final int color = ConfigColorParser.parseColor(parts.get(i));

            graphics.fill(swatchX, swatchY, swatchX + 12, swatchY + 12, hovered ? 0xFFAAAAAA : 0xFF2E2E32);
            graphics.fill(swatchX + 1, swatchY + 1, swatchX + 11, swatchY + 11, 0xFF000000 | (color & 0x00FFFFFF));

            row.swatchHits.add(new int[] { swatchX, swatchY, i });
            swatchX -= 14;
        }

    }

    private static List<String> colorParts(String raw, Kind kind) {
        final List<String> parts = new ArrayList<>();
        if (raw.trim().isEmpty()) {
            return parts;
        }

        if (kind == Kind.COLOR_LIST) {
            for (String part : raw.split("[,;]")) {
                if (!part.trim().isEmpty()) {
                    parts.add(part.trim());
                }
            }
        }
        else {
            parts.add(raw.trim());
        }

        return parts;
    }

    private int totalFormHeight() {
        int total = 12;
        for (FieldRow row : rows) {
            total += row.height;
        }

        return total;
    }

    private @Nullable int[] formBarGeometry() {
        final int view = formBottom() - formTop();
        final int content = totalFormHeight();
        if (content <= view || selectedEntry() == null) {
            return null;
        }

        final int thumbH = Math.max(20, view * view / content);
        final int thumbY = formTop() + (int) ((view - thumbH) * (formScroll / (content - view)));
        return new int[] { formTop(), formBottom(), thumbY, thumbH };
    }

    private @Nullable int[] listBarGeometry() {
        final int view = listBottom() - listTop();
        final int content = entries.size() * LIST_ROW_HEIGHT + 8;
        if (content <= view) {
            return null;
        }

        final int thumbH = Math.max(20, view * view / content);
        final int thumbY = listTop() + (int) ((view - thumbH) * (listScroll / (content - view)));
        return new int[] { listTop(), listBottom(), thumbY, thumbH };
    }

    private void renderScrollbar(GuiGraphics graphics, @Nullable int[] bar, int rightEdge) {
        if (bar == null) {
            return;
        }

        graphics.fill(rightEdge - 6, bar[0] + 2, rightEdge - 2, bar[1] - 2, 0x14FFFFFF);
        graphics.fill(rightEdge - 6, bar[2], rightEdge - 2, bar[2] + bar[3], withAlpha(dimAccent(accent), 0xC0));
    }

    private double maxFormScroll() {
        return Math.max(0, totalFormHeight() - (formBottom() - formTop()));
    }

    private double maxListScroll() {
        return Math.max(0, entries.size() * LIST_ROW_HEIGHT + 8 - (listBottom() - listTop()));
    }

    private void renderPreview(GuiGraphics graphics, int mouseX, int mouseY) {
        final int x0 = previewLeft();
        final int y0 = formTop();
        final int x1 = x0 + previewWidth();
        final int y1 = formBottom();

        drawCard(graphics, x0, y0, x1, y1, 0xE60D0D0F, 0xFF232327);

        final String previewLabel = I18n.get("tooltipoverhaul.config.frames.preview");
        graphics.drawString(font, previewLabel, x0 + 8, y0 + 6, 0xFF585858, false);

        final String hint = I18n.get("tooltipoverhaul.config.frames.preview_hint");
        if (font.width(hint) < previewWidth() - 24 - font.width(previewLabel)) {
            graphics.drawString(font, hint, x1 - 8 - font.width(hint), y0 + 6, 0xFF3A3A3E, false);
        }

        if (selectedEntry() == null) {
            return;
        }

        if (previewBroken) {
            final String broken = I18n.get("tooltipoverhaul.config.frames.preview_error");
            graphics.drawString(font, broken, x0 + (x1 - x0 - font.width(broken)) / 2, (y0 + y1) / 2, 0xFFCC5555, false);
            return;
        }

        if (previewStacks.isEmpty()) {
            return;
        }

        graphics.enableScissor(x0 + 1, y0 + 16, x1 - 1, y1 - 1);

        // Item mosaic
        final int count = previewStacks.size();
        final int columns = Math.max(1, (int) Math.ceil(Math.sqrt(count)));
        final int rowCount = (count + columns - 1) / columns;
        final float scale = (float) previewZoom;
        final float gap = 24 * scale;

        final int virtualW = this.width + 4096;
        final int virtualH = this.height + 4096;

        TooltipRenderer.COUNTER = (Util.getMillis() - selectedAt) / 1000f;

        try {
            CustomFrameManager.setPreviewOverride(previewData);
            TooltipAnimationState.setSuppressCapture(true);

            for (int i = 0; i < count && i < previewSizes.size(); i++) {
                if (previewSizes.get(i) == null) {
                    measurePreviewSlot(graphics, i, virtualW, virtualH);
                }

            }

            // Slot sizes in screen space
            final float[] stackW = new float[count];
            final float[] stackH = new float[count];
            final float[] insetX = new float[count];
            for (int i = 0; i < count; i++) {
                final PreviewBounds measured = i < previewSizes.size() ? previewSizes.get(i) : null;
                stackW[i] = (measured != null ? measured.width() : 170) * scale;
                stackH[i] = (measured != null ? measured.height() : 70) * scale;
                insetX[i] = (measured != null ? measured.insetLeft() : 0) * scale;
            }

            final float[] rowW = new float[rowCount];
            final float[] rowH = new float[rowCount];
            for (int i = 0; i < count; i++) {
                final int r = i / columns;
                rowW[r] += stackW[i] + (i % columns == 0 ? 0 : gap);
                rowH[r] = Math.max(rowH[r], stackH[i]);
            }

            float totalW = 0;
            float totalH = 0;
            for (int r = 0; r < rowCount; r++) {
                totalW = Math.max(totalW, rowW[r]);
                totalH += rowH[r] + (r == 0 ? 0 : gap);
            }

            previewContentW = totalW;
            previewContentH = totalH;

            // Mosaic shape wwith random offset values
            final float baseX = x0 + Math.max(18, (previewWidth() - totalW) / 2f) + (float) panX;
            final float baseY = y0 + 16 + Math.max(34, (y1 - y0 - 16 - totalH) / 2f) + (float) panY;

            float rowY = 0;
            for (int r = 0; r < rowCount; r++) {
                float itemX = (totalW - rowW[r]) / 2f + jitter(r, 3) * gap * 0.8f;

                for (int i = r * columns; i < Math.min(count, (r + 1) * columns); i++) {
                    final ItemStack stack = previewStacks.get(i);
                    final float slotX = baseX + itemX + jitter(i, 1) * gap * 0.35f;
                    final float slotY = baseY + rowY + (rowH[r] - stackH[i]) / 2f + jitter(i, 2) * gap * 0.35f;

                    final int targetX = (int) (slotX + insetX[i]);
                    final int targetY = (int) slotY;

                    itemX += stackW[i] + gap;

                    if (slotX >= x1 + 32 || slotX + stackW[i] <= x0 - 32 || targetY >= y1 + 32 || targetY + stackH[i] <= y0 - 64) {
                        continue;
                    }

                    if (brokenRenderItems.contains(stack.getItem())) {
                        renderBrokenSlot(graphics, stack, slotX, slotY, scale);
                        continue;
                    }

                    try {
                        final List<ClientTooltipComponent> components = TextUtils.getTooltipComponentsFrom(stack, font, this.width, 2.2f);
                        final TooltipContext context = new TooltipContext(graphics, font, components, targetX - 12, targetY + 12, virtualW, virtualH, DefaultTooltipPositioner.INSTANCE, stack, true);
                        final TooltipRenderer renderer = new TooltipRenderer(context);

                        renderer.init();

                        if (i < previewSizes.size()) {
                            previewSizes.set(i, measuredBounds(context));
                        }

                        final float offX = PositionUtils.getMainPanelPosition(context, TextAxis.X);
                        final float offY = PositionUtils.getMainPanelPosition(context, TextAxis.Y);
                        final Vec2 actual = context.getTooltipPosition();

                        graphics.pose().pushPose();
                        graphics.pose().translate(targetX + scale * (offX - actual.x), targetY + scale * (offY - actual.y), 0);
                        graphics.pose().scale(scale, scale, 1f);

                        try {
                            renderer.render();
                        }
                        finally {
                            graphics.pose().popPose();
                        }

                    }
                    catch (Throwable throwable) {
                        // Placeholder render when the actual item is not detected
                        if (!stack.is(Items.DIAMOND_SWORD)) {
                            flagBroken(stack, throwable);
                            if (i < previewSizes.size()) {
                                previewSizes.set(i, PLACEHOLDER_BOUNDS);
                            }

                        }
                        else {
                            previewBroken = true;
                            return;
                        }

                    }

                }

                rowY += rowH[r] + gap;
            }

        }
        finally {
            CustomFrameManager.setPreviewOverride(null);
            TooltipAnimationState.setSuppressCapture(false);
            graphics.disableScissor();
        }

        renderPreviewInfo(graphics, x0, y1, mouseX, mouseY);

        if (Math.abs(previewZoom - 1.0) > 0.01) {
            final String zoomLabel = Math.round(previewZoom * 100) + "%";
            graphics.drawString(font, zoomLabel, x1 - 8 - font.width(zoomLabel), y1 - 12, 0xFF585858, false);
        }

    }

    /**
     * Small info badge at the panel's bottom-left corner
     */
    private void renderPreviewInfo(GuiGraphics graphics, int x0, int y1, int mouseX, int mouseY) {
        final int hiddenCount = previewTotalItems - previewStacks.size();
        final boolean hasUnknown = !unresolvedIds.isEmpty();
        if (hiddenCount <= 0 && !hasUnknown) {
            return;
        }

        final int bx = x0 + 8;
        final int by = y1 - 18;
        final boolean hovered = mouseX >= bx && mouseX < bx + 11 && mouseY >= by + 1 && mouseY < by + 12;

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 500);

        final int border = hasUnknown ? 0xFFCC8040 : hovered ? withAlpha(dimAccent(accent), 0xFF) : 0xFF2E2E32;
        drawCard(graphics, bx, by + 1, bx + 11, by + 12, hovered ? 0xFF202024 : 0xE6161618, border);
        graphics.drawString(font, "i", bx + 5, by + 3, hasUnknown ? 0xFFCC8040 : 0xFFB0B0B0, false);

        if (hovered) {
            final List<FormattedCharSequence> lines = new ArrayList<>();
            final int maxWidth = Math.min(240, previewWidth() - 24);
            if (hiddenCount > 0) {
                lines.addAll(font.split(Component.translatable("tooltipoverhaul.config.frames.preview_more", hiddenCount), maxWidth));
            }
            if (hasUnknown) {
                lines.addAll(font.split(Component.translatable("tooltipoverhaul.config.frames.preview_unknown", String.join(", ", unresolvedIds)), maxWidth));
            }

            int textW = 0;
            for (FormattedCharSequence line : lines) {
                textW = Math.max(textW, font.width(line));
            }

            final int boxW = textW + 12;
            final int boxH = lines.size() * (font.lineHeight + 1) - 1 + 10;
            int px = bx;
            final int py = by - boxH - 4;
            if (px + boxW > this.width - 4) {
                px = this.width - 4 - boxW;
            }

            drawCard(graphics, px, py, px + boxW, py + boxH, 0xF2121214, withAlpha(mixRgb(0x2E2E32, dimAccent(accent), 0.4f), 0xFF));

            int lineY = py + 6;
            for (FormattedCharSequence line : lines) {
                graphics.drawString(font, line, px + 6, lineY, 0xFFDDDDDD, false);
                lineY += font.lineHeight + 1;
            }

        }

        graphics.pose().popPose();
    }

    private void measurePreviewSlot(GuiGraphics graphics, int index, int virtualW, int virtualH) {
        final ItemStack stack = previewStacks.get(index);
        if (brokenRenderItems.contains(stack.getItem())) {
            previewSizes.set(index, PLACEHOLDER_BOUNDS);
            return;
        }

        try {
            final List<ClientTooltipComponent> components = TextUtils.getTooltipComponentsFrom(stack, font, this.width, 2.2f);
            final TooltipContext context = new TooltipContext(graphics, font, components, 100, 100, virtualW, virtualH, DefaultTooltipPositioner.INSTANCE, stack, true);
            new TooltipRenderer(context).init();
            previewSizes.set(index, measuredBounds(context));
        }
        catch (Throwable throwable) {
            flagBroken(stack, throwable);
            previewSizes.set(index, PLACEHOLDER_BOUNDS);
        }

    }

    private static PreviewBounds measuredBounds(TooltipContext context) {
        final Vec2 size = context.getTooltipSize();

        float insetLeft = 0;
        float panelHeight = 0;
        if (RenderUtils.hasPreviewOfTieredItem(context) || RenderUtils.hasPreviewOfArmorItem(context)) {
            insetLeft = RenderUtils.calculateSecondPanelSize(context, TextAxis.X) + 30;
            panelHeight = RenderUtils.calculateSecondPanelSize(context, TextAxis.Y) + 4;
        }

        return new PreviewBounds(size.x + insetLeft, Math.max(size.y, panelHeight), insetLeft);
    }

    /**
     * Placeholder card for items whose tooltip refuses to load
     */
    private void renderBrokenSlot(GuiGraphics graphics, ItemStack stack, float slotX, float slotY, float scale) {
        graphics.pose().pushPose();
        graphics.pose().translate(slotX, slotY, 0);
        graphics.pose().scale(scale, scale, 1f);

        final int w = (int) PLACEHOLDER_BOUNDS.width();
        drawCard(graphics, 0, 0, w, (int) PLACEHOLDER_BOUNDS.height(), 0xE6151517, 0xFF2E2E32);

        final String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        graphics.drawString(font, font.plainSubstrByWidth(id, w - 16), 8, 11, 0xFFB0B0B0, false);

        final String reason = I18n.get("tooltipoverhaul.config.frames.preview_unrenderable");
        graphics.drawString(font, font.plainSubstrByWidth(reason, w - 16), 8, 27, 0xFF8A5A45, false);

        graphics.pose().popPose();
    }

    private static float jitter(int index, int salt) {
        int h = index * 374761393 + salt * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        return (((h ^ (h >>> 16)) & 0xFFFF) / 65535f) * 2f - 1f;
    }

    private boolean inPreviewPanel(double mouseX, double mouseY) {
        return mouseX >= previewLeft() && mouseX < previewLeft() + previewWidth() && mouseY >= formTop() && mouseY < formBottom();
    }

    private void clampPan() {
        final double limitX = Math.max(0, (previewContentW + previewWidth()) / 2.0 - 40);
        final double limitY = Math.max(0, (previewContentH + (formBottom() - formTop())) / 2.0 - 40);

        panX = Mth.clamp(panX, -limitX, limitX);
        panY = Mth.clamp(panY, -limitY, limitY);
    }

    private void renderFieldTooltip(GuiGraphics graphics) {
        if (hoveredRow == null || hoveredRow.spec == null || picker != null || listEditor != null || panning || draggingFormBar || draggingListBar) {
            return;
        }

        if (Util.getMillis() - hoveredRowSince < 400) {
            return;
        }

        final String key = hoveredRow.spec.key();
        final List<FormattedCharSequence> lines = new ArrayList<>();
        final int maxWidth = 200;

        final String descKey = "tooltipoverhaul.config.frames.field." + key + ".desc";
        if (I18n.exists(descKey)) {
            lines.addAll(font.split(Component.literal(I18n.get(descKey)), maxWidth));
        }

        final List<FormattedCharSequence> footer = new ArrayList<>();
        if (!"name".equals(key)) {
            footer.addAll(font.split(Component.translatable("tooltipoverhaul.config.frames.tooltip.inherit").withStyle(style -> style.withColor(0x7A7A7A)), maxWidth));
        }

        footer.addAll(font.split(Component.translatable("tooltipoverhaul.config.frames.tooltip.key", key).withStyle(style -> style.withColor(0x50505A)), maxWidth));

        if (lines.isEmpty() && footer.isEmpty()) {
            return;
        }

        int textWidth = 0;
        for (FormattedCharSequence line : lines) {
            textWidth = Math.max(textWidth, font.width(line));
        }
        for (FormattedCharSequence line : footer) {
            textWidth = Math.max(textWidth, font.width(line));
        }

        final int paddingX = 6;
        final int paddingY = 5;
        final int gap = lines.isEmpty() || footer.isEmpty() ? 0 : 3;
        final int boxWidth = textWidth + paddingX * 2;
        final int boxHeight = (lines.size() + footer.size()) * (font.lineHeight + 1) - 1 + gap + paddingY * 2;

        int x = hoverMouseX + 10;
        int y = hoverMouseY - boxHeight - 4;
        if (x + boxWidth > this.width - 4) {
            x = this.width - 4 - boxWidth;
        }
        if (y < HEADER_HEIGHT + 4) {
            y = hoverMouseY + 12;
        }
        if (y + boxHeight > this.height - 4) {
            y = this.height - 4 - boxHeight;
        }

        final float fade = AnimationUtils.easeOutQuad(Mth.clamp((Util.getMillis() - hoveredRowSince - 400) / 120f, 0f, 1f));
        final int alpha = (int) (0xFF * fade);
        if (alpha < 0x10) {
            return;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0, (1f - fade) * 3f, 400);

        drawCard(graphics, x, y, x + boxWidth, y + boxHeight, ((int) (0xF2 * fade) << 24) | 0x121214, withAlpha(mixRgb(0x2E2E32, dimAccent(accent), 0.4f), alpha));

        int lineY = y + paddingY;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, x + paddingX, lineY, withAlpha(0xDDDDDD, alpha), false);
            lineY += font.lineHeight + 1;
        }

        lineY += gap;
        for (FormattedCharSequence line : footer) {
            graphics.drawString(font, line, x + paddingX, lineY, withAlpha(0xFFFFFF, alpha), false);
            lineY += font.lineHeight + 1;
        }

        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (picker != null) {
            if (!picker.mouseClicked(mouseX, mouseY, button)) {
                picker = null;
            }

            return true;
        }

        if (listEditor != null) {
            if (!listEditor.mouseClicked(mouseX, mouseY, button)) {
                listEditor = null;
            }

            return true;
        }

        if (inPreviewPanel(mouseX, mouseY)) {
            applyFocused();
            if (button == 1) {
                panX = 0;
                panY = 0;
                previewZoom = 1.0;
                return true;
            }
            if (button == 0 && mouseY >= formTop() + 16) {
                panning = true;
                return true;
            }

            return true;
        }

        // Entry list scrollbar
        final int[] listBar = listBarGeometry();
        if (listBar != null && button == 0 && mouseX >= listLeft() + LIST_WIDTH - 8 && mouseX < listLeft() + LIST_WIDTH && mouseY >= listBar[0] && mouseY < listBar[1]) {
            if (mouseY >= listBar[2] && mouseY < listBar[2] + listBar[3]) {
                draggingListBar = true;
                barDragOffset = mouseY - listBar[2];
            }
            else {
                listScroll = scrollFromBar(mouseY, listBar, maxListScroll());
            }

            return true;
        }

        // List selection
        if (mouseX >= listLeft() && mouseX < listLeft() + LIST_WIDTH - (listBar != null ? 8 : 0) && mouseY >= listTop() && mouseY < listBottom()) {
            final int index = (int) ((mouseY - listTop() - 4 + listScroll) / LIST_ROW_HEIGHT);
            if (index >= 0 && index < entries.size() && button == 0) {
                applyFocused();
                select(index);
                playClickSound();
                return true;
            }

            return true;
        }

        // Form scrollbar
        final int[] formBar = formBarGeometry();
        if (formBar != null && button == 0 && mouseX >= formRight() - 10 && mouseX < formRight() && mouseY >= formBar[0] && mouseY < formBar[1]) {
            if (mouseY >= formBar[2] && mouseY < formBar[2] + formBar[3]) {
                draggingFormBar = true;
                barDragOffset = mouseY - formBar[2];
            }
            else {
                formScroll = scrollFromBar(mouseY, formBar, maxFormScroll());
            }

            return true;
        }

        // Form
        if (mouseX >= formLeft() && mouseX < formRight() && mouseY >= formTop() && mouseY < formBottom()) {
            for (FieldRow row : rows) {
                for (int[] hit : row.swatchHits) {
                    if (mouseX >= hit[0] && mouseX < hit[0] + 12 && mouseY >= hit[1] && mouseY < hit[1] + 12 && button == 0) {
                        if (hit[2] == -2) {
                            openListEditor(row, (int) mouseX, (int) mouseY);
                        }
                        else {
                            openPicker(row, hit[2], (int) mouseX, (int) mouseY);
                        }

                        playClickSound();
                        return true;
                    }

                }

            }

            for (FieldRow row : rows) {
                if (row.widget == null) continue;

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
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static double scrollFromBar(double mouseY, int[] bar, double maxScroll) {
        final int span = (bar[1] - bar[0]) - bar[3];
        if (span <= 0) {
            return 0;
        }

        final double t = (mouseY - bar[0] - bar[3] / 2.0) / span;
        return Mth.clamp(t, 0, 1) * maxScroll;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (picker != null) {
            return picker.mouseDragged(mouseX, mouseY);
        }

        if (panning) {
            panX += dragX;
            panY += dragY;
            clampPan();
            return true;
        }

        if (draggingFormBar) {
            final int[] bar = formBarGeometry();
            if (bar != null) {
                final int span = (bar[1] - bar[0]) - bar[3];
                if (span > 0) {
                    formScroll = Mth.clamp((mouseY - barDragOffset - bar[0]) / span, 0, 1) * maxFormScroll();
                }

            }

            return true;
        }

        if (draggingListBar) {
            final int[] bar = listBarGeometry();
            if (bar != null) {
                final int span = (bar[1] - bar[0]) - bar[3];
                if (span > 0) {
                    listScroll = Mth.clamp((mouseY - barDragOffset - bar[0]) / span, 0, 1) * maxListScroll();
                }

            }

            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (picker != null) {
            picker.mouseReleased();
        }

        panning = false;
        draggingFormBar = false;
        draggingListBar = false;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double delta) {
        if (picker != null) {
            return true;
        }

        if (listEditor != null) {
            listEditor.mouseScrolled(delta);
            return true;
        }

        // Scrolling over the preview zooms the mosaic
        if (inPreviewPanel(mouseX, mouseY)) {
            final double zoom = Mth.clamp(previewZoom * (delta > 0 ? 1.15 : 1 / 1.15), 0.4, 2.0);
            if (zoom != previewZoom) {
                panX *= zoom / previewZoom;
                panY *= zoom / previewZoom;
                previewZoom = zoom;
                clampPan();
            }

            return true;
        }

        if (mouseX >= listLeft() && mouseX < listLeft() + LIST_WIDTH) {
            listScroll = Mth.clamp(listScroll - delta * 18, 0, maxListScroll());
            return true;
        }

        if (mouseX >= formLeft() && mouseX < formRight()) {
            formScroll = Mth.clamp(formScroll - delta * 18, 0, maxFormScroll());
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, deltaX, delta);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (picker != null) {
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                picker = null;
                return true;
            }
            if (picker.keyPressed(key, scancode, modifiers)) {
                return true;
            }

        }

        if (listEditor != null && listEditor.keyPressed(key, scancode, modifiers)) {
            return true;
        }

        if (focusedBox != null && focusedBox.keyPressed(key, scancode, modifiers)) {
            return true;
        }

        if (focusedBox != null && focusedBox.isFocused() && key != GLFW.GLFW_KEY_ESCAPE) {
            return true;
        }

        return super.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (picker != null) {
            return picker.charTyped(chr, modifiers);
        }

        if (listEditor != null) {
            return listEditor.charTyped(chr, modifiers);
        }

        if (focusedBox != null && focusedBox.charTyped(chr, modifiers)) {
            return true;
        }

        return super.charTyped(chr, modifiers);
    }

    @Override
    public void tick() {
        if (picker != null) {
            picker.tick();
        }

        if (listEditor != null) {
            listEditor.tick();
        }

        super.tick();
    }

    private void applyFocused() {
        if (focusedBox != null) {
            focusedBox.setFocused(false);
            focusedBox = null;
        }

    }

    @Override
    public void onClose() {
        CustomFrameManager.setPreviewOverride(null);
        TooltipAnimationState.clear();
        minecraft.setScreen(parent);
    }

    private void openListEditor(FieldRow row, int anchorX, int anchorY) {
        if (row.spec == null || !(row.widget instanceof EditBox)) {
            return;
        }

        applyFocused();
        picker = null;
        listEditor = new ListEditor(row, anchorX, anchorY);
    }

    private void openPicker(FieldRow row, int partIndex, int anchorX, int anchorY) {
        if (row.spec == null || !(row.widget instanceof EditBox box)) {
            return;
        }

        applyFocused();
        listEditor = null;

        int initial = 0xFFFFFFFF;
        if (partIndex >= 0) {
            final List<String> parts = colorParts(box.getValue(), row.spec.kind());
            if (partIndex < parts.size()) {
                initial = ConfigColorParser.parseColor(parts.get(partIndex));
            }

        }

        pickerRow = row;
        pickerPartIndex = partIndex;
        picker = new ColorLevelPicker(anchorX, anchorY, HEADER_HEIGHT + 4, this.width, this.height, accent, initial, row.spec.kind() == Kind.COLOR_INT,
                argb -> pickerPartIndex = applyPickedColor(pickerRow, pickerPartIndex, argb));
    }

    private int applyPickedColor(FieldRow row, int partIndex, int argb) {
        if (row.spec == null || !(row.widget instanceof EditBox box)) {
            return partIndex;
        }

        final Kind kind = row.spec.kind();
        final String formatted = kind == Kind.COLOR_INT || (argb >>> 24) != 0xFF
                ? String.format("#%08X", argb)
                : String.format("#%06X", argb & 0x00FFFFFF);

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

    private final class ListEditor {

        private static final int ROW_H = 18;
        private static final int PAD = 8;
        private static final int MAX_VISIBLE = 8;

        private final FieldRow row;
        private final boolean itemsField;
        private final List<String> values = new ArrayList<>();
        private final StyledEditBox addBox;
        private final int x;
        private final int anchorY;
        private final int w;
        private int scroll = 0;

        ListEditor(FieldRow row, int anchorX, int anchorY) {
            this.row = row;
            this.itemsField = row.spec != null && "items".equals(row.spec.key());

            if (row.widget instanceof EditBox box) {
                for (String part : box.getValue().split(",")) {
                    if (!part.trim().isEmpty()) {
                        values.add(part.trim());
                    }

                }

            }

            this.w = 230;
            this.x = Mth.clamp(anchorX - w - 6, 4, FrameEditorScreen.this.width - w - 4);
            this.anchorY = anchorY;

            this.addBox = new StyledEditBox(minecraft.font, 0, 0, w - PAD * 2, 16, Component.empty(), accent);
            this.addBox.setMaxLength(256);
            this.addBox.setFocused(true);
        }

        private int visibleRows() {
            return Math.max(1, Math.min(values.size(), MAX_VISIBLE));
        }

        private int height() {
            return PAD + 13 + visibleRows() * ROW_H + 6 + 16 + PAD;
        }

        private int top() {
            return Mth.clamp(anchorY - height() / 2, HEADER_HEIGHT + 4, Math.max(HEADER_HEIGHT + 4, FrameEditorScreen.this.height - height() - 4));
        }

        private int rowsTop() {
            return top() + PAD + 13;
        }

        private void clampScroll() {
            scroll = Mth.clamp(scroll, 0, Math.max(0, values.size() - MAX_VISIBLE));
        }

        private void sync() {
            if (row.widget instanceof EditBox box) {
                box.setValue(String.join(", ", values));
            }

        }

        void render(GuiGraphics graphics, int mouseX, int mouseY) {
            final int y0 = top();
            final int y1 = y0 + height();

            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 400);

            drawCard(graphics, x, y0, x + w, y1, 0xF8101012, withAlpha(dimAccent(accent), 0xFF));

            graphics.drawString(font, row.spec != null ? fieldLabel(row.spec.key()) : "", x + PAD, y0 + 6, 0xFF888888, false);

            if (values.isEmpty()) {
                final String empty = I18n.get("tooltipoverhaul.config.frames.list.empty");
                graphics.drawString(font, empty, x + PAD, rowsTop() + (ROW_H - font.lineHeight) / 2, 0xFF555555, false);
            }

            clampScroll();
            for (int i = scroll; i < Math.min(values.size(), scroll + MAX_VISIBLE); i++) {
                final int rowY = rowsTop() + (i - scroll) * ROW_H;
                final boolean hovered = mouseX >= x + 4 && mouseX < x + w - 4 && mouseY >= rowY && mouseY < rowY + ROW_H;

                if (hovered) {
                    graphics.fill(x + 4, rowY, x + w - 4, rowY + ROW_H, 0xFF1B1B1F);
                }

                final String value = values.get(i);
                boolean known = false;
                if (itemsField) {
                    final ResourceLocation id = ResourceLocation.tryParse(value);
                    known = id != null && BuiltInRegistries.ITEM.containsKey(id);
                    if (known) {
                        drawItemSafe(graphics, new ItemStack(BuiltInRegistries.ITEM.get(id)), x + PAD, rowY + 1);
                    }
                    else {
                        graphics.drawString(font, "?", x + PAD + 5, rowY + 5, 0xFFCC8040, false);
                    }
                }
                else {
                    graphics.fill(x + PAD + 6, rowY + 8, x + PAD + 9, rowY + 11, 0xFF585858);
                }

                final int textColor = !itemsField || known ? 0xFFD0D0D0 : 0xFFCC8040;
                graphics.drawString(font, font.plainSubstrByWidth(value, w - PAD * 2 - 20 - 12), x + PAD + 20, rowY + (ROW_H - font.lineHeight) / 2, textColor, false);

                if (hovered) {
                    final boolean overDelete = mouseX >= x + w - PAD - 12 && mouseX < x + w - PAD;
                    graphics.drawString(font, "\u00d7", x + w - PAD - 8, rowY + (ROW_H - font.lineHeight) / 2 + 2, overDelete ? 0xFFE06060 : 0xFF915050, false);
                }

            }

            // Scrollbar when the list overflows
            if (values.size() > MAX_VISIBLE) {
                final int trackTop = rowsTop();
                final int trackH = MAX_VISIBLE * ROW_H;
                final int thumbH = Math.max(12, trackH * MAX_VISIBLE / values.size());
                final int thumbY = trackTop + (trackH - thumbH) * scroll / (values.size() - MAX_VISIBLE);
                graphics.fill(x + w - 5, trackTop, x + w - 3, trackTop + trackH, 0x14FFFFFF);
                graphics.fill(x + w - 5, thumbY, x + w - 3, thumbY + thumbH, withAlpha(dimAccent(accent), 0xC0));
            }

            addBox.setX(x + PAD);
            addBox.setY(y1 - PAD - 16);
            addBox.render(graphics, mouseX, mouseY, 0);

            if (addBox.getValue().isEmpty()) {
                graphics.drawString(font, I18n.get("tooltipoverhaul.config.frames.list.add_hint"), x + PAD + 11, y1 - PAD - 16 + 4, 0xFF4A4A52, false);
            }

            graphics.pose().popPose();
        }

        boolean mouseClicked(double mouseX, double mouseY, int button) {
            final int y0 = top();
            if (mouseX < x || mouseX >= x + w || mouseY < y0 || mouseY >= y0 + height()) {
                return false;
            }

            if (button != 0) {
                return true;
            }

            for (int i = scroll; i < Math.min(values.size(), scroll + MAX_VISIBLE); i++) {
                final int rowY = rowsTop() + (i - scroll) * ROW_H;
                if (mouseX >= x + w - PAD - 12 && mouseX < x + w - PAD && mouseY >= rowY && mouseY < rowY + ROW_H) {
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

        void mouseScrolled(double delta) {
            scroll -= (int) Math.signum(delta);
            clampScroll();
        }

        boolean keyPressed(int key, int scancode, int modifiers) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                listEditor = null;
                return true;
            }

            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                final String value = addBox.getValue().trim();
                if (value.isEmpty()) {
                    listEditor = null;
                    return true;
                }

                values.add(value);
                addBox.setValue("");
                scroll = Math.max(0, values.size() - MAX_VISIBLE);
                sync();
                playClickSound();
                return true;
            }

            return addBox.keyPressed(key, scancode, modifiers);
        }

        boolean charTyped(char chr, int modifiers) {
            addBox.charTyped(chr, modifiers);
            return true;
        }

        void tick() {
            ;;
        }

    }

    static class StringCycleButton extends AbstractWidget {

        interface ChangeListener { void onChange(String value); }

        private final String[] options;
        private final int accent;
        private final ChangeListener listener;
        private int index;

        StringCycleButton(int x, int y, int width, int height, String[] options, String current, int accent, ChangeListener listener) {
            super(x, y, width, height, Component.empty());
            this.options = options;
            this.accent = accent;
            this.listener = listener;

            this.index = 0;
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(current)) {
                    this.index = i;
                    break;
                }
            }

        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            final Minecraft minecraft = Minecraft.getInstance();
            final boolean inherit = index == 0 && "inherit".equals(options[0]);

            final int bg = isHovered() ? 0xFF202024 : 0xFF1A1A1C;
            final int border = isHovered() ? dimAccent(accent) : 0xFF2E2E32;
            drawCard(graphics, getX(), getY(), getX() + width, getY() + height, bg, border);

            String label = prettify(options[index]);
            if (minecraft.font.width(label) > width - 12) {
                label = minecraft.font.plainSubstrByWidth(label, width - 16) + "\u2026";
            }

            final int color = inherit ? 0xFF666666 : 0xFFE0E0E0;
            graphics.drawString(minecraft.font, label, getX() + (width - minecraft.font.width(label)) / 2, getY() + (height - minecraft.font.lineHeight) / 2 + 1, color, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!visible || !clicked(mouseX, mouseY) || (button != 0 && button != 1)) {
                return false;
            }

            index = Math.floorMod(index + (button == 0 ? 1 : -1), options.length);
            listener.onChange(options[index]);
            playClickSound();
            return true;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            ;;
        }

    }

}