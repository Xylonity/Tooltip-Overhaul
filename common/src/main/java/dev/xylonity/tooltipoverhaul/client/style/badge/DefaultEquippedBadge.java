package dev.xylonity.tooltipoverhaul.client.style.badge;

import dev.xylonity.tooltipoverhaul.client.layer.impl.BackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

public class DefaultEquippedBadge implements BackgroundLayer {

    private final int color1;
    private final int color2;
    private final int color3;

    public DefaultEquippedBadge(int color1, int color2, int color3) {
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
    }

    @Override
    public void render(TooltipContext context, Vec2 position) {

        final int sizeY = 20;
        final int offsetY = 20;

        final int x0 = (int) (position.x - 3);
        final int y0 = (int) (position.y - sizeY - offsetY);
        final int x1 = (int) (position.x + context.getTooltipSize().x + 2);
        final int y1 = (int) (position.y - offsetY);

        final int backgroundColor = ColorUtils.getBackgroundColor(context);

        // Background
        context.getGraphics().fill(x0, y0, x1, y1, 0, backgroundColor);

        // Top border
        context.getGraphics().fill(x0, y0 - 1, x1, y0, 0, backgroundColor);
        // Bottom border
        context.getGraphics().fill(x0, y1 + 1, x1, y1, 0, backgroundColor);
        // Left border
        context.getGraphics().fill(x0 - 1, y0, x0, y1, 0, backgroundColor);
        // Right border
        context.getGraphics().fill(x1 + 1, y0, x1, y1, 0, backgroundColor);

        RenderUtils.renderFrameGradient(context.getGraphics(), x1 - 1, y0 + 1, x0 - x1 + 2, y1 - y0 - 2, color1, color2, color3);

        // Top and bottom lines
        context.getGraphics().fill(x1, y0, x0, y0 + 1, color1);
        context.getGraphics().fill(x1, y1, x0, y1 - 1, color3);

        final Component text = Component.translatable("tooltipoverhaul.equipped_badge_text");
        final int textHeight = ClientTooltipComponent.create(text.getVisualOrderText()).getHeight();
        final int textX = (int) (x0 + context.getTooltipSize().x / 2f - context.getFont().width(text) / 2f + context.getPaddingX());
        final int textY = (int) (y0 - textHeight / 2f +  sizeY / 1.7f);

        // Equipped text
        context.getGraphics().drawString(context.getFont(), text, textX, textY, color1, false);
    }

}