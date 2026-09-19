package dev.xylonity.tooltipoverhaul.client.screen.config;

import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import dev.xylonity.tooltipoverhaul.config.wrapper.ConfigEntry;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.*;

/**
 * Scrollable configuration form
 */
final class ConfigPanel extends AbstractWidget {

    private final List<ConfigEntryRow> allRows = new ArrayList<>();
    private final List<ConfigEntryRow> visibleRows = new ArrayList<>();

    private double scroll = 0;
    private double targetScroll = 0;
    private boolean draggingScrollbar = false;
    private int dragOffset = 0;
    private ConfigEntryRow focused = null;
    final int accent;

    private String query = "";
    private String category = null;

    private long revealStart = -1L;

    private List<Component> pendingTooltip = null;
    private ConfigEntryRow pendingTooltipRow = null;
    private ConfigEntryRow tooltipRow = null;
    private long tooltipSince = 0;
    private int tooltipX;
    private int tooltipY;

    private static final int PADDING = 8;
    private static final int CATEGORY_HEIGHT = 18;

    interface SwatchClickListener {
        void onSwatchClicked(ConfigEntryRow row, int partIndex, int x, int y);
    }

    private SwatchClickListener swatchClickListener = null;
    private final ConfigModalHost modals;
    private final int screenWidth;
    private final int screenHeight;

    void setSwatchClickListener(SwatchClickListener listener) {
        this.swatchClickListener = listener;
    }

