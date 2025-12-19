package dev.xylonity.tooltipoverhaul.client.style.effect;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xylonity.tooltipoverhaul.client.layer.impl.EffectLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

public class RimLightEffect implements EffectLayer {

    private final int color1;
    private final int color2;

    public RimLightEffect(int color1, int color2) {
        this.color1 = color1;
        this.color2 = color2;
    }

    @Override
    public void render(TooltipContext context, Vec2 position) {
        Vec2 tooltipPos = context.getTooltipPosition();
        Vec2 tooltipSize = context.getTooltipSize();

        if (tooltipPos == null || tooltipSize == null) {
            return;
        }

        float time = context.getStartTime();
        float width = 8.0F + 3.0F * (float) Math.cos(time * 1.1F);

        Matrix4f matrix = context.getPose().last().pose();
        VertexConsumer buf = context.getBuffer().getBuffer(RenderType.guiOverlay());

        float basePadding = 4.0F;

        draw(matrix, buf, tooltipPos, tooltipSize, width, color1, color2, basePadding);

        draw(matrix, buf, tooltipPos, tooltipSize, width * 1.6F, ColorUtils.mulAlpha(color1, 0.35F), color2, basePadding + 1.0F);

        for (int edge = 0; edge < 4; edge++) {
            float k = (float) Math.max(0.0, Math.sin(time * 3.2F + edge * 1.57F));
            if (k < 0.05F) {
                continue;
            }

            drawEdge(matrix, buf, tooltipPos, tooltipSize, width * 0.6F, ColorUtils.mulAlpha(color1, 0.50F + 0.35F * k), color2, edge, basePadding);
        }

    }

    private static void draw(Matrix4f matrix4f, VertexConsumer vertexConsumer, Vec2 tooltipPos, Vec2 tooltipSize, float width, int color1, int color2, float padding) {

        float startX = tooltipPos.x - padding;
        float startY = tooltipPos.y - padding;
        float totalWidth = tooltipSize.x + padding * 2.0F;
        float totalHeight = tooltipSize.y + padding * 2.0F;

        float x0 = startX;
        float y0 = startY;
        float x1 = startX + width;
        float y1 = startY + width;
        float x2 = startX + totalWidth - width;
        float y2 = startY + totalHeight - width;
        float x3 = startX + totalWidth;
        float y3 = startY + totalHeight;

        // Left
        vertexConsumer.addVertex(matrix4f, x0, y0, 0f).setColor(color1);
        vertexConsumer.addVertex(matrix4f, x0, y3, 0f).setColor(color1);
        vertexConsumer.addVertex(matrix4f, x1, y2, 0f).setColor(color2);
        vertexConsumer.addVertex(matrix4f, x1, y1, 0f).setColor(color2);

        // Top
        vertexConsumer.addVertex(matrix4f, x0, y0, 0f).setColor(color1);
        vertexConsumer.addVertex(matrix4f, x1, y1, 0f).setColor(color2);
        vertexConsumer.addVertex(matrix4f, x2, y1, 0f).setColor(color2);
        vertexConsumer.addVertex(matrix4f, x3, y0, 0f).setColor(color1);

        // Right
        vertexConsumer.addVertex(matrix4f, x2, y1, 0f).setColor(color2);
        vertexConsumer.addVertex(matrix4f, x2, y2, 0f).setColor(color2);
        vertexConsumer.addVertex(matrix4f, x3, y3, 0f).setColor(color1);
        vertexConsumer.addVertex(matrix4f, x3, y0, 0f).setColor(color1);

        // Bottom
        vertexConsumer.addVertex(matrix4f, x1, y2, 0f).setColor(color2);
        vertexConsumer.addVertex(matrix4f, x0, y3, 0f).setColor(color1);
        vertexConsumer.addVertex(matrix4f, x3, y3, 0f).setColor(color1);
        vertexConsumer.addVertex(matrix4f, x2, y2, 0f).setColor(color2);
    }

    private static void drawEdge(Matrix4f matrix4f, VertexConsumer vertexConsumer, Vec2 tooltipPos, Vec2 tooltipSize, float width, int color1, int color2, int edge, float padding) {

        float startX = tooltipPos.x - padding;
        float startY = tooltipPos.y - padding;
        float totalWidth = tooltipSize.x + padding * 2.0F;
        float totalHeight = tooltipSize.y + padding * 2.0F;

        float x0 = startX;
        float y0 = startY;
        float x1 = startX + width;
        float y1 = startY + width;
        float x2 = startX + totalWidth - width;
        float y2 = startY + totalHeight - width;
        float x3 = startX + totalWidth;
        float y3 = startY + totalHeight;

        switch (edge) {
            case 0 -> { // Left
                vertexConsumer.addVertex(matrix4f, x0, y0, 0f).setColor(color1);
                vertexConsumer.addVertex(matrix4f, x0, y3, 0f).setColor(color1);
                vertexConsumer.addVertex(matrix4f, x1, y2, 0f).setColor(color2);
                vertexConsumer.addVertex(matrix4f, x1, y1, 0f).setColor(color2);
            }
            case 1 -> { // Top
                vertexConsumer.addVertex(matrix4f, x0, y0, 0f).setColor(color1);
                vertexConsumer.addVertex(matrix4f, x1, y1, 0f).setColor(color2);
                vertexConsumer.addVertex(matrix4f, x2, y1, 0f).setColor(color2);
                vertexConsumer.addVertex(matrix4f, x3, y0, 0f).setColor(color1);
            }
            case 2 -> { // Right
                vertexConsumer.addVertex(matrix4f, x2, y1, 0f).setColor(color2);
                vertexConsumer.addVertex(matrix4f, x2, y2, 0f).setColor(color2);
                vertexConsumer.addVertex(matrix4f, x3, y3, 0f).setColor(color1);
                vertexConsumer.addVertex(matrix4f, x3, y0, 0f).setColor(color1);
            }
            case 3 -> { // Bottom
                vertexConsumer.addVertex(matrix4f, x1, y2, 0f).setColor(color2);
                vertexConsumer.addVertex(matrix4f, x0, y3, 0f).setColor(color1);
                vertexConsumer.addVertex(matrix4f, x3, y3, 0f).setColor(color1);
                vertexConsumer.addVertex(matrix4f, x2, y2, 0f).setColor(color2);
            }

        }

    }

}
