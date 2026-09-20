package dev.xylonity.tooltipoverhaul.client.screen.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

final class LocalizedEditBox extends StyledEditBox {

    private final Font font;

    LocalizedEditBox(Font font, int width, int accent) {
        super(font, 0, 0, width, 18, Component.empty(), accent);
        this.font = font;
    }

    private boolean showsTranslation() {
        final String key = getValue();
        return !isFocused() && !key.isBlank() && I18n.exists(key);
    }

    @Override
    protected int fieldBackgroundColor() {
        return showsTranslation() ? 0xFF29292E : super.fieldBackgroundColor();
    }

    @Override
    protected void renderFieldText(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (showsTranslation()) {
            graphics.drawString(font, font.plainSubstrByWidth(I18n.get(getValue()), getInnerWidth()), getX(), getY(), 0xFFD0D0D0, false);
        }
        else {
            super.renderFieldText(graphics, mouseX, mouseY, partialTick);
        }

    }

}