    ConfigPanel(int x, int y, int width, int height, int accent, ConfigModalHost modals, int screenWidth, int screenHeight) {
        super(x, y, width, height, Component.empty());
        this.accent = accent;
        this.modals = modals;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    void tickPanel() {
        ;;
    }

    void addEntry(Field field, ConfigEntry configEntry, Object snapshotValue) {
        allRows.add(new ConfigEntryRow(field, configEntry, snapshotValue, accent, modals, screenWidth, screenHeight, this::applyToFields));
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

        for (ConfigEntryRow entryRow : allRows) {
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

        revealStart = -1L;
        targetScroll = 0;
        scroll = 0;
    }

    private boolean showHeaders() {
        if (!query.isBlank() || category == null) {
            final Set<String> keys = new HashSet<>();
            for (ConfigEntryRow entryRow : visibleRows) {
                keys.add(entryRow.categoryKey());
            }

            return keys.size() > 1;
        }

        return false;
    }

    void applyToFields() {
        for (ConfigEntryRow entryRow : allRows) {
            entryRow.writeToField();
        }

    }

    void resetAllToDefault() {
        for (ConfigEntryRow entryRow : allRows) {
            entryRow.resetToDefault();
        }

    }

    int visibleCount() {
        return visibleRows.size();
    }

    int countModified() {
        int count = 0;
        for (ConfigEntryRow entryRow : allRows) {
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
        final int width = contentWidth();
        for (ConfigEntryRow entryRow : visibleRows) {
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
                height += ConfigFormEntry.ROW_GAP;
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
            final double dragging = targetScroll - scroll;
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
        if (revealStart < 0L) {
            revealStart = now;
        }

        final int rowLength = rowLeft();
        final int rowWidth = contentWidth();
        int categoryY = y + topPad() - (int) scroll;
        String lastCategory = null;

        for (int i = 0; i < visibleRows.size(); i++) {
            ConfigEntryRow row = visibleRows.get(i);
            if (i > 0) {
                categoryY += ConfigFormEntry.ROW_GAP;
            }

            String category = row.categoryKey();
            final float entrance = AnimationUtils.easeOutCubic(Mth.clamp((now - revealStart - Math.min(i, 12) * 35L) / 170f, 0f, 1f));
            if (headers && !category.equals(lastCategory)) {
                if (categoryY + CATEGORY_HEIGHT >= y && categoryY <= addY) {
                    renderCategoryHeader(graphics, categoryLabel(category), rowLength, categoryY, rowWidth, entrance);
                }

                categoryY += CATEGORY_HEIGHT;
            }

            lastCategory = category;

            final int rowH = row.height;
            if (categoryY + rowH >= y && categoryY <= addY) {
                final boolean inPanel = mx >= x && mx <= addX && my >= y && my <= addY;
                final boolean hovered = inPanel && mx >= rowLength && mx <= rowLength + rowWidth && my >= categoryY && my <= categoryY + rowH;

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
                final float fade = 1f - i / 10f;
                graphics.fill(x, y + i, addX - 4, y + i + 1, ((int) (0xD0 * fade * fade) << 24) | 0x111111);
            }

        }

        if (scroll < maxScroll - 1) {
            for (int i = 0; i < 10; i++) {
                final float fade = 1f - i / 10f;
                graphics.fill(x, addY - 1 - i, addX - 4, addY - i, ((int) (0xD0 * fade * fade) << 24) | 0x111111);
            }

        }

    }

    private void renderEmptyState(GuiGraphics graphics, int x, int y, int addY) {
        final Minecraft minecraft = Minecraft.getInstance();
        final String text = query.isBlank() ? I18n.get("tooltipoverhaul.config.empty") : I18n.get("tooltipoverhaul.config.empty_search", query);
        final int textY = y + (addY - y - minecraft.font.lineHeight) / 2;

        graphics.drawString(minecraft.font, text, x + (width - minecraft.font.width(text)) / 2, textY, 0xFF4A4A4A, false);
    }

    private void captureTooltip(ConfigEntryRow row, int mx, int my, int rowLeft, int rowWidth) {
        if (row.swatchHovered) {
            pendingTooltip = List.of(Component.translatable("tooltipoverhaul.config.swatch_hint"));
            pendingTooltipRow = row;
            tooltipX = mx;
            tooltipY = my;
            return;
        }

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

        final List<Component> lines = row.tooltipLines();
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
        final int maxTextWidth = 200;
        final List<FormattedCharSequence> wrapped = new ArrayList<>();
        for (Component line : pendingTooltip) {
            wrapped.addAll(font.split(line, maxTextWidth));
        }

        ConfigTooltipRenderer.render(graphics, font, List.of(new ConfigTooltipRenderer.Section(wrapped, 0xDDDDDD)), tooltipX, tooltipY, screenWidth, screenHeight, 4, accent, fade);
    }

    private void renderCategoryHeader(GuiGraphics graphics, String category, int x, int y, int width, float entrance) {
        final int slide = Math.round((1f - entrance) * 16f);
        renderSectionHeader(graphics, Minecraft.getInstance().font, category, x + slide, y,
                width, accent, Math.round(0xFF * entrance));
    }

    private void renderScrollbar(GuiGraphics graphics, int mx, int my) {
        if (!scrollable()) {
            return;
        }

        ConfigScroll.render(graphics, getX() + width, scrollbarGeometry(), accent, mx, my, draggingScrollbar);
    }

    private ConfigScroll.Geometry scrollbarGeometry() {
        return ConfigScroll.fromContent(getY(), getY() + height, totalContentHeight(), scroll);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double deltaX, double delta) {
        if (!visible || !isIn(mx, my) || !scrollable()) {
            return false;
        }

        draggingScrollbar = false;
        targetScroll -= delta * 24.0;

        clampScroll();

        return true;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!visible || !active || !isIn(mx, my)) {
            return false;
        }

        if (scrollable()) {
            final int tx = getX() + width - 6;
            if (mx >= tx) {
                ConfigScroll.Geometry bar = scrollbarGeometry();
                final double maximum = Math.max(1, totalContentHeight() - height);
                if (my >= bar.thumbTop() && my <= bar.thumbTop() + bar.thumbHeight()) {
                    draggingScrollbar = true;
                    dragOffset = (int) my - bar.thumbTop();
                }
                else {
                    targetScroll = ConfigScroll.valueFromMouse(my, bar, maximum);
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
            final ConfigEntryRow row = visibleRows.get(i);
            if (i > 0) {
                contentY += ConfigFormEntry.ROW_GAP;
            }

            final String category = row.categoryKey();
            if (headers && !category.equals(lastCategory)) {
                contentY += CATEGORY_HEIGHT;
            }

            lastCategory = category;
            if (my >= contentY && my <= contentY + row.height && mx >= rowLeft() && mx <= rowLeft() + contentWidth()) {
                for (int[] hit : row.swatchHits) {
                    if (button == 0 && mx >= hit[0] && mx < hit[0] + hit[3] && my >= hit[1] && my < hit[1] + hit[3]) {
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
            final ConfigScroll.Geometry bar = scrollbarGeometry();
            if (bar == null || bar.span() <= 0) {
                return true;
            }

            targetScroll = ConfigScroll.valueFromDrag(my, dragOffset, bar, Math.max(1, totalContentHeight() - height));
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
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        return (focused != null && focused.keyPressed(key, scanCode, modifiers)) || super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        return (focused != null && focused.charTyped(character, modifiers)) || super.charTyped(character, modifiers);
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

    private void setFocusedRow(ConfigEntryRow entryRow) {
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