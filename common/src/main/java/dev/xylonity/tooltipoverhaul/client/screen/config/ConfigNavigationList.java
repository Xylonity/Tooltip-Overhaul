package dev.xylonity.tooltipoverhaul.client.screen.config;

import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.drawNotchedBorder;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.dimAccent;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.withAlpha;

/**
 * Navigation rail used by configuration categories
 */
class ConfigNavigationList<T> extends AbstractWidget {

    private static final int TOP_PAD = 18;
    private static final long INDICATOR_MOVE_DURATION = 360L;
    private static final long INDICATOR_EXPAND_DURATION = 280L;

    private List<T> items;
    private final Adapter<T> adapter;
    private final int accent;
    private final Consumer<Integer> onSelect;
    private final @Nullable BiConsumer<Integer, Integer> onReorder;
    private final Map<T, Float> hoverAnimations = new IdentityHashMap<>();

    private int selected = -1;
    private boolean dimmed;
    private double scroll;

    private long revealStart = -1L;
    private float indicatorY = Float.NaN;
    private float indicatorStartY = Float.NaN;
    private float indicatorTargetY = Float.NaN;
    private long indicatorMoveStart;
    private long indicatorExpandStart;

    private int pressedIndex = -1;
    private double pressX;
    private double pressY;
    private double dragX;
    private double dragY;
    private boolean draggingEntry;
    private boolean draggingScrollbar;
    private double scrollbarDragOffset;
    private boolean reorderingEnabled = true;

    ConfigNavigationList(int x, int y, int width, int height, Component title, int accent, List<T> items, Adapter<T> adapter, Consumer<Integer> onSelect) {
        this(x, y, width, height, title, accent, items, adapter, onSelect, null);
    }

    ConfigNavigationList(int x, int y, int width, int height, Component title, int accent, List<T> items, Adapter<T> adapter, Consumer<Integer> onSelect, @Nullable BiConsumer<Integer, Integer> onReorder) {
        super(x, y, width, height, title);
        this.items = items;
        this.adapter = adapter;
        this.accent = accent;
        this.onSelect = onSelect;
        this.onReorder = onReorder;
    }

    void setBounds(int x, int y, int width, int height) {
        setX(x);
        setY(y);
        setWidth(width);
        this.height = height;
        clampScroll();
    }

    void setItems(List<T> items) {
        this.items = items;
        hoverAnimations.clear();
        reveal();
        selected = Mth.clamp(selected, -1, items.size() - 1);
        clampScroll();
    }

    void setSelectedIndex(int index) {
        final int next = Mth.clamp(index, -1, items.size() - 1);
        if (selected != next) {
            selected = next;
        }

    }

    int selectedIndex() {
        return selected;
    }

    void setDimmed(boolean dimmed) {
        this.dimmed = dimmed;
    }

    void setReorderingEnabled(boolean reorderingEnabled) {
        this.reorderingEnabled = reorderingEnabled;
        if (!reorderingEnabled) {
            cancelDrag();
        }

    }

    void reveal() {
        revealStart = -1L;
        resetIndicator();
    }

    void resetScroll() {
        scroll = 0;
    }

    void revealSelected() {
        if (selected < 0 || selected >= items.size()) {
            return;
        }

        final double middle = offsetOf(selected) + rowHeight(selected) / 2.0;
        scroll = Mth.clamp(middle - height / 2.0, 0, maxScroll());
    }

    void tickDrag() {
        if (!draggingEntry || pressedIndex < 0) {
            return;
        }

        if (dragX < getX() || dragX >= getX() + width || dragY < getY() || dragY >= getY() + height) {
            return;
        }

        if (dragY < getY() + TOP_PAD + 16) {
            scroll = Math.max(0, scroll - 6);
        }

        if (dragY > getY() + height - 16) {
            scroll = Math.min(maxScroll(), scroll + 6);
        }

    }

