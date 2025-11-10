package dev.xylonity.tooltipoverhaul.client.util;

import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;

import java.util.Optional;

public class PositionUtils {

    public static int getMainPanelPosition(TooltipContext context, TextAxis axis) {
        if (axis == TextAxis.X) {
            return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getTooltipPositionX).orElse(TooltipsConfig.TOOLTIP_POSITION_X);
        }

        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getTooltipPositionY).orElse(TooltipsConfig.TOOLTIP_POSITION_Y);
    }

}
