package dev.xylonity.tooltipoverhaul.client.style.icon.animation;

import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;

@FunctionalInterface
public interface IIconAnimation {

    void apply(TooltipContext context, float progress, float scale);

}