package dev.xylonity.tooltipoverhaul.client.render;

import dev.xylonity.tooltipoverhaul.client.layout.TooltipPositionCalculator;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipSizeCalculator;
import dev.xylonity.tooltipoverhaul.client.style.StyleFactory;
import dev.xylonity.tooltipoverhaul.client.style.animation.TooltipAnimator;
import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextUtils;
import dev.xylonity.tooltipoverhaul.client.util.TooltipScrollState;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import net.minecraft.world.phys.Vec2;

import javax.annotation.Nullable;

public class TooltipRenderer {

    private final @Nullable TooltipContext context;

    public static float COUNTER = 0;

    public TooltipRenderer(@Nullable TooltipContext context) {
        this.context = context;
    }

    public void init() {
        if (context == null) {
            return;
        }
        if (context.getComponents().isEmpty()) {
            return;
        }

        // The style is computed here so the context knows if its context pair already exists
        context.setTooltipLayers(new StyleFactory().create(context, context.getFrameData()));

        TooltipSizeCalculator sizeCalculator = context.getSizeCalculator();
        TooltipPositionCalculator positionCalculator = context.getPositionCalculator();

        int margin = 5;

        // Uncapped size calculation
        Vec2 uncappedSize = sizeCalculator.calculate();

        int screenHeight = context.getScreenHeight();
        int maxTooltipHeight = screenHeight - margin;
        int cappedHeight = Math.min((int) uncappedSize.y, maxTooltipHeight);

        context.setTooltipSize(new Vec2(uncappedSize.x, cappedHeight));
        context.setTooltipPosition(positionCalculator.calculate());

        if (!TextUtils.shouldDisableScrolling(context)) {
            TooltipScrollState.begin((int) uncappedSize.y, cappedHeight);
            TooltipScrollState.tick();
        }
        else {
            TooltipScrollState.reset();
        }

    }

    /**
     * Simple bridge to readjust the tooltip positions in case the equipped stack is enabled and any (or both) tooltip layouts are
     * exceeding the screen margins. The init predicate is called to compute the default layout values.
     */
    public void adjustLayout() {
        if (context == null) {
            return;
        }
        if (context.getComponents().isEmpty()) {
            return;
        }

        context.setTooltipPosition(context.getPositionCalculator().adjustPosition());
        context.setTooltipSize(context.getSizeCalculator().adjustSize());
    }

    public boolean canRender() {
        if (context == null) {
            return false;
        }
        if (context.getComponents().isEmpty()) {
            return false;
        }
        if (!RenderUtils.shouldRender(context)) {
            return false;
        }

        return !context.getStack().isEmpty() || TooltipsConfig.SHOW_TOOLTIP_WITHOUT_STACK;
    }

    public boolean render() {
        if (!canRender()) {
            return false;
        }

        // The main tooltip is cached every frame so an "out" animation can keep drawing it after the hover ends
        if (context.isMainTooltip()) {
            TooltipAnimationState.capture(context);
        }

        final float progress = AnimationUtils.clamp01(COUNTER / TooltipAnimator.duration(context));
        TooltipAnimator.render(context, false, progress);

        return true;
    }

    /**
     * Renders the cached main tooltip with an "out" animation
     */
    public void renderOut(float progress) {
        if (context == null) {
            return;
        }
        if (context.getComponents().isEmpty()) {
            return;
        }
        if (!RenderUtils.shouldRender(context)) {
            return;
        }
        if (context.getStack().isEmpty() && !TooltipsConfig.SHOW_TOOLTIP_WITHOUT_STACK) {
            return;
        }

        TooltipAnimator.render(context, true, AnimationUtils.clamp01(progress));
    }

}
