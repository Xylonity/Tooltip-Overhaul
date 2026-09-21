package dev.xylonity.tooltipoverhaul.client.style.preview.background;

import dev.xylonity.tooltipoverhaul.client.layer.impl.PreviewBackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.style.preview.PreviewPanelDecorations;
import net.minecraft.world.phys.Vec2;

public class DefaultPreviewBackground implements PreviewBackgroundLayer {

    @Override
    @SuppressWarnings("deprecation")
    public void render(TooltipContext context, Vec2 startPosition, Vec2 endPosition) {
        context.getGraphics().drawManaged(() -> renderBackground(context, startPosition, endPosition));
    }

    private void renderBackground(TooltipContext context, Vec2 startPosition, Vec2 endPosition) {
        final int x0 = (int) startPosition.x;
        final int y0 = (int) startPosition.y;
        final int x1 = (int) endPosition.x;
        final int y1 = (int) endPosition.y;
        if (x0 - x1 < 2 || y1 - y0 < 2) {
            return;
        }

        final int backgroundColor = ColorUtils.getBackgroundColor(context);
        RenderUtils.fillBackgroundShape(context.getGraphics(), x1, y0, x0, y1, backgroundColor, RenderUtils.getPreviewPanelBackgroundCornerType(context));

        if (RenderUtils.hasPreviewPanelSideTriangles(context)) {
            for (int row = 1; row < y1 - y0 - 1; row++) {
                final int outset = PreviewPanelDecorations.triangleOutset(y1 - y0, row);
                if (outset <= 0) {
                    continue;
                }

                context.getGraphics().fill(x1 - outset - 1, y0 + row, x1 - 1, y0 + row + 1, backgroundColor);
                context.getGraphics().fill(x0 + 1, y0 + row, x0 + outset + 1, y0 + row + 1, backgroundColor);
            }

        }

    }

}
