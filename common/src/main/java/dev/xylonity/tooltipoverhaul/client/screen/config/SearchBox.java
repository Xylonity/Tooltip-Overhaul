package dev.xylonity.tooltipoverhaul.client.screen.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.drawCard;
import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.playClickSound;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.*;

/**
 * Search box field
 */
final class SearchBox extends EditBox {

    private float focusAnimation;
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !getValue().isEmpty() && mouseX >= getX() + getWidth() - 12 && mouseX < getX() + getWidth() && mouseY >= getY() && mouseY < getY() + getHeight()) {
            setValue("");
            playClickSound();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        final int x0 = getX();
        final int y0 = getY();
        final int width = getWidth();
        final int height = getHeight();
        final int x1 = x0 + width;
        final int y1 = y0 + height;
        final boolean engaged = mouseX >= x0 && mouseX < x1 && mouseY >= y0 && mouseY < y1 || isFocused();
        focusAnimation = engaged ? Math.min(1f, focusAnimation + 0.12f) : Math.max(0f, focusAnimation - 0.06f);

        final int borderTarget = isFocused() ? accent & 0x00FFFFFF : dimAccent(accent);
        drawCard(graphics, x0, y0, x1, y1, 0xFF151517, 0xFF000000 | mixRgb(0x26262A, borderTarget, focusAnimation));

        final int[] channels = rgb(accent);
        int iconColor = 0xFF000000 | ((int) (0x48 + (channels[0] - 0x48) * focusAnimation) << 16) | ((int) (0x48 + (channels[1] - 0x48) * focusAnimation) << 8) | (int) (0x48 + (channels[2] - 0x48) * focusAnimation);
        ConfigIconButton.drawIcon(graphics, ConfigIconButton.Icon.SEARCH, x0 + 5, y0 + (height - ConfigIconButton.Icon.SEARCH.height) / 2, iconColor);

        final Minecraft client = Minecraft.getInstance();
        if (!getValue().isEmpty()) {
            final boolean clearHovered = mouseX >= x1 - 12 && mouseX < x1 && mouseY >= y0 && mouseY < y1;
            graphics.drawString(client.font, "\u00d7", x1 - 9, y0 + (height - client.font.lineHeight) / 2 + 1, clearHovered ? accent : 0xFF666666, false);
        }

        final int textPaddingX = 20;
        final int offsetY = (height - client.font.lineHeight) / 2 + 1;
        graphics.pose().pushPose();

        graphics.pose().translate(textPaddingX, offsetY, 0);
        super.renderWidget(graphics, mouseX - textPaddingX, mouseY - offsetY, partialTick);

        graphics.pose().popPose();
    }

}
