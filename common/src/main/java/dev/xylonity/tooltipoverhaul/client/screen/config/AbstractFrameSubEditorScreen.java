package dev.xylonity.tooltipoverhaul.client.screen.config;

import com.google.gson.JsonObject;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.HEADER_HEIGHT;
import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.drawCard;
import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.restoreGuiRenderState;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.dimAccent;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.withAlpha;

/**
 * Shared navigation/form/preview composition for the focused frame editors
 */
abstract class AbstractFrameSubEditorScreen extends AbstractConfigScreen {

    protected final JsonObject root;
    protected final JsonObject draft;
    protected final ItemStack sample;
    protected final Consumer<JsonObject> apply;
    protected final List<AbstractWidget> controls = new ArrayList<>();
    protected AbstractWidget focusedControl;

    private final List<ConfigFormEntry> controlRows = new ArrayList<>();
    private final List<Integer> navigationItems = new ArrayList<>();
    private final ConfigThreePaneLayout panelLayout = new ConfigThreePaneLayout(10, 8, HEADER_HEIGHT + 6, 36, 36, 130, 112, 220, 140);
    private ConfigNavigationList<Integer> navigationList;
    private double controlScroll;
    private boolean draggingControlBar;
    private double controlBarOffset;
    private String previewError = "";
    private final FramePreviewPanel previewPanel;
    private JsonObject previousPreview;
    private ConfigFormEntry hoveredControlRow;
    private long hoveredControlSince;
    private int hoverMouseX;
    private int hoverMouseY;

    private long controlRevealAt = -1L;

    protected AbstractFrameSubEditorScreen(Component title, Screen parent, JsonObject root, JsonObject entry, ItemStack sample, int accent, Consumer<JsonObject> apply) {
        super(title, parent, accent);
        this.root = root.deepCopy();
        this.draft = entry.deepCopy();
        this.sample = sample.isEmpty() ? new ItemStack(Items.DIAMOND_SWORD) : sample.copy();
        this.previewPanel = new FramePreviewPanel(accent);
        this.apply = apply;
    }

    protected static Component text(String key) {
        return Component.translatable("tooltipoverhaul.config.frames." + key);
    }

    protected final int listLeft() {
        return panelLayout.left();
    }

    protected final int listWidth() {
        return panelLayout.leftWidth();
    }

    protected final int listTop() {
        return panelLayout.top();
    }

    protected final int listBottom() {
        return panelLayout.leftBottom();
    }

    protected final int formLeft() {
        return panelLayout.centerLeft();
    }

    protected final int formWidth() {
        return panelLayout.centerRight() - panelLayout.centerLeft();
    }

    protected final int formTop() {
        return panelLayout.top();
    }

    protected final int formBottom() {
        return panelLayout.contentBottom();
    }

    protected final int previewLeft() {
        return panelLayout.rightLeft();
    }

    protected final int previewWidth() {
        return panelLayout.rightWidth();
    }

    protected final int previewBottom() {
        return panelLayout.contentBottom();
    }

    protected abstract int entryCount();
    protected abstract int selectedEntryIndex();
    protected abstract Component entryLabel(int index);
    protected abstract void addToolbarWidgets();
    protected abstract void buildControls();
    protected abstract void syncControls();
    protected abstract void selectEntry(int index);
    protected abstract void configurePreview(JsonObject previewEntry);

    protected Component entryDescription(int index) {
        return Component.empty();
    }

    protected int entryRowHeight(int index, int contentWidth) {
        return entryDescription(index).getString().isEmpty() ? 22 : 30;
    }

    protected int entryLeadingWidth(int index) {
        return 0;
    }

    protected int entryTrailingWidth(int index) {
        return 0;
    }

    protected void renderEntryLeading(GuiGraphics graphics, int index, int x, int y, int rowHeight, int alpha, boolean selected, boolean hovered) {
        ;;
    }

    protected void renderEntryTrailing(GuiGraphics graphics, int index, int right, int y, int rowHeight, int alpha, boolean selected, boolean hovered) {
        ;;
    }

    protected Component navigationTitle() {
        return Component.literal(title.getString().toUpperCase(Locale.ROOT));
    }

    protected Component subtitle() {
        return Component.empty();
    }

    protected void renderEmptyList(GuiGraphics graphics) {
        ;;
    }

    @Override
    protected final void initScreen() {
        panelLayout.resize(width, height);
        rebuildNavigation();
        addToolbarWidgets();
        addCenteredFooterButtons(100, 8,
                new FooterAction(text("effect_apply"), () -> {commitControls();apply.accept(draft);onClose();}, true),
                new FooterAction(Component.translatable("gui.cancel"), this::onClose, false));
        buildControls();
        clampControlScroll();
    }

