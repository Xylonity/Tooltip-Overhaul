package dev.xylonity.tooltipoverhaul.client.style.animation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import net.minecraft.world.phys.Vec2;

/**
 * Plays the configured in/out animation while drawing a tooltip's layers. The animation is expressed as a single affine
 * transform applied around the tooltip center
 */
public final class TooltipAnimator {

    private static final float POP_MIN_SCALE = 0.8f;
    private static final float RISE_DISTANCE = 18f;
    private static final float UNFOLD_MIN_SCALE = 0.05f;
    private static final float ZOOM_MIN_SCALE = 0.3f;
    private static final float SLIDE_DISTANCE = 16f;
    private static final float SWING_MIN_SCALE = 0.9f;
    private static final float SWING_ANGLE = -16f;
    private static final float SQUASH_WIDE = 1.4f;
    private static final float SQUASH_SHORT = 0.6f;
    private static final float CARD_UP_ANGLE = -12f;
    private static final float CARD_DOWN_ANGLE = 12f;
    private static final float SHAKE_AMPLITUDE = 4f;

    public static float duration() {
        return Math.max(TooltipsConfig.TOOLTIP_ANIMATION_DURATION, 0.0001f);
    }

    /**
     * Draws the context's layers wrapped in the configured animation
     */
    public static void render(TooltipContext context, boolean out, float progress) {
        final TooltipAnimation animation = TooltipAnimation.fromString(TooltipsConfig.TOOLTIP_APPEAR_ANIMATION);
        final Transform transform = computeTransform(animation, out, AnimationUtils.clamp01(progress));

        final boolean faded = transform.alpha < 1f;
        if (faded) {
            RenderSystem.setShaderColor(1f, 1f, 1f, transform.alpha);
        }

        context.getPose().pushPose();

        final boolean transformed = transform.scaleX != 1f || transform.scaleY != 1f || transform.rotation != 0f || transform.offsetX != 0f || transform.offsetY != 0f;
        if (transformed) {
            final Vec2 position = context.getTooltipPosition();
            final Vec2 size = context.getTooltipSize();
            final float pivotX = position.x + size.x * transform.originX;
            final float pivotY = position.y + size.y * transform.originY;

            context.getPose().translate(transform.offsetX, transform.offsetY, 0f);
            context.getPose().translate(pivotX, pivotY, 0f);
            if (transform.rotation != 0f) {
                context.multiply(Axis.ZP, transform.rotation);
            }

            context.getPose().scale(transform.scaleX, transform.scaleY, 1f);
            context.getPose().translate(-pivotX, -pivotY, 0f);
        }

        for (final ITooltipLayer layer : context.getTooltipLayers()) {
            layer.renderInternal(context);
        }

        if (faded) {
            context.flush();
        }

        context.getPose().popPose();

        if (faded) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

    }

    /**
     * Computes the transform for the given animation at a point in time
     */
    private static Transform computeTransform(TooltipAnimation animation, boolean out, float progress) {
        final Transform transform = new Transform();

        final float shown = out ? 1f - AnimationUtils.easeInQuad(progress) : AnimationUtils.easeOutQuad(progress);
        final float bounced = out ? 1f - AnimationUtils.easeInQuad(progress) : AnimationUtils.easeOutBack(progress);

        switch (animation) {
            case FADE -> transform.alpha = shown;
            case POP -> {
                setScale(transform, AnimationUtils.lerp(POP_MIN_SCALE, 1f, bounced));
                transform.alpha = shown;
            }
            case RISE -> {
                // Slides up into place from slightly below
                transform.offsetY = AnimationUtils.lerp(RISE_DISTANCE, 0f, bounced);
                transform.alpha = shown;
            }
            case UNFOLD -> {
                // Unfolds vertically from the top
                transform.scaleY = AnimationUtils.lerp(UNFOLD_MIN_SCALE, 1f, shown);
                transform.originY = 0f;
                transform.alpha = shown;
            }
            case ZOOM -> {
                // Grow from very small to full size
                setScale(transform, AnimationUtils.lerp(ZOOM_MIN_SCALE, 1f, shown));
                transform.alpha = shown;
            }
            case SLIDE -> {
                // Slides in horizontally from the left
                transform.offsetX = AnimationUtils.lerp(-SLIDE_DISTANCE, 0f, shown);
                transform.alpha = shown;
            }
            case SWING -> {
                // Tilt and scale around the center
                setScale(transform, AnimationUtils.lerp(SWING_MIN_SCALE, 1f, bounced));
                transform.rotation = AnimationUtils.lerp(SWING_ANGLE, 0f, bounced);
                transform.alpha = shown;
            }
            case EMERGE -> {
                // Grows out from the top-left edge
                setScale(transform, shown);
                transform.originX = 0f;
                transform.originY = 0f;
                transform.alpha = shown;
            }
            case SQUASH -> {
                transform.scaleX = AnimationUtils.lerp(SQUASH_WIDE, 1f, bounced);
                transform.scaleY = AnimationUtils.lerp(SQUASH_SHORT, 1f, bounced);
                transform.alpha = out ? shown : AnimationUtils.clamp01(progress * 3f);
            }
            case CARD -> {
                // Idk how to define this so yeah
                transform.originX = 0f;
                transform.originY = 0f;
                transform.rotation = out
                        ? AnimationUtils.lerp(0f, CARD_DOWN_ANGLE, 1f - shown)
                        : AnimationUtils.lerp(CARD_UP_ANGLE, 0f, shown);
                transform.alpha = shown;
            }
            case SHAKE -> {
                if (!out) {
                    transform.offsetX = (float) Math.sin(progress * Math.PI * 6.0f) * (1f - progress) * SHAKE_AMPLITUDE;
                }

                transform.alpha = out ? shown : AnimationUtils.clamp01(progress * 2.5f);
            }
            default -> {
                ;;
            }

        }

        return transform;
    }

    private static void setScale(Transform transform, float scale) {
        transform.scaleX = scale;
        transform.scaleY = scale;
    }

    private static final class Transform {
        private float scaleX = 1f;
        private float scaleY = 1f;
        private float alpha = 1f;
        private float offsetX = 0f;
        private float offsetY = 0f;
        private float rotation = 0f;
        private float originX = 0.5f;
        private float originY = 0.5f;
    }

}