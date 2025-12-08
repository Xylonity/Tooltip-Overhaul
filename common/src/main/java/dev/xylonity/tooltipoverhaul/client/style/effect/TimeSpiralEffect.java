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

public class TimeSpiralEffect implements EffectLayer {

    private static final int HALO_COLOR_CORE = 0x35A87CFF;
    private static final int HALO_COLOR_EDGE = 0xFFA87CFF;

    private static final int RING_COLOR = 0x90A87CFF;
    private static final int RING_GLOW_COLOR = 0x40A87CFF;

    private static final int SPIRAL_COLOR_NEAR = 0xA0C8EFFF;
    private static final int SPIRAL_COLOR_FAR = 0x30A0E0FF;

    private static final int ARM_COUNT = 4;
    private static final int SEGMENTS = 96;

    @Override
    public void render(TooltipContext context, Vec2 position) {
        int positionX = (int) position.x;
        int positionY = (int) position.y;
        int tooltipWidth = (int) context.getTooltipSize().x;
        int tooltipHeight = (int) context.getTooltipSize().y;

        long now = System.currentTimeMillis();
        float time = (now % context.getStartTime()) / 5000f;
        float globalAngle = time * (float) (Math.PI * 2.0);

        float centerX = positionX + tooltipWidth * 0.5f;
        float centerY = positionY + tooltipHeight * 0.5f;

        float maxRadius = (float) (Math.hypot(tooltipWidth, tooltipHeight) * 0.75f);
        float baseThickness = Math.max(3.5f, maxRadius * 0.08f);

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
            Tesselator tess = Tesselator.getInstance();
            BufferBuilder buf = tess.getBuilder();

            renderRadialHalo(buf, tess, pose, centerX, centerY, maxRadius, tooltipWidth, tooltipHeight, time);

            renderOrbitRings(buf, tess, pose, centerX, centerY, maxRadius, time, globalAngle);

            renderSpiralArms(buf, tess, pose, centerX, centerY, maxRadius, baseThickness, time, globalAngle);

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

    private void renderRadialHalo(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, int width, int height, float time) {

        float aspect = (float) height / (float) width;
        float pulse = 1.13f + 0.06f * (float) Math.sin(time * Math.PI * 2.0f * 1.6f);
        float outerRadius = maxRadius * 0.95f * pulse;
        float innerRadius = outerRadius * 0.65f;
        float centerRadius = innerRadius * 0.3f;

        int coreRed = ColorUtils.red(HALO_COLOR_CORE);
        int coreGreen = ColorUtils.green(HALO_COLOR_CORE);
        int coreBlue = ColorUtils.blue(HALO_COLOR_CORE);
        int coreAlpha = ColorUtils.alpha(HALO_COLOR_CORE);

        int edgeRed = ColorUtils.red(HALO_COLOR_EDGE);
        int edgeGreen = ColorUtils.green(HALO_COLOR_EDGE);
        int edgeBlue = ColorUtils.blue(HALO_COLOR_EDGE);

        // External ring
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= SEGMENTS; i++) {
            float s = i / (float) SEGMENTS;
            float angle = s * (float) (Math.PI * 2.0);

            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float sx = cos;
            float sy = sin * aspect;

            float ix = centerX + sx * innerRadius;
            float iy = centerY + sy * innerRadius;
            float ox = centerX + sx * outerRadius;
            float oy = centerY + sy * outerRadius;

            float ringT = 1.0f - (float) Math.abs(Math.sin(angle * 0.5f));
            float alphaFactorInner = 0.75f + 0.25f * ringT;

            int innerA = AnimationUtils.clamp255((int) (coreAlpha * alphaFactorInner));

            bufferBuilder.vertex(pose, ox, oy, 0)
                    .color(edgeRed, edgeGreen, edgeBlue, 180)
                    .endVertex();
            bufferBuilder.vertex(pose, ix, iy, 0)
                    .color(coreRed, coreGreen, coreBlue, innerA)
                    .endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());

        // Internal ring
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= SEGMENTS; i++) {
            float s = i / (float) SEGMENTS;
            float angle = s * (float) (Math.PI * 2.0);

            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float sx = cos;
            float sy = sin * aspect;

            float cx_pos = centerX + sx * centerRadius;
            float cy_pos = centerY + sy * centerRadius;
            float ix = centerX + sx * innerRadius;
            float iy = centerY + sy * innerRadius;

            float ringT = 1.0f - (float) Math.abs(Math.sin(angle * 0.5f));
            float alphaFactorInner = 0.75f + 0.25f * ringT;

            int innerA = AnimationUtils.clamp255((int) (coreAlpha * alphaFactorInner));

            bufferBuilder.vertex(pose, ix, iy, 0)
                    .color(coreRed, coreGreen, coreBlue, innerA)
                    .endVertex();
            bufferBuilder.vertex(pose, cx_pos, cy_pos, 0)
                    .color(coreRed, coreGreen, coreBlue, 0)
                    .endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private void renderOrbitRings(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time, float globalAngle) {

        float baseRadius = maxRadius * 0.55f;

        int ringRed = ColorUtils.red(RING_COLOR);
        int ringGreen = ColorUtils.green(RING_COLOR);
        int ringBlue = ColorUtils.blue(RING_COLOR);
        int ringAlpha = ColorUtils.alpha(RING_COLOR);

        int glowRed = ColorUtils.red(RING_GLOW_COLOR);
        int glowGreen = ColorUtils.green(RING_GLOW_COLOR);
        int glowBlue = ColorUtils.blue(RING_GLOW_COLOR);
        int glowAlpha = ColorUtils.alpha(RING_GLOW_COLOR);

        for (int ringIndex = 0; ringIndex < 2; ringIndex++) {
            float offset = (ringIndex == 0 ? -1f : 1f);
            float radius = baseRadius * (1.0f + 0.12f * offset);
            float ringPhase = globalAngle * (ringIndex == 0 ? 1.1f : -0.9f);

            float thickness = maxRadius * 0.06f;

            bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i <= SEGMENTS; i++) {
                float s = i / (float) SEGMENTS;

                float angle = s * (float) (Math.PI * 2.0) + ringPhase;
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                float pulse = 0.6f + 0.4f * (float) Math.sin((s * 3.0f + time * 4.0f + ringIndex * 1.7f) * (float) Math.PI * 2.0);
                float localThickness = thickness * pulse;

                float innerRadius = radius - localThickness * 0.5f;
                float outerRadius = radius + localThickness * 0.5f;

                float ix = centerX + cos * innerRadius;
                float iy = centerY + sin * innerRadius;
                float ox = centerX + cos * outerRadius;
                float oy = centerY + sin * outerRadius;

                float glowMask = 0.5f + 0.5f * (float) Math.cos((s + time) * (float) Math.PI * 4.0);
                float alphaCore = 0.65f * glowMask;
                float alphaGlow = 0.3f * glowMask;

                int coreAlpha = AnimationUtils.clamp255((int) (ringAlpha * alphaCore));
                int haloAlpha = AnimationUtils.clamp255((int) (glowAlpha * alphaGlow));

                bufferBuilder.vertex(pose, ox, oy, 0)
                        .color(glowRed, glowGreen, glowBlue, haloAlpha)
                        .endVertex();

                bufferBuilder.vertex(pose, ix, iy, 0)
                        .color(ringRed, ringGreen, ringBlue, coreAlpha)
                        .endVertex();
            }

            BufferUploader.drawWithShader(bufferBuilder.end());
        }

    }

