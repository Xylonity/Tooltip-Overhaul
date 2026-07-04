package dev.xylonity.tooltipoverhaul.client.screen.config;

import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import dev.xylonity.tooltipoverhaul.config.ConfigManager;
import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;
import dev.xylonity.tooltipoverhaul.config.wrapper.AutoConfig;
import dev.xylonity.tooltipoverhaul.config.wrapper.ConfigEntry;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

public class TooltipOverhaulConfigScreen extends Screen {

    private final Screen parent;
    private final Class<?> configClass;
    private final AutoConfig meta;
    private final int accent;

    private ConfigPanel panel;
    private CategorySidebar sidebar;
    private SearchBox searchBox;

    // color level picker opened by clicking a color entry's swatch with the row/color index it writes into
    private ColorLevelPicker picker = null;
    private EntryRow pickerRow = null;
    private int pickerPartIndex = -1;

    private final Map<Field, Object> snapshot = new LinkedHashMap<>();

    // Preserved across resizes so the screen rebuilds in the same state
    private String selectedCategory = null;
    private String searchQuery = "";

    private final long openedAt = Util.getMillis();

    private static final int HEADER_HEIGHT = 46;
    private static final int DEFAULT_ACCENT = 0xFF4A9EFF;

    private final Minecraft minecraft = Minecraft.getInstance();

