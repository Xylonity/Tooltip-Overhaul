package dev.xylonity.tooltipoverhaul.client.style.icon.background;

import dev.xylonity.tooltipoverhaul.client.layer.impl.IconBackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipLayout;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import net.minecraft.world.phys.Vec2;

public class DetachedIconPlate implements IconBackgroundLayer {

    private final IconBackgroundLayer background;

    public DetachedIconPlate(IconBackgroundLayer background) {
        this.background = background;
    }

    @Override
    public void render(TooltipContext context, Vec2 position) {
        final int x = (int) position.x + TooltipLayout.iconX(context);
        final int y = (int) position.y + TooltipLayout.iconY(context);
        final int size = Constants.getIconSize(context);
        final int[] colors = ColorUtils.getRenderedInnerOverlayColors(context);
        final boolean badge = context.getLayoutStyle() == TooltipLayout.Style.BADGE;
        final boolean squareCorners = IconBorder.hasSquareCorners(context);

        context.getGraphics().fill(x - 1, y - 1, x + size + 1, y + size + 1, ColorUtils.getBackgroundColor(context));
        if (background != null) {
            context.push(() -> background.render(context, position));
        }

        for (int row = 0; row < size; row++) {
            final int color = ColorUtils.getInnerOverlayColorAtY(context, colors, y + row + 0.5f);
            context.getGraphics().fill(x - 1, y + row, x, y + row + 1, color);
            context.getGraphics().fill(x + size, y + row, x + size + 1, y + row + 1, color);
        }

        final int innerCorner = squareCorners ? 1 : 0;
        context.getGraphics().fill(x - innerCorner, y - 1, x + size + innerCorner, y, ColorUtils.getInnerOverlayColorAtY(context, colors, y - 0.5f));
        context.getGraphics().fill(x - innerCorner, y + size, x + size + innerCorner, y + size + 1, ColorUtils.getInnerOverlayColorAtY(context, colors, y + size + 0.5f));

        // Outer black edge
        final int blackEnd = badge ? (int) position.x - 3 : x + size;
        final int outerCorner = squareCorners ? 2 : 0;
        context.getGraphics().fill(x - outerCorner, y - 2, blackEnd + (badge ? 0 : outerCorner), y - 1, 0xFF000000);
        if (!squareCorners) {
            context.getGraphics().fill(x - 1, y - 1, x, y, 0xFF000000);
            context.getGraphics().fill(x - 1, y + size, x, y + size + 1, 0xFF000000);
        }

        context.getGraphics().fill(x - 2, y - outerCorner, x - 1, y + size + outerCorner, 0xFF000000);
        context.getGraphics().fill(x - outerCorner, y + size + 1, blackEnd + (badge ? 0 : outerCorner), y + size + 2, 0xFF000000);

        if (!badge) {
            if (!squareCorners) {
                context.getGraphics().fill(x + size, y - 1, x + size + 1, y, 0xFF000000);
                context.getGraphics().fill(x + size, y + size, x + size + 1, y + size + 1, 0xFF000000);
            }

            context.getGraphics().fill(x + size + 1, y - outerCorner, x + size + 2, y + size + outerCorner, 0xFF000000);
        }

    }

}
