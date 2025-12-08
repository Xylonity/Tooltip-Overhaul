package dev.xylonity.tooltipoverhaul.client.style.effect;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.xylonity.tooltipoverhaul.client.layer.impl.EffectLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

public class SpeedLinesEffect implements EffectLayer {

    private static final int RAYS_PER_SIDE = 14;

    private static final int WING_COLOR_INNER = 0xC0FFFFFF;
    private static final int WING_COLOR_OUTER = 0x60D0F0FF;

    @Override
    public void render(TooltipContext context, Vec2 position) {
        int positionX = (int) position.x;
        int positionY = (int) position.y;
        int tooltipWidth = (int) context.getTooltipSize().x;
        int tooltipHeight = (int) context.getTooltipSize().y;

        long now = System.currentTimeMillis();
        float time = (now - context.getStartTime()) / 5000f;

        float centerX = positionX + tooltipWidth * 0.5f;
        float centerY = positionY + tooltipHeight * 0.5f;
        float maxRadius = (float) Math.hypot(tooltipWidth, tooltipHeight);

        context.push(() -> {
            context.getGraphics().enableScissor(
                    positionX - context.getPaddingX() - 1,
                    positionY - context.getPaddingY(),
                    positionX + tooltipWidth + context.getPaddingX(),
                    positionY + tooltipHeight + context.getPaddingY()
            );

            context.translate(0, 0, context.getLayerDepth().getZ());

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();

            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            Matrix4f pose = context.getPose().last().pose();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tesselator.getBuilder();

            renderWings(bufferBuilder, tesselator, pose, centerX, centerY, maxRadius, time);

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );

            RenderSystem.disableBlend();
            RenderSystem.enableCull();

            context.getGraphics().disableScissor();
        });

    }

    private void renderWings(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float t) {

        bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        int innerRed = ColorUtils.red(WING_COLOR_INNER);
        int innerGreen = ColorUtils.green(WING_COLOR_INNER);
        int innerBlue = ColorUtils.blue(WING_COLOR_INNER);
        int innerAlpha = ColorUtils.alpha(WING_COLOR_INNER);

        int outerRed = ColorUtils.red(WING_COLOR_OUTER);
        int outerGreen = ColorUtils.green(WING_COLOR_OUTER);
        int outerBlue = ColorUtils.blue(WING_COLOR_OUTER);
        int outerAlpha = ColorUtils.alpha(WING_COLOR_OUTER);

        float rayMin = maxRadius * 0.25f;
        float rayMax = maxRadius * 0.95f;
        float spread = (float) (Math.PI * 0.85);

        for (int side = -1; side <= 1; side += 2) {
            float baseAngle = (side > 0) ? 0f : (float) Math.PI;
            for (int i = 0; i < RAYS_PER_SIDE; i++) {
                float s = i / (float) (RAYS_PER_SIDE - 1);

                float wingOffset = (s - 0.4f) * spread;
                float wobble = 0.10f * (float) Math.sin((t * 2.5f + i * 0.6f) * Math.PI * 2.0);
                float angle = baseAngle + side * (wingOffset + wobble);

                float dirX = (float) Math.cos(angle);
                float dirY = (float) Math.sin(angle);
                float nx = -dirY;
                float ny = dirX;

                float open = 0.75f + 0.25f * (float) Math.sin((t * 1.8f + s * 0.3f) * Math.PI * 2.0);
                float rayLength = rayMin + (rayMax - rayMin) * open;

                float baseRadius = rayMin * (0.4f + s * 0.4f);
                float thickness = maxRadius * 0.03f * (1.1f - s * 0.7f);

                float tipX = centerX + dirX * rayLength;
                float tipY = centerY + dirY * rayLength;

                float rootX = centerX + dirX * baseRadius;
                float rootY = centerY + dirY * baseRadius;

                float tip1X = tipX + nx * thickness;
                float tip1Y = tipY + ny * thickness;
                float tip2X = tipX - nx * thickness;
                float tip2Y = tipY - ny * thickness;

                float head = (float) Math.pow(1.0f - s, 1.4f);
                float flicker = 0.6f + 0.4f * (float) Math.sin((t * 3.0f + i * 0.8f) * Math.PI * 2.0);
                float alphaFactor = head * flicker;

                int rootAlpha = AnimationUtils.clamp255((int) (innerAlpha * alphaFactor * 0.8f));
                int tipAlpha = AnimationUtils.clamp255((int) (outerAlpha * alphaFactor));

                bufferBuilder.vertex(pose, rootX, rootY, 0)
                        .color(innerRed, innerGreen, innerBlue, rootAlpha)
                        .endVertex();
                bufferBuilder.vertex(pose, tip1X, tip1Y, 0)
                        .color(outerRed, outerGreen, outerBlue, tipAlpha)
                        .endVertex();
                bufferBuilder.vertex(pose, tip2X, tip2Y, 0)
                        .color(outerRed, outerGreen, outerBlue, tipAlpha / 2 + rootAlpha / 2)
                        .endVertex();
            }

        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

}
