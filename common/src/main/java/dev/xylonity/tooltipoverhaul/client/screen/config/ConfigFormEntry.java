package dev.xylonity.tooltipoverhaul.client.screen.config;

import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.List;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.drawCard;
import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.drawCardHoverFx;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.dimAccent;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.mixRgb;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.rgb;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.withAlpha;

/**
 * Visual and interaction contract for one configuration value
 */
class ConfigFormEntry {

    // Vertical gap
    static final int ROW_GAP = 4;

    Component label;
    String description;
    @Nullable AbstractWidget widget;
    String stableKey;
    int accent;

    float hoverAnim;
    long hoverStart;
    int height;
    int currentY;

    private int minimumHeight;
    private List<FormattedCharSequence> descriptionLines = List.of();
    private int cachedWidth = -1;

    ConfigFormEntry() {
        this(Component.empty(), "", null, "", 0, 0);
    }

    ConfigFormEntry(Component label, String description, @Nullable AbstractWidget widget, String stableKey, int accent, int minimumHeight) {
        this.label = label;
        this.description = description;
        this.widget = widget;
        this.stableKey = stableKey;
        this.accent = accent;
        this.minimumHeight = minimumHeight;
        this.height = minimumHeight;
    }

    final void initialize(Component label, String description, @Nullable AbstractWidget widget, String stableKey, int accent, int minimumHeight) {
        this.label = label;
        this.description = description;
        this.widget = widget;
        this.stableKey = stableKey;
        this.accent = accent;
        this.minimumHeight = minimumHeight;
        this.height = minimumHeight;
        this.cachedWidth = -1;
        this.descriptionLines = List.of();
    }

    boolean isModified() {
        return false;
    }

    boolean requiresRestart() {
        return false;
    }

    protected void prepareWidgetWidth(int rowWidth) {
        ;;
    }

    protected int expandedWidgetWidth(int rowWidth) {
        return rowWidth - 40;
    }

    protected int descriptionSpacing() {
        return 54;
    }

    final void recalcHeight(int rowWidth) {
        if (widget == null) {
            height = minimumHeight;
            descriptionLines = List.of();
            return;
        }

        prepareWidgetWidth(rowWidth);

        final int layoutWidth = Math.max(1, rowWidth);
        if (cachedWidth == layoutWidth) {
            return;
        }

        cachedWidth = layoutWidth;

        final Minecraft minecraft = Minecraft.getInstance();

        final int available = rowWidth - widget.getWidth() - descriptionSpacing();
        final int descriptionWidth = Math.min(available, (int) (rowWidth * 0.5f));

        descriptionLines = !description.isEmpty() && descriptionWidth > 30 ? minecraft.font.split(Component.literal(description), descriptionWidth) : List.of();

        final int contentHeight = minecraft.font.lineHeight + (descriptionLines.isEmpty() ? 0 : 2 + descriptionLines.size() * minecraft.font.lineHeight);
        height = Math.max(minimumHeight, Math.max(contentHeight + 12, widget.getHeight() + 12));
    }

    void render(GuiGraphics graphics, int index, int top, int left, int width, int mouseX, int mouseY, boolean hovered, float entrance, float partialTick) {
        if (widget == null) {
            return;
        }

        recalcHeight(width);

        currentY = top;
        if (!hovered) {
            hoverStart = 0;
        }

        final float target = hovered ? 1f : 0f;
        final float difference = target - hoverAnim;
        hoverAnim += difference * 0.12f;
        if (Math.abs(difference) < 0.005f) {
            hoverAnim = target;
        }

        final float hover = AnimationUtils.smoothstep(0f, 1f, hoverAnim);
        final int slide = (int) ((1f - entrance) * 16f);

        left += slide;

        final int alpha = (int) (0xFF * entrance);
        final int background = (alpha << 24) | mixRgb(0x151517, 0x1B1B1F, hover);
        final int border = (alpha << 24) | mixRgb(0x26262A, dimAccent(accent), hover);

        drawCard(graphics, left, top, left + width, top + height, background, border);
        drawCardHoverFx(graphics, left, top, width, height, hover * entrance, accent, index * 73 + stableKey.hashCode());

        prepareWidgetWidth(width);
        float textFade = 1f;
        if (widget instanceof StyledEditBox box) {
            widget.setWidth(Math.max(1, box.layoutWidth(Math.max(1, expandedWidgetWidth(width)))));
            textFade = 1f - box.expandProgress();
        }

        final int textAlpha = (int) (alpha * textFade);
        final Minecraft minecraft = Minecraft.getInstance();
        int contentHeight = minecraft.font.lineHeight + (descriptionLines.isEmpty() ? 0 : 2 + descriptionLines.size() * minecraft.font.lineHeight);
        final int contentTop = top + (height - contentHeight) / 2;
        final int labelX = left + 10;
        if (textAlpha >= 0x10) {
            graphics.drawString(minecraft.font, label, labelX, contentTop, withAlpha(mixRgb(0xC4C4C4, 0xFFFFFF, hover), textAlpha), false);
            int afterLabel = labelX + minecraft.font.width(label);
            if (requiresRestart()) {
                graphics.drawString(minecraft.font, "\u27f3", afterLabel + 4, contentTop, withAlpha(0xFF8844, textAlpha), false);
                afterLabel += 4 + minecraft.font.width("\u27f3");
            }

            if (isModified()) {
                final int[] channels = rgb(accent);
                graphics.fill(afterLabel + 5, contentTop + 2, afterLabel + 8, contentTop + 5, (textAlpha << 24) | (channels[0] << 16) | (channels[1] << 8) | channels[2]);
            }

            int descriptionY = contentTop + minecraft.font.lineHeight + 2;
            for (final FormattedCharSequence line : descriptionLines) {
                graphics.drawString(minecraft.font, line, labelX, descriptionY, withAlpha(mixRgb(0x646464, 0x8C8C8C, hover), textAlpha), false);
                descriptionY += minecraft.font.lineHeight;
            }

        }

        final int widgetX = left + width - widget.getWidth() - 8;
        final int widgetY = top + (height - widget.getHeight()) / 2;
        widget.setX(widgetX);
        widget.setY(widgetY);
        widget.setAlpha(entrance);

        if (entrance <= 0f) {
            return;
        }

        renderAccessories(graphics, mouseX, mouseY, widgetX, widgetY, alpha, textFade, entrance, hover);

        widget.render(graphics, mouseX, mouseY, partialTick);
    }

    protected void renderAccessories(GuiGraphics graphics, int mouseX, int mouseY, int widgetX, int widgetY, int alpha, float textFade, float entrance, float hover) {
        ;;
    }

    void setFocused(boolean focused) {
        if (widget != null) {
            widget.setFocused(focused);
            if (widget instanceof EditBox editBox) {
                editBox.setFocused(focused);
            }

        }

    }

    boolean mouseClicked(double mouseX, double mouseY, int button) {
        return widget != null && widget.mouseClicked(mouseX, mouseY, button);
    }

    boolean mouseReleased(double mouseX, double mouseY, int button) {
        return widget != null && widget.mouseReleased(mouseX, mouseY, button);
    }

    boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return widget != null && widget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    boolean keyPressed(int key, int scanCode, int modifiers) {
        return widget != null && widget.keyPressed(key, scanCode, modifiers);
    }

    boolean charTyped(char character, int modifiers) {
        return widget != null && widget.charTyped(character, modifiers);
    }

}