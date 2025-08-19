package dev.xylonity.tooltipoverhaul.client.style.effect;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipEffect;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

import java.awt.*;

public class RimLightEffect implements ITooltipEffect {

    private final int color1;
    private final int color2;

    public RimLightEffect(int color1, int color2) {
        this.color1 = color1;
        this.color2 = color2;
    }

    @Override
    public void render(LayerDepth depth, TooltipContext context, Vec2 pos, Point size) {
        Point size2 = new Point(size.x + 8, size.y + 8);
        Vec2 start = pos.add(-4.0F);
        float time = context.time();
        float width = 8.0F + 3.0F * (float) Math.cos(time * 1.1F);

        Matrix4f matrix = context.pose().last().pose();
        VertexConsumer buf = context.buffer().getBuffer(RenderType.guiOverlay());

        draw(matrix, buf, start, size2, width, color1, color2);

        draw(matrix, buf, start.add(-1.0F), new Point(size2.x + 2, size2.y + 2), width * 1.6f, mulAlpha(color1, 0.35f), color2);

        for (int edge = 0; edge < 4; edge++) {
            float k = (float) Math.max(0.0, Math.sin(time * 3.2f + edge * 1.57));
            if (k < 0.05f) continue;

            drawEdge(matrix, buf, start, size2, width * 0.6f, mulAlpha(color1, 0.50f + 0.35f * k), color2, edge);
        }

    }

    private static int mulAlpha(int color, float scale) {
        int alpha = (color >>> 24) & 0xFF;
        return (color & 0x00FFFFFF) | ((Math.max(0, Math.min(255, Math.round(alpha * scale)))) << 24);
    }

    private static void draw(Matrix4f m, VertexConsumer buf, Vec2 start, Point size, float w, int color1, int color2) {
        // left
        buf.addVertex(m, start.x, start.y, 0.0F).setColor(color1);
        buf.addVertex(m, start.x, start.y + size.y, 0.0F).setColor(color1);
        buf.addVertex(m, start.x + w, start.y + size.y - w, 0.0F).setColor(color2);
        buf.addVertex(m, start.x + w, start.y + w, 0.0F).setColor(color2);
        // Top
        buf.addVertex(m, start.x, start.y, 0.0F).setColor(color1);
        buf.addVertex(m, start.x + w, start.y + w, 0.0F).setColor(color2);
        buf.addVertex(m, start.x + size.x - w, start.y + w, 0.0F).setColor(color2);
        buf.addVertex(m, start.x + size.x, start.y, 0.0F).setColor(color1);
        // Right
        buf.addVertex(m, start.x + size.x - w, start.y + w, 0.0F).setColor(color2);
        buf.addVertex(m, start.x + size.x - w, start.y + size.y - w, 0.0F).setColor(color2);
        buf.addVertex(m, start.x + size.x, start.y + size.y, 0.0F).setColor(color1);
        buf.addVertex(m, start.x + size.x, start.y, 0.0F).setColor(color1);
        // Bottom
        buf.addVertex(m, start.x + w, start.y + size.y - w, 0.0F).setColor(color2);
        buf.addVertex(m, start.x, start.y + size.y, 0.0F).setColor(color1);
        buf.addVertex(m, start.x + size.x, start.y + size.y, 0.0F).setColor(color1);
        buf.addVertex(m, start.x + size.x - w, start.y + size.y - w, 0.0F).setColor(color2);
    }

    private static void drawEdge(Matrix4f m, VertexConsumer buf, Vec2 start, Point size, float w, int color1, int color2, int edge) {
        switch (edge) {
            case 0 -> { // Left
                buf.addVertex(m, start.x, start.y, 0.0F).setColor(color1);
                buf.addVertex(m, start.x, start.y + size.y, 0.0F).setColor(color1);
                buf.addVertex(m, start.x + w, start.y + size.y - w, 0.0F).setColor(color2);
                buf.addVertex(m, start.x + w, start.y + w, 0.0F).setColor(color2);
            }
            case 1 -> { // Top
                buf.addVertex(m, start.x, start.y, 0.0F).setColor(color1);
                buf.addVertex(m, start.x + w, start.y + w, 0.0F).setColor(color2);
                buf.addVertex(m, start.x + size.x - w, start.y + w, 0.0F).setColor(color2);
                buf.addVertex(m, start.x + size.x, start.y, 0.0F).setColor(color1);
            }
            case 2 -> { // Right
                buf.addVertex(m, start.x + size.x - w, start.y + w, 0.0F).setColor(color2);
                buf.addVertex(m, start.x + size.x - w, start.y + size.y - w, 0.0F).setColor(color2);
                buf.addVertex(m, start.x + size.x, start.y + size.y, 0.0F).setColor(color1);
                buf.addVertex(m, start.x + size.x, start.y, 0.0F).setColor(color1);
            }
            case 3 -> { // Bottom
                buf.addVertex(m, start.x + w, start.y + size.y - w, 0.0F).setColor(color2);
                buf.addVertex(m, start.x, start.y + size.y, 0.0F).setColor(color1);
                buf.addVertex(m, start.x + size.x, start.y + size.y, 0.0F).setColor(color1);
                buf.addVertex(m, start.x + size.x - w, start.y + size.y - w, 0.0F).setColor(color2);
            }
        }

    }

}
