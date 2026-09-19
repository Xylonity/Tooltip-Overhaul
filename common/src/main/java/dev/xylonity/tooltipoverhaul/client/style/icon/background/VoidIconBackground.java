package dev.xylonity.tooltipoverhaul.client.style.icon.background;

import com.mojang.math.Axis;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.layer.impl.IconBackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.render.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipLayout;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

public class VoidIconBackground implements IconBackgroundLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {

        final int positionX = (int) context.getTooltipPosition().x + TooltipLayout.iconX(context);
        final int positionY = (int) context.getTooltipPosition().y + TooltipLayout.iconY(context);

        final int slotSizeX = positionX + Constants.getIconSize(context) / 2;
        final int slotSizeY = positionY + Constants.getIconSize(context) / 2;

        final ResourceLocation texture = TooltipOverhaul.pathOf("textures/gui/star.png");

        context.translate(slotSizeX, slotSizeY, 0);
        context.multiply(Axis.ZP, -TooltipRenderer.COUNTER * 45);
        final float scale = 0.4225f * Constants.getIconSize(context) / 22f;
        context.scale(scale, scale, scale);

        final int frames = 8;
        final int currentFrame = (int) ((TooltipRenderer.COUNTER / 0.2f) % frames);

        final int dim = 64;
        final int tint = ColorUtils.getIconBackgroundColor(context, 0xFFFFFFFF);

        context.flush();

        final float[] saved = RenderSystem.getShaderColor().clone();
        RenderSystem.setShaderColor(saved[0] * ColorUtils.red(tint) / 255f, saved[1] * ColorUtils.green(tint) / 255f,
                saved[2] * ColorUtils.blue(tint) / 255f, saved[3] * ColorUtils.alpha(tint) / 255f);
        try {
            context.getGraphics().blit(texture, -32, -32, 0, currentFrame * dim, dim, dim, dim, frames * dim);
            context.flush();
        }
        finally {
            RenderSystem.setShaderColor(saved[0], saved[1], saved[2], saved[3]);
        }

    }

}