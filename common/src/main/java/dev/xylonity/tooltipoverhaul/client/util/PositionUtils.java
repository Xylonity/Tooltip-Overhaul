package dev.xylonity.tooltipoverhaul.client.util;

import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.style.vignette.parser.VignetteEntry;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;

import java.util.Optional;

public class PositionUtils {

    public static int getMainPanelPosition(TooltipContext context, TextAxis axis) {
        if (axis == TextAxis.X) {
            return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getTooltipPositionX).orElse(TooltipsConfig.TOOLTIP_POSITION_X);
        }

        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getTooltipPositionY).orElse(TooltipsConfig.TOOLTIP_POSITION_Y);
    }

    public static int getDividerLineTopPadding(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getDividerLineTopPadding).orElse(TooltipsConfig.DIVIDER_LINE_TOP_PADDING);
    }

    public static int getDividerLineBottomPadding(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getDividerLineBottomPadding).orElse(TooltipsConfig.DIVIDER_LINE_BOTTOM_PADDING);
    }

    public static int getSecondPanelPosition(TooltipContext context, TextAxis axis) {
        if (axis == TextAxis.X) {
            return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getSecondPanelX).orElse(TooltipsConfig.SECOND_PANEL_X);
        }

        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getSecondPanelY).orElse(TooltipsConfig.SECOND_PANEL_Y);
    }

    public static String getRatingTextAlignment(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getRatingAlignment).orElse(TooltipsConfig.RATING_X_ALIGNMENT);
    }

    public static String getTitleTextAlignment(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getTitleAlignment).orElse(TooltipsConfig.TITLE_X_ALIGNMENT);
    }

    public static int getVignettePosition(TooltipContext context, VignetteEntry vignetteEntry, TextAxis axis) {
        int tooltipWidth = (int) context.getTooltipSize().x;
        int tooltipHeight = (int) context.getTooltipSize().y;
        if (axis == TextAxis.X) {
            return (int) switch (vignetteEntry.position()) {
                case "middle", "top_middle", "bottom_middle" -> tooltipWidth * 0.5;
                case "right", "top_right", "bottom_right" -> tooltipWidth;
                default -> 0;
            };

        }

        return (int) switch (vignetteEntry.position()) {
            case "left", "middle", "right" -> tooltipHeight * 0.5;
            case "bottom_left", "bottom_middle", "bottom_right" -> tooltipHeight;
            default -> 0;
        };
    }

}
