package dev.xylonity.tooltipoverhaul.client.style.divider;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.xylonity.tooltipoverhaul.client.layer.impl.DividerLineLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

public class GradientDividerLine implements DividerLineLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {

        boolean hasIcon = context.hasIcon();
        boolean hasRating = RenderUtils.hasRating(context);

        int y = (int) context.getTooltipPosition().y + context.getPaddingY();
        int x = (int) ((int) context.getTooltipPosition().x + context.getTooltipSize().x * 0.1f);
        int width = (int) ((int) context.getTooltipSize().x - context.getTooltipSize().x * 0.2f);

        if (hasIcon) {
            y += Constants.ICON_SIZE + Constants.SEPARATION_TITLE_ICON;
        }
        else {
            int extraY = context.getComponents().get(0).getHeight();
            if (hasRating) {
                y += extraY * 2 + context.getPaddingY();
            }
            else {
                y += extraY + context.getPaddingY();
            }

        }

        PoseStack pose = context.getGraphics().pose();
        Matrix4f matrix = pose.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buf = tesselator.getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int baseColor = ColorUtils.getDividerLineColor(context);
        int r = (baseColor >>> 16) & 0xFF;
        int g = (baseColor >>> 8) & 0xFF;
        int b = baseColor & 0xFF;

        int halfWidth = width / 2;
        int centerX = x + halfWidth;

        buf.vertex(matrix, x, y + 1, 0).color(r, g, b, 0).endVertex();
        buf.vertex(matrix, centerX, y + 1, 0).color(r, g, b, 255).endVertex();
        buf.vertex(matrix, centerX, y, 0).color(r, g, b, 255).endVertex();
        buf.vertex(matrix, x, y, 0).color(r, g, b, 0).endVertex();

        buf.vertex(matrix, centerX, y + 1, 0).color(r, g, b, 255).endVertex();
        buf.vertex(matrix, x + width, y + 1, 0).color(r, g, b, 0).endVertex();
        buf.vertex(matrix, x + width, y, 0).color(r, g, b, 0).endVertex();
        buf.vertex(matrix, centerX, y, 0).color(r, g, b, 255).endVertex();

        BufferUploader.drawWithShader(buf.end());
    }
}