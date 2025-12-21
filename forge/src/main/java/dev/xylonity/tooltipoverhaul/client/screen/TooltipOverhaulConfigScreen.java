package dev.xylonity.tooltipoverhaul.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.xylonity.tooltipoverhaul.config.ConfigManager;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.config.wrapper.ConfigEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TooltipOverhaulConfigScreen extends Screen {

    private final Screen parent;
    private ConfigPanel panel;
    private EditBox searchBox;
    private float searchHoverProgress = 0f;

    private static final int HEADER_HEIGHT = 64;

    public TooltipOverhaulConfigScreen(Screen parent) {
        super(Component.literal("Tooltip Overhaul Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int panelTop = HEADER_HEIGHT + 8;
        int panelBottom = this.height - 40;
        int panelHeight = Math.max(0, panelBottom - panelTop);

        this.panel = new ConfigPanel(20, panelTop, this.width - 40, panelHeight);
        this.addRenderableWidget(this.panel);

        int searchWidth = 240;
        int searchX = this.panel.getX() + 10;
        int searchY = HEADER_HEIGHT - 26;

        this.searchBox = new CenteredEditBox(this.font, searchX, searchY, searchWidth, 20, Component.literal("Search"));
        this.searchBox.setHint(Component.literal("Search settings..."));
        this.searchBox.setResponder(this::onSearchChanged);

        this.searchBox.setBordered(false);
        this.searchBox.setTextColor(0xFFE8E8E8);
        this.searchBox.setTextColorUneditable(0xFF777777);

        this.addRenderableWidget(this.searchBox);

        for (Field field : TooltipsConfig.class.getDeclaredFields()) {
            ConfigEntry meta = field.getAnnotation(ConfigEntry.class);
            if (meta == null) {
                continue;
            }

            field.setAccessible(true);
            this.panel.addConfigEntry(field, meta);
        }

        int buttonWidth = 120;
        int buttonHeight = 20;
        int spacing = 10;
        int totalWidth = buttonWidth * 2 + spacing;
        int startX = (this.width - totalWidth) / 2;
        int buttonY = this.height - 28;

        // DONE
        this.addRenderableWidget(
                new ConfigPanel.ModernButton(startX, buttonY, buttonWidth, buttonHeight, CommonComponents.GUI_DONE, (b) -> {
                    saveConfig();
                    this.minecraft.setScreen(this.parent);
                })

        );

        // CANCEL
        this.addRenderableWidget(
                new ConfigPanel.ModernButton(startX + buttonWidth + spacing, buttonY, buttonWidth, buttonHeight, Component.literal("Cancel"), (b) -> {
                    this.minecraft.setScreen(this.parent);
                })

        );

        this.panel.applySearch("");
    }

    private void onSearchChanged(String text) {
        if (this.panel != null) {
            this.panel.applySearch(text);
        }

    }

    private void saveConfig() {
        if (this.panel != null) {
            this.panel.applyToFields();
            ConfigManager.save(TooltipsConfig.class);
        }

    }

    @Override
    public void tick() {
        if (this.searchBox != null) {
            this.searchBox.tick();
        }

        if (this.panel != null) {
            this.panel.tickPanel();
        }

        super.tick();
    }

    @Override
    public void onClose() {
        saveConfig();
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xFF101010);

        renderHeader(graphics);
        renderSearchBackground(graphics, mouseX, mouseY);

        renderFooterSeparator(graphics);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderFooterSeparator(GuiGraphics graphics) {
        int lineW = (int) (this.width * 0.80f);
        int x0 = (this.width - lineW) / 2;
        int x1 = x0 + lineW;

        int y = this.height - 36;
        int h = 1;

        int red = 0x4D;
        int green = 0x4D;
        int blue = 0x4D;

        int centerX = x0 + (lineW / 2);

        PoseStack pose = graphics.pose();
        Matrix4f matrix = pose.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // Left
        buffer.vertex(matrix, x0, y + h, 0).color(red, green, blue, 0).endVertex();
        buffer.vertex(matrix, centerX, y + h, 0).color(red, green, blue, 255).endVertex();
        buffer.vertex(matrix, centerX, y, 0).color(red, green, blue, 255).endVertex();
        buffer.vertex(matrix, x0, y, 0).color(red, green, blue, 0).endVertex();

        // Right
        buffer.vertex(matrix, centerX, y + h, 0).color(red, green, blue, 255).endVertex();
        buffer.vertex(matrix, x1, y + h, 0).color(red, green, blue, 0).endVertex();
        buffer.vertex(matrix, x1, y, 0).color(red, green, blue, 0).endVertex();
        buffer.vertex(matrix, centerX, y, 0).color(red, green, blue, 255).endVertex();

        BufferUploader.drawWithShader(buffer.end());

        //RenderSystem.disableBlend();
    }

    private void renderHeader(GuiGraphics graphics) {
        for (int i = 0; i < HEADER_HEIGHT; i++) {
            int alpha = Math.max(0, 80 - (i * 2));
            if (alpha <= 0) {
                continue;
            }

            int color = (alpha << 24);
            graphics.fill(0, i, this.width, i + 1, color);
        }

        String brandTitle = "Tooltip Overhaul";
        int brandTitleWidth = this.font.width(brandTitle);
        int brandTitleX = this.width - 18 - brandTitleWidth;
        int brandTitleY = 10;
        graphics.drawString(this.font, brandTitle, brandTitleX, brandTitleY, 0xFFFFFFFF, false);

        String brandSubtitle = "Every tooltip, more modern, sharper, cleaner.";
        int brandSubtitleWidth = this.font.width(brandSubtitle);
        int brandSubtitleX = this.width - 18 - brandSubtitleWidth;
        int brandSubtitleY = brandTitleY + 12;
        graphics.drawString(this.font, brandSubtitle, brandSubtitleX, brandSubtitleY, 0xFFAAAAAA, false);
    }

    private void renderSearchBackground(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.searchBox == null) return;

        int boxX = this.searchBox.getX();
        int boxY = this.searchBox.getY();
        int boxW = this.searchBox.getWidth();
        int boxH = this.searchBox.getHeight();

        boolean hovered = mouseX >= boxX && mouseX <= boxX + boxW && mouseY >= boxY && mouseY <= boxY + boxH;
        boolean active = hovered || this.searchBox.isFocused();

        float step = 0.1f;
        if (active) {
            searchHoverProgress = Math.min(1.0f, searchHoverProgress + step);
        }
        else {
            searchHoverProgress = Math.max(0.0f, searchHoverProgress - step);
        }

        graphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0x35353535);

        int baseBorder = 2;
        int extraBorder = Math.round(searchHoverProgress * 2.0f);
        int borderW = baseBorder + extraBorder;

        int idleBorder = 0x40FFFFFF;
        int hoverBorder = 0xFF4A9EFF;
        int borderColor = (searchHoverProgress > 0f) ? hoverBorder : idleBorder;

        graphics.fill(boxX, boxY, boxX + borderW, boxY + boxH, borderColor);

        if (searchHoverProgress > 0f) {
            int gradientW = Math.min(120, boxW / 3);
            int maxA = (int) (0x40 * searchHoverProgress);

            PoseStack pose = graphics.pose();
            Matrix4f matrix = pose.last().pose();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tesselator.getBuilder();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            int x0 = boxX;
            int x1 = boxX + gradientW;
            int y0 = boxY;
            int y1 = boxY + boxH;

            bufferBuilder.vertex(matrix, x0, y1, 0).color(0x4A, 0x9E, 0xFF, maxA).endVertex();
            bufferBuilder.vertex(matrix, x1, y1, 0).color(0x4A, 0x9E, 0xFF, 0).endVertex();
            bufferBuilder.vertex(matrix, x1, y0, 0).color(0x4A, 0x9E, 0xFF, 0).endVertex();
            bufferBuilder.vertex(matrix, x0, y0, 0).color(0x4A, 0x9E, 0xFF, maxA).endVertex();

            BufferUploader.drawWithShader(bufferBuilder.end());
        }

    }

    static class ConfigPanel extends AbstractWidget {

        private final List<ValueEntry> allEntries = new ArrayList<>();
        private final List<ValueEntry> visibleEntries = new ArrayList<>();

        private double scrollAmount = 0.0;
        private double targetScroll = 0.0;
        private static final int ROW_GAP = 4;

        private boolean draggingScrollbar = false;
        private int dragThumbOffsetY = 0;

        private ValueEntry focusedEntry = null;

        public ConfigPanel(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
        }

        public void tickPanel() {
            for (ValueEntry valueEntry : this.visibleEntries) {
                if (valueEntry.valueWidget instanceof EditBox box) {
                    box.tick();
                }

            }

        }

        public void addConfigEntry(Field field, ConfigEntry meta) {
            ValueEntry entry = new ValueEntry(field, meta);
            this.allEntries.add(entry);
            this.visibleEntries.add(entry);

            recalcLayout();
        }

        public void applySearch(String query) {
            this.visibleEntries.clear();

            ValueEntry prevFocused = this.focusedEntry;
            this.focusedEntry = null;

            if (query == null || query.isBlank()) {
                this.visibleEntries.addAll(this.allEntries);
            }
            else {
                String queryLowerCase = query.toLowerCase();
                for (ValueEntry valueEntry : this.allEntries) {
                    if (valueEntry.matches(queryLowerCase)) {
                        this.visibleEntries.add(valueEntry);
                    }
                }

            }

            if (prevFocused != null && this.visibleEntries.contains(prevFocused)) {
                this.focusedEntry = prevFocused;
                this.focusedEntry.setFocused(true);
            }
            else if (prevFocused != null) {
                prevFocused.setFocused(false);
            }

            recalcLayout();
        }

        public void applyToFields() {
            for (ValueEntry entry : this.allEntries) {
                entry.applyToField();
            }
        }

        private int getContentWidth() {
            return this.width - 20;
        }

        private int getRowLeft() {
            return this.getX() + 10;
        }

        private void recalcLayout() {
            int rowWidth = getContentWidth();
            for (ValueEntry e : this.visibleEntries) {
                e.recalculateHeight(rowWidth);
            }
            clampScroll();
        }

        private int getTotalContentHeight() {
            int total = 4;
            boolean first = true;
            for (ValueEntry e : this.visibleEntries) {
                if (!first) {
                    total += ROW_GAP;
                }

                total += e.getHeight();
                first = false;
            }

            return total;
        }

        private void clampScroll() {
            int innerHeight = this.height;
            int contentHeight = getTotalContentHeight();
            if (contentHeight <= innerHeight) {
                scrollAmount = 0.0;
                targetScroll = 0.0;
            }
            else {
                if (targetScroll < 0.0) {
                    targetScroll = 0.0;
                }

                double maxScroll = contentHeight - innerHeight;
                if (targetScroll > maxScroll) {
                    targetScroll = maxScroll;
                }

                if (scrollAmount < 0.0) {
                    scrollAmount = 0.0;
                }
                if (scrollAmount > maxScroll) {
                    scrollAmount = maxScroll;
                }

            }

        }

        private boolean isInside(double mouseX, double mouseY) {
            return mouseX >= this.getX() && mouseX <= this.getX() + this.width && mouseY >= this.getY() && mouseY <= this.getY() + this.height;
        }

        private boolean hasScrollableContent() {
            return getTotalContentHeight() > this.height;
        }

        private ScrollbarGeom getScrollbarGeom() {
            int contentHeight = getTotalContentHeight();
            int left = this.getX();
            int top = this.getY();
            int right = left + this.width;

            int barWidth = 6;
            int trackX0 = right - barWidth;
            int trackX1 = right;

            double maxScroll = Math.max(0.0, contentHeight - this.height);
            double scrollRatio = (maxScroll <= 0.0) ? 0.0 : (scrollAmount / maxScroll);
            double visibleRatio = (double) this.height / (double) contentHeight;

            int thumbH = (int) (this.height * visibleRatio);
            if (thumbH < 24) {
                thumbH = 24;
            }
            if (thumbH > this.height) {
                thumbH = this.height;
            }

            int thumbY = top + (int) ((this.height - thumbH) * scrollRatio);

            return new ScrollbarGeom(trackX0, trackX1, top, top + this.height, thumbY, thumbY + thumbH, thumbH, maxScroll);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (!this.visible) {
                return;
            }

            if (!draggingScrollbar && scrollAmount != targetScroll) {
                double diff = targetScroll - scrollAmount;
                if (Math.abs(diff) < 0.5) {
                    scrollAmount = targetScroll;
                }
                else {
                    scrollAmount += diff * 0.2;
                }

                clampScroll();
            }

            int left = this.getX();
            int top = this.getY();
            int right = left + this.width;
            int bottom = top + this.height;

            guiGraphics.fill(left, top, right, bottom, 0xFF111111);

            guiGraphics.enableScissor(left, top, right, bottom);

            int rowLeft = getRowLeft();
            int rowWidth = getContentWidth();
            int currentY = top + 4 - (int) scrollAmount;

            int index = 0;
            boolean first = true;
            for (ValueEntry valueEntry : this.visibleEntries) {
                int rowHeight = valueEntry.getHeight();
                if (!first) {
                    currentY += ROW_GAP;
                }

                int rowTop = currentY;
                int rowBottom = rowTop + rowHeight;

                if (rowBottom >= top && rowTop <= bottom) {
                    boolean mouseInPanel = mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
                    boolean hovered = mouseInPanel && mouseX >= rowLeft && mouseX <= rowLeft + rowWidth && mouseY >= rowTop && mouseY <= rowBottom;

                    valueEntry.render(guiGraphics, index, rowTop, rowLeft, rowWidth, rowHeight, mouseX, mouseY, hovered, partialTick);
                }

                currentY += rowHeight;
                index++;
                first = false;
            }

            guiGraphics.disableScissor();

            renderScrollbar(guiGraphics);
        }

        private void renderScrollbar(GuiGraphics guiGraphics) {
            if (!hasScrollableContent()) {
                return;
            }

            ScrollbarGeom scrollbarGeom = getScrollbarGeom();

            // The line itself
            guiGraphics.fill(scrollbarGeom.trackX0, scrollbarGeom.trackTop, scrollbarGeom.trackX1, scrollbarGeom.trackBottom, 0x80000000);

            // Thumb
            guiGraphics.fill(scrollbarGeom.trackX0, scrollbarGeom.thumbTop, scrollbarGeom.trackX1, scrollbarGeom.thumbBottom, 0xFF4A9EFF);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
            if (!this.visible) {
                return false;
            }
            if (!isInside(mouseX, mouseY)) {
                return false;
            }
            if (!hasScrollableContent()) {
                return false;
            }

            draggingScrollbar = false;

            this.targetScroll -= delta * 20.0;
            clampScroll();

            return true;
        }

        private void clearFocus() {
            if (this.focusedEntry != null) {
                this.focusedEntry.setFocused(false);
                this.focusedEntry = null;
            }

        }

        private void setFocus(ValueEntry entry) {
            if (this.focusedEntry == entry) {
                return;
            }

            if (this.focusedEntry != null) {
                this.focusedEntry.setFocused(false);
            }

            this.focusedEntry = entry;
            if (this.focusedEntry != null) {
                this.focusedEntry.setFocused(true);
            }

        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.visible || !this.active) {
                return false;
            }
            if (!isInside(mouseX, mouseY)) {
                return false;
            }

            if (hasScrollableContent()) {
                ScrollbarGeom scrollbarGeom = getScrollbarGeom();

                if (mouseX >= scrollbarGeom.trackX0 && mouseX <= scrollbarGeom.trackX1) {
                    if (mouseY >= scrollbarGeom.thumbTop && mouseY <= scrollbarGeom.thumbBottom) {
                        draggingScrollbar = true;
                        dragThumbOffsetY = (int) mouseY - scrollbarGeom.thumbTop;
                        return true;
                    }
                    else {
                        double time = (mouseY - scrollbarGeom.trackTop) / (double) (scrollbarGeom.trackBottom - scrollbarGeom.trackTop);
                        time = Math.max(0.0, Math.min(1.0, time));

                        double desired = time * scrollbarGeom.maxScroll - (this.height * 0.5);
                        this.targetScroll = Math.max(0.0, Math.min(scrollbarGeom.maxScroll, desired));

                        this.scrollAmount = this.targetScroll;
                        clampScroll();

                        return true;
                    }

                }

            }

            int top = this.getY();

            int rowLeft = getRowLeft();
            int rowWidth = getContentWidth();
            int currentY = top + 4 - (int) scrollAmount;

            boolean first = true;
            for (ValueEntry entry : this.visibleEntries) {
                int rowHeight = entry.getHeight();
                if (!first) {
                    currentY += ROW_GAP;
                }

                int rowTop = currentY;
                int rowBottom = rowTop + rowHeight;

                if (mouseY >= rowTop && mouseY <= rowBottom && mouseX >= rowLeft && mouseX <= rowLeft + rowWidth) {
                    if (entry.mouseClicked(mouseX, mouseY, button)) {
                        setFocus(entry);
                        return true;
                    }

                    clearFocus();

                    return true;
                }

                currentY += rowHeight;
                first = false;
            }

            clearFocus();

            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (!this.visible || !this.active) {
                return false;
            }

            if (draggingScrollbar && hasScrollableContent()) {
                ScrollbarGeom scrollbarGeom = getScrollbarGeom();

                int trackSpan = (scrollbarGeom.trackBottom - scrollbarGeom.trackTop) - scrollbarGeom.thumbHeight;
                if (trackSpan <= 0) {
                    return true;
                }

                int desiredThumbTop = (int) mouseY - dragThumbOffsetY;
                int minThumbTop = scrollbarGeom.trackTop;
                int maxThumbTop = scrollbarGeom.trackBottom - scrollbarGeom.thumbHeight;

                desiredThumbTop = Math.max(minThumbTop, Math.min(maxThumbTop, desiredThumbTop));
                double t = (desiredThumbTop - minThumbTop) / (double) (maxThumbTop - minThumbTop);

                this.targetScroll = t * scrollbarGeom.maxScroll;
                this.scrollAmount = this.targetScroll;

                clampScroll();

                return true;
            }

            if (this.focusedEntry != null) {
                if (this.focusedEntry.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                    return true;
                }

            }

            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (draggingScrollbar) {
                draggingScrollbar = false;
                return true;
            }

            if (this.focusedEntry != null) {
                return this.focusedEntry.mouseReleased(mouseX, mouseY, button);
            }

            return false;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (this.focusedEntry != null && this.focusedEntry.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }

            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            if (this.focusedEntry != null && this.focusedEntry.charTyped(codePoint, modifiers)) {
                return true;
            }

            return super.charTyped(codePoint, modifiers);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            ;;
        }

        private record ScrollbarGeom(int trackX0, int trackX1, int trackTop, int trackBottom, int thumbTop, int thumbBottom, int thumbHeight, double maxScroll) { ;; }

        private static class ValueEntry {

            private final Field field;
            private final Component label;
            private final String commentText;
            private final AbstractWidget valueWidget;
            private final boolean colorField;
            private final String searchIndex;

            private float hoverProgress = 0f;
            private int height = 40;

            private final Random particleRng;
            private final ArrayList<HoverParticle> hoverParticles = new ArrayList<>();
            private long lastParticleSpawnMs = 0L;
            private long lastParticleUpdateMs = 0L;

            private static final int PARTICLE_CAP = 22;

            private static class HoverParticle {
                float x;
                float y;
                float vx;
                float baseY;
                float amp;
                float freq;
                float phase;
                float size;
                int red;
                int green;
                int blue;
                float lifeMs;
                float ageMs;

                HoverParticle(float x, float y, float vx, float baseY, float amp, float freq, float phase, float size, int red, int green, int blue, float lifeMs) {
                    this.x = x; this.y = y;
                    this.vx = vx;
                    this.baseY = baseY;
                    this.amp = amp;
                    this.freq = freq;
                    this.phase = phase;
                    this.size = size;
                    this.red = red;
                    this.green = green;
                    this.blue = blue;
                    this.lifeMs = lifeMs;
                    this.ageMs = 0f;
                }

            }

            public ValueEntry(Field field, ConfigEntry meta) {
                this.field = field;

                this.colorField = isColorField(field, meta);

                String prettyLabel = buildLabel(meta, field.getName());
                this.label = Component.literal(prettyLabel);

                this.commentText = meta.comment().trim();
                String raw = field.getName()
                        + " " + field.getName().replace('_', ' ')
                        + " " + prettyLabel
                        + " " + meta.category()
                        + " " + this.commentText;

                this.searchIndex = normalizeForSearch(raw);

                this.valueWidget = createWidget();

                int seed = (field.getName() + "|" + meta.category() + "|" + meta.note()).hashCode();
                this.particleRng = new Random(seed);

                this.lastParticleUpdateMs = System.currentTimeMillis();
            }

            public int getHeight() {
                return this.height;
            }

            public void setFocused(boolean focused) {
                this.valueWidget.setFocused(focused);
                if (this.valueWidget instanceof EditBox box) {
                    box.setFocused(focused);
                }

            }

            public void recalculateHeight(int rowWidth) {
                Minecraft minecraft = Minecraft.getInstance();

                int PAD_TOP = 6;
                int LABEL_GAP = 3;
                int LINE_H = minecraft.font.lineHeight;

                int widgetW = this.valueWidget.getWidth();
                int labelXRelative = 8;
                int available = rowWidth - widgetW - 10 - labelXRelative - 16;
                int commentMaxWidth = Math.min(available, (int) (rowWidth * 0.45f));

                int commentLines = 0;
                if (!this.commentText.isEmpty() && commentMaxWidth > 40) {
                    commentLines = minecraft.font.split(Component.literal(this.commentText), commentMaxWidth).size();
                }

                int height = PAD_TOP + Math.max(this.valueWidget.getHeight(), LINE_H);

                if (commentLines > 0) {
                    height += LABEL_GAP + (commentLines * LINE_H);
                }

                this.height = height;
            }

            private static boolean isColorField(Field field, ConfigEntry meta) {
                String name = field.getName().toLowerCase();
                if (name.contains("color") || name.contains("colour") || name.contains("palette")) {
                    return true;
                }

                if (meta != null) {
                    String note = meta.note().toLowerCase();
                    String comment = meta.comment().toLowerCase();
                    if (note.contains("color") || note.contains("colour") || note.contains("hex") || comment.contains("color") || comment.contains("palette") || comment.contains("argb")) {
                        return true;
                    }

                }

                return false;
            }

            private static String buildLabel(ConfigEntry meta, String fieldName) {
                String prettyName = prettifyName(fieldName);
                String category = meta.category().trim();
                if (!category.isEmpty()) {
                    String catPretty = prettifyName(category);
                    return catPretty + " › " + prettyName;
                }

                return prettyName;
            }

            private static String prettifyName(String name) {
                String lower = name.toLowerCase().replace('$', ' ').replace('_', ' ');
                String[] parts = lower.split("\\s+");
                StringBuilder out = new StringBuilder();
                for (String part : parts) {
                    if (part.isEmpty()) {
                        continue;
                    }
                    if (out.length() > 0) {
                        out.append(' ');
                    }

                    out.append(Character.toUpperCase(part.charAt(0)));
                    if (part.length() > 1) {
                        out.append(part.substring(1));
                    }

                }

                return out.toString();
            }

            private AbstractWidget createWidget() {
                Minecraft minecraft = Minecraft.getInstance();
                Class<?> type = field.getType();
                try {
                    if (type == boolean.class) {
                        boolean value = field.getBoolean(null);
                        return new ModernCheckbox(0, 0, 20, 20, Component.empty(), value);
                    }

                    if (type == int.class && this.colorField) {
                        int value = field.getInt(null);

                        EditBox box = new ModernEditBox(minecraft.font, 0, 0, 100, 20, Component.empty());
                        box.setMaxLength(64);
                        box.setValue(formatColor(value));

                        return box;
                    }

                    String value = String.valueOf(field.get(null));
                    boolean isMultiColor = type == String.class && value.contains(",") && (value.contains("0x") || value.contains("#"));

                    int boxWidth = isMultiColor ? 260 : 180;

                    EditBox box = new ModernEditBox(minecraft.font, 0, 0, boxWidth, 20, Component.empty());
                    box.setMaxLength(512);
                    box.setValue(value);

                    return box;
                }
                catch (Exception expection) {
                    EditBox box = new ModernEditBox(Minecraft.getInstance().font, 0, 0, 180, 20, Component.empty());
                    box.setMaxLength(512);
                    box.setValue("");

                    return box;
                }

            }

            private static String formatColor(int argb) {
                return String.format("#%08X", argb);
            }

            public boolean matches(String query) {
                String normalizedQuery = normalizeForSearch(query);
                if (normalizedQuery.isEmpty()) {
                    return true;
                }

                for (String token : normalizedQuery.split(" ")) {
                    if (token.isEmpty()) {
                        continue;
                    }
                    if (!this.searchIndex.contains(token)) {
                        return false;
                    }

                }

                return true;
            }

            private static String normalizeForSearch(String string) {
                if (string == null) {
                    return "";
                }

                String out = string.toLowerCase();
                out = out.replace('_', ' ')
                        .replace('-', ' ')
                        .replace('.', ' ')
                        .replace('/', ' ');

                out = out.trim().replaceAll("\\s+", " ");

                return out;
            }


            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return this.valueWidget.mouseClicked(mouseX, mouseY, button);
            }

            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                return this.valueWidget.mouseReleased(mouseX, mouseY, button);
            }

            public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
                return this.valueWidget.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }

            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                return this.valueWidget.keyPressed(keyCode, scanCode, modifiers);
            }

            public boolean charTyped(char codePoint, int modifiers) {
                return this.valueWidget.charTyped(codePoint, modifiers);
            }

            public void applyToField() {
                try {
                    Class<?> type = field.getType();

                    if (type == boolean.class) {
                        if (this.valueWidget instanceof Checkbox cb) {
                            field.setBoolean(null, cb.selected());
                        }

                        return;
                    }

                    if (!(this.valueWidget instanceof EditBox box)) {
                        return;
                    }

                    String raw = box.getValue();
                    String trimmed = raw.trim();

                    if (type == String.class) {
                        field.set(null, raw);
                        return;
                    }

                    if (trimmed.isEmpty()) {
                        return;
                    }

                    if (type == int.class) {
                        int current = field.getInt(null);
                        int value = this.colorField ? parseColorInt(trimmed, current) : parseIntFlexible(trimmed, current);

                        field.setInt(null, value);
                    }
                    else if (type == long.class) {
                        long current = field.getLong(null);
                        long value = parseLongFlexible(trimmed, current);

                        field.setLong(null, value);
                    }
                    else if (type == float.class) {
                        try {
                            field.setFloat(null, Float.parseFloat(trimmed));
                        }
                        catch (NumberFormatException ignored) {
                            ;;
                        }
                    }
                    else if (type == double.class) {
                        try {
                            field.setDouble(null, Double.parseDouble(trimmed));
                        }
                        catch (NumberFormatException ignored) {
                            ;;
                        }

                    }

                }
                catch (Exception ignored) {
                    ;;
                }

            }

            private static int parseIntFlexible(String text, int fallback) {
                try {
                    String string = text.trim();
                    if (string.startsWith("0x") || string.startsWith("0X") || string.startsWith("#")) {
                        if (string.startsWith("#")) {
                            string = "0x" + string.substring(1);
                        }

                        return Integer.decode(string);
                    }

                    return Integer.parseInt(string);
                }
                catch (Exception ignored) {
                    return fallback;
                }

            }

            private static long parseLongFlexible(String txt, long fallback) {
                try {
                    String string = txt.trim();
                    if (string.startsWith("0x") || string.startsWith("0X") || string.startsWith("#")) {
                        if (string.startsWith("#")) {
                            string = "0x" + string.substring(1);
                        }

                        return Long.decode(string);
                    }

                    return Long.parseLong(string);
                }
                catch (Exception ignored) {
                    return fallback;
                }

            }

            private static int parseColorInt(String txt, int fallback) {
                try {
                    String string = txt.trim();
                    if (string.isEmpty()) {
                        return fallback;
                    }

                    if (string.startsWith("#")) {
                        string = string.substring(1);
                    }
                    if (string.startsWith("0x") || string.startsWith("0X")) {
                        string = string.substring(2);
                    }

                    if (string.length() == 6) {
                        long rgb = Long.parseLong(string, 16) & 0xFFFFFFL;
                        long argb = 0xFF000000L | rgb;

                        return (int) argb;
                    }

                    long argb = Long.parseLong(string, 16);
                    return (int) argb;
                }
                catch (Exception ignored) {
                    return fallback;
                }

            }

            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
                Minecraft minecraft = Minecraft.getInstance();

                float step = 0.1f;
                hoverProgress = hovered ? Math.min(1.0f, hoverProgress + step) : Math.max(0.0f, hoverProgress - step);

                int baseAlpha = (index % 2 == 0) ? 0x28 : 0x20;
                int extraAlpha = (int) (0x30 * hoverProgress);
                int totalAlpha = Math.min(255, baseAlpha + extraAlpha);
                int bgColor = (totalAlpha << 24) | 0x353535;
                guiGraphics.fill(left, top, left + width, top + height, bgColor);

                int gradientWidth = Math.min(120, width / 3);
                if (hoverProgress > 0f) {
                    int maxAlpha = (int) (0x40 * hoverProgress);

                    for (int x = 0; x < gradientWidth; x++) {
                        float t = (float) x / (float) gradientWidth;
                        int a = (int) (maxAlpha * (1.0f - t));
                        if (a <= 0) continue;

                        int color = (a << 24) | 0x4A9EFF;
                        int x1 = left + x;
                        guiGraphics.fill(x1, top, x1 + 1, top + height, color);
                    }

                }

                int baseBorderWidth = 2;
                int extraBorder = Math.round(hoverProgress * 2.0f);
                int borderWidth = baseBorderWidth + extraBorder;

                int idleColor = 0x40FFFFFF;
                int hoverColor = 0xFF4A9EFF;
                int borderColor = (hoverProgress > 0f) ? hoverColor : idleColor;
                guiGraphics.fill(left, top, left + borderWidth, top + height, borderColor);

                guiGraphics.fill(left, top + height - 1, left + width, top + height, 0x20000000);

                updateHoverParticles(top, height);
                if (hoverProgress > 0.15f) {
                    spawnHoverParticles(left, top, width, height, borderWidth, gradientWidth, hoverProgress);
                }

                renderHoverParticles(guiGraphics, hoverProgress);

                int labelX = left + 8;
                int labelY = top + 6;

                guiGraphics.drawString(minecraft.font, this.label, labelX + 1, labelY + 1, 0x80000000, false);
                guiGraphics.drawString(minecraft.font, this.label, labelX, labelY, 0xFFFFFFFF, false);

                int widgetWidth = this.valueWidget.getWidth();
                int widgetHeight = this.valueWidget.getHeight();
                int widgetX = left + width - widgetWidth - 10;
                int widgetY = top + (height - widgetHeight) / 2;

                this.valueWidget.setX(widgetX);
                this.valueWidget.setY(widgetY);

                if (this.colorField && field.getType() == int.class) {
                    try {
                        int color = field.getInt(null);
                        int previewSize = 18;
                        int previewRight = widgetX - 8;
                        int previewLeft = previewRight - previewSize;
                        int previewTop = top + (height - previewSize) / 2;

                        drawCheckerboard(guiGraphics, previewLeft, previewTop, previewSize, previewSize);
                        guiGraphics.fill(previewLeft, previewTop, previewRight, previewTop + previewSize, color);

                        guiGraphics.fill(previewLeft - 1, previewTop - 1, previewRight + 1, previewTop, 0xFF606060);
                        guiGraphics.fill(previewLeft - 1, previewTop + previewSize, previewRight + 1, previewTop + previewSize + 1, 0xFF303030);
                        guiGraphics.fill(previewLeft - 1, previewTop - 1, previewLeft, previewTop + previewSize + 1, 0xFF606060);
                        guiGraphics.fill(previewRight, previewTop - 1, previewRight + 1, previewTop + previewSize + 1, 0xFF303030);
                    }
                    catch (IllegalAccessException ignored) {
                        ;;
                    }

                }

                this.valueWidget.render(guiGraphics, mouseX, mouseY, partialTick);

                if (this.valueWidget instanceof EditBox box) {
                    renderInlineColorBars(guiGraphics, box);
                }

                // Description
                if (!this.commentText.isEmpty()) {
                    int available = widgetX - labelX - 16;
                    int commentMaxWidth = Math.min(available, (int) (width * 0.45f));

                    if (commentMaxWidth > 40) {
                        int lineHeight = minecraft.font.lineHeight;
                        int commentY = labelY + minecraft.font.lineHeight + 3;

                        List<FormattedCharSequence> lines = minecraft.font.split(Component.literal(this.commentText), commentMaxWidth);
                        int availableHeight = (top + height) - commentY;
                        int maxLines = Math.max(1, availableHeight / lineHeight);

                        int drawn = 0;
                        for (FormattedCharSequence line : lines) {
                            if (drawn >= maxLines) {
                                break;
                            }

                            guiGraphics.drawString(minecraft.font, line, labelX, commentY, 0xFFA0A0A0, false);
                            commentY += lineHeight;

                            drawn++;
                        }

                    }

                }

            }

            private static List<Integer> extractColorsFromText(String text) {
                if (text == null) {
                    return Collections.emptyList();
                }

                String string = text.trim();
                if (string.isEmpty()) {
                    return Collections.emptyList();
                }

                ArrayList<Integer> out = new ArrayList<>();

                Pattern prefixed = Pattern.compile("(?:#|0x|0X)([0-9a-fA-F]{8}|[0-9a-fA-F]{6})");
                Matcher matcher = prefixed.matcher(string);
                while (matcher.find()) {
                    String hex = matcher.group(1);
                    Integer color = parseHexColorToken(hex);

                    if (color != null) {
                        out.add(color);
                    }

                }

                if (out.isEmpty()) {
                    Pattern bare = Pattern.compile("(?<![0-9a-fA-F])([0-9a-fA-F]{8}|[0-9a-fA-F]{6})(?![0-9a-fA-F])");
                    Matcher matcher2 = bare.matcher(string);
                    while (matcher2.find()) {
                        String token = matcher2.group(1);
                        boolean hasLetter = token.chars().anyMatch(ch -> (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f'));
                        if (!hasLetter) {
                            continue;
                        }

                        Integer color = parseHexColorToken(token);
                        if (color != null) {
                            out.add(color);
                        }

                    }

                }

                return out;
            }

            private static Integer parseHexColorToken(String hex) {
                try {
                    if (hex.length() == 6) {
                        int rgb = (int) (Long.parseLong(hex, 16) & 0xFFFFFFL);
                        return 0xFF000000 | rgb;
                    }
                    if (hex.length() == 8) {
                        return (int) (Long.parseLong(hex, 16) & 0xFFFFFFFFL);
                    }

                    return null;
                }
                catch (Exception ignored) {
                    return null;
                }

            }

            private void renderInlineColorBars(GuiGraphics g, EditBox box) {
                List<Integer> colors = extractColorsFromText(box.getValue());
                if (colors.isEmpty()) {
                    return;
                }

                int max = Math.min(colors.size(), 8);

                int bx = box.getX();
                int by = box.getY();
                int bw = box.getWidth();
                int bh = box.getHeight();

                int padRight = 3;
                int padY = 3;

                int barW = 4;
                int gap = 2;

                int stripW = max * barW + (max - 1) * gap;

                int x1 = bx + bw - padRight;
                int x0 = x1 - stripW;

                int y0 = by + padY;
                int y1 = by + bh - padY;

                for (int i = 0; i < max; i++) {
                    int c = colors.get(i);

                    int px0 = x0 + i * (barW + gap);
                    int px1 = px0 + barW;

                    drawMiniChecker(g, px0, y0, barW, (y1 - y0));

                    g.fill(px0, y0, px1, y1, c);
                }

            }

            private void drawMiniChecker(GuiGraphics graphics, int x, int y, int w, int h) {
                int cs = 2;
                for (int yy = 0; yy < h; yy += cs) {
                    for (int xx = 0; xx < w; xx += cs) {
                        boolean light = ((xx / cs) + (yy / cs)) % 2 == 0;

                        int col = light ? 0xFFB0B0B0 : 0xFF7A7A7A;
                        int dx = Math.min(cs, w - xx);
                        int dy = Math.min(cs, h - yy);

                        graphics.fill(x + xx, y + yy, x + xx + dx, y + yy + dy, col);
                    }
                }

            }

            private void updateHoverParticles(int rowTop, int rowHeight) {
                long now = System.currentTimeMillis();
                long timeMs = Math.min(33L, Math.max(0L, now - lastParticleUpdateMs));
                lastParticleUpdateMs = now;

                float dateTime = (float) timeMs;

                for (int i = hoverParticles.size() - 1; i >= 0; i--) {
                    HoverParticle particle = hoverParticles.get(i);
                    particle.ageMs += dateTime;

                    particle.x += particle.vx * dateTime;

                    particle.y = particle.baseY + (float) Math.sin(particle.phase + particle.ageMs * particle.freq) * particle.amp;

                    if (particle.ageMs >= particle.lifeMs) {
                        hoverParticles.remove(i);

                        continue;
                    }

                    if (particle.y < rowTop - 6 || particle.y > rowTop + rowHeight + 6) {
                        hoverParticles.remove(i);
                    }

                }

            }

            private void spawnHoverParticles(int left, int top, int width, int height, int borderW, int gradientW, float hoverProgress) {
                long now = System.currentTimeMillis();

                int minInterval = 35;
                int maxInterval = 85;
                int interval = (int) (maxInterval - (maxInterval - minInterval) * hoverProgress);

                if (now - lastParticleSpawnMs < interval) {
                    return;
                }

                lastParticleSpawnMs = now;

                if (hoverParticles.size() >= PARTICLE_CAP) {
                    return;
                }

                int count = (hoverProgress > 0.6f && particleRng.nextFloat() < 0.55f) ? 2 : 1;

                for (int n = 0; n < count && hoverParticles.size() < PARTICLE_CAP; n++) {
                    float spawnX = left + borderW - 1 + particleRng.nextFloat() * 2.0f;
                    float spawnY = top + 6 + particleRng.nextFloat() * (height - 12);

                    float vx = (0.02f + particleRng.nextFloat() * 0.045f);

                    float amp  = 0.6f + particleRng.nextFloat() * 1.2f;
                    float frequency = 0.010f + particleRng.nextFloat() * 0.010f;
                    float phase = particleRng.nextFloat() * 6.28318f;

                    float size = 1.0f + particleRng.nextFloat() * 1.2f;
                    float life = 420f + particleRng.nextFloat() * 360f;

                    boolean white = particleRng.nextFloat() < 0.28f;
                    int red;
                    int green;
                    int blue;
                    if (white) {
                        red = 235;
                        green = 245;
                        blue = 255;
                        vx *= 1.08f;
                    }
                    else {
                        red = 0x4A; green = 0x9E; blue = 0xFF;
                    }

                    hoverParticles.add(new HoverParticle(spawnX, spawnY, vx, spawnY, amp, frequency, phase, size, red, green, blue, life));
                }

            }

            private void renderHoverParticles(GuiGraphics graphics, float hoverProgress) {
                if (hoverParticles.isEmpty()) {
                    return;
                }

                float intensity = Math.min(1.0f, 0.65f + hoverProgress * 0.55f);

                for (HoverParticle particle : hoverParticles) {
                    float time = particle.ageMs / particle.lifeMs;
                    float fade = 1.0f - time;
                    fade = fade * fade;

                    int alpha = (int) (fade * 190f * intensity);
                    if (alpha <= 2) {
                        continue;
                    }

                    int color = (alpha << 24) | (particle.red << 16) | (particle.green << 8) | particle.blue;

                    int x0 = (int) particle.x;
                    int y0 = (int) particle.y;
                    int size = Math.max(1, (int) (particle.size));

                    graphics.fill(x0, y0, x0 + size, y0 + size, color);

                    int trailA = alpha / 3;
                    if (trailA > 0) {
                        int trail = (trailA << 24) | (particle.red << 16) | (particle.green << 8) | particle.blue;
                        graphics.fill(x0 - 1, y0, x0, y0 + size, trail);
                    }
                }

            }

            private void drawCheckerboard(GuiGraphics graphics, int x, int y, int width, int height) {
                int checkerSize = 4;
                for (int cy = 0; cy < height; cy += checkerSize) {
                    for (int cx = 0; cx < width; cx += checkerSize) {
                        boolean isLight = ((cx / checkerSize) + (cy / checkerSize)) % 2 == 0;

                        int color = isLight ? 0xFFCCCCCC : 0xFF999999;
                        int drawWidth = Math.min(checkerSize, width - cx);
                        int drawHeight = Math.min(checkerSize, height - cy);

                        graphics.fill(x + cx, y + cy, x + cx + drawWidth, y + cy + drawHeight, color);
                    }

                }

            }

        }

        private static class ModernCheckbox extends Checkbox {

            public ModernCheckbox(int x, int y, int w, int h, Component msg, boolean selected) {
                super(x, y, w, h, msg, selected);
            }

            @Override
            public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
                boolean hovered = this.isHoveredOrFocused();

                int x0 = this.getX();
                int y0 = this.getY();
                int x1 = x0 + this.getWidth();
                int y1 = y0 + this.getHeight();

                int backgroundColor = 0xFF181818;
                g.fill(x0, y0, x1, y1, backgroundColor);

                int border = hovered ? 0xFF4A9EFF : 0xFF3A3A3A;
                g.fill(x0, y0, x1, y0 + 1, border);
                g.fill(x0, y1 - 1, x1, y1, border);
                g.fill(x0, y0, x0 + 1, y1, border);
                g.fill(x1 - 1, y0, x1, y1, border);

                if (this.selected()) {
                    int inset = 4;
                    int inner = 0xFF4A9EFF;
                    g.fill(x0 + inset, y0 + inset, x1 - inset, y1 - inset, inner);
                }
            }

        }

        private static class ModernEditBox extends EditBox {

            private float animationProgress = 0f;

            public ModernEditBox(net.minecraft.client.gui.Font font, int x, int y, int w, int h, Component msg) {
                super(font, x, y, w, h, msg);

                this.setBordered(false);
                this.setTextColor(0xFFE8E8E8);
                this.setTextColorUneditable(0xFF777777);
                this.setMaxLength(512);
            }

            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                int x0 = this.getX();
                int y0 = this.getY();
                int x1 = x0 + this.getWidth();
                int y1 = y0 + this.getHeight();

                boolean hovered = mouseX >= x0 && mouseX < x1 && mouseY >= y0 && mouseY < y1;
                boolean active = hovered || this.isFocused();

                float step = 0.16f;
                animationProgress = active ? Math.min(1f, animationProgress + step) : Math.max(0f, animationProgress - step);

                graphics.fill(x0, y0, x1, y1, 0xFF161616);
                graphics.fill(x0 + 1, y0 + 1, x1 - 1, y1 - 1, 0xFF1E1E1E);

                int borderIdle = 0xFF3A3A3A;
                int borderFocus = 0xFF4A9EFF;
                int border = (animationProgress > 0f) ? borderFocus : borderIdle;

                graphics.fill(x0, y0, x1, y0 + 1, border);
                graphics.fill(x0, y1 - 1, x1, y1, border);
                graphics.fill(x0, y0, x0 + 1, y1, border);
                graphics.fill(x1 - 1, y0, x1, y1, border);

                int lh = Minecraft.getInstance().font.lineHeight;
                int offY = Math.max(0, (this.height - lh) / 2);
                int padX = 6;

                graphics.pose().pushPose();
                graphics.pose().translate(padX, offY + 1, 0);

                super.renderWidget(graphics, mouseX - padX, mouseY - offY, partialTick);

                graphics.pose().popPose();
            }

        }

        static class ModernButton extends Button {

            private float animationProgress = 0f;

            public ModernButton(int x, int y, int w, int h, Component msg, OnPress onPress) {
                super(x, y, w, h, msg, onPress, Button.DEFAULT_NARRATION);
            }

            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                int x0 = this.getX();
                int y0 = this.getY();
                int x1 = x0 + this.getWidth();
                int y1 = y0 + this.getHeight();

                boolean hovered = mouseX >= x0 && mouseX < x1 && mouseY >= y0 && mouseY < y1;
                boolean active = hovered || this.isFocused();

                float step = 0.16f;
                animationProgress = active ? Math.min(1f, animationProgress + step) : Math.max(0f, animationProgress - step);

                int backgroundOuter = 0xFF141414;
                int backgroundInner = hovered ? 0xFF1E1E1E : 0xFF191919;

                graphics.fill(x0, y0, x1, y1, backgroundOuter);
                graphics.fill(x0 + 1, y0 + 1, x1 - 1, y1 - 1, backgroundInner);

                int baseBorder = 2;
                int extraBorder = Math.round(animationProgress * 2.0f);
                int borderW = baseBorder + extraBorder;

                int idleBorder = 0x40FFFFFF;
                int hoverBorder = 0xFF4A9EFF;
                int borderColor = (animationProgress > 0f) ? hoverBorder : idleBorder;

                graphics.fill(x0, y0, x0 + borderW, y1, borderColor);

                if (animationProgress > 0f) {
                    int gradientW = Math.min(90, this.getWidth() / 3);
                    int maxA = (int) (0x40 * animationProgress);

                    for (int x = 0; x < gradientW; x++) {
                        float time = (float) x / (float) gradientW;
                        int alpha = (int) (maxA * (1.0f - time));
                        if (alpha <= 0) {
                            continue;
                        }

                        int color = (alpha << 24) | 0x4A9EFF;
                        graphics.fill(x0 + x, y0, x0 + x + 1, y1, color);
                    }

                }

                int outline = hovered ? 0xFF2E2E2E : 0xFF242424;
                graphics.fill(x0, y0, x1, y0 + 1, outline);
                graphics.fill(x0, y1 - 1, x1, y1, outline);
                graphics.fill(x0, y0, x0 + 1, y1, outline);
                graphics.fill(x1 - 1, y0, x1, y1, outline);

                int txt = this.active ? 0xFFE8E8E8 : 0xFF777777;
                int ty = y0 + (this.height - 8) / 2;

                graphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), x0 + this.width / 2, ty, txt);
            }

        }

    }

    private static class CenteredEditBox extends EditBox {

        public CenteredEditBox(net.minecraft.client.gui.Font font, int x, int y, int width, int height, Component text) {
            super(font, x, y, width, height, text);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int leftHeight = Minecraft.getInstance().font.lineHeight;
            int offset = Math.max(0, (this.height - leftHeight) / 2);

            graphics.pose().pushPose();
            graphics.pose().translate(8, offset + 1, 0);

            super.renderWidget(graphics, mouseX, mouseY - offset, partialTick);

            graphics.pose().popPose();
        }

    }

}