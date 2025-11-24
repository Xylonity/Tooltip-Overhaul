package dev.xylonity.tooltipoverhaul.client.style.preview.renderer;

import com.mojang.math.Axis;
import dev.xylonity.tooltipoverhaul.client.layer.impl.IconLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextAxis;
import dev.xylonity.tooltipoverhaul.compat.modernfix.ModernFixCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec2;

public class DefaultPreviewStackRenderer implements IconLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {

        int sizeX = RenderUtils.calculateSecondPanelSize(context, TextAxis.X);
        int sizeY = RenderUtils.calculateSecondPanelSize(context, TextAxis.Y);
        int x0 = (int) (position.x - 22);
        int y0 = (int) (position.y - 2);
        int x1 = (int) (position.x - 26 - sizeX);
        int y1 = (int) (position.y + sizeY);

        context.translate((x1 + sizeX / 2f + 2), (y0 + sizeY / 2f) + 2, 0);

        context.multiply(Axis.YP, ((System.currentTimeMillis() - context.getStartTime()) / 20f) % 360);

        context.multiply(Axis.ZP, -45);

        float scale = Math.min(sizeX / 5f, sizeY / 5f) / 2.5f;

        context.scale(scale, scale, scale);

        Minecraft minecraft = Minecraft.getInstance();
        if (ModernFixCompat.SHOULD_RETURN_ORIGINAL_RENDER) {
            ModernFixCompat.push();
            try {
                RenderUtils.renderItem(context, minecraft.player, minecraft.level, context.getStack(), 0);
            }
            finally {
                ModernFixCompat.pop();
            }
        }
        else {
            RenderUtils.renderItem(context, minecraft.player, minecraft.level, context.getStack(), 0);
        }

    }

}