    boolean isDraggingEntry() {
        return draggingEntry;
    }

    void cancelDrag() {
        pressedIndex = -1;
        draggingEntry = false;
        draggingScrollbar = false;
    }

    private int rowHeight(int index) {
        return adapter.rowHeight(items.get(index), index, Math.max(1, width - 12));
    }

    private int offsetOf(int index) {
        int offset = TOP_PAD;
        for (int i = 0; i < Math.min(index, items.size()); i++) {
            offset += rowHeight(i);
        }

        return offset;
    }

    private int contentHeight() {
        return offsetOf(items.size()) + 4;
    }

    private double maxScroll() {
        return Math.max(0, contentHeight() - height);
    }

    private void clampScroll() {
        scroll = Mth.clamp(scroll, 0, maxScroll());
    }

    private @Nullable ConfigScroll.Geometry scrollbarGeometry() {
        return ConfigScroll.fromContent(getY(), getY() + height, contentHeight(), scroll);
    }

    private int indexAt(double screenY) {
        final double contentY = screenY - getY() + scroll;
        int rowY = TOP_PAD;
        for (int i = 0; i < items.size(); i++) {
            int rowHeight = rowHeight(i);
            if (contentY >= rowY && contentY < rowY + rowHeight) {
                return i;
            }

            rowY += rowHeight;
        }

        return -1;
    }

    private int dropIndex(double x, double y) {
        if (pressedIndex < 0 || x < getX() || x >= getX() + width - 8 || y < getY() + TOP_PAD - 2 || y >= getY() + height - 2) {
            return -1;
        }

        return ConfigReorderLayout.destinationIndex(rowHeights(), pressedIndex, y - getY() + scroll, TOP_PAD);
    }

    private int visualY(int itemIndex, int gapIndex) {
        if (!draggingEntry) {
            return getY() + offsetOf(itemIndex) - (int) scroll;
        }

        return getY() + ConfigReorderLayout.itemOffset(rowHeights(), itemIndex, pressedIndex, gapIndex, TOP_PAD) - (int) scroll;
    }

    private List<Integer> rowHeights() {
        final List<Integer> heights = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            heights.add(rowHeight(i));
        }

