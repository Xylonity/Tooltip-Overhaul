package dev.xylonity.tooltipoverhaul.client.style.divider;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.xylonity.tooltipoverhaul.client.layer.impl.DividerLineLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

public final class GradientOrnamentDividerLine implements DividerLineLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {
        final int width = Math.max(0, (int) context.getTooltipSize().x - context.getPaddingX() * 2);
        final int left = (int) position.x;
        final int right = left + width;
        final int y = (int) position.y;
        final int center = left + width / 2;
        final int color = ColorUtils.getDividerLineColor(context);
        final int red = (color >>> 16) & 0xFF;
        final int green = (color >>> 8) & 0xFF;
        final int blue = color & 0xFF;
        final int alpha = (color >>> 24) & 0xFF;

        if (width < 9) {
            context.getGraphics().fill(left, y, right, y + 1, color);
            return;
        }

        context.flush();

        final Matrix4f matrix = context.getGraphics().pose().last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        final BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // Left
        buffer.addVertex(matrix, left, y + 1, 0).setColor(red, green, blue, 0);
        buffer.addVertex(matrix, center - 3, y + 1, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, center - 3, y, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, left, y, 0).setColor(red, green, blue, 0);

        // Right
        buffer.addVertex(matrix, center + 4, y + 1, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, right, y + 1, 0).setColor(red, green, blue, 0);
        buffer.addVertex(matrix, right, y, 0).setColor(red, green, blue, 0);
        buffer.addVertex(matrix, center + 4, y, 0).setColor(red, green, blue, alpha);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        // +
        context.getGraphics().fill(center - 1, y, center + 2, y + 1, color);
        context.getGraphics().fill(center, y - 1, center + 1, y, color);
        context.getGraphics().fill(center, y + 1, center + 1, y + 2, color);
    }

}