    private void rebuildNavigation() {
        navigationItems.clear();
        for (int i = 0; i < entryCount(); i++) {
            navigationItems.add(i);
        }

        final ConfigNavigationList.Adapter<Integer> adapter = new ConfigNavigationList.Adapter<>() {

            @Override
            public Component label(Integer item, int index) {
                return entryLabel(index);
            }

            @Override
            public Component description(Integer item, int index) {
                return entryDescription(index);
            }

            @Override
            public int rowHeight(Integer item, int index, int contentWidth) {
                return entryRowHeight(index, contentWidth);
            }

            @Override
            public int leadingWidth(Integer item, int index) {
                return entryLeadingWidth(index);
            }

            @Override
            public int trailingWidth(Integer item, int index) {
                return entryTrailingWidth(index);
            }

            @Override
            public void renderLeading(GuiGraphics graphics, Integer item, int index, int x, int y, int rowHeight, int alpha, boolean selected, boolean hovered) {
                renderEntryLeading(graphics, index, x, y, rowHeight, alpha, selected, hovered);
            }

            @Override
            public void renderTrailing(GuiGraphics graphics, Integer item, int index, int right, int y, int rowHeight, int alpha, boolean selected, boolean hovered) {
                renderEntryTrailing(graphics, index, right, y, rowHeight, alpha, selected, hovered);
            }

        };

        navigationList = new ConfigNavigationList<>(listLeft(), listTop(), listWidth(), listBottom() - listTop(), navigationTitle(), accent, navigationItems, adapter, this::selectNavigationEntry);
        navigationList.setSelectedIndex(selectedEntryIndex());
    }

    private void selectNavigationEntry(int index) {
        commitControls();
        selectEntry(index);
        navigationList.setSelectedIndex(selectedEntryIndex());
    }

    protected final void refreshNavigation() {
        if (navigationItems.size() != entryCount()) {
            rebuildNavigation();
        }
        else if (navigationList != null) {
            navigationList.setSelectedIndex(selectedEntryIndex());
        }

        if (navigationList != null) {
            navigationList.setMessage(navigationTitle());
        }

    }

    protected final void addControl(AbstractWidget widget, Component label, String description, String key) {
        addControl(widget, label, description, key, null);
    }

    protected final void addControl(AbstractWidget widget, Component label, String description, String key, @Nullable Runnable reset) {
        controls.add(widget);
        controlRows.add(new SubEditorFormEntry(label, description, widget, key, reset));
    }

    protected final void addControl(AbstractWidget widget, Component label, String key) {
        addControl(widget, label, "", key, null);
    }

    protected final void addControl(AbstractWidget widget, Component label, String key, @Nullable Runnable reset) {
        addControl(widget, label, "", key, reset);
    }

    protected final void addColorControl(StyledEditBox box, Component label, String key, IntSupplier color, BiConsumer<Integer, Integer> openPicker, @Nullable Runnable reset) {
        controls.add(box);
        SubEditorFormEntry entry = new SubEditorFormEntry(label, "", box, key, reset);
        entry.swatchColor = color;
        entry.swatchOpen = openPicker;
        controlRows.add(entry);
    }

    protected static boolean isHexColor(String raw) {
        return raw != null && raw.trim().matches("(?i)(#|0x)[0-9a-f]{6}([0-9a-f]{2})?");
    }

    protected final void clearControls() {
        controls.clear();
        controlRows.clear();
        focusedControl = null;
        controlRevealAt = -1L;
    }

    private final class SubEditorFormEntry extends ConfigFormEntry {

        private final @Nullable Runnable reset;
        @Nullable IntSupplier swatchColor;
        @Nullable BiConsumer<Integer, Integer> swatchOpen;
        boolean swatchHovered;

        SubEditorFormEntry(Component label, String description, AbstractWidget widget, String key, @Nullable Runnable reset) {
            super(label, description, widget, key, AbstractFrameSubEditorScreen.this.accent, 32);
            this.reset = reset;
        }

        private int swatchX() {
            return widget == null ? 0 : widget.getX() - 16;
        }

        private int swatchY() {
            return widget == null ? 0 : widget.getY() + 3;
        }