    public TooltipOverhaulConfigScreen(Screen parent, Class<?> configClass) {
        super(Component.literal(resolveTitle(configClass)));
        this.parent = parent;
        this.configClass = configClass;
        this.meta = configClass.getAnnotation(AutoConfig.class);
        this.accent = meta != null ? meta.accentColor() : DEFAULT_ACCENT;
        takeSnapshot();
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
    protected void init() {
        final int panelTop = HEADER_HEIGHT + 6;
        final int panelBottom = this.height - 36;

        final List<Field> entryFields = new ArrayList<>();
        final Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        for (Field field : configClass.getDeclaredFields()) {
            ConfigEntry entry = field.getAnnotation(ConfigEntry.class);
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

        this.picker = null;
        this.panel = new ConfigPanel(panelLeft, panelTop, this.width - panelLeft - 10, Math.max(0, panelBottom - panelTop), accent);
        this.panel.setSwatchClickListener(this::openColorPicker);

        if (hasSidebar) {
            this.sidebar = new CategorySidebar(10, panelTop, sidebarWidth, Math.max(0, panelBottom - panelTop), accent, categoryCounts, entryFields.size(),
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

        // Entry point to the TO custom_frames.json editor
        final int framesWidth = this.font.width(I18n.get("tooltipoverhaul.config.custom_frames")) + 16;
        addRenderableWidget(new FlatButton(searchX - framesWidth - 8, searchY, framesWidth, 16,
                Component.translatable("tooltipoverhaul.config.custom_frames"),
                button -> minecraft.setScreen(new FrameEditorScreen(this, accent)),
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

        final int buttonWidth = 90;
        final int buttonHeight = 18;
        final int gap = 8;
        final int totalWidth = buttonWidth * 3 + gap * 2;
        final int sx = (this.width - totalWidth) / 2;
        final int buttonY = this.height - 26;

        addRenderableWidget(new FlatButton(sx, buttonY, buttonWidth, buttonHeight,
                CommonComponents.GUI_DONE,
                button -> {
                    panel.applyToFields(); ConfigManager.save(configClass); minecraft.setScreen(parent);
                },
                accent, true).withEntranceDelay(60));

        addRenderableWidget(new FlatButton(sx + buttonWidth + gap, buttonY, buttonWidth, buttonHeight,
                Component.translatable("tooltipoverhaul.config.cancel"),
                button -> {
                    restoreSnapshot(); minecraft.setScreen(parent);
                },
                accent).withEntranceDelay(120));

        addRenderableWidget(new FlatButton(sx + (buttonWidth + gap) * 2, buttonY, buttonWidth, buttonHeight,
                Component.translatable("tooltipoverhaul.config.reset_all"),
                button -> panel.resetAllToDefault(),
                accent).withEntranceDelay(180));
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        // Carries unsaved widget state through the rebuild
        if (panel != null) {
            panel.applyToFields();
        }

        super.resize(minecraft, width, height);
    }

    @Override
    public void tick() {
        if (panel != null) {
            panel.tickPanel();
        }

        if (picker != null) {
            picker.tick();
        }

        super.tick();
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

        if (key == GLFW.GLFW_KEY_F && hasControlDown() && searchBox != null) {
            this.setFocused(searchBox);
            searchBox.setFocused(true);
            return true;
        }

        return super.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (picker != null) {
            if (!picker.mouseClicked(mouseX, mouseY, button)) {
                picker = null;
            }

            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (picker != null) {
            return picker.mouseDragged(mouseX, mouseY);
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (picker != null) {
            picker.mouseReleased();
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double delta) {
        if (picker != null) {
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, deltaX, delta);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (picker != null) {
            return picker.charTyped(chr, modifiers);
        }

        return super.charTyped(chr, modifiers);
    }

    private void openColorPicker(EntryRow row, int partIndex, int x, int y) {
        if (!(row.widget instanceof EditBox box)) {
            return;
        }

        int initial = 0xFFFFFFFF;
        final String[] parts = box.getValue().split("[,;]");
        if (partIndex >= 0 && partIndex < parts.length) {
            initial = ConfigColorParser.parseColor(parts[partIndex].trim());
        }

        pickerRow = row;
        pickerPartIndex = partIndex;
        picker = new ColorLevelPicker(x, y, HEADER_HEIGHT + 4, this.width, this.height, accent, initial, false,
                argb -> applyPickedColor(pickerRow, pickerPartIndex, argb));

        playClickSound();
    }

    private void applyPickedColor(EntryRow row, int partIndex, int argb) {
        if (row == null || !(row.widget instanceof EditBox box)) {
            return;
        }

        final String[] parts = box.getValue().split("[,;]");
        if (partIndex < 0 || partIndex >= parts.length) {
            return;
        }

        parts[partIndex] = (argb >>> 24) != 0xFF ? String.format("#%08X", argb) : String.format("#%06X", argb & 0x00FFFFFF);

        final StringBuilder joined = new StringBuilder();
        for (final String part : parts) {
            if (!joined.isEmpty()) {
                joined.append(", ");
            }

            joined.append(part.trim());
        }

        box.setValue(joined.toString());
    }

    @Override
    public void onClose() {
        panel.applyToFields();
        ConfigManager.save(configClass);
        minecraft.setScreen(parent);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // blur
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xFF0E0E0E);

        renderBackgroundGlow(graphics);
        renderHeader(graphics);
        renderHeaderInfo(graphics);

        super.render(graphics, mouseX, mouseY, partialTick);

        renderModifiedCounter(graphics);

        if (panel != null) {
            panel.renderPendingTooltip(graphics, font, width, height);
        }

        if (picker != null) {
            picker.render(graphics, mouseX, mouseY);
        }

    }

    private void renderBackgroundGlow(GuiGraphics graphics) {
        // Soft accent wash behind the header title
        final float breath = 0.85f + 0.15f * (float) Math.sin(Util.getMillis() / 1300.0);
        final int[] accentt = rgb(accent);
        final int glowW = Math.min(220, width / 2);
        for (int x = 0; x < glowW; x += 2) {
            final float t = (float) x / glowW;
            final int alpha = (int) (0x0A * (1f - t * t) * breath);
            if (alpha > 0) {
                graphics.fill(x, 0, x + 2, HEADER_HEIGHT, (alpha << 24) | (accentt[0] << 16) | (accentt[1] << 8) | accentt[2]);
            }

        }

        // Bottom vignette
        for (int i = 0; i < 24; i++) {
            final int alpha = (int) (26 * ((float) i / 24) * ((float) i / 24));
            if (alpha > 0) {
                graphics.fill(0, height - 24 + i, width, height - 23 + i, alpha << 24);
            }

        }

    }

    private void renderHeader(GuiGraphics graphics) {
        for (int i = 0; i < HEADER_HEIGHT; i++) {
            final int alpha = Math.max(0, 60 - i * 2);
            if (alpha > 0) {
                graphics.fill(0, i, width, i + 1, alpha << 24);
            }

        }

        final String title = resolveTitle(configClass).toUpperCase(Locale.ROOT);
        final String description = meta != null ? meta.description() : "";
        final boolean hasDescription = !description.isEmpty();

        final int titleY = hasDescription ? 12 : 19;

        final long now = Util.getMillis();
        final float t = AnimationUtils.easeOutCubic(Mth.clamp((now - openedAt) / 240f, 0f, 1f));
        final float t2 = AnimationUtils.easeOutCubic(Mth.clamp((now - openedAt - 80) / 240f, 0f, 1f));

        // Accent bar next to the title
        final int barHeight = (int) ((font.lineHeight + 3) * t);
        if (barHeight > 0) {
            graphics.fill(14, titleY - 2, 17, titleY - 2 + barHeight, accent);
            graphics.fill(17, titleY - 2, 18, titleY - 2 + barHeight, withAlpha(accent, 0x50));
        }

        final int titleAlpha = (int) (0xFF * t);
        if (titleAlpha >= 0x10) {
            graphics.drawString(font, title, 23 - (int) ((1f - t) * 6f), titleY, withAlpha(0xFFFFFF, titleAlpha), false);
        }

        final int descriptionAlpha = (int) (0xFF * t2);
        if (hasDescription && descriptionAlpha >= 0x10) {
            graphics.drawString(font, description, 23 - (int) ((1f - t2) * 6f), titleY + font.lineHeight + 4, withAlpha(0x888888, descriptionAlpha), false);
        }

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

    static String resolveTitle(Class<?> clazz) {
        AutoConfig autoConfig = clazz.getAnnotation(AutoConfig.class);
        if (autoConfig != null && !autoConfig.title().isEmpty()) {
            return autoConfig.title();
        }
        if (autoConfig != null) {
            return prettify(autoConfig.file());
        }

        return clazz.getSimpleName();
    }

    static String prettify(String name) {
        String[] parts = name.replace('_', ' ').replace('-', ' ').split("\\s+");
        StringBuilder stringBuilder = new StringBuilder();
        for (final String part : parts) {
            if (part.isEmpty()) {
                continue;
            }

            if (!stringBuilder.isEmpty()) {
                stringBuilder.append(' ');
            }

            stringBuilder.append(Character.toUpperCase(part.charAt(0)));

            if (part.length() > 1) {
                stringBuilder.append(part.substring(1).toLowerCase());
            }

        }

        return stringBuilder.toString();
    }

    static int[] rgb(int argb) {
        return new int[] {
                (argb >> 16) & 0xFF,
                (argb >> 8) & 0xFF,
                argb & 0xFF
        };

    }

    static int withAlpha(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    static int mixRgb(int from, int to, float t) {
        final int red = (int) (((from >> 16) & 0xFF) + ((((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t));
        final int green = (int) (((from >> 8) & 0xFF) + ((((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t));
        final int blue = (int) ((from & 0xFF) + (((to & 0xFF) - (from & 0xFF)) * t));
        return (red << 16) | (green << 8) | blue;
    }

    static int dimAccent(int accent) {
        final int[] accentt = rgb(accent);
        return ((accentt[0] * 11 / 20) << 16) | ((accentt[1] * 11 / 20) << 8) | (accentt[2] * 11 / 20);
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

    static void drawCard(GuiGraphics graphics, int x0, int y0, int x1, int y1, int bg, int border) {
        graphics.fill(x0 + 1, y0 + 1, x1 - 1, y1 - 1, bg);
        drawNotchedBorder(graphics, x0, y0, x1, y1, border);
    }

    static void drawCardHoverFx(GuiGraphics graphics, int left, int top, int width, int height, float anim, int accent, int seed) {
        if (anim <= 0.1f || height < 12) {
            return;
        }

        final int[] ac = rgb(accent);
        final long time = Util.getMillis();

        for (int i = 0; i < 7; i++) {
            final long period = 1700L + Math.floorMod((long) seed * 31L + i * 197L, 900L);
            final long offset = Math.floorMod((long) seed * 137L + i * 311L, period);
            final float phase = Math.floorMod(time + offset, period) / (float) period;

            final float fx = Math.floorMod((long) seed * 53L + i * 419L, 1000L) / 1000f;
            final float sway = (float) Math.sin(phase * Math.PI * 2.0 + i * 1.3) * 2f;
            final int px = (int) (left + 5 + fx * (width - 10) + sway);
            final int py = (int) (top + height - 4 - phase * (height - 8));

            final float life = (float) Math.sin(phase * Math.PI);
            final int alpha = (int) (life * anim * 0x66);
            if (alpha < 6) {
                continue;
            }

            final int size = (i % 3 == 0) ? 2 : 1;
            graphics.fill(px, py, px + size, py + size, (alpha << 24) | (ac[0] << 16) | (ac[1] << 8) | ac[2]);
        }

    }

    static void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
    }

    static class CategorySidebar extends AbstractWidget {

        private record Item(
                String key,
                String label,
                int count
        ) {
            ;;
        }

        private final List<Item> items = new ArrayList<>();
        private final float[] hoverAnims;
        private final int accent;
        private final java.util.function.Consumer<String> onSelect;
        private final long revealStart = Util.getMillis();

        private int selected = 0;
        private boolean dimmed = false;
        private double scroll = 0;
        private float indicatorY = Float.NaN;

        private static final int ITEM_HEIGHT = 20;
        private static final int ITEM_GAP = 2;
        private static final int TOP_PAD = 18;

        CategorySidebar(int x, int y, int width, int height, int accent, Map<String, Integer> categoryCounts, int total, java.util.function.Consumer<String> onSelect) {
            super(x, y, width, height, Component.empty());
            this.accent = accent;
            this.onSelect = onSelect;

            items.add(new Item(null, I18n.get("tooltipoverhaul.config.category.all"), total));
            for (final Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                items.add(new Item(entry.getKey(), entry.getKey().isEmpty() ? I18n.get("tooltipoverhaul.config.category.general") : prettify(entry.getKey()), entry.getValue()));
            }

            this.hoverAnims = new float[items.size()];
        }

        void setSelectedKey(String key) {
            selected = 0;
            for (int i = 0; i < items.size(); i++) {
                if (Objects.equals(items.get(i).key, key)) {
                    selected = i;
                    break;
                }

            }

        }

        void setDimmed(boolean dimmed) {
            this.dimmed = dimmed;
        }

        private int contentHeight() {
            return TOP_PAD + items.size() * (ITEM_HEIGHT + ITEM_GAP);
        }

        private boolean scrollable() {
            return contentHeight() > height;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick) {
            if (!visible) {
                return;
            }

            final int x = getX();
            final int y = getY();
            final int addX = x + width;
            final int addY = y + height;

            graphics.fill(x, y, addX, addY, 0xFF101010);

            // Inner shadow at top edge
            for (int i = 0; i < 4; i++) {
                graphics.fill(x, y + i, addX, y + i + 1, (Math.max(0, 30 - i * 8)) << 24);
            }

            graphics.enableScissor(x, y, addX, addY);

            final float dim = dimmed ? 0.35f : 1f;
            final long now = Util.getMillis();

            graphics.drawString(Minecraft.getInstance().font, I18n.get("tooltipoverhaul.config.categories").toUpperCase(Locale.ROOT), x + 8, y + 6 - (int) scroll, withAlpha(0x585858, (int) (0xFF * dim)), false);

            final int[] ac = rgb(accent);
            int itemY = y + TOP_PAD - (int) scroll;

            for (int i = 0; i < items.size(); i++) {
                final Item item = items.get(i);

                final float entrance = AnimationUtils.easeOutCubic(Mth.clamp((now - revealStart - i * 40L) / 180f, 0f, 1f));
                final int slide = (int) ((1f - entrance) * -12f);

                final boolean isSelected = i == selected;
                final boolean hovered = !dimmed && mx >= x && mx < addX && my >= itemY && my < itemY + ITEM_HEIGHT && my >= y && my < addY;

                final float target = hovered ? 1f : 0f;
                hoverAnims[i] += (target - hoverAnims[i]) * 0.15f;
                final float e = AnimationUtils.smoothstep(0f, 1f, hoverAnims[i]);

                final int bgAlpha = (int) ((isSelected ? 0x30 : 0x12 + 0x18 * e) * entrance * dim);
                graphics.fill(x + 3 + slide, itemY, addX - 3 + slide, itemY + ITEM_HEIGHT, (bgAlpha << 24) | 0x1A1A1A);

                if (!isSelected && e > 0.02f) {
                    final int barAlpha = (int) (0x66 * e * entrance * dim);
                    graphics.fill(x + 3 + slide, itemY, x + 5 + slide, itemY + ITEM_HEIGHT, (barAlpha << 24) | (ac[0] << 16) | (ac[1] << 8) | ac[2]);
                }

                final int textAlpha = (int) (0xFF * Math.max(0.1f, entrance) * dim);
                if (textAlpha >= 0x10) {
                    final Minecraft minecraft = Minecraft.getInstance();
                    final int labelColor = isSelected ? 0xE8E8E8 : (hovered ? 0xC8C8C8 : 0x808080);
                    final int textY = itemY + (ITEM_HEIGHT - minecraft.font.lineHeight) / 2 + 1;

                    final String count = String.valueOf(item.count);
                    final int countX = addX - 8 - minecraft.font.width(count) + slide;

                    // Long labels scroll back and forth
                    final int labelX = x + 10 + slide;
                    final int labelMaxX = countX - 4;
                    if (minecraft.font.width(item.label) <= labelMaxX - labelX) {
                        graphics.drawString(minecraft.font, item.label, labelX, textY, withAlpha(labelColor, textAlpha), false);
                    }
                    else {
                        renderScrollingString(graphics, minecraft.font, Component.literal(item.label), labelX, itemY, labelMaxX, itemY + ITEM_HEIGHT, withAlpha(labelColor, textAlpha));
                    }

                    final int countColor = isSelected ? (accent & 0x00FFFFFF) : 0x505050;
                    graphics.drawString(minecraft.font, count, countX, textY, withAlpha(countColor, textAlpha), false);
                }

                itemY += ITEM_HEIGHT + ITEM_GAP;
            }

            // Selection indicator slides between items
            final float indicatorTarget = TOP_PAD + selected * (ITEM_HEIGHT + ITEM_GAP);
            if (Float.isNaN(indicatorY) || Math.abs(indicatorTarget - indicatorY) < 0.5f) {
                indicatorY = indicatorTarget;
            }
            else {
                indicatorY += (indicatorTarget - indicatorY) * 0.25f;
            }

            final int barY = y + (int) indicatorY - (int) scroll;
            final int indicatorAlpha = (int) (0xFF * dim);
            graphics.fill(x + 3, barY, x + 5, barY + ITEM_HEIGHT, (indicatorAlpha << 24) | (ac[0] << 16) | (ac[1] << 8) | ac[2]);

            graphics.disableScissor();

            drawNotchedBorder(graphics, x, y, addX, addY, 0xFF1C1C20);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (!visible || dimmed || button != 0 || mx < getX() || mx >= getX() + width || my < getY() || my >= getY() + height) {
                return false;
            }

            int itemY = getY() + TOP_PAD - (int) scroll;
            for (int i = 0; i < items.size(); i++) {
                if (my >= itemY && my < itemY + ITEM_HEIGHT) {
                    if (i != selected) {
                        selected = i;
                        playDownSound(Minecraft.getInstance().getSoundManager());
                        onSelect.accept(items.get(i).key);
                    }

                    return true;
                }

                itemY += ITEM_HEIGHT + ITEM_GAP;
            }

            return true;
        }

        @Override
        public boolean mouseScrolled(double mx, double my, double dx, double d) {
            if (!visible || !scrollable() || mx < getX() || mx >= getX() + width || my < getY() || my >= getY() + height) {
                return false;
            }

            scroll = Mth.clamp(scroll - d * 16.0, 0, Math.max(0, contentHeight() - height));

            return true;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            ;;
        }

    }

    static class ConfigPanel extends AbstractWidget {

        private final List<EntryRow> allRows = new ArrayList<>();
        private final List<EntryRow> visibleRows = new ArrayList<>();

        private double scroll = 0;
        private double targetScroll = 0;
        private boolean draggingScrollbar = false;
        private int dragOffset = 0;
        private EntryRow focused = null;
        final int accent;

        private String query = "";
        private String category = null;
        private long revealStart = Util.getMillis();

        private List<Component> pendingTooltip = null;
        private EntryRow pendingTooltipRow = null;
        private EntryRow tooltipRow = null;
        private long tooltipSince = 0;
        private int tooltipX;
        private int tooltipY;

        private float scrollbarAnim = 0f;

        private static final int ROW_GAP = 4;
        private static final int PADDING = 8;
        private static final int CATEGORY_HEIGHT = 18;

        interface SwatchClickListener {
            void onSwatchClicked(EntryRow row, int partIndex, int x, int y);
        }

        private SwatchClickListener swatchClickListener = null;

        void setSwatchClickListener(SwatchClickListener listener) {
            this.swatchClickListener = listener;
        }

        ConfigPanel(int x, int y, int width, int height, int accent) {
            super(x, y, width, height, Component.empty());
            this.accent = accent;
        }

        void tickPanel() {
            ;;
        }

        void addEntry(Field field, ConfigEntry configEntry, Object snapshotValue) {
            allRows.add(new EntryRow(field, configEntry, snapshotValue, accent));
            visibleRows.add(allRows.get(allRows.size() - 1));

            recalcLayout();
        }

        void setQuery(String query) {
            this.query = query == null ? "" : query;
            applyFilter();
        }

        void setCategory(String category) {
            this.category = category;
            applyFilter();
        }

        private void applyFilter() {
            visibleRows.clear();

            final boolean searching = !query.isBlank();
            final String content = query.toLowerCase().replace('_', ' ');

            for (EntryRow entryRow : allRows) {
                if (searching) {
                    if (entryRow.matchesSearch(content)) {
                        visibleRows.add(entryRow);
                    }

                }
                else if (category == null || entryRow.categoryKey().equals(category)) {
                    visibleRows.add(entryRow);
                }

            }

            recalcLayout();

            revealStart = Util.getMillis();
            targetScroll = 0;
            scroll = 0;
        }

        private boolean showHeaders() {
            if (!query.isBlank() || category == null) {
                Set<String> keys = new HashSet<>();
                for (EntryRow entryRow : visibleRows) {
                    keys.add(entryRow.categoryKey());
                }

                return keys.size() > 1;
            }

            return false;
        }

        void applyToFields() {
            for (EntryRow entryRow : allRows) {
                entryRow.writeToField();
            }

        }

        void resetAllToDefault() {
            for (EntryRow entryRow : allRows) {
                entryRow.resetToDefault();
            }

        }

        int visibleCount() {
            return visibleRows.size();
        }

        int countModified() {
            int count = 0;
            for (EntryRow entryRow : allRows) {
                if (entryRow.isModified()) {
                    count++;
                }

            }

            return count;
        }

        private int contentWidth() {
            return width - 16;
        }

        private int rowLeft() {
            return getX() + PADDING;
        }

        private int topPad() {
            return showHeaders() ? 4 : PADDING;
        }

        private void recalcLayout() {
            int width = contentWidth();
            for (EntryRow entryRow : visibleRows) {
                entryRow.recalcHeight(width);
            }

            clampScroll();
        }

        private int totalContentHeight() {
            final boolean headers = showHeaders();
            int height = topPad();
            String lastCategory = null;
            for (int i = 0; i < visibleRows.size(); i++) {
                if (i > 0) {
                    height += ROW_GAP;
                }

                String category = visibleRows.get(i).categoryKey();
                if (headers && !category.equals(lastCategory)) {
                    height += CATEGORY_HEIGHT;
                }

                lastCategory = category;
                height += visibleRows.get(i).height;
            }

            // Bottom margin
            return height + PADDING;
        }

        private boolean scrollable() {
            return totalContentHeight() > height;
        }

        private void clampScroll() {
            final double max = Math.max(0, totalContentHeight() - height);
            targetScroll = Math.max(0, Math.min(max, targetScroll));
            scroll = Math.max(0, Math.min(max, scroll));
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick) {
            if (!visible) {
                return;
            }

            pendingTooltip = null;
            pendingTooltipRow = null;

            if (!draggingScrollbar && scroll != targetScroll) {
                double dragging = targetScroll - scroll;
                scroll = Math.abs(dragging) < 0.5 ? targetScroll : scroll + dragging * 0.25;

                clampScroll();
            }

            final int x = getX();
            final int y = getY();
            final int addX = x + width;
            final int addY = y + height;
            graphics.fill(x, y, addX, addY, 0xFF111111);

            for (int i = 0; i < 4; i++) {
                graphics.fill(x, y + i, addX, y + i + 1, (Math.max(0, 30 - i * 8)) << 24);
            }

            graphics.enableScissor(x, y, addX, addY);

            if (visibleRows.isEmpty()) {
                renderEmptyState(graphics, x, y, addY);
            }

            final boolean headers = showHeaders();
            final long now = Util.getMillis();
            final int rowLength = rowLeft();
            final int rowWidth = contentWidth();
            int categoryY = y + topPad() - (int) scroll;
            String lastCategory = null;

            for (int i = 0; i < visibleRows.size(); i++) {
                EntryRow row = visibleRows.get(i);
                if (i > 0) {
                    categoryY += ROW_GAP;
                }

                String category = row.categoryKey();
                if (headers && !category.equals(lastCategory)) {
                    if (categoryY + CATEGORY_HEIGHT >= y && categoryY <= addY) {
                        renderCategoryHeader(graphics, category.isEmpty() ? I18n.get("tooltipoverhaul.config.category.general") : category, rowLength, categoryY, rowWidth);
                    }

                    categoryY += CATEGORY_HEIGHT;
                }

                lastCategory = category;

                int rowH = row.height;
                if (categoryY + rowH >= y && categoryY <= addY) {
                    boolean inPanel = mx >= x && mx <= addX && my >= y && my <= addY;
                    boolean hovered = inPanel && mx >= rowLength && mx <= rowLength + rowWidth && my >= categoryY && my <= categoryY + rowH;

                    final float entrance = AnimationUtils.easeOutCubic(Mth.clamp((now - revealStart - Math.min(i, 12) * 35L) / 170f, 0f, 1f));

                    row.render(graphics, i, categoryY, rowLength, rowWidth, mx, my, hovered, entrance, partialTick);

                    if (hovered) {
                        captureTooltip(row, mx, my, rowLength, rowWidth);
                    }

                }
                else {
                    row.hoverStart = 0;
                }

                categoryY += rowH;
            }

            graphics.disableScissor();

            if (pendingTooltipRow != tooltipRow) {
                tooltipRow = pendingTooltipRow;
                tooltipSince = Util.getMillis();
            }

            renderEdgeFades(graphics, x, y, addX, addY);

            drawNotchedBorder(graphics, x, y, addX, addY, 0xFF1E1E22);

            renderScrollbar(graphics, mx, my);
        }

        private void renderEdgeFades(GuiGraphics graphics, int x, int y, int addX, int addY) {
            final double maxScroll = Math.max(0, totalContentHeight() - height);

            if (scroll > 1) {
                for (int i = 0; i < 10; i++) {
                    final float t = 1f - i / 10f;
                    graphics.fill(x, y + i, addX - 4, y + i + 1, ((int) (0xD0 * t * t) << 24) | 0x111111);
                }

            }

            if (scroll < maxScroll - 1) {
                for (int i = 0; i < 10; i++) {
                    final float t = 1f - i / 10f;
                    graphics.fill(x, addY - 1 - i, addX - 4, addY - i, ((int) (0xD0 * t * t) << 24) | 0x111111);
                }

            }

        }

        private void renderEmptyState(GuiGraphics graphics, int x, int y, int addY) {
            final Minecraft minecraft = Minecraft.getInstance();
            final String text = query.isBlank() ? I18n.get("tooltipoverhaul.config.empty") : I18n.get("tooltipoverhaul.config.empty_search", query);
            final int textY = y + (addY - y - minecraft.font.lineHeight) / 2;

            graphics.drawString(minecraft.font, text, x + (width - minecraft.font.width(text)) / 2, textY, 0xFF4A4A4A, false);
        }

        private void captureTooltip(EntryRow row, int mx, int my, int rowLeft, int rowWidth) {
            if (mx > rowLeft + rowWidth - row.widget.getWidth() - 30) {
                if (row.hoverStart == 0) {
                    row.hoverStart = Util.getMillis();
                }

                return;
            }

            if (row.hoverStart == 0) {
                row.hoverStart = Util.getMillis();
            }

            if (Util.getMillis() - row.hoverStart < 450) {
                return;
            }

            List<Component> lines = row.tooltipLines();
            if (!lines.isEmpty()) {
                pendingTooltip = lines;
                pendingTooltipRow = row;
                tooltipX = mx;
                tooltipY = my;
            }

        }

        void renderPendingTooltip(GuiGraphics graphics, Font font, int screenWidth, int screenHeight) {
            if (pendingTooltip == null || pendingTooltip.isEmpty()) {
                return;
            }

            final float fade = AnimationUtils.easeOutQuad(Mth.clamp((Util.getMillis() - tooltipSince) / 120f, 0f, 1f));
            final int textAlpha = (int) (0xFF * fade);
            if (textAlpha < 0x10) {
                return;
            }

            final int maxTextWidth = 200;
            final List<FormattedCharSequence> wrapped = new ArrayList<>();
            for (Component line : pendingTooltip) {
                wrapped.addAll(font.split(line, maxTextWidth));
            }

            int textWidth = 0;
            for (FormattedCharSequence seq : wrapped) {
                textWidth = Math.max(textWidth, font.width(seq));
            }

            final int paddingX = 6;
            final int paddingY = 5;
            final int boxWidth = textWidth + paddingX * 2;
            final int boxHeight = wrapped.size() * (font.lineHeight + 1) - 1 + paddingY * 2;

            int x = tooltipX + 10;
            int y = tooltipY - boxHeight - 4;
            if (x + boxWidth > screenWidth - 4) {
                x = screenWidth - 4 - boxWidth;
            }
            if (y < 4) {
                y = tooltipY + 12;
            }
            if (y + boxHeight > screenHeight - 4) {
                y = screenHeight - 4 - boxHeight;
            }

            y += (int) ((1f - fade) * 3f);

            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 400);

            final int boxBg = ((int) (0xF2 * fade) << 24) | 0x121214;
            final int boxBorder = withAlpha(mixRgb(0x2E2E32, dimAccent(accent), 0.4f), textAlpha);
            drawCard(graphics, x, y, x + boxWidth, y + boxHeight, boxBg, boxBorder);

            final Minecraft minecraft = Minecraft.getInstance();
            int lineY = y + paddingY;
            for (FormattedCharSequence seq : wrapped) {
                graphics.drawString(minecraft.font, seq, x + paddingX, lineY, withAlpha(0xDDDDDD, textAlpha), false);
                lineY += minecraft.font.lineHeight + 1;
            }

            graphics.pose().popPose();
        }

        private void renderCategoryHeader(GuiGraphics graphics, String category, int x, int y, int width) {
            final Minecraft minecraft = Minecraft.getInstance();
            final String label = prettify(category).toUpperCase();
            final int labelW = minecraft.font.width(label);
            final int textX = x + 12;
            final int lineY = y + 8;
            final int lineEnd = x + width;

            int[] rgbAccent = rgb(accent);
            graphics.fill(x + 3, lineY - 2, x + 6, lineY + 1, 0xFF000000 | (rgbAccent[0] << 16) | (rgbAccent[1] << 8) | rgbAccent[2]);

            graphics.drawString(minecraft.font, label, textX, y + 4, 0xFF585858, false);

            int lineStart = textX + labelW + 6;
            if (lineStart < lineEnd) {
                graphics.fill(lineStart, lineY, lineEnd, lineY + 1, 0x20FFFFFF);
            }

        }

        private void renderScrollbar(GuiGraphics graphics, int mx, int my) {
            if (!scrollable()) {
                return;
            }

            // momentum
            final boolean engaged = draggingScrollbar || Math.abs(targetScroll - scroll) > 0.5 || (isIn(mx, my) && mx >= getX() + width - 10);
            scrollbarAnim += ((engaged ? 1f : 0f) - scrollbarAnim) * 0.1f;

            final int contentHeight = totalContentHeight();
            final int newX = getX() + width - 4;
            final int newY = getY();
            final int newHeight = height;
            final double maxSwap = Math.max(1, contentHeight - height);
            final int thumbHeight = Math.max(20, (int) (newHeight * ((double) height / contentHeight)));
            final int thumbY = newY + (int) ((newHeight - thumbHeight) * (scroll / maxSwap));

            final int trackAlpha = (int) (0x08 + 0x10 * scrollbarAnim);
            graphics.fill(newX, newY, newX + 3, newY + newHeight, (trackAlpha << 24) | 0x00FFFFFF);

            final int thumbColor = mixRgb(0x3E3E44, accent & 0x00FFFFFF, scrollbarAnim);
            final int thumbAlpha = (int) (0x90 + 0x6F * scrollbarAnim);
            graphics.fill(newX, thumbY, newX + 3, thumbY + thumbHeight, (thumbAlpha << 24) | thumbColor);
        }

        @Override
        public boolean mouseScrolled(double mx, double my, double dx, double d) {
            if (!visible || !isIn(mx, my) || !scrollable()) {
                return false;
            }

            draggingScrollbar = false;
            targetScroll -= d * 24.0;

            clampScroll();

            return true;
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (!visible || !active || !isIn(mx, my)) {
                return false;
            }

            if (scrollable()) {
                int tx = getX() + width - 6;
                if (mx >= tx) {
                    final int contentHeight = totalContentHeight();
                    final double maxSroll = Math.max(1, contentHeight - height);
                    final int thumbH = Math.max(20, (int) (height * ((double) height / contentHeight)));
                    final int thumbY = getY() + (int) ((height - thumbH) * (scroll / maxSroll));
                    if (my >= thumbY && my <= thumbY + thumbH) {
                        draggingScrollbar = true;
                        dragOffset = (int) my - thumbY;
                    }
                    else {
                        double t = (my - getY()) / (double) height;
                        targetScroll = t * maxSroll;
                        scroll = targetScroll;

                        clampScroll();
                    }

                    return true;
                }

            }

            final boolean headers = showHeaders();
            int contentY = getY() + topPad() - (int) scroll;
            String lastCategory = null;
            for (int i = 0; i < visibleRows.size(); i++) {
                final EntryRow row = visibleRows.get(i);
                if (i > 0) {
                    contentY += ROW_GAP;
                }

                final String category = row.categoryKey();
                if (headers && !category.equals(lastCategory)) {
                    contentY += CATEGORY_HEIGHT;
                }

                lastCategory = category;
                if (my >= contentY && my <= contentY + row.height && mx >= rowLeft() && mx <= rowLeft() + contentWidth()) {
                    for (int[] hit : row.swatchHits) {
                        if (button == 0 && mx >= hit[0] && mx < hit[0] + 10 && my >= hit[1] && my < hit[1] + 10) {
                            clearFocused();
                            if (swatchClickListener != null) {
                                swatchClickListener.onSwatchClicked(row, hit[2], (int) mx, (int) my);
                            }

                            return true;
                        }

                    }

                    if (row.clickedReset(mx, my, button, rowLeft(), contentY, contentWidth())) {
                        clearFocused();
                        return true;
                    }

                    if (row.mouseClicked(mx, my, button)) {
                        setFocusedRow(row);
                        return true;
                    }

                    clearFocused();

                    return true;
                }

                contentY += row.height;
            }

            clearFocused();

            return false;
        }

        @Override
        public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
            if (draggingScrollbar && scrollable()) {
                final int contentHeight = totalContentHeight();
                final double maxS = Math.max(1, contentHeight - height);
                final int thumbH = Math.max(20, (int) (height * ((double) height / contentHeight)));
                final int span = height - thumbH;
                if (span <= 0) {
                    return true;
                }

                double maxed = Math.max(0, Math.min(1, ((my - dragOffset - getY()) / span)));

                targetScroll = maxed * maxS;
                scroll = targetScroll;

                clampScroll();

                return true;
            }

            return focused != null && focused.mouseDragged(mx, my, button, dx, dy);
        }

        @Override
        public boolean mouseReleased(double mx, double my, int button) {
            if (draggingScrollbar) {
                draggingScrollbar = false;

                return true;
            }

            return focused != null && focused.mouseReleased(mx, my, button);
        }

        @Override
        public boolean keyPressed(int key, int s, int m) {
            return (focused != null && focused.keyPressed(key, s, m)) || super.keyPressed(key, s, m);
        }

        @Override
        public boolean charTyped(char c, int m) {
            return (focused != null && focused.charTyped(c, m)) || super.charTyped(c, m);
        }

        private boolean isIn(double mx, double my) {
            return mx >= getX() && mx <= getX() + width && my >= getY() && my <= getY() + height;
        }

        private void clearFocused() {
            if (focused != null) {
                focused.setFocused(false);
                focused = null;
            }

        }

        private void setFocusedRow(EntryRow entryRow) {
            if (focused == entryRow) {
                return;
            }

            if (focused != null) {
                focused.setFocused(false);
            }

            focused = entryRow;
            if (entryRow != null) {
                entryRow.setFocused(true);
            }

        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            ;;
        }

    }

    private static class EntryRow {

        final Field field;
        final ConfigEntry meta;
        final Component label;
        final String description;
        final AbstractWidget widget;
        final String searchIndex;
        final int accent;
        final Object snapshotValue;
        final Object defaultValue;

        float hoverAnim = 0f;
        long hoverStart = 0;
        int height = 36;
        int descriptionLines = 0;
        int descriptionMaxWidth = 0;

        final List<int[]> swatchHits = new ArrayList<>();

        private List<Component> cachedTooltip = null;
        private List<FormattedCharSequence> descriptionCache = List.of();

        EntryRow(Field field, ConfigEntry meta, Object snapshotValue, int accent) {
            this.field = field;
            this.meta = meta;
            this.accent = accent;
            this.snapshotValue = snapshotValue;
            this.defaultValue = ConfigManager.getCodeDefault(field);
            this.label = Component.literal(prettify(field.getName()));
            this.description = meta.comment().trim();
            this.searchIndex = (field.getName() + " " + prettify(field.getName()) + " " + meta.category() + " " + description).toLowerCase().replace('_', ' ');
            this.widget = createWidget();
        }

        String categoryKey() {
            return meta.category().trim();
        }

        boolean matchesSearch(String query) {
            for (String chunk : query.split("\\s+")) {
                if (!chunk.isEmpty() && !searchIndex.contains(chunk)) {
                    return false;
                }

            }

            return true;
        }

        void setFocused(boolean focused) {
            widget.setFocused(focused);

            if (widget instanceof EditBox editBox) {
                editBox.setFocused(focused);
            }

        }

        void recalcHeight(int rowWidth) {
            final Minecraft minecraft = Minecraft.getInstance();
            final int lineHeight = minecraft.font.lineHeight;
            final int avail = rowWidth - widget.getWidth() - 54;

            descriptionMaxWidth = Math.min(avail, (int) (rowWidth * 0.5f));
            descriptionCache = (!description.isEmpty() && descriptionMaxWidth > 30) ? minecraft.font.split(Component.literal(description), descriptionMaxWidth) : List.of();
            descriptionLines = descriptionCache.size();

            final int contentHeight = lineHeight + (descriptionLines > 0 ? 2 + descriptionLines * lineHeight : 0);
            height = Math.max(Math.max(contentHeight + 12, widget.getHeight() + 12), 30);
        }

        private AbstractWidget createWidget() {
            final Minecraft minecraft = Minecraft.getInstance();
            final Class<?> type = field.getType();
            try {
                if (type == boolean.class) {
                    return new ToggleButton(0, 0, 44, 18, field.getBoolean(null), accent);
                }

                if (meta.slider() && Double.isFinite(meta.min()) && Double.isFinite(meta.max())) {
                    boolean isInt = type == int.class || type == long.class;
                    return new ConfigSlider(0, 0, 120, 18, meta.min(), meta.max(), ((Number) field.get(null)).doubleValue(), isInt, accent);
                }

                if (type.isEnum()) {
                    return new EnumCycleButton(0, 0, 100, 18, (Enum<?>[]) type.getEnumConstants(), (Enum<?>) field.get(null), accent);
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

        private Object currentValue() {
            final Class<?> type = field.getType();
            if (widget instanceof ToggleButton toggleButton) {
                return toggleButton.isToggled();
            }
            if (widget instanceof ConfigSlider configSlider) {
                return configSlider.getValueRaw();
            }
            if (widget instanceof EnumCycleButton enumCycleButton) {
                return enumCycleButton.getCurrent();
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

        List<Component> tooltipLines() {
            if (cachedTooltip != null) {
                return cachedTooltip;
            }

            final List<Component> lines = new ArrayList<>();
            final String note = meta.note().trim();
            if (!note.isEmpty()) {
                lines.add(Component.literal(note));
            }

            if (defaultValue != null) {
                lines.add(Component.translatable("tooltipoverhaul.config.tooltip.default", formatValue(defaultValue)).withStyle(style -> style.withColor(0x7A7A7A)));
            }

            if (Double.isFinite(meta.min()) && Double.isFinite(meta.max())) {
                final boolean isInt = field.getType() == int.class || field.getType() == long.class;
                final String min = isInt ? String.valueOf((long) meta.min()) : trimDouble(meta.min());
                final String max = isInt ? String.valueOf((long) meta.max()) : trimDouble(meta.max());
                lines.add(Component.translatable("tooltipoverhaul.config.tooltip.range", min, max).withStyle(style -> style.withColor(0x7A7A7A)));
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
            if (value instanceof Double d) {
                return trimDouble(d);
            }
            if (value instanceof Float f) {
                return trimDouble(f);
            }
            if (value instanceof Enum<?> e) {
                return prettify(e.name());
            }

            return String.valueOf(value);
        }

        private static String trimDouble(double value) {
            if (value == Math.floor(value) && !Double.isInfinite(value)) {
                return String.valueOf((long) value);
            }

            return String.valueOf(value);
        }

        void writeToField() {
            try {
                final Class<?> type = field.getType();
                if (type == boolean.class && widget instanceof ToggleButton toggleButton) {
                    field.setBoolean(null, toggleButton.isToggled()); return;
                }
                if (widget instanceof ConfigSlider configSlider) {
                    double valueRaw = configSlider.getValueRaw();
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
                if (widget instanceof EnumCycleButton enumCycleButton) {
                    field.set(null, enumCycleButton.getCurrent()); return;
                }
                if (widget instanceof EditBox box) {
                    String raw = box.getValue().trim();
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
                else if (widget instanceof EnumCycleButton enumCycleButton && defaultValue instanceof Enum<?> enumType) {
                    enumCycleButton.setCurrent(enumType);
                }
                else if (widget instanceof EditBox box) {
                    box.setValue(String.valueOf(defaultValue));
                }

            }
            catch (Exception ignored) {
                ;;
            }

        }

        void render(GuiGraphics graphics, int index, int top, int left, int width, int mx, int my, boolean hovered, float entrance, float partialTick) {
            final Minecraft minecraft = Minecraft.getInstance();
            final int lineHeight = minecraft.font.lineHeight;

            if (!hovered) {
                hoverStart = 0;
            }

            final float target = hovered ? 1f : 0f;
            final float diff = target - hoverAnim;
            hoverAnim += diff * 0.12f;
            if (Math.abs(diff) < 0.005f) {
                hoverAnim = target;
            }

            float e = AnimationUtils.smoothstep(0f, 1f, hoverAnim);

            // Entrance slide (from the right) and fade
            final int slide = (int) ((1f - entrance) * 16f);
            left += slide;

            final int alpha = (int) (0xFF * entrance);
            final int[] accent = rgb(this.accent);

            // Card body brightens slightly on hover
            final int accentDim = dimAccent(this.accent);
            final int bg = (alpha << 24) | mixRgb(0x151517, 0x1B1B1F, e);
            final int border = (alpha << 24) | mixRgb(0x26262A, accentDim, e);
            drawCard(graphics, left, top, left + width, top + height, bg, border);

            drawCardHoverFx(graphics, left, top, width, height, e * entrance, this.accent, index * 73 + field.getName().hashCode());

            // Focused text boxes expand leftwards
            float textFade = 1f;
            if (widget instanceof StyledEditBox box) {
                widget.setWidth(box.layoutWidth(width - 40));
                textFade = 1f - box.expandProgress();
            }

            final int textAlpha = (int) (alpha * textFade);
            final int contentH = lineHeight + (descriptionLines > 0 ? 2 + descriptionLines * lineHeight : 0);
            final int contentTop = top + (height - contentH) / 2;
            final int labelX = left + 10;

            if (textAlpha >= 0x10) {
                graphics.drawString(minecraft.font, label, labelX, contentTop, withAlpha(mixRgb(0xC4C4C4, 0xFFFFFF, e), textAlpha), false);

                int afterLabel = labelX + minecraft.font.width(label);

                if (meta.requiresRestart()) {
                    graphics.drawString(minecraft.font, "\u27f3", afterLabel + 4, contentTop, withAlpha(0xFF8844, textAlpha), false);
                    afterLabel += 4 + minecraft.font.width("\u27f3");
                }

                // accent dot right after the label if the value was changed
                if (isModified()) {
                    graphics.fill(afterLabel + 5, contentTop + 2, afterLabel + 8, contentTop + 5, (textAlpha << 24) | (accent[0] << 16) | (accent[1] << 8) | accent[2]);
                }

            }

            // Description
            if (!descriptionCache.isEmpty() && textAlpha >= 0x10) {
                int commentY = contentTop + lineHeight + 2;
                for (FormattedCharSequence line : descriptionCache) {
                    graphics.drawString(minecraft.font, line, labelX, commentY, withAlpha(mixRgb(0x646464, 0x8C8C8C, e), textAlpha), false);
                    commentY += lineHeight;
                }

            }

            final int widgetX = left + width - widget.getWidth() - 8;
            final int widgetY = top + (height - widget.getHeight()) / 2;

            widget.setX(widgetX);
            widget.setY(widgetY);

            swatchHits.clear();
            if (meta.color() && widget instanceof EditBox editBox && textFade > 0.5f && alpha >= 0x10) {
                int swatchX = widgetX - 24 - 14;
                final int swatchY = top + (height - 10) / 2;

                final String[] parts = editBox.getValue().split("[,;]");
                for (int p = parts.length - 1; p >= 0; p--) {
                    final String part = parts[p].trim();
                    if (!(part.startsWith("#") || part.startsWith("0x") || part.startsWith("0X"))) {
                        continue;
                    }

                    final boolean swatchHovered = mx >= swatchX && mx < swatchX + 10 && my >= swatchY && my < swatchY + 10;
                    final int color = ConfigColorParser.parseColor(part);
                    graphics.fill(swatchX, swatchY, swatchX + 10, swatchY + 10, withAlpha(swatchHovered ? 0xAAAAAA : 0x2E2E32, alpha));
                    graphics.fill(swatchX + 1, swatchY + 1, swatchX + 9, swatchY + 9, (alpha << 24) | (color & 0x00FFFFFF));

                    swatchHits.add(new int[] { swatchX, swatchY, p });
                    swatchX -= 12;
                }

            }

            // Reset button
            if (e > 0.3f && defaultValue != null) {
                final int resetX = widgetX - 24;
                final int resetY = top + (height - 18) / 2;
                final boolean resetHovered = mx >= resetX && mx < resetX + 18 && my >= resetY && my < resetY + 18;
                final int resetAlpha = (int) (0xFF * (e - 0.3f) / 0.7f);

                final int boxBg = withAlpha(resetHovered ? 0x202024 : 0x1A1A1C, resetAlpha);
                final int boxBorder = withAlpha(resetHovered ? accentDim : 0x2E2E32, resetAlpha);
                drawCard(graphics, resetX, resetY, resetX + 18, resetY + 18, boxBg, boxBorder);

                if (resetAlpha >= 0x10) {
                    final int resetColor = resetHovered ? withAlpha(this.accent & 0x00FFFFFF, resetAlpha) : withAlpha(0x8A8A8A, resetAlpha);
                    final float glyphScale = 2.4f;
                    final float glyphW = minecraft.font.width("\u21ba") * glyphScale;
                    final float glyphH = minecraft.font.lineHeight * glyphScale;

                    graphics.pose().pushPose();
                    graphics.pose().translate(resetX + (18 - glyphW) / 2f + 1.5f, resetY + (18 - glyphH) / 2f - 0.5f, 0);
                    graphics.pose().scale(glyphScale, glyphScale, 1f);
                    graphics.drawString(minecraft.font, "\u21ba", 0, 0, resetColor, false);
                    graphics.pose().popPose();
                }

            }

            widget.render(graphics, mx, my, partialTick);
        }

        boolean clickedReset(double mx, double my, int button, int rowLeft, int rowTop, int rowWidth) {
            if (button != 0 || hoverAnim <= 0.3f || defaultValue == null) {
                return false;
            }

            final int widgetX = rowLeft + rowWidth - widget.getWidth() - 8;
            final int resetX = widgetX - 24;
            final int resetY = rowTop + (height - 18) / 2;
            if (mx >= resetX && mx < resetX + 18 && my >= resetY && my < resetY + 18) {
                resetToDefault();
                playClickSound();
                return true;
            }

            return false;
        }

        boolean mouseClicked(double mx, double my, int button) {
            return widget.mouseClicked(mx, my, button);
        }

        boolean mouseReleased(double mx, double my, int button) {
            return widget.mouseReleased(mx, my, button);
        }

        boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
            return widget.mouseDragged(mx, my, button, dx, dy);
        }

        boolean keyPressed(int key, int s, int m) {
            return widget.keyPressed(key, s, m);
        }

        boolean charTyped(char c, int m) {
            return widget.charTyped(c, m);
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

    static class SearchBox extends EditBox {

        private float focusAnim = 0f;
        private final int accent;

        SearchBox(Font font, int x, int y, int width, int height, int accent) {
            super(font, x, y, width, height, Component.literal("Search"));
            this.accent = accent;
            setBordered(false);
            setTextColor(0xFFCCCCCC);
            setTextColorUneditable(0xFF555555);
            setMaxLength(128);
            setHint(Component.translatable("tooltipoverhaul.config.search"));
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (button == 0 && !getValue().isEmpty()) {
                final int zoneX = getX() + getWidth() - 12;
                if (mx >= zoneX && mx < getX() + getWidth() && my >= getY() && my < getY() + getHeight()) {
                    setValue("");
                    playClickSound();
                    return true;
                }

            }

            return super.mouseClicked(mx, my, button);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick) {
            final int x0 = getX();
            final int y0 = getY();
            final int width = getWidth();
            final int height = getHeight();
            final int x1 = x0 + width;
            final int y1 = y0 + height;

            final boolean hovered = mx >= x0 && mx < x1 && my >= y0 && my < y1;
            final boolean active = hovered || isFocused();

            focusAnim = active ? Math.min(1f, focusAnim + 0.12f) : Math.max(0f, focusAnim - 0.06f);

            // Card body matching the rest of the screen
            final int borderTarget = isFocused() ? (this.accent & 0x00FFFFFF) : dimAccent(this.accent);
            final int boxBorder = 0xFF000000 | mixRgb(0x26262A, borderTarget, focusAnim);
            drawCard(graphics, x0, y0, x1, y1, 0xFF151517, boxBorder);

            // Magnifying glass icon (for the search bar)
            final Minecraft minecraft = Minecraft.getInstance();
            final int[] accent = rgb(this.accent);
            final float time = focusAnim;
            final int iconRed = (int) (0x48 + ((accent[0] - 0x48) * time));
            final int iconGreen = (int) (0x48 + ((accent[1] - 0x48) * time));
            final int iconBlue = (int) (0x48 + ((accent[2] - 0x48) * time));
            final int iconColor = 0xFF000000 | (iconRed << 16) | (iconGreen << 8) | iconBlue;
            final float scale = 2.45f;
            final int iconOriginX = x0 + 3;
            final int iconOriginY = y0 + (height - (int)(minecraft.font.lineHeight * scale)) / 2 - 3;

            graphics.pose().pushPose();

            graphics.pose().translate(iconOriginX, iconOriginY, 0);
            graphics.pose().scale(scale, scale, 1f);
            graphics.drawString(minecraft.font, "\u2315", 0, 0, iconColor, false);

            graphics.pose().popPose();

            // Clear "x" when there is text
            if (!getValue().isEmpty()) {
                final boolean clearHovered = mx >= x1 - 12 && mx < x1 && my >= y0 && my < y1;
                graphics.drawString(minecraft.font, "\u00d7", x1 - 9, y0 + (height - minecraft.font.lineHeight) / 2 + 1, clearHovered ? this.accent : 0xFF666666, false);
            }

            // Renders text
            final int textPaddingX = 20;
            final int lineHeight = minecraft.font.lineHeight;
            final int offsetY = (height - lineHeight) / 2 + 1;

            graphics.pose().pushPose();

            graphics.pose().translate(textPaddingX, offsetY, 0);

            super.renderWidget(graphics, mx - textPaddingX, my - offsetY, partialTick);

            graphics.pose().popPose();
        }

    }

    static class ToggleButton extends AbstractWidget {

        private boolean toggled;
        private float animation;
        private final int accent;

        ToggleButton(int x, int y, int width, int height, boolean initial, int accent) {
            super(x, y, width, height, Component.empty());
            this.toggled = initial;
            this.animation = initial ? 1f : 0f;
            this.accent = accent;
        }

        boolean isToggled() {
            return toggled;
        }

        void setToggled(boolean toggled) {
            this.toggled = toggled;
            animation = toggled ? 1f : 0f;
        }

        @Override
        public void onClick(double mx, double my) {
            toggled = !toggled;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick) {
            animation = toggled ? Math.min(1f, animation + 0.2f) : Math.max(0f, animation - 0.2f);
            final float e = AnimationUtils.smoothstep(0f, 1f, animation);

            final int x0 = getX();
            final int y0 = getY();
            final int width = getWidth();
            final int height = getHeight();
            final int[] accent = rgb(this.accent);
            final int dark = 0xFF000000 | ((accent[0] / 4) << 16) | ((accent[1] / 4) << 8) | (accent[2] / 4);

            final int border = lerp(0xFF3A3A3E, 0xFF000000 | dimAccent(this.accent), e);
            drawCard(graphics, x0, y0, x0 + width, y0 + height, lerp(0xFF2A2A2E, dark, e), border);

            final int padding = 3;
            final int knobWidth = 14;
            final int knobX = x0 + padding + (int) ((width - knobWidth - padding * 2) * e);

            graphics.fill(knobX, y0 + padding, knobX + knobWidth, y0 + height - padding, lerp(0xFF555555, this.accent, e));

            // Soft glow
            if (e > 0.5f) {
                final int glowAlpha = (int) (0x30 * (e - 0.5f) * 2f);
                graphics.fill(knobX - 1, y0 + padding, knobX, y0 + height - padding, (glowAlpha << 24) | (accent[0] << 16) | (accent[1] << 8) | accent[2]);
                graphics.fill(knobX + knobWidth, y0 + padding, knobX + knobWidth + 1, y0 + height - padding, (glowAlpha << 24) | (accent[0] << 16) | (accent[1] << 8) | accent[2]);
            }

            final Minecraft minecraft = Minecraft.getInstance();
            final int textY = y0 + (height - minecraft.font.lineHeight) / 2 + 1;
            if (e > 0.6f) {
                final int alpha = (int) (0xFF * (e - 0.6f) / 0.4f);
                if (alpha >= 0x10) {
                    graphics.drawString(minecraft.font, "ON", x0 + 6, textY, withAlpha(this.accent & 0x00FFFFFF, alpha), false);
                }

            }
            else if (e < 0.4f) {
                final int alpha = (int) (0xFF * (0.4f - e) / 0.4f);
                if (alpha >= 0x10) {
                    final String text = "OFF";
                    graphics.drawString(minecraft.font, text, x0 + width - 5 - minecraft.font.width(text), textY, withAlpha(0x666666, alpha), false);
                }

            }

        }

        private static int lerp(int from, int origin, float t) {
            final int finalAlpha = (from >> 24) & 0xFF;
            final int finalRed = (from >> 16) & 0xFF;
            final int finalGreen = (from >> 8) & 0xFF;
            final int finalBlue = from & 0xFF;
            final int totalAlpha = (origin >> 24) & 0xFF;
            final int totalRed = (origin >> 16) & 0xFF;
            final int totalGreen = (origin >> 8) & 0xFF;
            final int totalBlue = origin & 0xFF;
            return ((finalAlpha + (int) ((totalAlpha - finalAlpha) * t)) << 24) | ((finalRed + (int) ((totalRed - finalRed) * t)) << 16) | ((finalGreen + (int) ((totalGreen - finalGreen) * t)) << 8)  | (finalBlue + (int) ((totalBlue - finalBlue) * t));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            ;;
        }

    }

    static class ConfigSlider extends AbstractWidget {

        private final double min;
        private final double max;
        private double value;
        private final boolean intMode;
        private final int accent;
        private boolean dragging = false;
        private float activeAnim = 0f;

        private boolean editing = false;
        private String editBuffer = "";

        ConfigSlider(int x, int y, int w, int h, double min, double max, double value, boolean intMode, int accent) {
            super(x, y, w, h, Component.empty());
            this.min = min;
            this.max = max;
            this.value = Math.max(min, Math.min(max, value));
            this.intMode = intMode;
            this.accent = accent;
        }

        double getValueRaw() {
            return intMode ? Math.round(value) : value;
        }

        void setValueRaw(double value) {
            this.value = Math.max(min, Math.min(max, value));
        }

        private double frac() {
            return max <= min ? 0 : (value - min) / (max - min);
        }

        private void fromMouse(double mx) {
            double maxed = Math.max(0, Math.min(1, (mx - getX()) / (double) getWidth()));
            value = min + maxed * (max - min);
            if (intMode) {
                value = Math.round(value);
            }

        }

        @Override
        public void onClick(double mx, double my) {
            if (editing) {
                commitEdit();
                return;
            }

            if (Screen.hasControlDown() && Screen.hasShiftDown()) {
                editing = true;
                editBuffer = formatCurrent();
                return;
            }

            dragging = true;
            fromMouse(mx);
        }

        @Override
        protected void onDrag(double mx, double my, double dx, double dy) {
            if (dragging) {
                fromMouse(mx);
            }

        }

        @Override
        public void onRelease(double mx, double my) {
            dragging = false;
        }

        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);

            if (!focused && editing) {
                commitEdit();
            }

        }

        @Override
        public boolean keyPressed(int key, int s, int m) {
            if (!editing) {
                return false;
            }

            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                commitEdit();
                return true;
            }
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                editing = false;
                return true;
            }
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (!editBuffer.isEmpty()) {
                    editBuffer = editBuffer.substring(0, editBuffer.length() - 1);
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean charTyped(char c, int m) {
            if (!editing) {
                return false;
            }

            if (Character.isDigit(c) || c == '-' || (!intMode && (c == '.' || c == ','))) {
                editBuffer += (c == ',' ? '.' : c);
            }

            return true;
        }

        private void commitEdit() {
            editing = false;
            try {
                setValueRaw(Double.parseDouble(editBuffer.trim()));
                if (intMode) {
                    value = Math.round(value);
                }

            }
            catch (NumberFormatException ignored) {
                ;;
            }

        }

        private String formatCurrent() {
            if (intMode) {
                return String.valueOf((long) getValueRaw());
            }

            final double raw = getValueRaw();
            if (raw == Math.floor(raw) && !Double.isInfinite(raw)) {
                return String.valueOf((long) raw);
            }

            return String.format(Locale.ROOT, "%.6f", raw).replaceAll("0+$", "").replaceAll("\\.$", "");
        }


        @Override
        protected void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick) {
            final int x0 = getX();
            final int y0 = getY();
            final int width = getWidth();
            final int height = getHeight();
            final int[] accent = rgb(this.accent);

            final boolean hovered = mx >= x0 && mx < x0 + width && my >= y0 && my < y0 + height;
            activeAnim = (hovered || dragging) ? Math.min(1f, activeAnim + 0.15f) : Math.max(0f, activeAnim - 0.1f);

            final Minecraft minecraft = Minecraft.getInstance();

            if (editing) {
                drawCard(graphics, x0, y0, x0 + width, y0 + height, 0xFF161618, this.accent);

                final boolean caret = (Util.getMillis() / 400L) % 2 == 0;
                final String text = editBuffer + (caret ? "_" : "");
                graphics.drawString(minecraft.font, text, x0 + (width - minecraft.font.width(text)) / 2, y0 + (height - minecraft.font.lineHeight) / 2 + 1, 0xFFE8E8E8, false);
                return;
            }

            final int border = mixRgb(0x3A3A3E, dimAccent(this.accent), activeAnim);
            drawCard(graphics, x0, y0, x0 + width, y0 + height, 0xFF1A1A1C, 0xFF000000 | border);

            int fillWidth = (int) (width * frac());
            if (fillWidth > 1) {
                final float boost = 1f + activeAnim * 0.4f;
                final int fr = Math.min(255, (int) (accent[0] / 3f * boost));
                final int fg = Math.min(255, (int) (accent[1] / 3f * boost));
                final int fb = Math.min(255, (int) (accent[2] / 3f * boost));
                graphics.fill(x0 + 1, y0 + 1, Math.min(x0 + fillWidth, x0 + width - 1), y0 + height - 1, 0xFF000000 | (fr << 16) | (fg << 8) | fb);
            }

            int thumbX = x0 + Math.max(0, Math.min(width - 3, fillWidth - 1));
            graphics.fill(thumbX, y0, thumbX + 3, y0 + height, this.accent);

            if (activeAnim > 0.05f) {
                final int glowAlpha = (int) (0x50 * activeAnim);
                graphics.fill(thumbX - 1, y0 + 1, thumbX, y0 + height - 1, (glowAlpha << 24) | (accent[0] << 16) | (accent[1] << 8) | accent[2]);
                graphics.fill(thumbX + 3, y0 + 1, thumbX + 4, y0 + height - 1, (glowAlpha << 24) | (accent[0] << 16) | (accent[1] << 8) | accent[2]);
            }

            final String text = intMode ? String.valueOf((long) getValueRaw()) : String.format("%.3f", getValueRaw());
            graphics.drawString(minecraft.font, text, x0 + (width - minecraft.font.width(text)) / 2, y0 + (height - minecraft.font.lineHeight) / 2 + 1, 0xFFD0D0D0, false);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            ;;
        }

    }

    static class EnumCycleButton extends AbstractWidget {

        private final Enum<?>[] values;
        private int idx;
        private float hoverAnim = 0f;
        private final int accent;

        EnumCycleButton(int x, int y, int width, int height, Enum<?>[] values, Enum<?> current, int accent) {
            super(x, y, width, height, Component.empty());
            this.values = values;
            this.accent = accent;
            this.idx = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i] == current) {
                    idx = i;
                    break;
                }

            }

        }

        Enum<?> getCurrent() {
            return values[idx];
        }

        void setCurrent(Enum<?> value) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] == value) {
                    idx = i;
                    return;
                }

            }

        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (!active || !visible || (button != 0 && button != 1) || !clicked(mx, my)) {
                return false;
            }

            playDownSound(Minecraft.getInstance().getSoundManager());
            idx = Math.floorMod(idx + (button == 1 ? -1 : 1), values.length);

            return true;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick) {
            final int x0 = getX();
            final int y0 = getY();
            final int width = getWidth();
            final int height = getHeight();
            final boolean hovering = mx >= x0 && mx < x0 + width && my >= y0 && my < y0 + height;
            hoverAnim = hovering ? Math.min(1f, hoverAnim + 0.15f) : Math.max(0f, hoverAnim - 0.1f);

            final int border = 0xFF000000 | mixRgb(0x3A3A3E, dimAccent(accent), AnimationUtils.smoothstep(0f, 1f, hoverAnim));
            drawCard(graphics, x0, y0, x0 + width, y0 + height, 0xFF18181A, border);

            if (values.length > 1 && values.length <= 12) {
                final int pipsW = values.length * 3 - 1;
                int pipX = x0 + (width - pipsW) / 2;
                final int[] ac = rgb(accent);
                for (int i = 0; i < values.length; i++) {
                    final int color = i == idx ? (0xFF000000 | (ac[0] << 16) | (ac[1] << 8) | ac[2]) : 0xFF333333;
                    graphics.fill(pipX, y0 + height - 3, pipX + 2, y0 + height - 2, color);
                    pipX += 3;
                }

            }

            final Minecraft minecraft = Minecraft.getInstance();
            final String text = prettify(values[idx].name());

            graphics.drawString(minecraft.font, text, x0 + (width - minecraft.font.width(text)) / 2, y0 + (height - minecraft.font.lineHeight) / 2, 0xFFD0D0D0, false);

            final int alphaY = y0 + (height - minecraft.font.lineHeight) / 2 + 1;
            graphics.drawString(minecraft.font, "\u2190", x0 + 3, alphaY, border, false);
            graphics.drawString(minecraft.font, "\u2192", x0 + width - 10, alphaY, border, false);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            ;;
        }

    }

    public static class StyledEditBox extends EditBox {

        private float focusAnim = 0f;
        private float expandAnim = 0f;
        private final int baseWidth;
        private final int accent;
        private Predicate<String> validator = raw -> true;

        StyledEditBox(net.minecraft.client.gui.Font font, int x, int y, int width, int height, Component message, int accent) {
            super(font, x, y, width, height, message);
            this.baseWidth = width;
            this.accent = accent;
            setBordered(false);
            setTextColor(0xFFD0D0D0);
            setTextColorUneditable(0xFF666666);
            setMaxLength(512);
        }

        void setValidator(Predicate<String> validator) {
            this.validator = validator;
        }

        @Override
        public void setValue(String text) {
            super.setValue(text);
            if (!isFocused()) {
                setCursorPosition(0);
                setHighlightPos(0);
            }

        }

        int layoutWidth(int expandedMax) {
            final float target = isFocused() ? 1f : 0f;
            expandAnim += (target - expandAnim) * 0.2f;
            if (Math.abs(target - expandAnim) < 0.01f) {
                expandAnim = target;
            }

            if (expandedMax <= baseWidth || expandAnim <= 0f) {
                return baseWidth;
            }

            return baseWidth + (int) ((expandedMax - baseWidth) * AnimationUtils.smoothstep(0f, 1f, expandAnim));
        }

        float expandProgress() {
            return AnimationUtils.smoothstep(0f, 1f, expandAnim);
        }

        @Override
        public int getInnerWidth() {
            return this.width - 12;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick) {
            final int x0 = getX();
            final int y0 = getY();
            final int x1 = x0 + getWidth();
            final int y1 = y0 + getHeight();
            boolean enable = (mx >= x0 && mx < x1 && my >= y0 && my < y1) || isFocused();
            focusAnim = enable ? Math.min(1f, focusAnim + 0.15f) : Math.max(0f, focusAnim - 0.1f);

            final boolean valid = validator.test(getValue());

            final int borderTarget = isFocused() ? (accent & 0x00FFFFFF) : dimAccent(accent);
            final int border = !valid ? 0xFFCC4444 : 0xFF000000 | mixRgb(0x3A3A3E, borderTarget, focusAnim);
            drawCard(graphics, x0, y0, x1, y1, valid ? 0xFF161618 : 0xFF221214, border);

            int lineHeight = Minecraft.getInstance().font.lineHeight;
            int offsetY = (this.height - lineHeight) / 2 + 1;
            int paddingX = 5;

            graphics.enableScissor(x0 + 1, y0 + 1, x1 - 1, y1 - 1);

            graphics.pose().pushPose();
            graphics.pose().translate(paddingX, offsetY, 0);
            super.renderWidget(graphics, mx - paddingX, my - offsetY, partialTick);
            graphics.pose().popPose();

            graphics.disableScissor();
        }

    }

    static class FlatButton extends Button {

        private float hoverAnim = 0f;
        private final int accent;
        private final boolean primary;

        private final long createdAt = Util.getMillis();
        private int entranceDelay = 0;
        private int entranceAlpha = 0xFF;

        FlatButton(int x, int y, int width, int height, Component message, OnPress onPress, int accent) {
            this(x, y, width, height, message, onPress, accent, false);
        }

        FlatButton(int x, int y, int width, int height, Component message, OnPress onPress, int accent, boolean primary) {
            super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
            this.accent = accent;
            this.primary = primary;
        }

        FlatButton withEntranceDelay(int delayMs) {
            this.entranceDelay = delayMs;
            return this;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick) {
            final float entrance = AnimationUtils.easeOutCubic(Mth.clamp((Util.getMillis() - createdAt - entranceDelay) / 180f, 0f, 1f));
            entranceAlpha = (int) (0xFF * entrance);
            if (entranceAlpha < 0x10) {
                return;
            }

            final int rise = (int) ((1f - entrance) * 4f);
            final int x0 = getX();
            final int y0 = getY() + rise;
            final int x1 = x0 + getWidth();
            final int y1 = y0 + getHeight();
            boolean hovering = mx >= x0 && mx < x1 && my >= y0 && my < y1;
            hoverAnim = hovering ? Math.min(1f, hoverAnim + 0.15f) : Math.max(0f, hoverAnim - 0.1f);

            if (primary) {
                renderPrimary(graphics, x0, y0, x1, y1);
            }
            else {
                renderSecondary(graphics, x0, y0, x1, y1);
            }

            final Minecraft minecraft = Minecraft.getInstance();
            final int textColor = !active ? 0x666666 : (primary ? 0xFFFFFF : 0xD0D0D0);
            graphics.drawCenteredString(minecraft.font, getMessage(), x0 + width / 2, y0 + (this.height - minecraft.font.lineHeight) / 2 + 1, withAlpha(textColor, entranceAlpha));
        }

        private void renderPrimary(GuiGraphics graphics, int x0, int y0, int x1, int y1) {
            final int[] ac = rgb(accent);

            final float boost = 0.32f + hoverAnim * 0.18f;
            final int red = (int) (ac[0] * boost);
            final int green = (int) (ac[1] * boost);
            final int blue = (int) (ac[2] * boost);

            drawCard(graphics, x0, y0, x1, y1, (entranceAlpha << 24) | (red << 16) | (green << 8) | blue, withAlpha(accent & 0x00FFFFFF, entranceAlpha));
        }

        private void renderSecondary(GuiGraphics graphics, int x0, int y0, int x1, int y1) {
            final float e = AnimationUtils.smoothstep(0f, 1f, hoverAnim);
            final int bg = (entranceAlpha << 24) | mixRgb(0x161618, 0x1E1E22, e);
            final int border = (entranceAlpha << 24) | mixRgb(0x2A2A2E, dimAccent(accent), e);

            drawCard(graphics, x0, y0, x1, y1, bg, border);
        }

    }

}
