package dev.xylonity.tooltipoverhaul.client.screen.config;

import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Predicate;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.drawCard;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.dimAccent;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.mixRgb;

public class StyledEditBox extends EditBox {

    private float focusAnimation;
    private float expandAnimation;
    private int baseWidth;
    private final int accent;
    private Predicate<String> validator = raw -> true;

    StyledEditBox(Font font, int x, int y, int width, int height, Component message, int accent) {
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

    void setBaseWidth(int width) {
        baseWidth = Math.max(1, width);
        if (!isFocused()) {
            setWidth(baseWidth);
        }

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
        expandAnimation += (target - expandAnimation) * 0.2f;
        if (Math.abs(target - expandAnimation) < 0.01f) {
            expandAnimation = target;
        }

        if (expandedMax <= baseWidth || expandAnimation <= 0f) {
            return baseWidth;
        }

        return baseWidth + (int) ((expandedMax - baseWidth) * AnimationUtils.smoothstep(0f, 1f, expandAnimation));
    }

    float expandProgress() {
        return AnimationUtils.smoothstep(0f, 1f, expandAnimation);
    }

    @Override
    public int getInnerWidth() {
        return width - 12;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        final int x0 = getX();
        final int y0 = getY();
        final int x1 = x0 + getWidth();
        final int y1 = y0 + getHeight();
        final boolean engaged = mouseX >= x0 && mouseX < x1 && mouseY >= y0 && mouseY < y1 || isFocused();
        focusAnimation = engaged ? Math.min(1f, focusAnimation + 0.15f) : Math.max(0f, focusAnimation - 0.1f);

        final boolean valid = validator.test(getValue());
        final int borderTarget = isFocused() ? accent & 0x00FFFFFF : dimAccent(accent);
        final int border = valid ? 0xFF000000 | mixRgb(0x3A3A3E, borderTarget, focusAnimation) : 0xFFCC4444;
        drawCard(graphics, x0, y0, x1, y1, valid ? 0xFF161618 : 0xFF221214, border);

        final int offsetY = (height - Minecraft.getInstance().font.lineHeight) / 2 + 1;
        final int paddingX = 5;

        graphics.enableScissor(x0 + 1, y0 + 1, x1 - 1, y1 - 1);
        graphics.pose().pushPose();

        graphics.pose().translate(paddingX, offsetY, 0);

        super.renderWidget(graphics, mouseX - paddingX, mouseY - offsetY, partialTick);

        graphics.pose().popPose();
        graphics.disableScissor();
    }

}