        @Override
        protected void renderAccessories(GuiGraphics graphics, int mouseX, int mouseY, int widgetX, int widgetY, int alpha, float textFade, float entrance, float hover) {
            swatchHovered = false;
            int resetX = widgetX - 24;
            if (swatchColor != null && textFade > 0.5f) {
                final int sx = widgetX - 16, sy = widgetY + 3;
                swatchHovered = mouseX >= sx && mouseX < sx + 12 && mouseY >= sy && mouseY < sy + 12;
                ConfigScreenStyle.drawColorSwatch(graphics, sx, sy, 12, swatchColor.getAsInt(), accent, swatchHovered, alpha);
                resetX -= 16;
            }

            if (reset == null || hover <= 0.3f) {
                return;
            }

            int resetY = currentY + (height - 18) / 2;
            final boolean resetHovered = mouseX >= resetX && mouseX < resetX + 18 && mouseY >= resetY && mouseY < resetY + 18;
            final int resetAlpha = (int) (0xFF * (hover - 0.3f) / 0.7f);
            drawCard(graphics, resetX, resetY, resetX + 18, resetY + 18, withAlpha(resetHovered ? 0x202024 : 0x1A1A1C, resetAlpha), withAlpha(resetHovered ? dimAccent(accent) : 0x2E2E32, resetAlpha));
            ConfigIconButton.drawIcon(graphics, ConfigIconButton.Icon.REPEAT, resetX + (18 - ConfigIconButton.Icon.REPEAT.width) / 2,
                    resetY + (18 - ConfigIconButton.Icon.REPEAT.height) / 2, resetHovered ? withAlpha(accent & 0x00FFFFFF, resetAlpha) : withAlpha(0x8A8A8A, resetAlpha));
        }

        boolean clickedSwatch(double mouseX, double mouseY) {
            if (swatchOpen == null || widget == null) {
                return false;
            }

            final int sx = swatchX(), sy = swatchY();
            if (mouseX >= sx && mouseX < sx + 12 && mouseY >= sy && mouseY < sy + 12) {
                ConfigScreenStyle.playClickSound();
                swatchOpen.accept(sx, sy);
                return true;
            }

            return false;
        }

        boolean clickedReset(double mouseX, double mouseY) {
            if (reset == null || widget == null || hoverAnim <= 0.3f) {
                return false;
            }

            final int resetX = widget.getX() - 24 - (swatchColor != null ? 16 : 0);
            final int resetY = currentY + (height - 18) / 2;
            if (mouseX >= resetX && mouseX < resetX + 18 && mouseY >= resetY && mouseY < resetY + 18) {
                ConfigScreenStyle.playClickSound();
                reset.run();
                return true;
            }

            return false;
        }

        @Override
        protected void prepareWidgetWidth(int rowWidth) {
            final int controlWidth = Math.min(Math.max(40, rowWidth - 28), Math.min(220, Math.max(100, (int) (rowWidth * 0.42f))));
            if (widget instanceof StyledEditBox box) {
                box.setBaseWidth(controlWidth);
            }
            else if (widget != null) {
                widget.setWidth(controlWidth);
            }

        }

        @Override
        protected int expandedWidgetWidth(int rowWidth) {
            return rowWidth - 16;
        }

    }

    protected final void commitControls() {
        if (focusedControl != null) {
            focusedControl.setFocused(false);
        }

        syncControls();
    }

    protected final void rebuildScreen() {
        clearWidgets();
        init();
    }

    private int controlsContentHeight() {
        int total = 12;
        int rowWidth = Math.max(1, formWidth() - 14);
        for (final ConfigFormEntry row : controlRows) {
            row.recalcHeight(rowWidth);
            total += row.height;
        }

        total += Math.max(0, controlRows.size() - 1) * ConfigFormEntry.ROW_GAP;

        return total;
    }

    private double maxControlScroll() {
        return Math.max(0, controlsContentHeight() - (formBottom() - formTop()));
    }

    private void clampControlScroll() {
        controlScroll = Mth.clamp(controlScroll, 0, maxControlScroll());
    }

    private ConfigScroll.Geometry controlScrollbar() {
        return ConfigScroll.fromContent(formTop(), formBottom(), controlsContentHeight(), controlScroll);
    }

    @Override
    public final void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        restoreGuiRenderState(graphics);
        syncControls();
        panelLayout.resize(width, height);
        refreshNavigation();
        clampControlScroll();
        renderScreenShell(graphics, subtitle().getString());

        navigationList.setBounds(listLeft(), listTop(), listWidth(), listBottom() - listTop());
        navigationList.render(graphics, modals.isOpen() ? -1 : mouseX, modals.isOpen() ? -1 : mouseY, partialTick);
        if (entryCount() == 0) {
            renderEmptyList(graphics);
        }

