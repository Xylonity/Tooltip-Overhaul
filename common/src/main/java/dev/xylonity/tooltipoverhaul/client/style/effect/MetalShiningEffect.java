package dev.xylonity.tooltipoverhaul.client.style.effect;

import com.mojang.math.Axis;
import dev.xylonity.tooltipoverhaul.client.layer.impl.EffectLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import net.minecraft.world.phys.Vec2;

public class MetalShiningEffect implements EffectLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {
        int positionX = (int) position.x;
        int positionY = (int) position.y;
        int tooltipWidth = (int) context.getTooltipSize().x;
        int tooltipHeight = (int) context.getTooltipSize().y;

        context.push(() -> {
            context.getGraphics().enableScissor(
                    positionX - context.getPaddingX() - 1,
                    positionY - context.getPaddingY(),
                    positionX + tooltipWidth + context.getPaddingX(),
                    positionY + tooltipHeight + context.getPaddingY()
            );

            float phase = (System.currentTimeMillis() % 3500L) / 3500f;
            float frac = 1000f / 3500f;

            if (phase < frac) {
                float time = phase / frac;
                float startX = positionX + tooltipWidth + 40f;
                float curX = startX + ((positionX - 100f) - startX) * time;

                int baseDims = 3;
                int maxDims = 20;

                float theta = 1f - Math.abs(time - 0.25f) * 2f;

                int width = baseDims + Math.round(maxDims * theta);
                int height = 1000;

                int y = positionY + (tooltipHeight - height) / 2;
                float pivot = y + height / 2f;

                context.push(() -> {
                    context.translate(curX + width/2f, pivot, context.getLayerDepth().getZ());

                    context.multiply(Axis.ZP, -30);

                    context.multiply(Axis.ZP, 90);

                    int newX = -height / 2;
                    int newY = -width / 2;

                    int fadeDims = width / 3;
                    int centerDims = width /3;

                    // upper
                    context.getGraphics().fillGradient(newX, newY, newX + height, newY + fadeDims, 0x00FFFFFF, 0xFFFFFFFF);

                    // center
                    context.getGraphics().fill(newX, newY + fadeDims, newX + height, newY + fadeDims + centerDims, 0xFFFFFFFF);

                    // lower
                    context.getGraphics().fillGradient(newX, newY + fadeDims + centerDims, newX + height, newY + width, 0xFFFFFFFF, 0x00FFFFFF);
                });

            }

            context.getGraphics().disableScissor();
        });

    }

}