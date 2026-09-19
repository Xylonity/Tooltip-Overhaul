package dev.xylonity.tooltipoverhaul.client.screen.config;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.buttonBorder;
import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.drawCard;

/**
 * Small textured pixel icons with accessible names (per resource location)
 */
final class ConfigIconButton extends Button {

    enum Icon {

        ADD("icon_add", 9, 9),
        ALIGN("icon_align", 9, 9),
        COPY("icon_copy", 9, 9),
        EXPAND("icon_expand", 9, 9),
        MENU("icon_menu", 9, 9, 1, 1),
        PLAY("icon_play", 9, 9, 1, 1),
        QUESTION("icon_question", 9, 9),
        REPEAT("icon_repeat", 9, 11, 1, 1),
        RESIZE_PANEL("icon_resize_panel", 11, 9),
        SEARCH("icon_search", 9, 9),
        TEMPLATES("icon_templates", 9, 9),
        TRASH("icon_trash", 9, 9);

        final ResourceLocation texture;
        final int width;
        final int height;
        final int offsetX;
        final int offsetY;

        Icon(String name, int width, int height) {
            this(name, width, height, 0, 0);
        }

        Icon(String name, int width, int height, int offsetX, int offsetY) {
            this.texture = name == null ? null : TooltipOverhaul.pathOf("textures/gui/" + name + ".png");
            this.width = width;
            this.height = height;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

    }

    private final Icon icon;
    private final int accent;

    ConfigIconButton(int x, int y, int width, Icon icon, Component label, OnPress action, int accent) {
        super(x, y, width, 18, label, action, DEFAULT_NARRATION);
        this.icon = icon;
        this.accent = accent;
        setTooltip(Tooltip.create(label));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mx, int my, float delta) {
        drawCard(graphics, getX(), getY(), getX() + width, getY() + height, isHoveredOrFocused() ? 0xFF24242B : 0xFF161618, buttonBorder(accent, isHoveredOrFocused() ? 1f : 0f));
        final int x = getX() + (width - icon.width) / 2;
        final int y = getY() + (height - icon.height) / 2;
        final int color = !active ? 0xFF55555D : isHoveredOrFocused() ? accent : 0xFFC6C6D0;
        drawIcon(graphics, icon, x, y, color);
    }

    static void drawIcon(GuiGraphics graphics, Icon icon, int x, int y, int color) {
        final float[] saved = RenderSystem.getShaderColor().clone();
        final float red = ((color >> 16) & 0xFF) / 255f;
        final float green = ((color >> 8) & 0xFF) / 255f;
        final float blue = (color & 0xFF) / 255f;
        final float alpha = ((color >>> 24) & 0xFF) / 255f;

        graphics.flush();

        RenderSystem.setShaderColor(saved[0] * red, saved[1] * green, saved[2] * blue, saved[3] * alpha);
        try {
            graphics.blit(icon.texture, x + icon.offsetX, y + icon.offsetY, 0, 0, icon.width, icon.height, icon.width, icon.height);

            graphics.flush();
        }
        finally {
            RenderSystem.setShaderColor(saved[0], saved[1], saved[2], saved[3]);
        }

    }

    static void drawDiamond(GuiGraphics graphics, int cx, int cy, int color) {
        for (int row = -3; row <= 3; row++) {
            final int half = 3 - Math.abs(row);
            graphics.fill(cx - half, cy + row, cx + half + 1, cy + row + 1, color);
        }

    }

}