        renderForm(graphics, mouseX, mouseY, partialTick);
        restoreGuiRenderState(graphics);
        renderPreviewPanel(graphics, modals.isOpen() ? -1 : mouseX, modals.isOpen() ? -1 : mouseY);
        restoreGuiRenderState(graphics);

        super.render(graphics, modals.isOpen() ? -1 : mouseX, modals.isOpen() ? -1 : mouseY, partialTick);

        renderControlTooltip(graphics);
        renderModalLayer(graphics, mouseX, mouseY, partialTick);
    }

    private void renderForm(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        final int left = formLeft();
        final int right = left + formWidth();

        drawCard(graphics, left, formTop(), right, formBottom(), 0xE6111113, 0xFF232327);
        graphics.enableScissor(left, formTop() + 2, right, formBottom() - 2);

        int y = formTop() + 6 - (int) controlScroll;

        final long now = Util.getMillis();
        if (controlRevealAt < 0L) {
            controlRevealAt = now;
        }

        ConfigFormEntry hoverCandidate = null;
        for (int i = 0; i < controlRows.size(); i++) {
            if (i > 0) {
                y += ConfigFormEntry.ROW_GAP;
            }

            final ConfigFormEntry row = controlRows.get(i);
            final int rowLeft = left + 6;
            final int rowWidth = formWidth() - 14;

            row.recalcHeight(rowWidth);

            boolean hovered = !modals.isOpen() && mouseX >= rowLeft && mouseX < rowLeft + rowWidth && mouseY >= y && mouseY < y + row.height && mouseY >= formTop() && mouseY < formBottom();
            if (y + row.height >= formTop() && y <= formBottom()) {
                float entrance = dev.xylonity.tooltipoverhaul.client.util.AnimationUtils.easeOutCubic(Mth.clamp((now - controlRevealAt - Math.min(i, 12) * 35L) / 170f, 0f, 1f));

                row.render(graphics, i, y, rowLeft, rowWidth, mouseX, mouseY, hovered, entrance, partialTick);

                if (hovered && row.widget != null && mouseX < row.widget.getX() - 10) {
                    hoverCandidate = row;
                }

            }
            else if (row.widget != null) {
                row.widget.setY(-1000);
            }

            y += row.height;
        }

        graphics.disableScissor();

        if (hoveredControlRow != hoverCandidate) {
            hoveredControlRow = hoverCandidate;
            hoveredControlSince = Util.getMillis();
        }

        if (hoveredControlRow != null || mouseX >= left && mouseX < right) {
            hoverMouseX = mouseX;
            hoverMouseY = mouseY;
        }

        ConfigScroll.render(graphics, right, controlScrollbar(), accent, mouseX, mouseY, draggingControlBar);
    }

    private void renderControlTooltip(GuiGraphics graphics) {
        if (modals.isOpen()) {
            return;
        }

        for (final ConfigFormEntry row : controlRows) {
            if (row instanceof SubEditorFormEntry entry && entry.swatchHovered) {
                ConfigTooltipRenderer.render(graphics, font, List.of(new ConfigTooltipRenderer.Section(font.split(Component.translatable("tooltipoverhaul.config.swatch_hint"), 200), 0xFFFFFF)), hoverMouseX, hoverMouseY, width, height, HEADER_HEIGHT + 4, accent, 1f);
                return;
            }

        }

        if (hoveredControlRow == null || Util.getMillis() - hoveredControlSince < 400) {
            return;
        }

        List<FormattedCharSequence> lines = font.split(Component.translatable("tooltipoverhaul.config.frames.tooltip.key",
                        hoveredControlRow.stableKey).withStyle(style -> style.withColor(0x70707A)), 200);
        float fade = dev.xylonity.tooltipoverhaul.client.util.AnimationUtils.easeOutQuad(Mth.clamp((Util.getMillis() - hoveredControlSince - 400) / 120f, 0f, 1f));
        ConfigTooltipRenderer.render(graphics, font, List.of(new ConfigTooltipRenderer.Section(lines, 0xFFFFFF)), hoverMouseX, hoverMouseY, width, height, HEADER_HEIGHT + 4, accent, fade);
    }

    private void renderPreviewPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        final JsonObject entry = draft.deepCopy();

        configurePreview(entry);

        entry.addProperty("tooltipAppearAnimation", "none");
        entry.addProperty("tooltipDisappearAnimation", "none");
        entry.addProperty("iconAppearAnimation", "none");
        entry.addProperty("showSecondPanel", false);

        if (!entry.equals(previousPreview)) {
            previewError = previewPanel.refresh(root, entry);
            if (previousPreview == null) {
                previewPanel.selectStack(sample);
            }

            previousPreview = entry;
        }

        final int top = panelLayout.top() + 26;
        previewPanel.setBounds(previewLeft(), top, previewWidth(), previewBottom() - top);
        previewPanel.render(graphics, font, width, height, mouseX, mouseY, previewError);
    }

    @Override
    protected final boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        if (focusedControl != null && !focusedControl.isMouseOver(mouseX, mouseY)) {
            focusedControl.setFocused(false);
            focusedControl = null;
            syncControls();
        }

        if (previewPanel.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (navigationList != null && navigationList.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        ConfigScroll.Geometry scrollbar = controlScrollbar();
        if (button == 0 && scrollbar != null && mouseX >= formLeft() + formWidth() - 8 && mouseX < formLeft() + formWidth() && mouseY >= scrollbar.top() && mouseY < scrollbar.bottom()) {
            if (mouseY >= scrollbar.thumbTop() && mouseY < scrollbar.thumbTop() + scrollbar.thumbHeight()) {
                draggingControlBar = true;
                controlBarOffset = mouseY - scrollbar.thumbTop();
            }
            else {
                controlScroll = ConfigScroll.valueFromMouse(mouseY, scrollbar, maxControlScroll());
            }

            return true;
        }

        if (mouseX >= formLeft() && mouseX < formLeft() + formWidth() && mouseY >= formTop() && mouseY < formBottom()) {
            if (button == 0) {
                for (final ConfigFormEntry row : new ArrayList<>(controlRows)) {
                    if (row instanceof SubEditorFormEntry entry && (entry.clickedSwatch(mouseX, mouseY) || entry.clickedReset(mouseX, mouseY))) {
                        return true;
                    }

                }

            }

            for (final AbstractWidget widget : controls) {
                if (widget.mouseClicked(mouseX, mouseY, button)) {
                    if (focusedControl != null && focusedControl != widget) {
                        focusedControl.setFocused(false);
                    }

                    if (controls.contains(widget)) {
                        focusedControl = widget;
                        widget.setFocused(true);
                    }

                    return true;
                }

            }

            return true;
        }

        return super.handleMouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected final boolean handleMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (previewPanel.mouseDragged(deltaX, deltaY)) {
            return true;
        }

        if (navigationList != null && navigationList.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }

        if (draggingControlBar && button == 0) {
            final ConfigScroll.Geometry scrollbar = controlScrollbar();
            if (scrollbar != null) {
                controlScroll = ConfigScroll.valueFromDrag(mouseY, controlBarOffset, scrollbar, maxControlScroll());
            }

            return true;
        }

        return focusedControl != null && focusedControl.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) || super.handleMouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    protected final boolean handleMouseReleased(double mouseX, double mouseY, int button) {
        previewPanel.mouseReleased();

        final boolean listConsumed = navigationList != null && navigationList.mouseReleased(mouseX, mouseY, button);
        if (draggingControlBar && button == 0) {
            draggingControlBar = false;
            return true;
        }

        if (focusedControl != null) {
            focusedControl.mouseReleased(mouseX, mouseY, button);
        }

        syncControls();

        return listConsumed || super.handleMouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected final boolean handleMouseScrolled(double mouseX, double mouseY, double delta) {
        if (previewPanel.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }

        if (navigationList != null && navigationList.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }

        if (mouseX >= formLeft() && mouseX < formLeft() + formWidth() && mouseY >= formTop() && mouseY < formBottom()) {
            controlScroll = Mth.clamp(controlScroll - delta * 24, 0, maxControlScroll());
            return true;
        }

        return super.handleMouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected final boolean handleKeyPressed(int key, int scanCode, int modifiers) {
        return focusedControl != null && focusedControl.keyPressed(key, scanCode, modifiers) || super.handleKeyPressed(key, scanCode, modifiers);
    }

    @Override
    protected final boolean handleCharTyped(char character, int modifiers) {
        return focusedControl != null && focusedControl.charTyped(character, modifiers) || super.handleCharTyped(character, modifiers);
    }

    @Override
    protected final void tickScreen() {
        ;;
    }

    @Override
    public void onClose() {
        if (parent instanceof FrameEditorScreen editor && previousPreview != null) {
            editor.selectPreviewItem(previewPanel.selectedStack());
        }

        previewPanel.close();
        returnToParent();
    }

    @Override
    public void removed() {
        previewPanel.close();
        super.removed();
    }

}