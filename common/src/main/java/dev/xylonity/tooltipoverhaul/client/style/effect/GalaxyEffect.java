package dev.xylonity.tooltipoverhaul.client.style.effect;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.xylonity.tooltipoverhaul.client.layer.impl.EffectLayer;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectClip;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

import java.util.Random;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectRuntime;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCanvas.parameter;
import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public class GalaxyEffect implements EffectLayer {

    private static final int CORE_COLOR = 0xFF1A0A2E;
    private static final int NEBULA_INNER = 0xDD4A2080;
    private static final int NEBULA_OUTER = 0x881A1040;
    private static final int SPIRAL_COLOR = 0xBB6A40A0;
    private static final int STAR_COLOR = 0xFFFFFFDD;
    private static final int DUST_COLOR = 0xAA8A60C0;

    private static final int SEGMENTS = 96;
    private static final int STAR_COUNT = 80;
    private static final int DUST_COUNT = 120;

    private static final float[][] STARS = new float[STAR_COUNT][4];
    private static final float[][] DUST_PARTICLES = new float[DUST_COUNT][4];

    static {
        final Random random = new Random(31415L);

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
            final float distance = random.nextFloat();
            DUST_PARTICLES[i][0] = angle;
            DUST_PARTICLES[i][1] = distance;
            DUST_PARTICLES[i][2] = 0.5f + random.nextFloat(); // Size
            DUST_PARTICLES[i][3] = 0.4f + random.nextFloat() * 0.8f; // speed
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
        final float time = (float) (EffectRuntime.seconds(context) / 12);

        final float centerX = positionX + tooltipWidth * 0.5f;
        final float centerY = positionY + tooltipHeight * 0.5f;
        final float maxRadius = (float) Math.hypot(tooltipWidth, tooltipHeight) * 0.6f;

        context.flush();
        context.push(() -> {

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
            final Tesselator tess = Tesselator.getInstance();

            // Inner
            renderNebula(clip, tess, pose, centerX, centerY, maxRadius, time);

            // Random particles
            renderParticles(clip, tess, pose, centerX, centerY, maxRadius, time);

            // Spirals
            renderSpirals(clip, tess, pose, centerX, centerY, maxRadius, time);

            // More particles
            renderStars(clip, tess, pose, centerX, centerY, maxRadius, time);

            // Dark core inner
            renderCore2(clip, tess, pose, centerX, centerY, maxRadius, time);

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

    private void renderNebula(EffectClip clip, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time) {

        float pulse = 0.95f + 0.05f * (float) Math.sin(time * Math.PI * 2.0f);

        final float[] radii = {maxRadius * 0.85f * pulse, maxRadius * 0.55f * pulse, maxRadius * 0.25f * pulse};
        final int[] colors = {EffectRuntime.color(0, NEBULA_OUTER), EffectRuntime.color(0, NEBULA_INNER), EffectRuntime.color(0, CORE_COLOR)};

        for (int layer = 0; layer < 3; layer++) {
            final BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

            float radius = radii[layer];
            int color = colors[layer];

            int alpha = layer == 0 ? 120 : (layer == 1 ? 180 : 220);

            bufferBuilder.addVertex(pose, centerX, centerY, 0)
                    .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), EffectRuntime.alpha(alpha));

            for (int i = 0; i <= SEGMENTS; i++) {
                final float along = i / (float) SEGMENTS;
                float angle = along * (float) Math.PI * 2.0f;

                final float noise = 0.15f * (float) Math.sin(angle * 3.0f + time * Math.PI * 1.5f) * (float) Math.cos(angle * 5.0f - time * Math.PI * 2.0f);
                final float localRadius = radius * (1.0f + noise);

                float x = centerX + (float) Math.cos(angle) * localRadius;
                float y = centerY + (float) Math.sin(angle) * localRadius;

                bufferBuilder.addVertex(pose, x, y, 0)
                        .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), 0);
            }

            clip.draw(bufferBuilder.build());
        }

    }

    private void renderParticles(EffectClip clip, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time) {
        final BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        EffectCanvas canvas = new EffectCanvas(bufferBuilder, pose, centerX - maxRadius, centerY - maxRadius, maxRadius * 2, maxRadius * 2, time * 12.0, 1);
        for (int index = 0; index < Math.round(DUST_COUNT * EffectRuntime.density()); index++) {
            final float[] dust = DUST_PARTICLES[index % DUST_PARTICLES.length];

            float angle = dust[0] + (index / DUST_PARTICLES.length) * 2.399963f + time * dust[3] * EffectCanvas.TAU;
            float radius = maxRadius * dust[1] * (0.8f + 0.2f * EffectCanvas.sin(time * Math.PI * 4 + dust[0]));

            float x = centerX + EffectCanvas.cos(angle) * radius;
            float y = centerY + EffectCanvas.sin(angle) * radius;

            float twinkle = 0.35f + 0.65f * EffectCanvas.bell(EffectCanvas.cycle(time * 1.5 + dust[0]));
            canvas.glow(x, y, dust[2] * 1.4f, EffectRuntime.color(1, DUST_COLOR), twinkle * (1 - dust[1] * 0.3f) * 0.6f);
        }

        clip.draw(bufferBuilder.build());
    }

    private void renderSpirals(EffectClip clip, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time) {
        final BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        EffectCanvas canvas = new EffectCanvas(bufferBuilder, pose, centerX - maxRadius, centerY - maxRadius, maxRadius * 2, maxRadius * 2, time * 12.0, 1);
        final int arms = (int) parameter(ARM_COUNT);
        for (int arm = 0; arm < arms; arm++) {
            final float offset = (float) (arm * Math.PI * 2 / arms);
            canvas.ribbon(112, along -> {
                float radius = maxRadius * (0.2f + along * 0.7f);

                float angle = offset + along * (float) Math.PI * 3 * parameter(TWIST) - time * (float) Math.PI * 0.5f;
                angle += 0.08f * EffectCanvas.sin(along * Math.PI * 5 + time * Math.PI * 3);

                float thickness = maxRadius * 0.05f * (1 - along * 0.6f) * (0.7f + 0.3f * EffectCanvas.sin(along * Math.PI * 3 + time * Math.PI * 4));
                final float brightness = 0.5f + 0.5f * EffectCanvas.sin(along * Math.PI * 4 + time * Math.PI * 5);
                final float fade = (float) Math.pow(1 - along, 1.2) * EffectCanvas.smooth(along / 0.07f);

                return new EffectCanvas.Knot(centerX + EffectCanvas.cos(angle) * radius, centerY + EffectCanvas.sin(angle) * radius, thickness * 1.5f, EffectRuntime.color(1, SPIRAL_COLOR), fade * brightness);
            });

        }

        clip.draw(bufferBuilder.build());
    }

    private void renderStars(EffectClip clip, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time) {
        final BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        EffectCanvas canvas = new EffectCanvas(bufferBuilder, pose, centerX - maxRadius, centerY - maxRadius, maxRadius * 2, maxRadius * 2, time * 12.0, 1);
        for (int i = 0; i < Math.round(STARS.length * EffectRuntime.density()); i++) {
            final float[] star = STARS[i % STARS.length];

            final float angle = star[0] + (i / STARS.length) * 2.399963f + time * star[3] * EffectCanvas.TAU;
            final float radius = maxRadius * star[1];

            final float x = centerX + EffectCanvas.cos(angle) * radius;
            final float y = centerY + EffectCanvas.sin(angle) * radius;

            float twinkle = 1 - parameter(TWINKLE_DEPTH) * 0.45f + 0.45f * parameter(TWINKLE_DEPTH) * EffectCanvas.sin((time * 5 * parameter(TWINKLE_SPEED) + star[0] * 10) * Math.PI);

            final float size = star[2] * (0.85f + twinkle * 0.15f);
            final float alpha = (0.25f + twinkle * 0.6f) * (1 - star[1] * 0.2f);

            final int color = EffectCanvas.mix(EffectRuntime.color(2, STAR_COLOR), 0xFFD6D0FA, EffectCanvas.seed(i, 21) * 0.65f);
            if (i % 3 == 0) {
                final int points = 4 + i % 4;
                canvas.star(x, y, size * (i % 9 == 0 ? 2.7f : 1.7f), points, 0.24f + (i % 2) * 0.1f, star[0] + time * 0.2f, color, alpha * 0.85f);
            }
            else {
                canvas.glow(x, y, size * 1.35f, color, alpha * 0.65f);
            }

        }

        clip.draw(bufferBuilder.build());
    }

    private void renderCore2(EffectClip clip, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float maxRadius, float time) {
        final float pulse = 0.92f + 0.08f * (float) Math.sin(time * Math.PI * 3.0f);
        final float coreRadius = maxRadius * 0.12f * pulse;

        final BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        EffectCanvas canvas = new EffectCanvas(bufferBuilder, pose, centerX - maxRadius, centerY - maxRadius, maxRadius * 2, maxRadius * 2, time * 12.0, 1);

        canvas.glow(centerX, centerY, coreRadius * 1.9f, EffectRuntime.color(0, CORE_COLOR), 0.85f);

        clip.draw(bufferBuilder.build());
    }

}
