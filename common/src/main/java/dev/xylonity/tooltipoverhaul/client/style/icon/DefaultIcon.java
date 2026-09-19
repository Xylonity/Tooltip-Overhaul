package dev.xylonity.tooltipoverhaul.client.style.icon;

import com.mojang.math.Axis;
import dev.xylonity.tooltipoverhaul.client.layer.impl.IconLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.render.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.style.icon.animation.IconAnimation;
import dev.xylonity.tooltipoverhaul.client.style.icon.animation.IconAnimationFactory;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipLayout;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.compat.modernfix.ModernFixCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec2;

public class DefaultIcon implements IconLayer {

    private static final float ANIMATION_DURATION = 0.6f;
    private static final float ICON_SCALE = 1.35f;

    @Override
    public void render(TooltipContext context, Vec2 position) {

        final float positionX = context.getTooltipPosition().x + TooltipLayout.iconX(context);
        final float positionY = context.getTooltipPosition().y + TooltipLayout.iconY(context);

        context.push(() -> {

            context.translate(positionX + Constants.getIconSize(context) / 2f, positionY + Constants.getIconSize(context) / 2f, context.getLayerDepth().getZ());
            final float layoutScale = Constants.getIconSize(context) / 22f;
            context.scale(layoutScale, layoutScale, layoutScale);

            final IconAnimation animationType = IconAnimation.fromString(RenderUtils.getIconAppearAnimation(context));

            final float elapsed = TooltipRenderer.COUNTER;
            final float progress = Math.min(elapsed / ANIMATION_DURATION, 1.0f);

            // Entry animation before a continuous rotation is applied
            if (elapsed < ANIMATION_DURATION) {
                applyEntryAnimation(context, animationType, progress);
            }
            else {
                applyContinuousRotation(context, animationType, elapsed);
            }

            if (ModernFixCompat.SHOULD_RETURN_ORIGINAL_RENDER) {
                ModernFixCompat.push();
                try {
                    RenderUtils.renderItem(context, Minecraft.getInstance().player, Minecraft.getInstance().level, context.getStack(), 0);
                }
                finally {
                    ModernFixCompat.pop();
                }

            }
            else {
                RenderUtils.renderItem(context, Minecraft.getInstance().player, Minecraft.getInstance().level, context.getStack(), 0);
            }

        });

    }

    private void applyEntryAnimation(TooltipContext context, IconAnimation type, float progress) {
        if (type == IconAnimation.ROTATE || type == IconAnimation.ROTATE_ZOOM) {
            context.multiply(Axis.YP, 180);
        }

        IconAnimationFactory.get(type).apply(context, progress, ICON_SCALE);
    }

    private void applyContinuousRotation(TooltipContext context, IconAnimation type, float elapsed) {
        context.scale(ICON_SCALE, ICON_SCALE, ICON_SCALE);

        final float rotationSpeed = RenderUtils.getIconRotatingSpeed(context);
        if (rotationSpeed > 0) {
            final float additionalRotation = (elapsed - ANIMATION_DURATION) * (360f / (6.0f / rotationSpeed));
            context.multiply(Axis.YP, additionalRotation % 360);
        }

    }

}
