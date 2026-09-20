package dev.xylonity.tooltipoverhaul.client.style.preview.inner;

import dev.xylonity.tooltipoverhaul.client.layer.impl.PreviewInnerOverlayLayer;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipLayout;
import dev.xylonity.tooltipoverhaul.client.style.preview.PreviewPanelDecorations;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import net.minecraft.world.phys.Vec2;

import java.util.function.IntUnaryOperator;

public class DefaultPreviewGradientInnerOverlay implements PreviewInnerOverlayLayer {

    private final int color1;
    private final int color2;
    private final int color3;

    public DefaultPreviewGradientInnerOverlay(int color1, int color2, int color3) {
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void render(TooltipContext context, Vec2 startPosition, Vec2 endPosition) {
        context.getGraphics().drawManaged(() -> renderFrame(context, startPosition, endPosition));
    }

    private void renderFrame(TooltipContext context, Vec2 startPosition, Vec2 endPosition) {
        final int x0 = (int) startPosition.x;
        final int y0 = (int) startPosition.y;
        final int x1 = (int) endPosition.x;
        final int y1 = (int) endPosition.y;
        if (x0 - x1 < 2 || y1 - y0 < 2) {
            return;
        }

        final PreviewPanelDecorations.SideTriangles triangles = RenderUtils.getPreviewPanelSideTriangles(context);
        final boolean floating = context.getLayoutStyle() == TooltipLayout.Style.FLOATING;
        final int[] colors = floating ? ColorUtils.getRenderedInnerOverlayColors(context) : new int[]{color1, color2, color3};
        IntUnaryOperator colorAtRow = row -> floating
                ? ColorUtils.getInnerOverlayColorAtY(context, colors, y0 + row + 0.5f)
                : borderColor(colors, y1 - y0, row);

        final String cornerType = RenderUtils.getPreviewPanelCornerType(context);
        final boolean cornerCut = RenderUtils.getPreviewPanelBackgroundCornerType(context) == PreviewPanelDecorations.BackgroundCornerType.NOTCH;
        final int trim = Math.max(cornerCut ? 1 : 0, RenderUtils.cornerTrim(cornerType));
        final int sideTrim = Math.max(1, trim);

        if (triangles != PreviewPanelDecorations.SideTriangles.NONE) {
            PreviewPanelDecorations.renderSides(context.getGraphics(), x1, y0, x0, y1, triangles, sideTrim, colorAtRow);
        }
        else if (floating) {
            for (int y = y0 + sideTrim; y < y1 - sideTrim; y++) {
                final int color = ColorUtils.getInnerOverlayColorAtY(context, colors, y + 0.5f);
                context.getGraphics().fill(x1, y, x1 + 1, y + 1, color);
                context.getGraphics().fill(x0 - 1, y, x0, y + 1, color);
            }

        }
        else {
            RenderUtils.renderFrameGradient(context.getGraphics(), x1, y0 + 1, x0 - x1, y1 - y0 - 2, color1, color2, color3, sideTrim - 1, sideTrim - 1);
        }

        // Top and bottom lines
        final int topColor = colorAtRow.applyAsInt(0), bottomColor = colorAtRow.applyAsInt(y1 - y0 - 1);
        context.getGraphics().fill(x1 + trim, y0, x0 - trim, y0 + 1, topColor);
        context.getGraphics().fill(x1 + trim, y1, x0 - trim, y1 - 1, bottomColor);
        RenderUtils.applyFrameCorners(context.getGraphics(), x1, y0, x0 - x1, y1 - y0, topColor, bottomColor, cornerType);
    }

    private static int borderColor(int[] colors, int height, int row) {
        if (row == 0) {
            return colors[0];
        }

        if (row == height - 1) {
            return colors[2];
        }

        final int middle = (height - 1) / 2;
        final float offset = row - 0.5f;
        return offset < middle ? ColorUtils.lerpColor(colors[0], colors[1], offset / Math.max(1, middle))
                : ColorUtils.lerpColor(colors[1], colors[2], (offset - middle) / Math.max(1, height - 1 - middle));
    }

}
