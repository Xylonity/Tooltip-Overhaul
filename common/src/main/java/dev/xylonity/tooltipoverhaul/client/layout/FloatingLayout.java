package dev.xylonity.tooltipoverhaul.client.layout;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.style.preview.PreviewPanelDecorations;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.client.util.PositionUtils;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextAxis;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import net.minecraft.world.phys.Vec2;

import org.joml.Matrix4f;

/**
 * One placement for the external icon and the preview below it, relative to the main panel
 */
public record FloatingLayout(
        int iconX,
        int iconY,
        Vec2 previewStart,
        Vec2 previewEnd
) {

    private static final int GAP = 6;
    private static final int SCREEN_MARGIN = 2;

    private static boolean hasPreview(TooltipContext context) {
        return RenderUtils.hasPreviewOfStack(context) || RenderUtils.hasPreviewOfArmorItem(context);
    }

    private static int width(TooltipContext context) {
        final int iconWidth = Constants.getIconSize(context) + 4;
        final int sideExtent = RenderUtils.hasPreviewPanelSideTriangles(context) ? PreviewPanelDecorations.TRIANGLE_WIDTH : 0;
        return hasPreview(context) ? Math.max(iconWidth, RenderUtils.calculateSecondPanelSize(context, TextAxis.X) + 6 + sideExtent * 2) : iconWidth;
    }

    private static int offset(TooltipContext context, TextAxis axis) {
        return hasPreview(context) ? PositionUtils.getSecondPanelPosition(context, axis) : 0;
    }

    /**
     * Ensures one side has room for the complete group when the combined width fits the screen
     */
    public static float adjustPanelX(TooltipContext context, float panelX) {
        int width = width(context);
        int offsetX = offset(context, TextAxis.X);
        final float panelWidth = context.getTooltipSize().x;
        float left = panelX - 3 - GAP - width + offsetX;
        float right = panelX + panelWidth + 3 + GAP - offsetX;
        if (left >= SCREEN_MARGIN || (TooltipsConfig.AUTO_REPOSITION_PREVIEW_PANEL && right + width <= context.getScreenWidth() - SCREEN_MARGIN)) {
            return panelX;
        }

        final float minX = SCREEN_MARGIN + 3 + GAP + width - offsetX;
        final float maxX = context.getScreenWidth() - panelWidth - Math.max(context.getPaddingX(), SCREEN_MARGIN);
        return minX <= maxX ? Math.max(panelX, minX) : panelX;
    }

    public static FloatingLayout create(TooltipContext context) {
        final int iconSize = Constants.getIconSize(context);
        final int iconExtent = iconSize + 4;
        final boolean preview = hasPreview(context);
        final int previewWidth = RenderUtils.calculateSecondPanelSize(context, TextAxis.X) + 4;
        final int previewHeight = RenderUtils.calculateSecondPanelSize(context, TextAxis.Y) + 2;
        final int width = width(context);
        final int height = iconExtent + (preview ? GAP + previewHeight + 2 : 0);
        final int offsetX = offset(context, TextAxis.X);
        final int offsetY = offset(context, TextAxis.Y);

        final Matrix4f view = RenderSystem.getModelViewStack().last().pose();
        final float panelX = context.getTooltipPosition().x + Math.round(view.m30());
        final float panelY = context.getTooltipPosition().y + Math.round(view.m31());
        final int left = -3 - GAP - width + offsetX;
        final int right = (int) context.getTooltipSize().x + 3 + GAP - offsetX;
        final boolean moveRight = TooltipsConfig.AUTO_REPOSITION_PREVIEW_PANEL && panelX + left < SCREEN_MARGIN;
        final int groupX = moveRight ? right : left;

        // Vertically centered when the combined group is taller than the tooltip itself
        int groupY = height > context.getTooltipSize().y ? (int) Math.floor((context.getTooltipSize().y - height) / 2f) : context.getPaddingY() - 2;
        groupY += offsetY;

        final float top = panelY + groupY;
        final float clampedTop = Math.max(SCREEN_MARGIN, Math.min(top, context.getScreenHeight() - height - SCREEN_MARGIN));
        groupY += Math.round(clampedTop - top);

        final int iconX = groupX + (width - iconExtent) / 2 + 2 - (preview ? 0 : 6);
        final int iconY = groupY + 2;
        final int previewLeft = groupX + (width - previewWidth) / 2;
        final int previewTop = groupY + iconExtent + GAP + 1;
        return new FloatingLayout(iconX, iconY, new Vec2(previewLeft + previewWidth, previewTop), new Vec2(previewLeft, previewTop + previewHeight));
    }

}