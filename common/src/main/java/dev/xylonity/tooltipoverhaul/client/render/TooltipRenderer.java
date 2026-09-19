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
import net.minecraft.util.Mth;
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

        final TooltipSizeCalculator sizeCalculator = context.getSizeCalculator();
        final TooltipPositionCalculator positionCalculator = context.getPositionCalculator();

        final int margin = 5;

        // Uncapped size calculation
        final Vec2 uncappedSize = sizeCalculator.calculate();

        final int screenHeight = context.getScreenHeight();
        int maxTooltipHeight = screenHeight - margin;
        final boolean scrollable = !TextUtils.shouldDisableScrolling(context);
        // The configured cap only applies when the overflow can be scrolled into view, otherwise it would just cut content
        if (scrollable) {
            maxTooltipHeight = Math.min(maxTooltipHeight, screenHeight * Mth.clamp(TooltipsConfig.MAX_TOOLTIP_HEIGHT, 10, 100) / 100);
        }

        final int cappedHeight = Math.min((int) uncappedSize.y, maxTooltipHeight);

        context.setTooltipSize(new Vec2(uncappedSize.x, cappedHeight));
        context.setTooltipPosition(positionCalculator.calculate());

        if (scrollable) {
            TooltipScrollState.begin((int) uncappedSize.y, cappedHeight);
            TooltipScrollState.tick();
        }
        else {
            TooltipScrollState.reset();
        }

    }

    /**
     * Simple bridge to readjust the tooltip positions in case the equipped stack is enabled and any (or both) tooltip layouts are
     * exceeding the screen margins. The init predicate is called to compute the default layout values
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

        // Still inside the configured appear delay
        if (COUNTER < 0) {
            return true;
        }

        int index = 0;
        if (context.isMainTooltip()) {
            // A tooltip that is a prefix of a bigger one at the same spot is consumed (preventing that one bug where multiple tooltips were drawn at the same time)
            if (TooltipFrameStack.superseded(context)) {
                return true;
            }

            index = TooltipFrameStack.register(context);

            // Tooltip cache so it can still be drawn after hovering (for the disappear animation)
            TooltipAnimationState.capture(context);
        }

        final float progress = AnimationUtils.clamp01(COUNTER / TooltipAnimator.duration(context));
        renderAtDepth(false, progress, index);

        return true;
    }

    private void renderAtDepth(boolean out, float progress, int index) {
        context.getPose().pushPose();
        context.getPose().translate(0, 0, Mth.clamp(TooltipsConfig.TOOLTIP_Z_OFFSET, 0, 700) + index * TooltipFrameStack.EXTRA_DEPTH);
        try {
            TooltipAnimator.render(context, out, progress);
        }
        finally {
            context.flush();
            context.getPose().popPose();
        }

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

        renderAtDepth(true, AnimationUtils.clamp01(progress), 0);
    }

}