package dev.xylonity.tooltipoverhaul.client.old.style.divider;

import com.mojang.blaze3d.vertex.*;
import dev.xylonity.tooltipoverhaul.client.old.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.old.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.old.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.old.layer.bridge.ITooltipDividerLine;
import dev.xylonity.tooltipoverhaul.util.Util;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

import net.minecraft.world.phys.Vec2;

import java.awt.*;
import java.util.List;

@SuppressWarnings("unchecked")
public class DefaultDividerLine implements ITooltipDividerLine {

    private static final int MIN_SEGMENTS = 2;
    private static final int MAX_SEGMENTS = 24;

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        if (ctx.getComponents().size() <= 1 || size.x <= 4) {
            return;
        }

        int y = (int) pos.y + 4 + TooltipRenderer.PADDING_Y + ((List<ClientTooltipComponent>) ctx.getComponents()).get(0).getHeight() + 4 + 10;

        int x = (int) ((int) pos.x + size.x * 0.1f);
        int width = (int) (size.x - size.x * 0.2f);
        if (width <= 1) return;

        int segments = clamp(width / 12);

        ctx.push(() -> {
            ctx.translate(0, 0, depth.getZ());

            PoseStack pose = ctx.graphics().pose();
            Matrix4f matrix = pose.last().pose();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buf = tesselator.getBuilder();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            int segmentLength = Math.max(1, width / segments);
            int drawn = 0;
            int px = x;
            float center = width / 2.0f;

            while (drawn < width) {
                int run = Math.min(segmentLength, width - drawn);

                int leftAlpha = alphaFromDistance(Math.abs((drawn) - center) / center, 0.025f);
                int rightAlpha = alphaFromDistance(Math.abs((drawn + run - 1) - center) / center, 0.025f);

                int lineColor = Util.getDividerLineColor(ctx);

                int leftColor = (leftAlpha << 24) | (lineColor & 0x00FFFFFF);
                int rightColor = (rightAlpha << 24) | (lineColor & 0x00FFFFFF);

                int alphaLeft = (leftColor >>> 24) & 0xFF;
                int redLeft = (leftColor >>> 16) & 0xFF;
                int greenLeft = (leftColor >>> 8) & 0xFF;
                int blueLeft = (leftColor) & 0xFF;

                int alphaRight = (rightColor >>> 24) & 0xFF;
                int redRight = (rightColor >>> 16) & 0xFF;
                int greenRight = (rightColor >>> 8) & 0xFF;
                int blueRight = (rightColor) & 0xFF;

                // bottom left
                buf.vertex(matrix, px, y + 1, 0).color(redLeft, greenLeft, blueLeft, alphaLeft).endVertex();
                // bottom right
                buf.vertex(matrix, px + run, y + 1, 0).color(redRight, greenRight, blueRight, alphaRight).endVertex();
                // top right
                buf.vertex(matrix, px + run, y, 0).color(redRight, greenRight, blueRight, alphaRight).endVertex();
                // top left
                buf.vertex(matrix, px, y, 0).color(redLeft, greenLeft, blueLeft, alphaLeft).endVertex();

                px += run;
                drawn += run;
            }

            BufferUploader.drawWithShader(buf.end());
        });

    }

    private static int alphaFromDistance(float distanceNorm, float fade) {
        if (distanceNorm <= fade) {
            return 255;
        }
        else {
            float z = (distanceNorm - fade) / (1.0f - fade);
            return Math.max(0, Math.min(255, (int) (255 * (1.0f - z * z))));
        }

    }

    private static int clamp(int value) {
        return Math.max(MIN_SEGMENTS, Math.min(MAX_SEGMENTS, value));
    }

}
