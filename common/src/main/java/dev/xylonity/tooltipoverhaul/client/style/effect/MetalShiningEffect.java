package dev.xylonity.tooltipoverhaul.client.style.effect;

import com.mojang.math.Axis;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipEffect;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class MetalShiningEffect implements ITooltipEffect {

    @Override
    public void render(LayerDepth depth, TooltipContext context, Vec2 pos, Point size) {
        int guiX = (int) pos.x;
        int guiY = (int) pos.y;
        int guiW = size.x;
        int guiH = size.y;

        context.push(() -> {
            context.graphics().enableScissor(guiX - 3, guiY - 3, guiX + guiW + 3, guiY + guiH + 3);

            float phase = (System.currentTimeMillis() % 3500L) / 3500f;
            float frac = 1000f / 3500f;

            if (phase < frac) {
                float time = phase / frac;
                float startX = guiX + guiW + 40f;
                float curX = startX + ((guiX - 100f) - startX) * time;

                int baseDims = 3;
                int maxDims = 20;

                float theta = 1f - Math.abs(time - 0.25f) * 2f;

                int width = baseDims + Math.round(maxDims * theta);
                int height = 1000;

                int y = guiY + (guiH - height) / 2;
                float pivot = y + height / 2f;

                context.push(() -> {
                    context.translate(curX + width/2f, pivot, depth.getZ());

                    context.multiply(Axis.ZP, -30);

                    context.multiply(Axis.ZP, 90);

                    int newX = -height / 2;
                    int newY = -width / 2;

                    int fadeDims = width / 3;
                    int centerDims = width /3;

                    // upper
                    context.graphics().fillGradient(newX, newY, newX + height, newY + fadeDims, 0x00FFFFFF, 0xFFFFFFFF);

                    // center
                    context.graphics().fill(newX, newY + fadeDims, newX + height, newY + fadeDims + centerDims, 0xFFFFFFFF);

                    // lower
                    context.graphics().fillGradient(newX, newY + fadeDims + centerDims, newX + height, newY + width, 0xFFFFFFFF, 0x00FFFFFF);
                });

            }

            context.graphics().disableScissor();
        });

    }

}