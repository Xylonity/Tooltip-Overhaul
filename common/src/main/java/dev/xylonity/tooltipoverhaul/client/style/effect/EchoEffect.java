package dev.xylonity.tooltipoverhaul.client.style.effect;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.xylonity.tooltipoverhaul.client.layer.impl.EffectLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

public class EchoEffect implements EffectLayer {

    private static final int PULSE_COLOR_INNER = 0x88B3E5FF;
    private static final int PULSE_COLOR_OUTER = 0x2264B5FF;
    private static final int CORE_GLOW_COLOR = 0x3040A0FF;
    private static final int CORE_CENTER_COLOR = 0x60FFFFFF;

    private static final int SEGMENTS = 96;
    private static final int PULSE_COUNT = 3;

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
        float maxRadius = (float) Math.hypot(tooltipWidth, tooltipHeight) * 0.95f;

        context.push(() -> {
            context.enableScissor(
                    positionX - context.getPaddingX() - 1,
                    positionY - context.getPaddingY(),
                    positionX + tooltipWidth + context.getPaddingX(),
                    positionY + tooltipHeight + context.getPaddingY()
            );

            //context.translate(0, 0, context.getLayerDepth().getZ());

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
            Tesselator tess = Tesselator.getInstance();
            BufferBuilder buf = tess.getBuilder();

            // Inner
            renderCore(buf, tess, pose, centerX, centerY, maxRadius, time);

            // Rings
            renderPulses(buf, tess, pose, centerX, centerY, maxRadius, time);

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

    private void renderCore(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time) {

        float radius = maxRadius * (0.28f + 0.05f * (float) Math.sin(time * Math.PI * 2.0f));
        int segments = 64;

        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        int centerA = AnimationUtils.clamp255((int) (ColorUtils.alpha(CORE_CENTER_COLOR) * 0.95f));
        bufferBuilder.vertex(pose, centerX, centerY, 0)
                .color(ColorUtils.red(CORE_CENTER_COLOR), ColorUtils.green(CORE_CENTER_COLOR), ColorUtils.blue(CORE_CENTER_COLOR), centerA)
                .endVertex();

        for (int i = 0; i <= segments; i++) {
            float ang = i / (float) segments * (float) Math.PI * 2.0f;

            float wobble = 0.12f * (float) Math.sin(ang * 3.0f + time * Math.PI * 3.0f);
            float r = radius * (1.0f + wobble);

            float x = centerX + (float) Math.cos(ang) * r;
            float y = centerY + (float) Math.sin(ang) * r;

            int edgeA = AnimationUtils.clamp255((int) (ColorUtils.alpha(CORE_GLOW_COLOR) * 0.0f));
            bufferBuilder.vertex(pose, x, y, 0)
                    .color(ColorUtils.red(CORE_GLOW_COLOR), ColorUtils.green(CORE_GLOW_COLOR), ColorUtils.blue(CORE_GLOW_COLOR), edgeA)
                    .endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private void renderPulses(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time) {
        for (int i = 0; i < PULSE_COUNT; i++) {
            float offset = i / (float) PULSE_COUNT;

            float localT = (time + offset) % 1f;
            float envelope = (float) Math.sin(localT * Math.PI);
            if (envelope <= 0.001f) {
                continue;
            }

            float radiusInner = maxRadius * (0.18f + 0.62f * localT);
            float radiusOuter = radiusInner + maxRadius * 0.18f * (0.7f + 0.3f * localT);

            float alphaFactor = (float) Math.pow(envelope, 1.2f);

            int innerA = AnimationUtils.clamp255((int) (ColorUtils.alpha(PULSE_COLOR_INNER) * alphaFactor * 0.95f));
            int outerA = AnimationUtils.clamp255((int) (ColorUtils.alpha(PULSE_COLOR_OUTER) * alphaFactor * 0.55f));

            bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int seg = 0; seg <= SEGMENTS; seg++) {
                float s = seg / (float) SEGMENTS;
                float angle = s * (float) (Math.PI * 2.0);

                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                float wave1 = 0.06f * (float) Math.sin(angle * 3.0f + time * Math.PI * 4.0f + i);
                float wave2 = 0.04f * (float) Math.sin(angle * 7.0f - time * Math.PI * 3.0f + i * 1.7f);
                float wave = (wave1 + wave2) * envelope;

                float rIn = radiusInner * (1.0f + wave * 0.5f);
                float rOut = radiusOuter * (1.0f + wave);

                float ix = centerX + cos * rIn;
                float iy = centerY + sin * rIn;
                float ox = centerX + cos * rOut;
                float oy = centerY + sin * rOut;

                float cardinal = (float) (Math.pow(Math.abs(cos), 3.0) + Math.pow(Math.abs(sin), 3.0)) * 0.5f;
                float glow = 0.55f + 0.45f * cardinal * (0.7f + 0.3f * envelope);

                int ringInnerA = AnimationUtils.clamp255((int) (innerA * glow));
                int ringOuterA = AnimationUtils.clamp255((int) (outerA * glow));

                bufferBuilder.vertex(pose, ox, oy, 0)
                        .color(ColorUtils.red(PULSE_COLOR_OUTER), ColorUtils.green(PULSE_COLOR_OUTER), ColorUtils.blue(PULSE_COLOR_OUTER), ringOuterA)
                        .endVertex();

                bufferBuilder.vertex(pose, ix, iy, 0)
                        .color(ColorUtils.red(PULSE_COLOR_INNER), ColorUtils.green(PULSE_COLOR_INNER), ColorUtils.blue(PULSE_COLOR_INNER), ringInnerA)
                        .endVertex();
            }

            BufferUploader.drawWithShader(bufferBuilder.end());
        }

    }

}
