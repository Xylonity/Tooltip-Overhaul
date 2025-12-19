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

public class NebulaEffect implements EffectLayer {

    private static final int NEBULA_COLOR_1 = 0x60FADFF6;
    private static final int NEBULA_COLOR_2 = 0x408AC791;
    private static final int NEBULA_COLOR_3 = 0x502A5193;

    private static final int STAR_COLOR_CORE = 0xFFFFFFFF;
    private static final int STAR_COLOR_GLOW = 0x80AACCFF;

    private static final int ENERGY_WAVE_COLOR = 0x50FFAAFF;

    private static final int PARTICLE_COUNT = 10;
    private static final int SEGMENTS = 96;

    private static final float[][] PARTICLE_DATA = new float[PARTICLE_COUNT][5];

    static {
        Random random = new Random(12345L);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            PARTICLE_DATA[i][0] = random.nextFloat();
            PARTICLE_DATA[i][1] = random.nextFloat();
            PARTICLE_DATA[i][2] = random.nextFloat() * (float) (Math.PI * 2.0);
            PARTICLE_DATA[i][3] = 0.3f + random.nextFloat() * 0.7f;
            PARTICLE_DATA[i][4] = 0.5f + random.nextFloat() * 1.5f;
        }

    }

    @Override
    public void render(TooltipContext context, Vec2 position) {
        int positionX = (int) position.x;
        int positionY = (int) position.y;
        int tooltipWidth = (int) context.getTooltipSize().x;
        int tooltipHeight = (int) context.getTooltipSize().y;

        long now = System.currentTimeMillis();
        float time = (now - context.getStartTime()) / 8000f;

        float centerX = positionX + tooltipWidth * 0.5f;
        float centerY = positionY + tooltipHeight * 0.5f;
        float maxRadius = (float) Math.hypot(tooltipWidth, tooltipHeight) * 0.85f;

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

            renderNebulaLayers(tesselator, pose, centerX, centerY, maxRadius, tooltipWidth, tooltipHeight, time);

            renderSideWaves(tesselator, pose, centerX, centerY, maxRadius, time);

            renderParticles(tesselator, pose, positionX, positionY, tooltipWidth, tooltipHeight, centerX, centerY, maxRadius, time);

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

    private void renderNebulaLayers(Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, int width, int height, float time) {

        float aspect = (float) height / (float) Math.max(1, width);

        int[] colors = {NEBULA_COLOR_1, NEBULA_COLOR_2, NEBULA_COLOR_3};
        float[] ringOffsets = {0.60f, 0.75f, 0.92f};
        float[] ringThickness = {0.20f, 0.18f, 0.16f};
        float[] speeds = {0.50f, -0.35f, 0.22f};

        for (int layer = 0; layer < 3; layer++) {
            int color = colors[layer];

            float baseRadius = maxRadius * ringOffsets[layer];
            float thickness = maxRadius * ringThickness[layer];
            float halfTh = thickness * 0.5f;
            float phase = time * (float) (Math.PI * 2.0) * speeds[layer];

            // Inner part of the halo
            BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i <= SEGMENTS; i++) {
                float s = i / (float) SEGMENTS;
                float angle = s * (float) (Math.PI * 2.0) + phase;

                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                float swirl1 = 0.12f * (float) Math.sin(angle * 3.0f + phase * 2.1f + layer * 0.7f);
                float swirl2 = 0.07f * (float) Math.sin(angle * 7.0f - phase * 1.4f);
                float swirl = (swirl1 + swirl2) * maxRadius;

                float ringRadius = baseRadius + swirl;

                float inner = ringRadius - halfTh * 0.4f;
                float outer = ringRadius + halfTh * 0.4f;

                float sxInner = cos * inner;
                float syInner = sin * inner * aspect;
                float sxOuter = cos * outer;
                float syOuter = sin * outer * aspect;

                float ix = centerX + sxInner;
                float iy = centerY + syInner;
                float ox = centerX + sxOuter;
                float oy = centerY + syOuter;

                float vertical = (float) Math.max(0.0, sin * 0.8f + 0.2f);
                float band = 0.5f + 0.5f * (float) Math.sin(angle * 2.0f - phase * 1.3f + layer);
                float intensity = vertical * (0.4f + 0.6f * band);

                int innerA = AnimationUtils.clamp255((int) (ColorUtils.alpha(color) * intensity * 0.95f));
                int outerA = AnimationUtils.clamp255((int) (ColorUtils.alpha(color) * intensity * 0.70f));

                bufferBuilder.addVertex(pose, ox, oy, 0)
                        .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), outerA);
                bufferBuilder.addVertex(pose, ix, iy, 0)
                        .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), innerA);
            }
            try (MeshData data = bufferBuilder.buildOrThrow()) {
                BufferUploader.drawWithShader(data);
            }

            // Second inner
            BufferBuilder bufferBuilder1 = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i <= SEGMENTS; i++) {
                float s = i / (float) SEGMENTS;
                float angle = s * (float) (Math.PI * 2.0) + phase;

                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                float swirl1 = 0.12f * (float) Math.sin(angle * 3.0f + phase * 2.1f + layer * 0.7f);
                float swirl2 = 0.07f * (float) Math.sin(angle * 7.0f - phase * 1.4f);
                float swirl = (swirl1 + swirl2) * maxRadius;

                float ringRadius = baseRadius + swirl;

                float inner = ringRadius + halfTh * 0.4f;
                float outer = ringRadius + halfTh;

                float sxInner = cos * inner;
                float syInner = sin * inner * aspect;
                float sxOuter = cos * outer;
                float syOuter = sin * outer * aspect;

                float ix = centerX + sxInner;
                float iy = centerY + syInner;
                float ox = centerX + sxOuter;
                float oy = centerY + syOuter;

                float horizontalMask = (float) Math.pow(Math.abs(sin), 1.4f);

                float band = 0.5f + 0.5f * (float) Math.sin(angle * 2.0f - phase * 1.3f + layer);
                float intensity = horizontalMask * (0.35f + 0.65f * band);

                int innerA = AnimationUtils.clamp255((int) (ColorUtils.alpha(color) * intensity * 0.30f));
                int outerA = AnimationUtils.clamp255((int) (ColorUtils.alpha(color) * intensity * 0.04f));

                bufferBuilder1.addVertex(pose, ox, oy, 0)
                        .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), outerA);
                bufferBuilder1.addVertex(pose, ix, iy, 0)
                        .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), innerA);
            }
            try (MeshData data = bufferBuilder1.buildOrThrow()) {
                BufferUploader.drawWithShader(data);
            }

            // Third inner
            BufferBuilder bufferBuilder2 = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i <= SEGMENTS; i++) {
                float s = i / (float) SEGMENTS;
                float angle = s * (float) (Math.PI * 2.0) + phase;

                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                float swirl1 = 0.12f * (float) Math.sin(angle * 3.0f + phase * 2.1f + layer * 0.7f);
                float swirl2 = 0.07f * (float) Math.sin(angle * 7.0f - phase * 1.4f);
                float swirl = (swirl1 + swirl2) * maxRadius;

                float ringRadius = baseRadius + swirl;

                float inner = ringRadius - halfTh;
                float outer = ringRadius - halfTh * 0.4f;

                float sxInner = cos * inner;
                float syInner = sin * inner * aspect;
                float sxOuter = cos * outer;
                float syOuter = sin * outer * aspect;

                float ix = centerX + sxInner;
                float iy = centerY + syInner;
                float ox = centerX + sxOuter;
                float oy = centerY + syOuter;

                float horizontalMask = (float) Math.pow(Math.abs(sin), 1.4f);

                float band = 0.5f + 0.5f * (float) Math.sin(angle * 2.0f - phase * 1.3f + layer);
                float intensity = horizontalMask * (0.35f + 0.65f * band);

                int innerA = AnimationUtils.clamp255((int) (ColorUtils.alpha(color) * intensity * 0.04f));
                int outerA = AnimationUtils.clamp255((int) (ColorUtils.alpha(color) * intensity * 0.30f));

                bufferBuilder2.addVertex(pose, ox, oy, 0)
                        .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), outerA);
                bufferBuilder2.addVertex(pose, ix, iy, 0)
                        .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), innerA);
            }

            try (MeshData data = bufferBuilder2.buildOrThrow()) {
                BufferUploader.drawWithShader(data);
            }
        }

    }

    private void renderSideWaves(Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time) {

        for (int band = 0; band < 2; band++) {
            float bandOffset = (band == 0) ? 0f : (float) Math.PI;
            float phase = time * (float) (Math.PI * 2.0) * (0.35f + band * 0.18f);

            float radius = maxRadius * (0.58f + band * 0.10f);
            float thickness = maxRadius * (0.06f + band * 0.02f);
            float arcSpan = (float) (Math.PI * 1.4);

            BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i <= SEGMENTS; i++) {
                float s = i / (float) SEGMENTS;
                float local = (s - 0.5f) * arcSpan;
                float angle = local + phase + bandOffset;

                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                float wobble = 0.08f * (float) Math.sin(s * 6.0f + time * (float) Math.PI * 4.0f + band * 1.7f);
                float baseRadius = radius + wobble * maxRadius * 0.18f;

                float inner = baseRadius - thickness * 0.5f;
                float outer = baseRadius + thickness * 0.5f;

                float ix = centerX + cos * inner;
                float iy = centerY + sin * inner;
                float ox = centerX + cos * outer;
                float oy = centerY + sin * outer;

                float arcMask = (float) Math.sin(s * Math.PI);
                float pulse = 0.6f + 0.4f * (float) Math.sin(time * (float) Math.PI * 2.0f * 3.0f + s * 5.0f + band * 1.3f);
                float alphaF = arcMask * pulse;

                int edgeAlpha = AnimationUtils.clamp255((int) (ColorUtils.alpha(ENERGY_WAVE_COLOR) * alphaF * 0.4f));
                int coreAlpha = AnimationUtils.clamp255((int) (ColorUtils.alpha(ENERGY_WAVE_COLOR) * alphaF * 0.9f));

                bufferBuilder.addVertex(pose, ox, oy, 0)
                        .setColor(ColorUtils.red(ENERGY_WAVE_COLOR), ColorUtils.green(ENERGY_WAVE_COLOR), ColorUtils.blue(ENERGY_WAVE_COLOR), edgeAlpha);
                bufferBuilder.addVertex(pose, ix, iy, 0)
                        .setColor(ColorUtils.red(ENERGY_WAVE_COLOR), ColorUtils.green(ENERGY_WAVE_COLOR), ColorUtils.blue(ENERGY_WAVE_COLOR), coreAlpha);
            }

            try (MeshData data = bufferBuilder.buildOrThrow()) {
                BufferUploader.drawWithShader(data);
            }
        }

    }

    private void renderParticles(Tesselator tesselator, Matrix4f pose, int x, int y, int width, int height, float centerX, float centerY, float maxRadius, float time) {

        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float aspect = (float) height / (float) Math.max(1, width);
        float ringRadius = maxRadius * 0.68f;
        float ringThickness = maxRadius * 0.06f;

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float[] data = PARTICLE_DATA[i];

            float seedAngle = data[0] * (float) (Math.PI * 2.0);
            float seedRadOffset = (data[1] - 0.5f);
            float phase = data[2];
            float speed = data[3];
            float sizeSeed = data[4];

            float angle = seedAngle + time * (float) (Math.PI * 2.0) * speed;

            float arm = (float) Math.sin(angle * 3.0f + phase);
            float radius = ringRadius + seedRadOffset * ringThickness * 3.0f + arm * ringThickness * 0.7f;

            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float px = centerX + cos * radius;
            float py = centerY + sin * radius * aspect;

            float twinkle = 0.35f + 0.65f * (float) Math.pow(Math.sin(time * (float) Math.PI * 2.0f * speed + phase) * 0.5f + 0.5f, 2.0f);
            float size = sizeSeed * (0.7f + twinkle * 0.9f);

            int alpha = AnimationUtils.clamp255((int) (255 * twinkle));
            int color = (i % 3 == 0) ? STAR_COLOR_CORE : STAR_COLOR_GLOW;

            float half = size * 0.5f;

            // Particle shape
            bufferBuilder.addVertex(pose, px, py - half, 0)
                    .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), alpha);
            bufferBuilder.addVertex(pose, px + half, py, 0)
                    .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), alpha);
            bufferBuilder.addVertex(pose, px, py + half, 0)
                    .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), alpha);
            bufferBuilder.addVertex(pose, px - half, py, 0)
                    .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), alpha);
        }

        try (MeshData data = bufferBuilder.buildOrThrow()) {
            BufferUploader.drawWithShader(data);
        }
    }

}
