package dev.xylonity.tooltipoverhaul.client.screen.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameLoader;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameManager;
import dev.xylonity.tooltipoverhaul.client.render.TooltipAnimationState;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.render.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.style.animation.TooltipAnimator;
import dev.xylonity.tooltipoverhaul.client.util.PositionUtils;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextAxis;
import dev.xylonity.tooltipoverhaul.client.util.TextUtils;
import dev.xylonity.tooltipoverhaul.client.util.TooltipScrollState;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.drawCard;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.dimAccent;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.mixRgb;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.withAlpha;

final class FramePreviewPanel {

    private static final PreviewBounds PLACEHOLDER_BOUNDS = new PreviewBounds(150, 46, 0);
    private static final int STRIP_CAP = 12;

    private final int accent;
    private final List<ItemStack> stacks = new ArrayList<>();
    private final List<PreviewBounds> sizes = new ArrayList<>();
    private final Set<Item> brokenItems = new HashSet<>();
    private final List<String> unresolvedIds = new ArrayList<>();

    private @Nullable CustomFrameData data;
    private boolean hasEntry;
    private boolean broken;
    private int totalItems;
    private float contentWidth;
    private float contentHeight;
    private double panX;
    private double panY;
    private double zoom = 1;
    private int selectedItem;
    private boolean comparison;
    private float fittedScale = 1;
    private boolean panning;
    private long iconAnimationAt = Util.getMillis();
    private long tooltipAnimationAt = iconAnimationAt;
    private boolean tooltipAnimationOut;

    private int x;
    private int y;
    private int width;
    private int height;

    FramePreviewPanel(int accent) {
        this.accent = accent;
    }

    String refresh(JsonObject root, @Nullable JsonObject entry) {
        final int previousIndex = selectedItem;
        final Item previousItem = stacks.isEmpty() ? null : stacks.get(Math.min(selectedItem, stacks.size() - 1)).getItem();
        hasEntry = entry != null;
        broken = false;
        data = null;
        stacks.clear();
        sizes.clear();
        if (entry == null) {
            return "";
        }

        String error = "";
        try {
            data = CustomFrameLoader.parseFrame(root, entry);
        }
        catch (Exception exception) {
            broken = true;
            error = exception.getMessage() == null ? exception.toString() : exception.getMessage();
        }

        resolveStacks(entry);
        selectedItem = 0;
        if (previousItem != null) {
            if (previousIndex < stacks.size() && stacks.get(previousIndex).is(previousItem)) {
                selectedItem = previousIndex;
            }
            else for (int i = 0; i < stacks.size(); i++) {
                if (stacks.get(i).is(previousItem)) { selectedItem = i; break; }
            }

        }

        for (int i = 0; i < stacks.size(); i++) {
            sizes.add(null);
        }

        return error;
    }

    void resetViewAndAnimations() {
        panX = 0;
        panY = 0;
        zoom = 1;
        panning = false;
        iconAnimationAt = Util.getMillis();
        tooltipAnimationAt = iconAnimationAt;
    }

    void replayIconAnimation() {
        iconAnimationAt = Util.getMillis();
    }

    void replayTooltipAnimation() {
        tooltipAnimationAt = Util.getMillis();
        tooltipAnimationOut = false;
    }

    void replayTooltipDisappearAnimation() {
        tooltipAnimationAt = Util.getMillis();
        tooltipAnimationOut = true;
    }

    ItemStack stackFor(JsonObject entry) {
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

    ItemStack selectedStack() {
        return stacks.isEmpty() ? new ItemStack(Items.DIAMOND_SWORD) : stacks.get(selectedItem).copy();
    }

    void selectStack(ItemStack stack) {
        for (int i = 0; i < stacks.size(); i++) {
            if (stacks.get(i).is(stack.getItem())) {
                selectItem(i);
                return;
            }

        }

    }

    void renderListIcon(GuiGraphics graphics, JsonObject entry, int x, int y, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, Mth.clamp(alpha, 0f, 1f));
        try {
            renderItemSafe(graphics, stackFor(entry), x, y);
        }
        finally {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

    }

    void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        clampPan();
    }

