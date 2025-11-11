package dev.xylonity.tooltipoverhaul.client.style.icon.background;

import com.mojang.math.Axis;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.layer.impl.IconBackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.render.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

public class VoidIconBackground implements IconBackgroundLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {

        int positionX = (int) context.getTooltipPosition().x - 1 + context.getPaddingX();
        int positionY = (int) context.getTooltipPosition().y + context.getPaddingY();

        int slotSizeX = positionX + Constants.ICON_SIZE / 2;
        int slotSizeY = positionY + Constants.ICON_SIZE / 2;

        ResourceLocation texture = TooltipOverhaul.pathOf("textures/gui/star.png");

        context.translate(slotSizeX, slotSizeY, 0);
        context.multiply(Axis.ZP, -TooltipRenderer.COUNTER * 45);
        context.scale(0.4225f, 0.4225f, 0.4225f);

        int frames = 8;
        int currentFrame = (int) ((TooltipRenderer.COUNTER / 0.2f) % frames);

        int dim = 64;
        context.getGraphics().blit(texture, -32, -32, 0, currentFrame * dim, dim, dim, dim, frames * dim);
    }

}