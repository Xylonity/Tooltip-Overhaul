package dev.xylonity.tooltipoverhaul.client.style.effect;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.xylonity.tooltipoverhaul.client.layer.impl.EffectLayer;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectClip;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

import java.util.Random;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectRuntime;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.parameter;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

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
        final Random random = new Random(12345L);
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
        final EffectClip clip = new EffectClip(context, position);
        final int positionX = clip.hasExtension() ? clip.left() : (int) position.x;
        final int positionY = clip.hasExtension() ? clip.top() : (int) position.y;
        final int tooltipWidth = clip.hasExtension() ? clip.width() : (int) context.getTooltipSize().x;
        final int tooltipHeight = clip.hasExtension() ? clip.height() : (int) context.getTooltipSize().y;

        final long now = System.currentTimeMillis();
        final float time = (float) (EffectRuntime.seconds(context) / 8);

        final float centerX = positionX + tooltipWidth * 0.5f;
        final float centerY = positionY + tooltipHeight * 0.5f;
        final float maxRadius = (float) Math.hypot(tooltipWidth, tooltipHeight) * 0.85f * parameter(ORBIT_RADIUS);

        context.flush();
        context.push(() -> {

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

            final Matrix4f pose = context.getPose().last().pose();
            final Tesselator tesselator = Tesselator.getInstance();

            renderNebulaLayers(clip, tesselator, pose, centerX, centerY, maxRadius, tooltipWidth, tooltipHeight, time);

            renderSideWaves(clip, tesselator, pose, centerX, centerY, maxRadius, time);

            renderParticles(clip, tesselator, pose, positionX, positionY, tooltipWidth, tooltipHeight, centerX, centerY, maxRadius, time);

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

    private void renderNebulaLayers(EffectClip clip, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, int width, int height, float time) {

        float aspect = (float) height / (float) Math.max(1, width);

        final int[] colors = {EffectRuntime.color(0, NEBULA_COLOR_1), EffectRuntime.color(1, NEBULA_COLOR_2), EffectRuntime.color(2, NEBULA_COLOR_3)};
        final float[] ringOffsets = {0.60f, 0.75f, 0.92f};
        float[] ringThickness = {0.20f, 0.18f, 0.16f};
        final float[] speeds = {0.50f, -0.35f, 0.22f};

        for (int layer = 0; layer < 3; layer++) {
            int color = colors[layer];

            float baseRadius = maxRadius * ringOffsets[layer];
            float thickness = maxRadius * ringThickness[layer];
            final float halfTh = thickness * 0.5f;
            float phase = time * (float) (Math.PI * 2.0) * speeds[layer];

            // Inner part of the halo
            final BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i <= SEGMENTS; i++) {
                float along = i / (float) SEGMENTS;
                float angle = along * (float) (Math.PI * 2.0) + phase;

                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                float swirl1 = 0.12f * (float) Math.sin(angle * 3.0f + phase * 2.1f + layer * 0.7f);
                float swirl2 = 0.07f * (float) Math.sin(angle * 7.0f - phase * 1.4f);
                float swirl = (swirl1 + swirl2) * maxRadius * parameter(TWIST);

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

                final float vertical = (float) Math.max(0.0, sin * 0.8f + 0.2f);
                float band = 0.5f + 0.5f * (float) Math.sin(angle * 2.0f - phase * 1.3f + layer);
                float intensity = vertical * (0.4f + 0.6f * band);

                int innerA = AnimationUtils.clamp255((int) (ColorUtils.alpha(color) * intensity * 0.95f));
                int outerA = AnimationUtils.clamp255((int) (ColorUtils.alpha(color) * intensity * 0.70f));

                bufferBuilder.addVertex(pose, ox, oy, 0)
                        .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), EffectRuntime.alpha(outerA));
                bufferBuilder.addVertex(pose, ix, iy, 0)
                        .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), EffectRuntime.alpha(innerA));
            }

            clip.draw(bufferBuilder.build());

            // Second inner
            final BufferBuilder second = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i <= SEGMENTS; i++) {
                float along = i / (float) SEGMENTS;
                float angle = along * (float) (Math.PI * 2.0) + phase;

                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                float swirl1 = 0.12f * (float) Math.sin(angle * 3.0f + phase * 2.1f + layer * 0.7f);
                float swirl2 = 0.07f * (float) Math.sin(angle * 7.0f - phase * 1.4f);
                float swirl = (swirl1 + swirl2) * maxRadius * parameter(TWIST);

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

                second.addVertex(pose, ox, oy, 0)
                        .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), EffectRuntime.alpha(outerA));
                second.addVertex(pose, ix, iy, 0)
                        .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), EffectRuntime.alpha(innerA));
            }

            clip.draw(second.build());

            // Third inner
            final BufferBuilder third = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i <= SEGMENTS; i++) {
                float along = i / (float) SEGMENTS;
                float angle = along * (float) (Math.PI * 2.0) + phase;

                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                final float swirl1 = 0.12f * (float) Math.sin(angle * 3.0f + phase * 2.1f + layer * 0.7f);
                final float swirl2 = 0.07f * (float) Math.sin(angle * 7.0f - phase * 1.4f);
                final float swirl = (swirl1 + swirl2) * maxRadius * parameter(TWIST);

                float ringRadius = baseRadius + swirl;

                float inner = ringRadius - halfTh;
                float outer = ringRadius - halfTh * 0.4f;

                final float sxInner = cos * inner;
                final float syInner = sin * inner * aspect;
                final float sxOuter = cos * outer;
                final float syOuter = sin * outer * aspect;

                float ix = centerX + sxInner;
                float iy = centerY + syInner;
                float ox = centerX + sxOuter;
                float oy = centerY + syOuter;

                final float horizontalMask = (float) Math.pow(Math.abs(sin), 1.4f);

                float band = 0.5f + 0.5f * (float) Math.sin(angle * 2.0f - phase * 1.3f + layer);
                final float intensity = horizontalMask * (0.35f + 0.65f * band);

                final int innerA = AnimationUtils.clamp255((int) (ColorUtils.alpha(color) * intensity * 0.04f));
                final int outerA = AnimationUtils.clamp255((int) (ColorUtils.alpha(color) * intensity * 0.30f));

                third.addVertex(pose, ox, oy, 0)
                        .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), EffectRuntime.alpha(outerA));
                third.addVertex(pose, ix, iy, 0)
                        .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), EffectRuntime.alpha(innerA));
            }

            clip.draw(third.build());
        }

    }

    private void renderSideWaves(EffectClip clip, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time) {

        for (int band = 0; band < 2; band++) {
            final float bandOffset = (band == 0) ? 0f : (float) Math.PI;
            float phase = time * (float) (Math.PI * 2.0) * (0.35f + band * 0.18f);

            float radius = maxRadius * (0.58f + band * 0.10f);
            final float thickness = maxRadius * (0.06f + band * 0.02f);
            final float arcSpan = (float) (Math.PI * 1.4);

            final BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i <= SEGMENTS; i++) {
                final float along = i / (float) SEGMENTS;
                final float local = (along - 0.5f) * arcSpan;
                float angle = local + phase + bandOffset;

                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                final float wobble = 0.08f * (float) Math.sin(along * 6.0f + time * (float) Math.PI * 4.0f + band * 1.7f);
                final float baseRadius = radius + wobble * maxRadius * 0.18f;

                final float inner = baseRadius - thickness * 0.5f;
                final float outer = baseRadius + thickness * 0.5f;

                final float ix = centerX + cos * inner;
                final float iy = centerY + sin * inner;
                final float ox = centerX + cos * outer;
                final float oy = centerY + sin * outer;

                final float arcMask = (float) Math.sin(along * Math.PI);
                final float pulse = 0.6f + 0.4f * (float) Math.sin(time * (float) Math.PI * 2.0f * 3.0f + along * 5.0f + band * 1.3f);
                final float alphaF = arcMask * pulse;

                final int edgeAlpha = AnimationUtils.clamp255((int) (ColorUtils.alpha(EffectRuntime.color(0, ENERGY_WAVE_COLOR)) * alphaF * 0.4f));
                final int coreAlpha = AnimationUtils.clamp255((int) (ColorUtils.alpha(EffectRuntime.color(0, ENERGY_WAVE_COLOR)) * alphaF * 0.9f));

                bufferBuilder.addVertex(pose, ox, oy, 0)
                        .setColor(ColorUtils.red(EffectRuntime.color(0, ENERGY_WAVE_COLOR)), ColorUtils.green(EffectRuntime.color(0, ENERGY_WAVE_COLOR)), ColorUtils.blue(EffectRuntime.color(0, ENERGY_WAVE_COLOR)), EffectRuntime.alpha(edgeAlpha));
                bufferBuilder.addVertex(pose, ix, iy, 0)
                        .setColor(ColorUtils.red(EffectRuntime.color(0, ENERGY_WAVE_COLOR)), ColorUtils.green(EffectRuntime.color(0, ENERGY_WAVE_COLOR)), ColorUtils.blue(EffectRuntime.color(0, ENERGY_WAVE_COLOR)), EffectRuntime.alpha(coreAlpha));
            }

            clip.draw(bufferBuilder.build());
        }

    }

    private void renderParticles(EffectClip clip, Tesselator tesselator, Matrix4f pose, int x, int y, int width, int height, float centerX, float centerY, float maxRadius, float time) {

        final BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        final float aspect = (float) height / (float) Math.max(1, width);
        final float ringRadius = maxRadius * 0.68f;
        final float ringThickness = maxRadius * 0.06f;

        for (int i = 0; i < Math.round(PARTICLE_COUNT * EffectRuntime.density()); i++) {
            final float[] data = PARTICLE_DATA[i % PARTICLE_DATA.length];

            final float seedAngle = (data[0] + (i / PARTICLE_DATA.length) * 0.618034f) * (float) (Math.PI * 2.0);
            final float seedRadOffset = (data[1] - 0.5f);
            final float phase = data[2];
            final float speed = data[3];
            final float sizeSeed = data[4];

            final float angle = seedAngle + time * (float) (Math.PI * 2.0) * speed;

            final float arm = (float) Math.sin(angle * 3.0f + phase);
            final float radius = ringRadius + seedRadOffset * ringThickness * 3.0f + arm * ringThickness * 0.7f;

            final float cos = (float) Math.cos(angle);
            final float sin = (float) Math.sin(angle);

            final float px = centerX + cos * radius;
            final float py = centerY + sin * radius * aspect;

            final float twinkle = 0.35f + 0.65f * (float) Math.pow(Math.sin(time * (float) Math.PI * 2.0f * speed + phase) * 0.5f + 0.5f, 2.0f);
            final float size = sizeSeed * (0.7f + twinkle * 0.9f);

            final int alpha = AnimationUtils.clamp255((int) (255 * twinkle));
            final int color = (i % 3 == 0) ? EffectRuntime.color(0, STAR_COLOR_CORE) : EffectRuntime.color(2, STAR_COLOR_GLOW);

            final float half = size * 0.5f;

            // Particle shape
            bufferBuilder.addVertex(pose, px, py - half, 0)
                    .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), EffectRuntime.alpha(alpha));
            bufferBuilder.addVertex(pose, px + half, py, 0)
                    .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), EffectRuntime.alpha(alpha));
            bufferBuilder.addVertex(pose, px, py + half, 0)
                    .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), EffectRuntime.alpha(alpha));
            bufferBuilder.addVertex(pose, px - half, py, 0)
                    .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), EffectRuntime.alpha(alpha));
        }

        clip.draw(bufferBuilder.build());
    }

}