    private void renderSpiralArms(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float baseThickness, float time, float globalAngle) {

        for (int arm = 0; arm < ARM_COUNT; arm++) {
            float armOffset = (float) (2.0 * Math.PI / ARM_COUNT) * arm;
            float startAngle = globalAngle + armOffset;
            float endAngle = startAngle + (float) (Math.PI * 1.9);

            bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i <= SEGMENTS; i++) {
                float s = i / (float) SEGMENTS;

                float eased = AnimationUtils.smoothstep(0f, 1f, s);
                float spiralT = eased;

                float radius = (float) Math.pow(spiralT, 0.9f) * maxRadius;

                float thicknessPulse = 0.65f + 0.35f * (float) Math.sin((time + arm * 0.33f + s * 1.4f) * Math.PI * 2.0);
                float thickness = baseThickness * thicknessPulse * (1.05f - spiralT * 0.75f);

                float wobble = 0.12f * (float) Math.sin(s * 10.0f + arm * 2.5f + time * Math.PI * 4.0);
                float angle = AnimationUtils.lerp(startAngle, endAngle, s) + wobble;

                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                float innerRadius = radius - thickness * 0.5f;
                float outerRadius = radius + thickness * 0.5f;

                float innerX = centerX + cos * innerRadius;
                float innerY = centerY + sin * innerRadius;
                float outerX = centerX + cos * outerRadius;
                float outerY = centerY + sin * outerRadius;

                int segmentColor = ColorUtils.lerpColor(SPIRAL_COLOR_NEAR, SPIRAL_COLOR_FAR, spiralT);

                int segR = ColorUtils.red(segmentColor);
                int segG = ColorUtils.green(segmentColor);
                int segB = ColorUtils.blue(segmentColor);
                int segA = ColorUtils.alpha(segmentColor);

                float headGlow = (float) Math.pow(1.0f - spiralT, 1.6f);
                float sparkle = 0.6f + 0.4f * (float) Math.sin((time * 1.7f + s * 1.3f + arm * 0.5f) * (float) Math.PI * 2.0);

                float alphaFactor = AnimationUtils.clamp01(headGlow * (0.55f + 0.45f * sparkle));

                float edgeFade = AnimationUtils.clamp01((spiralT - 0.05f) / 0.9f);
                alphaFactor *= edgeFade;

                int innerAlpha = AnimationUtils.clamp255((int) (segA * alphaFactor));
                int outerAlpha = AnimationUtils.clamp255((int) (segA * alphaFactor * 0.7f));

                bufferBuilder.vertex(pose, outerX, outerY, 0)
                        .color(segR, segG, segB, outerAlpha)
                        .endVertex();
                bufferBuilder.vertex(pose, innerX, innerY, 0)
                        .color(segR, segG, segB, innerAlpha)
                        .endVertex();
            }

            BufferUploader.drawWithShader(bufferBuilder.end());
        }

    }

}
