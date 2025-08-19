package dev.xylonity.tooltipoverhaul.client.style.background.preview;

import com.mojang.math.Axis;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipPreviewBackground;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class DefaultBackgroundPreview implements ITooltipPreviewBackground {

    private static final ResourceLocation PREVIEW = ResourceLocation.fromNamespaceAndPath(TooltipOverhaul.MOD_ID, "textures/gui/star.png");

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        ctx.translate(pos.x + TooltipRenderer.PADDING_X + 12, pos.y + TooltipRenderer.PADDING_Y + 12, LayerDepth.BACKGROUND_TEXT.getZ());
        ctx.multiply(Axis.ZP, -TooltipRenderer.ELAPSED * 45f);

        ctx.scale(0.4225f, 0.4225f, 0.4225f);

        int frames = 8;
        int currentFrame = (int) ((TooltipRenderer.ELAPSED / 0.2f) % frames);

        int dim = 64;
        ctx.graphics().blit(PREVIEW, -32, -32, 0, currentFrame * dim, dim, dim, dim, frames * dim);
    }

}