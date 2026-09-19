package dev.xylonity.tooltipoverhaul.client.screen.config;

import dev.xylonity.tooltipoverhaul.client.util.Constants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.*;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.mixRgb;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.withAlpha;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Searchable popup
 */
final class SelectionPopup implements ConfigModal {

    private final Minecraft client = Minecraft.getInstance();

    private final Component title;
    private final List<Option> options;
    private final List<String> searchIndex;
    private List<Option> filtered;
    private final Predicate<String> selected;
    private final Consumer<String> choose;
    private final boolean multiple;
    private final boolean hasFramePreviews;

    private final int x;
    private final int y;
    private final int w;
    private final int h;
    private final int rowHeight;
    private final int accent;

    private final StyledEditBox search;
    private int scroll, cursor;
    private boolean draggingScrollbar;
    private double scrollbarDragOffset;
    private long selectedCount;
    private final Set<Item> brokenIcons = new HashSet<>();
    private final Set<ResourceLocation> brokenTextures = new HashSet<>();

    private boolean selectedOnly;
    private boolean closed;

    private float doneHoverAnimation;
    private long doneRenderedAt = Util.getMillis();

    SelectionPopup(int screenWidth, int screenHeight, Component title, List<Option> options, Predicate<String> selected, Consumer<String> choose, boolean multiple, int accent) {
        this.title = title;
        this.options = List.copyOf(options);
        this.searchIndex = options.stream().map(Option::searchText).toList();
        this.selected = selected;
        this.selectedCount = options.stream().filter(option -> selected.test(option.value())).count();
        this.choose = choose;
        this.multiple = multiple;
        this.hasFramePreviews = options.stream().anyMatch(option -> option.framePreview() != null);
        this.accent = accent;
        this.w = Math.min(360, screenWidth - 16);
        this.h = Math.min(320, screenHeight - 20);
        this.x = (screenWidth - w) / 2;
        this.y = (screenHeight - h) / 2;
        this.rowHeight = hasFramePreviews ? 42 : multiple ? 30 : 22;
        this.search = new StyledEditBox(client.font, x + 10, y + 28, w - 20, 18, Component.translatable("tooltipoverhaul.config.frames.search"), accent);
        search.setMaxLength(256);
        search.setFocused(true);
        search.setResponder(value -> filter());
        filter();
        if (!multiple) {
            for (int i = 0; i < filtered.size(); i++) {
                if (selected.test(filtered.get(i).value())) {
                    cursor = i;
                }

            }

            revealCursor();
        }

    }

    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }

    private int rowsTop() {
        return y + 54;
    }

    private int visibleRows() {
        return Math.max(1, (h - 84) / rowHeight);
    }

    private int maximumScroll() {
        return Math.max(0, filtered.size() - visibleRows());
    }

    private ConfigScroll.Geometry scrollbarGeometry() {
        if (filtered.size() <= visibleRows()) {
            return null;
        }

        final int trackHeight = visibleRows() * rowHeight;
        return ConfigScroll.fromContent(rowsTop(), rowsTop() + trackHeight, filtered.size() * rowHeight, scroll * rowHeight);
    }

    private void setScrollFromPixels(double pixels) {
        scroll = Mth.clamp((int) Math.round(pixels / rowHeight), 0, maximumScroll());
        cursor = Mth.clamp(cursor, scroll, Math.max(scroll, Math.min(filtered.size() - 1, scroll + visibleRows() - 1)));
    }

    private void filter() {
        final String[] words = normalize(search.getValue()).trim().split("\\s+");
        final ArrayList<Option> matches = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            Option option = options.get(i);
            if (selectedOnly && !selected.test(option.value())) {
                continue;
            }

            boolean match = true;
            for (String word : words) if (!searchIndex.get(i).contains(word)) { match = false; break; }
            if (match) {
                matches.add(option);
            }

        }

        filtered = matches;
        scroll = cursor = 0;
        draggingScrollbar = false;
    }

    private void revealCursor() {
        cursor = Mth.clamp(cursor, 0, Math.max(0, filtered.size() - 1));
        scroll = Mth.clamp(scroll, Math.max(0, cursor - visibleRows() + 1), cursor);
        scroll = Mth.clamp(scroll, 0, Math.max(0, filtered.size() - visibleRows()));
    }

    private void activate(int index) {
        if (index < 0 || index >= filtered.size()) {
            return;
        }

        final String value = filtered.get(index).value();
        final boolean wasSelected = selected.test(value);

        choose.accept(value);

        if (multiple) {
            selectedCount += selected.test(value) == wasSelected ? 0 : wasSelected ? -1 : 1;
        }

        playClickSound();
        if (!multiple) {
            closed = true;
        }
        else if (selectedOnly) {
            filter();
        }

    }

    @Override
    public boolean closed() {
        return closed;
    }

    @Override
    public void render(GuiGraphics graphics, int mx, int my, float delta) {
        graphics.pose().pushPose();

        graphics.pose().translate(0, 0, 450);

        graphics.fill(0, 0, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight(), 0xA0000000);

        drawRoundedCard(graphics, x, y, x + w, y + h, 0xFF111115, 0xFF2E2E32);
        updateDoneHover(mx, my);

        graphics.drawString(client.font, title, x + 10, y + 10, 0xFFFFFFFF, false);

        search.render(graphics, mx, my, delta);

        if (search.getValue().isEmpty()) {
            graphics.drawString(client.font, Component.translatable("tooltipoverhaul.config.frames.search"), x + 16, y + 33, 0xFF666670, false);
        }

        for (int i = scroll; i < Math.min(filtered.size(), scroll + visibleRows()); i++) {
            final Option option = filtered.get(i);
            final int ry = rowsTop() + (i - scroll) * rowHeight;
            final boolean enabled = selected.test(option.value());
            final boolean hover = mx >= x + 6 && mx < x + w - 9 && my >= ry && my < ry + rowHeight;
            graphics.fill(x + 6, ry, x + w - 9, ry + rowHeight - 2, hover || i == cursor ? 0xFF25252D : enabled ? 0xFF182736 : 0xFF17171C);
            int tx = x + 14;
            if (option.framePreview() != null) {
                renderFramePreview(graphics, option, tx, ry + 2);
                tx += 43;
            }
            else if (multiple) {
                if (!option.icon().isEmpty() && !brokenIcons.contains(option.icon().getItem())) {
                    final PoseStack.Pose savedPose = graphics.pose().last();
                    try {
                        graphics.renderItem(option.icon(), tx, ry + 5);
                    }
                    catch (RuntimeException ignored) {
                        brokenIcons.add(option.icon().getItem());
                    }
                    finally {
                        while (graphics.pose().last() != savedPose && !graphics.pose().clear()) {
                            graphics.pose().popPose();
                        }

                    }

                }

                tx += 23;
            }

            final int color = enabled ? 0xFF70B7FF : 0xFFD0D0D8;
            graphics.drawString(client.font, client.font.plainSubstrByWidth(option.label(), x + w - 28 - tx), tx, ry + (hasFramePreviews ? 7 : multiple ? 5 : 7), color, false);
            if (multiple || hasFramePreviews) {
                graphics.drawString(client.font, client.font.plainSubstrByWidth(option.detail(), x + w - 28 - tx), tx, ry + (hasFramePreviews ? 23 : 17), 0xFF777783, false);
            }

            if (enabled) {
                ConfigIconButton.drawDiamond(graphics, x + w - 21, ry + rowHeight / 2 - 1, 0xFF70B7FF);
            }

        }

        if (filtered.isEmpty()) graphics.drawString(client.font, Component.translatable("tooltipoverhaul.config.frames.search_empty"), x + 14, rowsTop() + 8, 0xFF888894, false);
        if (filtered.size() > visibleRows()) {
            ConfigScroll.render(graphics, x + w, scrollbarGeometry(), accent, mx, my, draggingScrollbar);
        }

        String count = multiple ? Component.translatable("tooltipoverhaul.config.frames.items_selected",
                selectedCount).getString() : Integer.toString(filtered.size());
        graphics.drawString(client.font, count, x + 10, y + h - 18, selectedOnly ? 0xFF70B7FF : 0xFF9999A3, false);
        graphics.drawString(client.font, Component.translatable("gui.done"), x + w - 52, y + h - 18, withAlpha(mixRgb(0xD0D0D8, accent, doneHoverAnimation), 0xFF), false);

        graphics.pose().popPose();
    }

    private boolean isDoneHovered(double mx, double my) {
        return mx >= x + w - 64 && mx < x + w && my >= y + h - 26 && my < y + h;
    }

    private void updateDoneHover(int mx, int my) {
        final long now = Util.getMillis();
        final boolean hovered = isDoneHovered(mx, my);
        final float step = Math.min(50L, now - doneRenderedAt) / 160f;
        doneRenderedAt = now;
        doneHoverAnimation = Mth.clamp(doneHoverAnimation + (hovered ? step : -step), 0f, 1f);
    }

    private void renderFramePreview(GuiGraphics graphics, Option option, int px, int py) {
        final FramePreview preview = option.framePreview();
        if (preview == null || brokenTextures.contains(preview.texture())) {
            return;
        }

        final int size = 36;
        final int frameDimension = Constants.getOverlayFrameDimension();
        final int frames = Math.max(1, preview.height() / frameDimension);
        final int frameOffset = (int) ((System.currentTimeMillis() / Constants.getOverlayFrameTime()) % frames) * frameDimension;

        graphics.fill(px, py, px + size, py + size, 0xFF0C0C10);

        if (!option.icon().isEmpty() && !brokenIcons.contains(option.icon().getItem())) {
            final PoseStack.Pose savedPose = graphics.pose().last();
            try {
                graphics.renderItem(option.icon(), px + 10, py + 10);
            }
            catch (RuntimeException ignored) {
                brokenIcons.add(option.icon().getItem());
            }

            finally {
                while (graphics.pose().last() != savedPose && !graphics.pose().clear()) {
                    graphics.pose().popPose();
                }

            }

        }

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 200);
        try {
            graphics.blit(preview.texture(), px, py, size, size, 0, frameOffset, frameDimension, frameDimension, preview.width(), preview.height());
        }
        catch (RuntimeException ignored) {
            brokenTextures.add(preview.texture());
        }

        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (mx < x || mx >= x + w || my < y || my >= y + h) {
            closed = true;
            return true;
        }

        if (button != 0) {
            return true;
        }

        if (my >= y + h - 26) {
            if (isDoneHovered(mx, my)) {
                closed = true;
            }
            else if (multiple) {
                selectedOnly = !selectedOnly;
                filter();
            }

            return true;
        }

        ConfigScroll.Geometry scrollbar = scrollbarGeometry();
        if (scrollbar != null && ConfigScroll.isHovered(x + w, scrollbar, mx, my)) {
            if (my >= scrollbar.thumbTop() && my < scrollbar.thumbTop() + scrollbar.thumbHeight()) {
                draggingScrollbar = true;
                scrollbarDragOffset = my - scrollbar.thumbTop();
            }
            else {
                setScrollFromPixels(ConfigScroll.valueFromMouse(my, scrollbar, maximumScroll() * rowHeight));
            }

            return true;
        }

        if (mx >= x + 6 && mx < x + w - 9 && my >= rowsTop() && my < rowsTop() + visibleRows() * rowHeight) {
            cursor = scroll + (int) (my - rowsTop()) / rowHeight;
            activate(cursor);
        }
        else {
            search.mouseClicked(mx, my, button);
        }

        search.setFocused(true);

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingScrollbar && button == 0) {
            final ConfigScroll.Geometry scrollbar = scrollbarGeometry();
            if (scrollbar != null) {
                setScrollFromPixels(ConfigScroll.valueFromDrag(mouseY, scrollbarDragOffset, scrollbar, maximumScroll() * rowHeight));
            }

        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            draggingScrollbar = false;
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll = Mth.clamp(scroll - (int) Math.signum(delta) * 3, 0, maximumScroll());
        cursor = Mth.clamp(cursor, scroll, Math.max(scroll, Math.min(filtered.size() - 1, scroll + visibleRows() - 1)));
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            closed = true;
        }
        else if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            activate(cursor);
        }
        else if (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN) {
            cursor += key == GLFW.GLFW_KEY_UP ? -1 : 1; revealCursor();
        }
        else if (key == GLFW.GLFW_KEY_PAGE_UP || key == GLFW.GLFW_KEY_PAGE_DOWN) {
            cursor += key == GLFW.GLFW_KEY_PAGE_UP ? -visibleRows() : visibleRows();
            revealCursor();
        }
        else {
            search.keyPressed(key, scan, mods);
        }

        return true;
    }

    @Override
    public boolean charTyped(char chr, int mods) {
        search.charTyped(chr, mods);
        return true;
    }

    @Override
    public void tick() {
        ;;
    }

    record FramePreview(
            ResourceLocation texture,
            int width,
            int height
    ) {
        ;;
    }

    record Option(
            String value,
            String label,
            String detail,
            ItemStack icon,
            FramePreview framePreview
    ) {

        Option(String value, String label, String detail, ItemStack icon) {
            this(value, label, detail, icon, null);
        }

        String searchText() {
            return normalize(label + " " + value + " " + detail);
        }

    }

}