    void render(GuiGraphics graphics, Font font, int screenWidth, int screenHeight, int mouseX, int mouseY, String validationError) {
        final int right = x + width, bottom = y + height;
        drawCard(graphics, x, y, right, bottom, 0xE60D0D0F, 0xFF232327);
        graphics.drawString(font, font.plainSubstrByWidth(I18n.get(key("preview")), Math.max(0, width - 78)), x + 8, y + 9, 0xFF9898A2, false);
        renderViewButtons(graphics, mouseX, mouseY);
        if (!hasEntry) {
            return;
        }

        if (broken || !validationError.isEmpty()) {
            int errorY = y + 34;
            for (FormattedCharSequence line : font.split(Component.literal(I18n.get(key("preview_error")) + ": " + validationError), Math.max(20, width - 20))) {
                if (errorY + font.lineHeight > bottom - 8) {
                    break;
                }

                graphics.drawString(font, line, x + 10, errorY, 0xFFFF9999, false);
                errorY += font.lineHeight + 2;
            }

            return;
        }

        if (stacks.isEmpty()) {
            return;
        }

        final int first = comparison ? selectedItem / 4 * 4 : selectedItem;
        final int end = comparison ? Math.min(stacks.size(), first + 4) : first + 1;
        final int virtualWidth = screenWidth + 4096, virtualHeight = screenHeight + 4096;
        final float previousCounter = TooltipRenderer.COUNTER;
        final TooltipScrollState.Snapshot previousScroll = TooltipScrollState.snapshot();

        TooltipRenderer.COUNTER = (Util.getMillis() - iconAnimationAt) / 1000f;

        graphics.enableScissor(x + 1, stageTop(), right - 1, stageBottom());
        try {
            CustomFrameManager.setPreviewOverride(data);
            TooltipAnimationState.setSuppressCapture(true);
            final List<FramePreviewLayout.Size> dimensions = new ArrayList<>();
            for (int i = first; i < end; i++) {
                if (sizes.get(i) == null) {
                    measureSlot(graphics, font, screenWidth, i, virtualWidth, virtualHeight);
                }

                final PreviewBounds measured = sizes.get(i);
                dimensions.add(new FramePreviewLayout.Size(measured.width(), measured.height()));
            }

            final FramePreviewLayout.Layout layout = FramePreviewLayout.arrange(dimensions, width, stageBottom() - stageTop(), comparison, zoom);
            contentWidth = layout.width();
            contentHeight = layout.height();
            fittedScale = layout.scale();

            clampPan();

            for (int i = first; i < end; i++) {
                final FramePreviewLayout.Slot slot = layout.slots().get(i - first);
                final float slotX = x + slot.x() + (float) panX;
                final float slotY = stageTop() + slot.y() + (float) panY;
                renderSlot(graphics, font, screenWidth, virtualWidth, virtualHeight, i, slotX, slotY, layout.scale());
            }

        }
        finally {
            CustomFrameManager.setPreviewOverride(null);
            TooltipAnimationState.setSuppressCapture(false);
            TooltipRenderer.COUNTER = previousCounter;
            TooltipScrollState.restore(previousScroll);

            graphics.disableScissor();
        }

        renderItemStrip(graphics, font, mouseX, mouseY);
        renderInfo(graphics, font, screenWidth, bottom, mouseX, mouseY);

        final String zoomLabel = Math.round(fittedScale * 100) + "%";
        graphics.drawString(font, zoomLabel, right - 8 - font.width(zoomLabel), bottom - 11, 0xFF777782, false);

        final String hint = I18n.get(key("preview_controls"));
        if (font.width(hint) < width - 72) {
            graphics.drawCenteredString(font, hint, x + width / 2, bottom - 11, 0xFF666671);
        }

        int control = headerControl(mouseX, mouseY);
        if (control >= 0) graphics.renderTooltip(font, Component.translatable(key(control == 0 ? "preview_focus" : control == 1 ? "preview_compare" : "preview_fit")), mouseX, mouseY);
    }

    private void renderSlot(GuiGraphics graphics, Font font, int screenWidth, int virtualWidth, int virtualHeight, int index, float slotX, float slotY, float scale) {
        ItemStack stack = stacks.get(index);
        if (brokenItems.contains(stack.getItem())) {
            renderBrokenSlot(graphics, font, stack, slotX, slotY, scale);
            return;
        }

        final float targetX = slotX + sizes.get(index).insetLeft() * scale;
        final float targetY = slotY;
        try {
            List<ClientTooltipComponent> components = TextUtils.getTooltipComponentsFrom(stack, font, screenWidth, 2.2f);
            TooltipContext context = new TooltipContext(graphics, font, components, 100, 100, virtualWidth, virtualHeight, DefaultTooltipPositioner.INSTANCE, stack, true);
            final TooltipRenderer renderer = new TooltipRenderer(context);

            renderer.init();

            sizes.set(index, measuredBounds(context));

            final float offsetX = PositionUtils.getMainPanelPosition(context, TextAxis.X);
            final float offsetY = PositionUtils.getMainPanelPosition(context, TextAxis.Y);
            final Vec2 actual = context.getTooltipPosition();

            graphics.pose().pushPose();

            graphics.pose().translate(targetX + scale * (offsetX - actual.x), targetY + scale * (offsetY - actual.y), 0);
            graphics.pose().scale(scale, scale, 1f);

            try {
                if (renderer.canRender()) {
                    float progress = (Util.getMillis() - tooltipAnimationAt) / 1000f / TooltipAnimator.duration(context);
                    if (tooltipAnimationOut) {
                        if (progress < 1) {
                            TooltipAnimator.render(context, true, progress);
                        }
                        else if (progress >= 1.5f) {
                            tooltipAnimationOut = false;
                            tooltipAnimationAt = Util.getMillis();
                        }

                    }
                    else {
                        TooltipAnimator.render(context, false, progress);
                    }

                }

            }
            finally {
                graphics.pose().popPose();
            }

        }
        catch (Throwable throwable) {
            flagBroken(stack, throwable);
            sizes.set(index, PLACEHOLDER_BOUNDS);
        }

    }

