package dev.xylonity.tooltipoverhaul.client.style.background.icon;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipPreviewBackground;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class FocusIconBackground implements ITooltipPreviewBackground {
    @Override public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size){
        int x0 = (int) pos.x + 3;
        int y0 = (int) pos.y + 3;
        int x1 = x0 + 22;
        int y1 = y0 + 22;

        GlStateManager._enableBlend();
        GlStateManager._blendFuncSeparate(770, 1, 1, 771);

        ctx.graphics().fill(x0, y0, x1, y1, depth.getZ(), 0x8018181C);

        int col = (((int) (40 + 30 * Math.sin(TooltipRenderer.ELAPSED * 2.5))) << 24) | 0xFFFFFF;

        // Top left
        ctx.graphics().fill(x0, y0 - 1, x0 + 5, y0, depth.getZ(), col);
        ctx.graphics().fill(x0 - 1, y0 - 1, x0, y0 + 5, depth.getZ(), col);
        // Bottom left
        ctx.graphics().fill(x0, y1 + 1, x0 + 5, y1, depth.getZ(), col);
        ctx.graphics().fill(x0 - 1, y1 - 5, x0, y1 + 1, depth.getZ(), col);
        // Top right
        ctx.graphics().fill(x1 - 5, y0 - 1, x1 + 1, y0, depth.getZ(), col);
        ctx.graphics().fill(x1 + 1, y0, x1, y0 + 5, depth.getZ(), col);
        // Bottom right
        ctx.graphics().fill(x1 - 5, y1, x1 + 1, y1 + 1, depth.getZ(), col);
        ctx.graphics().fill(x1 + 1, y1 - 5, x1, y1, depth.getZ(), col);

        GlStateManager._disableBlend();
    }


}