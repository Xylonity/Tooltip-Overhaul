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

public class FirefliesEffect implements EffectLayer {

    private static final int CORE_COLOR = 0xFFFFF2B8;
    private static final int GLOW_COLOR = 0xFFD8E86A;

    private static final int FIREFLY_COUNT = 12;
    private static final int GLOW_SEGMENTS = 10;

    private static final float[][] FIREFLIES = new float[FIREFLY_COUNT][6];

    static {
        final Random random = new Random(77777L);
        for (int i = 0; i < FIREFLY_COUNT; i++) {
            FIREFLIES[i][0] = 0.25f + random.nextFloat() * 0.55f; // Horizontal speed
            FIREFLIES[i][1] = 0.20f + random.nextFloat() * 0.50f; // Vertical speed
            FIREFLIES[i][2] = 0.55f + random.nextFloat() * 0.55f; // Horizontal radial seed
            FIREFLIES[i][3] = 0.55f + random.nextFloat() * 0.55f; // Vertical radial seed
            FIREFLIES[i][4] = 0.65f + random.nextFloat() * 0.90f; // Blink speed
            FIREFLIES[i][5] = random.nextFloat() * (float) Math.PI * 2.0f; // Phase
        }

    }

    @Override
    public void render(TooltipContext context, Vec2 position) {
        final int positionX = (int) position.x;
        final int positionY = (int) position.y;
        final int tooltipWidth = (int) context.getTooltipSize().x;
        final int tooltipHeight = (int) context.getTooltipSize().y;

        final long now = System.currentTimeMillis();
        final float time = (now - context.getStartTime()) / 9000f;

        final float centerX = positionX + tooltipWidth * 0.5f;
        final float centerY = positionY + tooltipHeight * 0.5f;
        final float radiusX = tooltipWidth * 0.5f + 14.0f;
        final float radiusY = tooltipHeight * 0.5f + 10.0f;

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
            final BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();

            for (final float[] firefly : FIREFLIES) {
                renderFirefly(bufferBuilder, pose, centerX, centerY, radiusX, radiusY, time, firefly);
            }

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

    private void renderFirefly(BufferBuilder bufferBuilder, Matrix4f pose, float centerX, float centerY, float radiusX, float radiusY, float time, float[] firefly) {
        final float speedX = firefly[0];
        final float speedY = firefly[1];
        final float radialSeedX = firefly[2];
        final float radialSeedY = firefly[3];
        final float blinkSpeed = firefly[4];
        final float phase = firefly[5];

        // Independent horizontal and vertical oscillations trace a wandering path
        final float x = centerX + (float) Math.cos(time * speedX * Math.PI * 14.0f + phase) * radiusX * radialSeedX;
        final float y = centerY + (float) Math.sin(time * speedY * Math.PI * 10.0f + phase * 1.9f) * radiusY * radialSeedY;

        // Cubed sine keeps them dark most of the cycle
        final float blinkBase = (float) Math.sin(time * blinkSpeed * Math.PI * 22.0f + phase);
        final float blink = blinkBase > 0 ? blinkBase * blinkBase * blinkBase : 0.0f;
        if (blink < 0.03f) {
            return;
        }

        // Soft halo
        final float glowRadius = 2.6f + 2.4f * blink;
        final int glowAlpha = AnimationUtils.clamp255((int) (150 * blink));

        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.vertex(pose, x, y, 0).color(ColorUtils.red(GLOW_COLOR), ColorUtils.green(GLOW_COLOR), ColorUtils.blue(GLOW_COLOR), glowAlpha).endVertex();
        for (int i = 0; i <= GLOW_SEGMENTS; i++) {
            final float angle = (i / (float) GLOW_SEGMENTS) * (float) Math.PI * 2.0f;
            bufferBuilder.vertex(pose, x + (float) Math.cos(angle) * glowRadius, y + (float) Math.sin(angle) * glowRadius, 0)
                    .color(ColorUtils.red(GLOW_COLOR), ColorUtils.green(GLOW_COLOR), ColorUtils.blue(GLOW_COLOR), 0)
                    .endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());

        // Bright core
        final float half = 0.8f + 0.5f * blink;
        final int coreAlpha = AnimationUtils.clamp255((int) (255 * blink));

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.vertex(pose, x, y - half, 0).color(ColorUtils.red(CORE_COLOR), ColorUtils.green(CORE_COLOR), ColorUtils.blue(CORE_COLOR), coreAlpha).endVertex();
        bufferBuilder.vertex(pose, x + half, y, 0).color(ColorUtils.red(CORE_COLOR), ColorUtils.green(CORE_COLOR), ColorUtils.blue(CORE_COLOR), coreAlpha).endVertex();
        bufferBuilder.vertex(pose, x, y + half, 0).color(ColorUtils.red(CORE_COLOR), ColorUtils.green(CORE_COLOR), ColorUtils.blue(CORE_COLOR), coreAlpha).endVertex();
        bufferBuilder.vertex(pose, x - half, y, 0).color(ColorUtils.red(CORE_COLOR), ColorUtils.green(CORE_COLOR), ColorUtils.blue(CORE_COLOR), coreAlpha).endVertex();

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

}