    private int stageTop() {
        return y + 28;
    }

    private int stageBottom() {
        return Math.max(stageTop() + 1, y + height - 58);
    }

    private int stripY() {
        return y + height - 39;
    }

    private int stripCapacity() {
        return Math.max(1, Math.min(STRIP_CAP, (width - 52) / 24));
    }

    private int stripStart() {
        return selectedItem / stripCapacity() * stripCapacity();
    }

    private int stripCount() {
        return Math.min(stripCapacity(), stacks.size() - stripStart());
    }

    private int stripLeft() {
        return x + (width - stripCount() * 24) / 2;
    }

    private int headerButtonX(int control) {
        return x + width - 64 + control * 20;
    }

    private int headerControl(double mouseX, double mouseY) {
        if (mouseY < y + 5 || mouseY >= y + 23) {
            return -1;
        }

        for (int i = 0; i < 3; i++) if (mouseX >= headerButtonX(i) && mouseX < headerButtonX(i) + 18) return i;
        return -1;
    }

    private void renderViewButtons(GuiGraphics graphics, int mouseX, int mouseY) {
        int hovered = headerControl(mouseX, mouseY);
        for (int i = 0; i < 3; i++) {
            int bx = headerButtonX(i), by = y + 5;
            final boolean active = i == (comparison ? 1 : 0);
            drawCard(graphics, bx, by, bx + 18, by + 18, active ? 0xFF202630 : hovered == i ? 0xFF222227 : 0xFF141416, active ? withAlpha(accent, 0xAA) : 0xFF303038);
            final int color = active || hovered == i ? accent : 0xFF92929E;
            if (i == 0) {
                drawCard(graphics, bx + 4, by + 5, bx + 14, by + 13, 0, color);
            }
            else if (i == 1) {
                for (int row = 0; row < 2; row++) for (int col = 0; col < 2; col++)
                    graphics.fill(bx + 4 + col * 6, by + 4 + row * 6, bx + 8 + col * 6, by + 8 + row * 6, color);
            }
            else {
                ConfigIconButton.drawIcon(graphics, ConfigIconButton.Icon.EXPAND, bx + 4, by + 4, color);
            }

        }

    }

    private void renderItemStrip(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
        final int bottom = y + height;
        graphics.fill(x + 8, bottom - 57, x + width - 8, bottom - 56, 0xFF222229);

        final String counter = (selectedItem + 1) + " / " + stacks.size();

        graphics.drawString(font, counter, x + width - 8 - font.width(counter), bottom - 51, 0xFF747481, false);
        graphics.drawString(font, font.plainSubstrByWidth(stacks.get(selectedItem).getHoverName().getString(), Math.max(1, width - 26 - font.width(counter))), x + 8, bottom - 51, 0xFFBCBCC6, false);
        graphics.drawCenteredString(font, "<", x + 13, stripY() + 7, selectedItem > 0 ? 0xFFA0A0AB : 0xFF41414A);
        graphics.drawCenteredString(font, ">", x + width - 13, stripY() + 7, selectedItem + 1 < stacks.size() ? 0xFFA0A0AB : 0xFF41414A);

        int hovered = -1;
        for (int i = 0; i < stripCount(); i++) {
            int index = stripStart() + i, bx = stripLeft() + i * 24, by = stripY();
            final boolean selected = index == selectedItem;
            final boolean over = mouseX >= bx && mouseX < bx + 22 && mouseY >= by && mouseY < by + 22;
            drawCard(graphics, bx, by, bx + 22, by + 22, selected ? 0xFF202833 : over ? 0xFF25252C : 0xFF151519, selected ? withAlpha(accent, 0xCC) : 0xFF303038);
            renderItemSafe(graphics, stacks.get(index), bx + 3, by + 3);
            if (over) {
                hovered = index;
            }

        }

        if (hovered >= 0) {
            graphics.renderTooltip(font, stacks.get(hovered).getHoverName(), mouseX, mouseY);
        }

    }

