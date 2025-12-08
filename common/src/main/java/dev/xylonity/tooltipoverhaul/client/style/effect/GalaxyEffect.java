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

import java.util.Random;

public class GalaxyEffect implements EffectLayer {

    private static final int CORE_COLOR = 0xFF1A0A2E;
    private static final int NEBULA_INNER = 0xDD4A2080;
    private static final int NEBULA_OUTER = 0x881A1040;
    private static final int SPIRAL_COLOR = 0xBB6A40A0;
    private static final int STAR_COLOR = 0xFFFFFFDD;
    private static final int DUST_COLOR = 0xAA8A60C0;

    private static final int SEGMENTS = 64;
    private static final int STAR_COUNT = 80;
    private static final int DUST_COUNT = 120;

    private static final float[][] STARS = new float[STAR_COUNT][4];
    private static final float[][] DUST_PARTICLES = new float[DUST_COUNT][4];

    static {
        Random random = new Random(31415L);

        // Stars
        for (int i = 0; i < STAR_COUNT; i++) {
            float angle = random.nextFloat() * (float) Math.PI * 2.0f;
            float distance = (float) Math.pow(random.nextFloat(), 0.6f);
            STARS[i][0] = angle;
            STARS[i][1] = distance;
            STARS[i][2] = 0.5f + random.nextFloat() * 1.5f;
            STARS[i][3] = 0.3f + random.nextFloat() * 0.7f;
        }

        // Particles
        for (int i = 0; i < DUST_COUNT; i++) {
            float angle = random.nextFloat() * (float) Math.PI * 2.0f;
            float distance = random.nextFloat();
            DUST_PARTICLES[i][0] = angle;
            DUST_PARTICLES[i][1] = distance;
            DUST_PARTICLES[i][2] = 0.5f + random.nextFloat(); // Size
            DUST_PARTICLES[i][3] = 0.4f + random.nextFloat() * 0.8f; // speed
        }

    }

