package dev.xylonity.tooltipoverhaul.client.util;

import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;

public class Constants {

    public static int getDividerLineTopPadding(TooltipContext context) {
        return Math.max(context.getPaddingY(), context.hasIcon() ? 1 : 0) + PositionUtils.getDividerLineTopPadding(context);
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
        return 22;
    }

    public static int getIconTitleSeparation(TooltipContext context) {
        return 3;
    }

}