    private static String key(String suffix) {
        return "tooltipoverhaul.config.frames." + suffix;
    }

    private void resolveStacks(JsonObject entry) {
        totalItems = 0;
        unresolvedIds.clear();
        if (entry.has("items") && entry.get("items").isJsonArray()) {
            for (JsonElement element : entry.getAsJsonArray("items")) {
                if (!element.isJsonPrimitive()) {
                    continue;
                }

                final String raw = element.getAsString().trim();
                if (raw.isEmpty()) {
                    continue;
                }

                ResourceLocation id = ResourceLocation.tryParse(raw);
                if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
                    unresolvedIds.add(raw);
                    continue;
                }

                totalItems++;
                stacks.add(new ItemStack(BuiltInRegistries.ITEM.get(id)));
            }

        }

        if (stacks.isEmpty()) {
            stacks.add(new ItemStack(Items.DIAMOND_SWORD));
        }

    }

    private void flagBroken(ItemStack stack, Throwable throwable) {
        if (brokenItems.add(stack.getItem())) {
            TooltipOverhaul.LOGGER.warn("Frame editor cannot preview {} on this screen: {}", BuiltInRegistries.ITEM.getKey(stack.getItem()), throwable.toString());
        }

    }

    private void renderItemSafe(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (brokenItems.contains(stack.getItem())) {
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

    private void renderInfo(GuiGraphics graphics, Font font, int screenWidth, int bottom, int mouseX, int mouseY) {
        final int hidden = totalItems - stacks.size();
        final boolean unknown = !unresolvedIds.isEmpty();
        if (hidden <= 0 && !unknown) {
            return;
        }

        final int badgeX = x + 8;
        final int badgeY = bottom - 18;
        boolean hovered = mouseX >= badgeX && mouseX < badgeX + 11 && mouseY >= badgeY + 1 && mouseY < badgeY + 12;
        graphics.pose().pushPose();

        graphics.pose().translate(0, 0, 500);

        ConfigIconButton.drawIcon(graphics, ConfigIconButton.Icon.QUESTION, badgeX + 1, badgeY + 2, unknown ? 0xFFCC8040 : hovered ? accent : 0xFFB0B0B0);

        if (hovered) {
            final List<FormattedCharSequence> lines = new ArrayList<>();
            final int maxWidth = Math.min(240, width - 24);
            if (hidden > 0) {
                lines.addAll(font.split(Component.translatable(
                        "tooltipoverhaul.config.frames.preview_more", hidden), maxWidth));
            }
            if (unknown) {
                lines.addAll(font.split(Component.translatable(
                        "tooltipoverhaul.config.frames.preview_unknown", String.join(", ", unresolvedIds)), maxWidth));
            }

            int textWidth = 0;
            for (FormattedCharSequence line : lines) {
                textWidth = Math.max(textWidth, font.width(line));
            }

            final int boxWidth = textWidth + 12;
            final int boxHeight = lines.size() * (font.lineHeight + 1) - 1 + 10;
            final int popupX = Math.min(badgeX, screenWidth - 4 - boxWidth);
            final int popupY = badgeY - boxHeight - 4;

            drawCard(graphics, popupX, popupY, popupX + boxWidth, popupY + boxHeight, 0xF2121214, withAlpha(mixRgb(0x2E2E32, dimAccent(accent), 0.4f), 0xFF));

            int lineY = popupY + 6;
            for (FormattedCharSequence line : lines) {
                graphics.drawString(font, line, popupX + 6, lineY, 0xFFDDDDDD, false);
                lineY += font.lineHeight + 1;
            }

        }

        graphics.pose().popPose();
    }

    private void measureSlot(GuiGraphics graphics, Font font, int screenWidth, int index, int virtualWidth, int virtualHeight) {
        final ItemStack stack = stacks.get(index);
        if (brokenItems.contains(stack.getItem())) {
            sizes.set(index, PLACEHOLDER_BOUNDS);
            return;
        }

        try {
            final List<ClientTooltipComponent> components = TextUtils.getTooltipComponentsFrom(stack, font, screenWidth, 2.2f);
            TooltipContext context = new TooltipContext(graphics, font, components, 100, 100, virtualWidth, virtualHeight, DefaultTooltipPositioner.INSTANCE, stack, true);
            new TooltipRenderer(context).init();
            sizes.set(index, measuredBounds(context));
        }
        catch (Throwable throwable) {
            flagBroken(stack, throwable);
            sizes.set(index, PLACEHOLDER_BOUNDS);
        }

    }

    private static PreviewBounds measuredBounds(TooltipContext context) {
        final Vec2 size = context.getTooltipSize();
        float inset = 0;
        float panelHeight = 0;
        if (RenderUtils.hasPreviewOfStack(context) || RenderUtils.hasPreviewOfArmorItem(context)) {
            inset = RenderUtils.calculateSecondPanelSize(context, TextAxis.X) + 30;
            panelHeight = RenderUtils.calculateSecondPanelSize(context, TextAxis.Y) + 4;
        }

        return new PreviewBounds(size.x + inset, Math.max(size.y, panelHeight), inset);
    }

    private void renderBrokenSlot(GuiGraphics graphics, Font font, ItemStack stack, float slotX, float slotY, float scale) {
        graphics.pose().pushPose();

        graphics.pose().translate(slotX, slotY, 0);
        graphics.pose().scale(scale, scale, 1f);

        final int cardWidth = (int) PLACEHOLDER_BOUNDS.width();
        drawCard(graphics, 0, 0, cardWidth, (int) PLACEHOLDER_BOUNDS.height(), 0xE6151517, 0xFF2E2E32);

        final String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        graphics.drawString(font, font.plainSubstrByWidth(id, cardWidth - 16), 8, 11, 0xFFB0B0B0, false);

        final String reason = I18n.get("tooltipoverhaul.config.frames.preview_unrenderable");
        graphics.drawString(font, font.plainSubstrByWidth(reason, cardWidth - 16), 8, 27, 0xFF8A5A45, false);

        graphics.pose().popPose();
    }

    boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!contains(mouseX, mouseY)) {
            return false;
        }

        if (button == 1) {
            resetViewAndAnimations();
            return true;
        }

        if (button == 0) {
            final int control = headerControl(mouseX, mouseY);
            if (control >= 0) {
                if (control < 2) {
                    comparison = control == 1;
                }

                resetViewAndAnimations();
                return true;
            }

            if (!stacks.isEmpty() && mouseY >= stripY() && mouseY < stripY() + 22) {
                if (mouseX < x + 24) {
                    selectItem(selectedItem - 1);
                }
                else if (mouseX >= x + width - 24) {
                    selectItem(selectedItem + 1);
                }
                else {
                    final int index = (int) ((mouseX - stripLeft()) / 24);
                    if (mouseX >= stripLeft() && index >= 0 && index < stripCount()) {
                        selectItem(stripStart() + index);
                    }

                }

                return true;
            }

        }

        if (button == 0 && mouseY >= stageTop() && mouseY < stageBottom()) {
            panning = true;
            return true;
        }

        return true;
    }

    boolean mouseDragged(double deltaX, double deltaY) {
        if (!panning) {
            return false;
        }

        panX += deltaX;
        panY += deltaY;
        clampPan();

        return true;
    }

    void mouseReleased() {
        panning = false;
    }

    boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!contains(mouseX, mouseY)) {
            return false;
        }

        if (delta == 0) {
            return true;
        }

        if (mouseY >= stripY() && mouseY < stripY() + 22 && !stacks.isEmpty()) {
            selectItem(selectedItem + (delta > 0 ? -1 : 1));
            return true;
        }

        if (mouseY < stageTop() || mouseY >= stageBottom()) {
            return true;
        }

        final double nextZoom = Mth.clamp(zoom * (delta > 0 ? 1.15 : 1 / 1.15), 0.4, 2.0);
        if (nextZoom != zoom) {
            panX *= nextZoom / zoom;
            panY *= nextZoom / zoom;
            zoom = nextZoom;
            clampPan();
        }

        return true;
    }

    boolean isPanning() {
        return panning;
    }

    private void selectItem(int index) {
        final int next = Mth.clamp(index, 0, stacks.size() - 1);
        if (next == selectedItem) {
            return;
        }

        selectedItem = next;
        resetViewAndAnimations();
    }

    void clampPan() {
        final double limitX = Math.max(0, (contentWidth - width) / 2.0 + 24);
        final double limitY = Math.max(0, (contentHeight - (stageBottom() - stageTop())) / 2.0 + 24);
        panX = Mth.clamp(panX, -limitX, limitX);
        panY = Mth.clamp(panY, -limitY, limitY);
    }

    void close() {
        CustomFrameManager.setPreviewOverride(null);
        TooltipAnimationState.clear();
    }

    private record PreviewBounds(
            float width,
            float height,
            float insetLeft
    ) {
        ;;
    }

}