    @Override
    public void render(TooltipContext context, Vec2 position) {
        int positionX = (int) position.x;
        int positionY = (int) position.y;
        int tooltipWidth = (int) context.getTooltipSize().x;
        int tooltipHeight = (int) context.getTooltipSize().y;

        long now = System.currentTimeMillis();
        float time = (now - context.getStartTime()) / 12000f;

        float centerX = positionX + tooltipWidth * 0.5f;
        float centerY = positionY + tooltipHeight * 0.5f;
        float maxRadius = (float) Math.hypot(tooltipWidth, tooltipHeight) * 0.6f;

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

            // Inner
            renderNebula(buf, tess, pose, centerX, centerY, maxRadius, time);

            // Random particles
            renderParticles(buf, tess, pose, centerX, centerY, maxRadius, time);

            // Spirals
            renderSpirals(buf, tess, pose, centerX, centerY, maxRadius, time);

            // More particles
            renderStars(buf, tess, pose, centerX, centerY, maxRadius, time);

            // Dark core inner
            renderCore2(buf, tess, pose, centerX, centerY, maxRadius, time);

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

    private void renderNebula(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time) {

        float pulse = 0.95f + 0.05f * (float) Math.sin(time * Math.PI * 2.0f);

        float[] radii = {maxRadius * 0.85f * pulse, maxRadius * 0.55f * pulse, maxRadius * 0.25f * pulse};
        int[] colors = {NEBULA_OUTER, NEBULA_INNER, CORE_COLOR};

        for (int layer = 0; layer < 3; layer++) {
            bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

            float radius = radii[layer];
            int color = colors[layer];

            int alpha = layer == 0 ? 120 : (layer == 1 ? 180 : 220);

            bufferBuilder.vertex(pose, centerX, centerY, 0)
                    .color(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), alpha)
                    .endVertex();

            for (int i = 0; i <= SEGMENTS; i++) {
                float s = i / (float) SEGMENTS;
                float angle = s * (float) Math.PI * 2.0f;

                float noise = 0.15f * (float) Math.sin(angle * 3.0f + time * Math.PI * 1.5f) * (float) Math.cos(angle * 5.0f - time * Math.PI * 2.0f);
                float localRadius = radius * (1.0f + noise);

                float x = centerX + (float) Math.cos(angle) * localRadius;
                float y = centerY + (float) Math.sin(angle) * localRadius;

                bufferBuilder.vertex(pose, x, y, 0)
                        .color(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), layer == 0 ? 20 : (layer == 1 ? 40 : 80))
                        .endVertex();
            }

            BufferUploader.drawWithShader(bufferBuilder.end());
        }
    }

    private void renderParticles(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time) {

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (float[] dust : DUST_PARTICLES) {
            float baseAngle = dust[0];
            float distance = dust[1];
            float size = dust[2];
            float speed = dust[3];

            float angle = baseAngle + time * speed * (float) Math.PI * 2.0f;

            float radius = maxRadius * distance * (0.8f + 0.2f * (float) Math.sin(time * Math.PI * 4.0f + baseAngle));

            float px = centerX + (float) Math.cos(angle) * radius;
            float py = centerY + (float) Math.sin(angle) * radius;

            float twinkle = 0.4f + 0.6f * (float) Math.pow(Math.sin((time * 3.0f + baseAngle) * Math.PI) * 0.5f + 0.5f, 2.0f);

            float particleSize = size * twinkle;
            int alpha = AnimationUtils.clamp255((int) (ColorUtils.alpha(DUST_COLOR) * twinkle * (1.0f - distance * 0.3f)));

            bufferBuilder.vertex(pose, px - particleSize, py - particleSize, 0)
                    .color(ColorUtils.red(DUST_COLOR), ColorUtils.green(DUST_COLOR), ColorUtils.blue(DUST_COLOR), alpha)
                    .endVertex();
            bufferBuilder.vertex(pose, px - particleSize, py + particleSize, 0)
                    .color(ColorUtils.red(DUST_COLOR), ColorUtils.green(DUST_COLOR), ColorUtils.blue(DUST_COLOR), alpha)
                    .endVertex();
            bufferBuilder.vertex(pose, px + particleSize, py + particleSize, 0)
                    .color(ColorUtils.red(DUST_COLOR), ColorUtils.green(DUST_COLOR), ColorUtils.blue(DUST_COLOR), alpha)
                    .endVertex();
            bufferBuilder.vertex(pose, px + particleSize, py - particleSize, 0)
                    .color(ColorUtils.red(DUST_COLOR), ColorUtils.green(DUST_COLOR), ColorUtils.blue(DUST_COLOR), alpha)
                    .endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private void renderSpirals(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time) {

        int armCount = 3;
        for (int arm = 0; arm < armCount; arm++) {
            float armOffset = (float) (arm * 2.0 * Math.PI / armCount);

            bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            int segments = 40;
            for (int i = 0; i <= segments; i++) {
                float s = i / (float) segments;

                float radius = maxRadius * (0.2f + s * 0.7f);
                float angle = armOffset + s * (float) Math.PI * 3.0f - time * (float) Math.PI * 0.5f;

                float wave = 0.08f * (float) Math.sin(s * Math.PI * 5.0f + time * Math.PI * 3.0f);
                angle += wave;

                float x = centerX + (float) Math.cos(angle) * radius;
                float y = centerY + (float) Math.sin(angle) * radius;

                float thickness = maxRadius * 0.05f * (1.0f - s * 0.6f) * (0.7f + 0.3f * (float) Math.sin(s * Math.PI * 3.0f + time * Math.PI * 4.0f));

                float perpendicular = angle + (float) Math.PI * 0.5f;
                float x1 = x + (float) Math.cos(perpendicular) * thickness;
                float y1 = y + (float) Math.sin(perpendicular) * thickness;
                float x2 = x - (float) Math.cos(perpendicular) * thickness;
                float y2 = y - (float) Math.sin(perpendicular) * thickness;

                float fadeOut = (float) Math.pow(1.0f - s, 1.2f);
                float brightness = 0.5f + 0.5f * (float) Math.sin(s * Math.PI * 4.0f + time * Math.PI * 5.0f);

                int alpha = AnimationUtils.clamp255((int) (ColorUtils.alpha(SPIRAL_COLOR) * fadeOut * brightness));

                bufferBuilder.vertex(pose, x1, y1, 0)
                        .color(ColorUtils.red(SPIRAL_COLOR), ColorUtils.green(SPIRAL_COLOR), ColorUtils.blue(SPIRAL_COLOR), alpha)
                        .endVertex();
                bufferBuilder.vertex(pose, x2, y2, 0)
                        .color(ColorUtils.red(SPIRAL_COLOR), ColorUtils.green(SPIRAL_COLOR), ColorUtils.blue(SPIRAL_COLOR), alpha)
                        .endVertex();
            }

            BufferUploader.drawWithShader(bufferBuilder.end());
        }

    }

    private void renderStars(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time) {

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (float[] star : STARS) {
            float baseAngle = star[0];
            float distance = star[1];
            float size = star[2];
            float speed = star[3];

            float angle = baseAngle + time * speed * (float) Math.PI * 2.0f;
            float radius = maxRadius * distance;

            float px = centerX + (float) Math.cos(angle) * radius;
            float py = centerY + (float) Math.sin(angle) * radius;

            float twinkle = 0.6f + 0.4f * (float) Math.sin((time * 8.0f + baseAngle * 10.0f) * Math.PI);

            float starSize = size * twinkle;
            int alpha = AnimationUtils.clamp255((int) (255 * twinkle * (1.0f - distance * 0.2f)));

            bufferBuilder.vertex(pose, px - starSize, py - starSize * 0.3f, 0)
                    .color(ColorUtils.red(STAR_COLOR), ColorUtils.green(STAR_COLOR), ColorUtils.blue(STAR_COLOR), alpha)
                    .endVertex();
            bufferBuilder.vertex(pose, px - starSize, py + starSize * 0.3f, 0)
                    .color(ColorUtils.red(STAR_COLOR), ColorUtils.green(STAR_COLOR), ColorUtils.blue(STAR_COLOR), alpha)
                    .endVertex();
            bufferBuilder.vertex(pose, px + starSize, py + starSize * 0.3f, 0)
                    .color(ColorUtils.red(STAR_COLOR), ColorUtils.green(STAR_COLOR), ColorUtils.blue(STAR_COLOR), alpha)
                    .endVertex();
            bufferBuilder.vertex(pose, px + starSize, py - starSize * 0.3f, 0)
                    .color(ColorUtils.red(STAR_COLOR), ColorUtils.green(STAR_COLOR), ColorUtils.blue(STAR_COLOR), alpha)
                    .endVertex();

            bufferBuilder.vertex(pose, px - starSize * 0.3f, py - starSize, 0)
                    .color(ColorUtils.red(STAR_COLOR), ColorUtils.green(STAR_COLOR), ColorUtils.blue(STAR_COLOR), alpha)
                    .endVertex();
            bufferBuilder.vertex(pose, px - starSize * 0.3f, py + starSize, 0)
                    .color(ColorUtils.red(STAR_COLOR), ColorUtils.green(STAR_COLOR), ColorUtils.blue(STAR_COLOR), alpha)
                    .endVertex();
            bufferBuilder.vertex(pose, px + starSize * 0.3f, py + starSize, 0)
                    .color(ColorUtils.red(STAR_COLOR), ColorUtils.green(STAR_COLOR), ColorUtils.blue(STAR_COLOR), alpha)
                    .endVertex();
            bufferBuilder.vertex(pose, px + starSize * 0.3f, py - starSize, 0)
                    .color(ColorUtils.red(STAR_COLOR), ColorUtils.green(STAR_COLOR), ColorUtils.blue(STAR_COLOR), alpha)
                    .endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private void renderCore2(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time) {

        float pulse = 0.92f + 0.08f * (float) Math.sin(time * Math.PI * 3.0f);
        float coreRadius = maxRadius * 0.12f * pulse;

        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.vertex(pose, centerX, centerY, 0)
                .color(ColorUtils.red(CORE_COLOR), ColorUtils.green(CORE_COLOR), ColorUtils.blue(CORE_COLOR), 255)
                .endVertex();

        for (int i = 0; i <= SEGMENTS; i++) {
            float angle = (i / (float) SEGMENTS) * (float) Math.PI * 2.0f;

            float distortion = 0.1f * (float) Math.sin(angle * 4.0f + time * Math.PI * 6.0f);
            float localRadius = coreRadius * (1.0f + distortion);

            float x = centerX + (float) Math.cos(angle) * localRadius;
            float y = centerY + (float) Math.sin(angle) * localRadius;

            bufferBuilder.vertex(pose, x, y, 0)
                    .color(ColorUtils.red(CORE_COLOR), ColorUtils.green(CORE_COLOR), ColorUtils.blue(CORE_COLOR), 200)
                    .endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

}