        return heights;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }

        final long now = Util.getMillis();
        if (revealStart < 0L) {
            revealStart = now;
        }

        clampScroll();
        final int x0 = getX();
        final int y0 = getY();
        final int x1 = x0 + width;
        final int y1 = y0 + height;

        graphics.fill(x0, y0, x1, y1, 0xFF101010);

        for (int i = 0; i < 4; i++) {
            graphics.fill(x0, y0 + i, x1, y0 + i + 1, (Math.max(0, 30 - i * 8)) << 24);
        }

        drawNotchedBorder(graphics, x0, y0, x1, y1, 0xFF1C1C20);

        final float dim = dimmed ? 0.35f : 1f;
        graphics.drawString(Minecraft.getInstance().font, getMessage(), x0 + 8, y0 + 6, withAlpha(0x585858, (int) (0xFF * dim)), false);

        final int gapIndex = draggingEntry ? dropIndex(mouseX, mouseY) : -1;
        final int draggedHeight = pressedIndex >= 0 && pressedIndex < items.size() ? rowHeight(pressedIndex) : 0;

        graphics.enableScissor(x0, y0 + TOP_PAD, x1, y1 - 2);
        renderIndicator(graphics, gapIndex, dim, now);

        int position = 0;
        for (int i = 0; i < items.size(); i++) {
            if (draggingEntry && i == pressedIndex) {
                continue;
            }

            if (draggingEntry && position == gapIndex) {
                final int gapY = getY() + ConfigReorderLayout.gapOffset(rowHeights(), pressedIndex, gapIndex, TOP_PAD) - (int) scroll;
                renderDropGap(graphics, gapY, draggedHeight);
            }

            final int rowY = visualY(i, gapIndex);
            renderRow(graphics, i, rowY, mouseX, mouseY, dim, false);
            position++;
        }

        if (draggingEntry && gapIndex == items.size() - 1) {
            final int gapY = getY() + ConfigReorderLayout.gapOffset(rowHeights(), pressedIndex, gapIndex, TOP_PAD) - (int) scroll;
            renderDropGap(graphics, gapY, draggedHeight);
        }

        if (items.isEmpty()) {
            final String empty = I18n.get("tooltipoverhaul.config.empty");
            graphics.drawString(Minecraft.getInstance().font, empty, x0 + (width - Minecraft.getInstance().font.width(empty)) / 2, (y0 + y1) / 2 - 4, 0xFF555555, false);
        }

        graphics.disableScissor();

        ConfigScroll.render(graphics, x1, scrollbarGeometry(), accent, mouseX, mouseY, draggingScrollbar);

        if (draggingEntry && pressedIndex >= 0 && pressedIndex < items.size()) {
            final int floatingX = Mth.clamp(mouseX + 10, 2, Math.max(2, Minecraft.getInstance().getWindow().getGuiScaledWidth() - width - 2));
            int floatingY = Mth.clamp(mouseY - draggedHeight / 2, 2, Minecraft.getInstance().getWindow().getGuiScaledHeight() - draggedHeight - 2);
            graphics.pose().pushPose();

            graphics.pose().translate(0, 0, 500);
            renderRow(graphics, pressedIndex, floatingY, floatingX, floatingY, 1f, true, floatingX);

            graphics.pose().popPose();
        }

    }

    private void renderIndicator(GuiGraphics graphics, int gapIndex, float dim, long now) {
        if (selected < 0 || selected >= items.size() || (draggingEntry && selected == pressedIndex)) {
            resetIndicator();
            return;
        }

        final int selectedY = visualY(selected, gapIndex) - getY() + (int) scroll;
        if (Float.isNaN(indicatorY)) {
            indicatorY = indicatorStartY = indicatorTargetY = selectedY;
            indicatorMoveStart = indicatorExpandStart = now;
        }
        else if (Math.abs(selectedY - indicatorTargetY) > 0.01f) {
            indicatorStartY = indicatorY;
            indicatorTargetY = selectedY;
            indicatorMoveStart = now;
            indicatorExpandStart = now + INDICATOR_MOVE_DURATION;
        }

        final float move = Mth.clamp((now - indicatorMoveStart) / (float) INDICATOR_MOVE_DURATION, 0f, 1f);
        indicatorY = AnimationUtils.lerp(indicatorStartY, indicatorTargetY, AnimationUtils.easeInOutCubic(move));
        final float expand = AnimationUtils.easeOutCubic(Mth.clamp((now - indicatorExpandStart) / (float) INDICATOR_EXPAND_DURATION, 0f, 1f));
        final int indicatorWidth = 2 + Math.round((width - 8) * expand);
        final int y = getY() + Math.round(indicatorY) - (int) scroll;
        float entrance = entranceProgress(selected, now);
        graphics.fill(getX() + 3, y, Math.min(getX() + width - 3, getX() + 3 + indicatorWidth), y + rowHeight(selected) - 2, withAlpha(0x242428, (int) (0xFF * dim * entrance)));
    }

    private void resetIndicator() {
        indicatorY = indicatorStartY = indicatorTargetY = Float.NaN;
        indicatorMoveStart = indicatorExpandStart = 0;
    }

    private void renderDropGap(GuiGraphics graphics, int y, int gapHeight) {
        final int top = y + 2;
        final int bottom = y + gapHeight - 4;
        if (bottom <= top) {
            return;
        }

        graphics.fill(getX() + 6, top, getX() + width - 8, bottom, withAlpha(dimAccent(accent), 0x18));
        drawNotchedBorder(graphics, getX() + 6, top, getX() + width - 8, bottom, withAlpha(dimAccent(accent), 0x80));
    }

    private void renderRow(GuiGraphics graphics, int index, int rowY, int mouseX, int mouseY, float dim, boolean floating) {
        renderRow(graphics, index, rowY, mouseX, mouseY, dim, floating, getX());
    }

    private void renderRow(GuiGraphics graphics, int index, int rowY, int mouseX, int mouseY, float dim, boolean floating, int originX) {
        final T item = items.get(index);
        final int rowHeight = rowHeight(index);
        if (!floating && (rowY + rowHeight < getY() + TOP_PAD || rowY > getY() + height)) {
            return;
        }

        final long now = Util.getMillis();
        final float entrance = floating ? 1f : entranceProgress(index, now);
        final int slide = floating ? 0 : (int) ((1f - entrance) * -12f);
        final int left = originX + 3 + slide;
        final int right = originX + width - 3 + slide;
        final boolean isSelected = index == selected;
        boolean hovered = !dimmed && mouseX >= left && mouseX < right && mouseY >= rowY && mouseY < rowY + rowHeight - 2;
        final float hover = hoverAnimations.getOrDefault(item, 0f);
        final float nextHover = hover + ((hovered ? 1f : 0f) - hover) * 0.15f;
        hoverAnimations.put(item, nextHover);
        final float hoverEase = AnimationUtils.smoothstep(0f, 1f, nextHover);
        final int alpha = (int) (0xFF * entrance * dim);

        if (floating) {
            ConfigScreenStyle.drawCard(graphics, left, rowY, right, rowY + rowHeight - 2, 0xF022222A, dropIndex(dragX, dragY) >= 0 ? accent : 0xFF777783);
        }
        else if (!isSelected) {
            final int bgAlpha = (int) ((0x12 + 0x18 * hoverEase) * entrance * dim);
            graphics.fill(left, rowY, right, rowY + rowHeight - 2, (bgAlpha << 24) | 0x1A1A1A);
        }

        if (alpha < 0x10) {
            return;
        }

        final int leading = adapter.leadingWidth(item, index);
        final int trailing = adapter.trailingWidth(item, index);

        adapter.renderLeading(graphics, item, index, left + 5, rowY, rowHeight, alpha, isSelected, hovered);
        adapter.renderTrailing(graphics, item, index, right - 5, rowY, rowHeight, alpha, isSelected, hovered);

        final Minecraft minecraft = Minecraft.getInstance();
        final int labelX = left + 7 + leading;
        final int labelRight = right - 7 - trailing;
        final Component description = adapter.description(item, index);
        final int labelColor = isSelected ? 0xE8E8E8 : hovered ? 0xC8C8C8 : 0x808080;
        if (!description.getString().isEmpty()) {
            graphics.drawString(minecraft.font, minecraft.font.plainSubstrByWidth(adapter.label(item, index).getString(), Math.max(4, labelRight - labelX)), labelX, rowY + 5, withAlpha(labelColor, alpha), false);
            graphics.drawString(minecraft.font, minecraft.font.plainSubstrByWidth(description.getString(), Math.max(4, labelRight - labelX)), labelX, rowY + 16, withAlpha(0x777780, alpha), false);
        }
        else {
            final Component label = adapter.label(item, index);
            if (adapter.wrapLabel(item, index)) {
                final List<FormattedCharSequence> lines = new ArrayList<>(minecraft.font.split(label, Math.max(4, labelRight - labelX)));
                final int blockHeight = Math.max(minecraft.font.lineHeight, lines.size() * minecraft.font.lineHeight + Math.max(0, lines.size() - 1));
                int labelY = rowY + (rowHeight - 2 - blockHeight) / 2;
                for (final FormattedCharSequence line : lines) {
                    graphics.drawString(minecraft.font, line, labelX, labelY, withAlpha(labelColor, alpha), false);
                    labelY += minecraft.font.lineHeight + 1;
                }

            }
            else {
                final int labelY = rowY + (rowHeight - 2 - minecraft.font.lineHeight) / 2 + 1;
                if (minecraft.font.width(label) <= labelRight - labelX) {
                    graphics.drawString(minecraft.font, label, labelX, labelY, withAlpha(labelColor, alpha), false);
                }
                else {
                    renderScrollingString(graphics, minecraft.font, label, labelX, rowY, labelRight, rowY + rowHeight - 2, withAlpha(labelColor, alpha));
                }

            }

        }

    }

    private float entranceProgress(int index, long now) {
        return AnimationUtils.easeOutCubic(Mth.clamp((now - revealStart - index * 40L) / 180f, 0f, 1f));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !active || dimmed || mouseX < getX() || mouseX >= getX() + width || mouseY < getY() || mouseY >= getY() + height) {
            return false;
        }

        final ConfigScroll.Geometry bar = scrollbarGeometry();
        if (button == 0 && bar != null && mouseX >= getX() + width - 8) {
            if (mouseY >= bar.thumbTop() && mouseY < bar.thumbTop() + bar.thumbHeight()) {
                draggingScrollbar = true;
                scrollbarDragOffset = mouseY - bar.thumbTop();
            }
            else {
                scroll = ConfigScroll.valueFromMouse(mouseY, bar, maxScroll());
            }

            return true;
        }

        final int index = indexAt(mouseY);
        if (button == 0 && index >= 0) {
            selected = index;
            onSelect.accept(index);

            playDownSound(Minecraft.getInstance().getSoundManager());

            if (onReorder != null && reorderingEnabled) {
                pressedIndex = index;
                pressX = dragX = mouseX;
                pressY = dragY = mouseY;
                draggingEntry = false;
            }

            return true;
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingScrollbar && button == 0) {
            final ConfigScroll.Geometry bar = scrollbarGeometry();
            if (bar != null) {
                scroll = ConfigScroll.valueFromDrag(mouseY, scrollbarDragOffset, bar, maxScroll());
            }

            return true;
        }

        if (pressedIndex >= 0 && button == 0) {
            dragX = mouseX;
            dragY = mouseY;
            draggingEntry |= Math.hypot(mouseX - pressX, mouseY - pressY) > 3;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingScrollbar && button == 0) {
            draggingScrollbar = false;
            return true;
        }

        if (pressedIndex >= 0 && button == 0) {
            final int from = pressedIndex;
            final int target = dropIndex(mouseX, mouseY);
            final boolean wasDragging = draggingEntry;
            pressedIndex = -1;
            draggingEntry = false;
            if (wasDragging && target >= 0 && target != from && onReorder != null) {
                onReorder.accept(from, target);
                selected = Mth.clamp(target, -1, items.size() - 1);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double delta) {
        if (!visible || mouseX < getX() || mouseX >= getX() + width || mouseY < getY() || mouseY >= getY() + height || maxScroll() <= 0) {
            return false;
        }

        scroll = Mth.clamp(scroll - delta * 18, 0, maxScroll());
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        ;;
    }

    interface Adapter<T> {

        Component label(T item, int index);

        default Component description(T item, int index) {
            return Component.empty();
        }

        default int rowHeight(T item, int index, int contentWidth) {
            return description(item, index).getString().isEmpty() ? 22 : 28;
        }

        default int leadingWidth(T item, int index) {
            return 0;
        }

        default int trailingWidth(T item, int index) {
            return 0;
        }

        default boolean wrapLabel(T item, int index) {
            return false;
        }

        default void renderLeading(GuiGraphics graphics, T item, int index, int x, int y, int rowHeight, int alpha, boolean selected, boolean hovered) {
            ;;
        }

        default void renderTrailing(GuiGraphics graphics, T item, int index, int right, int y, int rowHeight, int alpha, boolean selected, boolean hovered) {
            ;;
        }

    }

}
