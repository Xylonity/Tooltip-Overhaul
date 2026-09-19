package dev.xylonity.tooltipoverhaul.client.util;

import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipLayout;

public class Constants {

    public static int getDividerLineTopPadding(TooltipContext context) {
        return Math.max(context.getPaddingY(), TooltipLayout.hasHeaderIcon(context) ? 1 : 0) + PositionUtils.getDividerLineTopPadding(context);
    }

    public static int getDividerLineBottomPadding(TooltipContext context) {
        return Math.max(context.getPaddingY(), 1) + PositionUtils.getDividerLineBottomPadding(context);
    }

    public static int getDividerLineHeight(TooltipContext context) {
        return 1;
    }

    public static int getDividerLineFullPadding(TooltipContext context) {
        return getDividerLineBottomPadding(context) + getDividerLineHeight(context) + getDividerLineTopPadding(context);
    }

    public static int getIconSize(TooltipContext context) {
        final int base = context.getLayoutStyle() == TooltipLayout.Style.COMPACT ? 16 : 22;
        return Math.max(8, Math.round(base * RenderUtils.getIconScale(context)));
    }

    public static int getIconTitleSeparation(TooltipContext context) {
        return 3;
    }

    public static int getOverlayFrameDimension() {
        return 132;
    }

    public static int getOverlayFrameTime() {
        return 120;
    }

}