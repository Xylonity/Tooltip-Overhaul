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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MagicOrbsEffect implements EffectLayer {

    private static final int ORB_COUNT = 5;
    private static final int SEGMENTS = 48;

    private static final int ORB_CORE_COLOR = 0xFFCCFFFF;
    private static final int ORB_GLOW_COLOR = 0xAA88DDFF;
    private static final int TRAIL_COLOR = 0x6666CCFF;
    private static final int BEAM_COLOR = 0x8899EEFF;

    private static final Random RAND = new Random();
    private static final List<OrbData> ORBS = new ArrayList<>();

    static {
        for (int i = 0; i < ORB_COUNT; i++) {
            float phase = (float) i / ORB_COUNT;
            float distance = 0.6f + RAND.nextFloat() * 0.3f;
            float speed = 0.8f + RAND.nextFloat() * 0.4f;

            ORBS.add(new OrbData(phase, distance, speed));
        }

    }

    @Override
    public void render(TooltipContext context, Vec2 position) {
        int positionX = (int) position.x;
        int positionY = (int) position.y;
        int tooltipWidth = (int) context.getTooltipSize().x;
        int tooltipHeigth = (int) context.getTooltipSize().y;

        long now = System.currentTimeMillis();
        float time = (now - context.getStartTime()) / 10000f;

        float centerX = positionX + tooltipWidth * 0.5f;
        float centerY = positionY + tooltipHeigth * 0.5f;
        float maxRadius = (float) Math.hypot(tooltipWidth, tooltipHeigth) * 0.5f;

        context.push(() -> {

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

            renderBeams(buf, tess, pose, centerX, centerY, maxRadius, time);

            renderTrails(buf, tess, pose, centerX, centerY, maxRadius, time);

            renderOrbs(buf, tess, pose, centerX, centerY, maxRadius, time);

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );

            RenderSystem.disableBlend();
            RenderSystem.enableCull();
        });

    }

    private void renderBeams(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time) {

        for (int i = 0; i < ORB_COUNT; i++) {
            OrbData orb1 = ORBS.get(i);
            float[] pos1 = orb1.getPosition(centerX, centerY, maxRadius, time);

            for (int j = i + 1; j < ORB_COUNT; j++) {
                OrbData orb2 = ORBS.get(j);
                float[] pos2 = orb2.getPosition(centerX, centerY, maxRadius, time);

                float dist = (float) Math.hypot(pos2[0] - pos1[0], pos2[1] - pos1[1]);
                float maxBeamDist = maxRadius * 0.8f;

                if (dist < maxBeamDist) {
                    float strength = 1.0f - (dist / maxBeamDist);
                    float pulse = 0.5f + 0.5f * (float) Math.sin(time * Math.PI * 4.0 + i + j);

                    renderBeam(bufferBuilder, tesselator, pose, pos1[0], pos1[1], pos2[0], pos2[1], strength * pulse);
                }

            }

        }

    }

    private void renderBeam(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float x1, float y1, float x2, float y2, float strength) {

        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.hypot(dx, dy);
        if (len < 0.001f) {
            return;
        }

        float nx = -dy / len;
        float ny = dx / len;

        float thickness = 1.5f * strength;

        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        int segments = 12;
        for (int i = 0; i <= segments; i++) {
            float s = i / (float) segments;

            float wave = 0.15f * (float) Math.sin(s * Math.PI * 3.0);
            float localThickness = thickness * (0.5f + 0.5f * (float) Math.sin(s * Math.PI));

            float mx = AnimationUtils.lerp(x1, x2, s) + nx * wave;
            float my = AnimationUtils.lerp(y1, y2, s) + ny * wave;

            float alpha = strength * (float) Math.sin(s * Math.PI);
            int beamAlpha = AnimationUtils.clamp255((int) (ColorUtils.alpha(BEAM_COLOR) * alpha));

            bufferBuilder.vertex(pose, mx + nx * localThickness, my + ny * localThickness, 0)
                    .color(ColorUtils.red(BEAM_COLOR), ColorUtils.green(BEAM_COLOR), ColorUtils.blue(BEAM_COLOR), beamAlpha)
                    .endVertex();
            bufferBuilder.vertex(pose, mx - nx * localThickness, my - ny * localThickness, 0)
                    .color(ColorUtils.red(BEAM_COLOR), ColorUtils.green(BEAM_COLOR), ColorUtils.blue(BEAM_COLOR), beamAlpha)
                    .endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private void renderTrails(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time) {

        for (OrbData orb : ORBS) {
            bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            int trailLength = 20;
            for (int i = 0; i <= trailLength; i++) {
                float s = i / (float) trailLength;
                float pastT = time - s * 0.15f;

                float[] pos = orb.getPosition(centerX, centerY, maxRadius, pastT);

                float fade = (float) Math.pow(1.0f - s, 2.0f);
                float thickness = 0.1f * fade;

                int trailAlpha = AnimationUtils.clamp255((int) (ColorUtils.alpha(TRAIL_COLOR) * fade));

                float angle = orb.getAngle(pastT);
                float perpAngle = angle + (float) Math.PI * 0.5f;

                float x1 = pos[0] + (float) Math.cos(perpAngle) * thickness;
                float y1 = pos[1] + (float) Math.sin(perpAngle) * thickness;
                float x2 = pos[0] - (float) Math.cos(perpAngle) * thickness;
                float y2 = pos[1] - (float) Math.sin(perpAngle) * thickness;

                bufferBuilder.vertex(pose, x1, y1, 0)
                        .color(ColorUtils.red(TRAIL_COLOR), ColorUtils.green(TRAIL_COLOR), ColorUtils.blue(TRAIL_COLOR), trailAlpha)
                        .endVertex();
                bufferBuilder.vertex(pose, x2, y2, 0)
                        .color(ColorUtils.red(TRAIL_COLOR), ColorUtils.green(TRAIL_COLOR), ColorUtils.blue(TRAIL_COLOR), trailAlpha)
                        .endVertex();
            }

            BufferUploader.drawWithShader(bufferBuilder.end());
        }

    }

    private void renderOrbs(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time) {
        for (OrbData orb : ORBS) {
            float[] pos = orb.getPosition(centerX, centerY, maxRadius, time);
            float orbPulse = 0.85f + 0.15f * (float) Math.sin(time * Math.PI * 6.0 + orb.phase * 10.0f);

            // External glow
            drawCircle2(bufferBuilder, tesselator, pose, pos[0], pos[1], 12.0f * orbPulse, ORB_GLOW_COLOR, 0.4f);

            // Internal glow
            drawCircle(bufferBuilder, tesselator, pose, pos[0], pos[1], 6.0f * orbPulse, ORB_CORE_COLOR, 0.9f);
        }

    }

    private void drawCircle(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float radius, int color, float alphaMult) {

        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        int alpha = AnimationUtils.clamp255((int) (ColorUtils.alpha(color) * alphaMult));
        bufferBuilder.vertex(pose, centerX, centerY, 0)
                .color(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), alpha)
                .endVertex();

        for (int i = 0; i <= SEGMENTS; i++) {
            float angle = (i / (float) SEGMENTS) * (float) Math.PI * 2.0f;
            float x = centerX + (float) Math.cos(angle) * radius;
            float y = centerY + (float) Math.sin(angle) * radius;

            int edgeAlpha = AnimationUtils.clamp255((int) (alpha * 0.3f));
            bufferBuilder.vertex(pose, x, y, 0)
                    .color(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), edgeAlpha)
                    .endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private void drawCircle2(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float radius, int color, float alphaMult) {

        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        int alpha = AnimationUtils.clamp255((int) (ColorUtils.alpha(color) * alphaMult));
        bufferBuilder.vertex(pose, centerX, centerY, 0)
                .color(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), alpha)
                .endVertex();

        for (int i = 0; i <= SEGMENTS; i++) {
            float angle = (i / (float) SEGMENTS) * (float) Math.PI * 2.0f;
            float x = centerX + (float) Math.cos(angle) * radius;
            float y = centerY + (float) Math.sin(angle) * radius;

            bufferBuilder.vertex(pose, x, y, 0)
                    .color(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), 0)
                    .endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private record OrbData(float phase, float distance, float speed) {

        float[] getPosition(float centerX, float centerY, float maxRadius, float t) {
            float angle = getAngle(t);
            float radius = maxRadius * distance;

            return new float[]{centerX + (float) Math.cos(angle) * radius, centerY + (float) Math.sin(angle) * radius};
        }

        float getAngle(float t) {
            return (t * speed + phase) * (float) Math.PI * 2.0f;
        }

    }

}