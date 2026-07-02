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

public class BubblesEffect implements EffectLayer {

    private static final int BUBBLE_COLOR = 0xFFBFE8FF;
    private static final int SHINE_COLOR = 0xFFFFFFFF;

    private static final int BUBBLE_COUNT = 10;
    private static final int RING_SEGMENTS = 14;

    private static final float[][] BUBBLES = new float[BUBBLE_COUNT][6];

    static {
        final Random random = new Random(44444L);
        for (int i = 0; i < BUBBLE_COUNT; i++) {
            BUBBLES[i][0] = random.nextFloat(); // Horizontal seed
            BUBBLES[i][1] = 0.07f + random.nextFloat() * 0.08f; // Rise speed
            BUBBLES[i][2] = 1.4f + random.nextFloat() * 2.2f; // Radius
            BUBBLES[i][3] = 2.0f + random.nextFloat() * 3.5f; // Wobble amplitude
            BUBBLES[i][4] = 1.0f + random.nextFloat() * 1.8f; // Wobble speed
            BUBBLES[i][5] = random.nextFloat(); // Phase
        }

    }

    @Override
    public void render(TooltipContext context, Vec2 position) {
        final int positionX = (int) position.x;
        final int positionY = (int) position.y;
        final int tooltipWidth = (int) context.getTooltipSize().x;
        final int tooltipHeight = (int) context.getTooltipSize().y;

        final long now = System.currentTimeMillis();
        final float time = (now - context.getStartTime()) / 1000f;

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

            for (final float[] bubble : BUBBLES) {
                renderBubble(bufferBuilder, pose, positionX, positionY, tooltipWidth, tooltipHeight, time, bubble);
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

    private void renderBubble(BufferBuilder bufferBuilder, Matrix4f pose, int positionX, int positionY, int tooltipWidth, int tooltipHeight, float time, float[] bubble) {
        final float horizontalSeed = bubble[0];
        final float riseSpeed = bubble[1];
        final float radius = bubble[2];
        final float wobbleAmplitude = bubble[3];
        final float wobbleSpeed = bubble[4];
        final float phase = bubble[5];

        // Looping rise from below the tooltip to above
        final float progress = (time * riseSpeed + phase) % 1f;

        final float x = positionX + horizontalSeed * tooltipWidth + (float) Math.sin(time * wobbleSpeed + phase * (float) Math.PI * 2.0f) * wobbleAmplitude;
        final float y = positionY + tooltipHeight + 12 - progress * (tooltipHeight + 26);

        // Fades in and out at the loop edges
        final float alphaEnvelope = (float) Math.sin(progress * Math.PI);
        if (alphaEnvelope < 0.04f) {
            return;
        }

        final float currentRadius = radius * (0.8f + 0.3f * progress);
        final int ringAlpha = AnimationUtils.clamp255((int) (185 * alphaEnvelope));

        // Thin ring, brightest at its middle so the bubble looks hollow
        final float thickness = Math.max(0.7f, currentRadius * 0.36f);
        final float innerRadius = currentRadius - thickness * 0.5f;
        final float outerRadius = currentRadius + thickness * 0.5f;

        ring(bufferBuilder, pose, x, y, innerRadius, currentRadius, outerRadius, ringAlpha);

        // Small highlight towards the upper left
        final float shineX = x - currentRadius * 0.42f;
        final float shineY = y - currentRadius * 0.42f;
        final float shineRadius = Math.max(0.5f, currentRadius * 0.32f);
        final int shineAlpha = AnimationUtils.clamp255((int) (220 * alphaEnvelope));

        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.vertex(pose, shineX, shineY, 0).color(ColorUtils.red(SHINE_COLOR), ColorUtils.green(SHINE_COLOR), ColorUtils.blue(SHINE_COLOR), shineAlpha).endVertex();
        for (int i = 0; i <= 8; i++) {
            final float angle = (i / 8.0f) * (float) Math.PI * 2.0f;
            bufferBuilder.vertex(pose, shineX + (float) Math.cos(angle) * shineRadius, shineY + (float) Math.sin(angle) * shineRadius, 0)
                    .color(ColorUtils.red(SHINE_COLOR), ColorUtils.green(SHINE_COLOR), ColorUtils.blue(SHINE_COLOR), 0)
                    .endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private void ring(BufferBuilder bufferBuilder, Matrix4f pose, float x, float y, float innerRadius, float midRadius, float outerRadius, int alpha) {
        // Inner half
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= RING_SEGMENTS; i++) {
            float angle = (i / (float) RING_SEGMENTS) * (float) Math.PI * 2.0f;
            float cos = (float) Math.cos(angle), sin = (float) Math.sin(angle);

            bufferBuilder.vertex(pose, x + cos * midRadius, y + sin * midRadius, 0).color(ColorUtils.red(BUBBLE_COLOR), ColorUtils.green(BUBBLE_COLOR), ColorUtils.blue(BUBBLE_COLOR), alpha).endVertex();
            bufferBuilder.vertex(pose, x + cos * innerRadius, y + sin * innerRadius, 0).color(ColorUtils.red(BUBBLE_COLOR), ColorUtils.green(BUBBLE_COLOR), ColorUtils.blue(BUBBLE_COLOR), 0).endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());

        // Outer half
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= RING_SEGMENTS; i++) {
            float angle = (i / (float) RING_SEGMENTS) * (float) Math.PI * 2.0f;
            float cos = (float) Math.cos(angle), sin = (float) Math.sin(angle);

            bufferBuilder.vertex(pose, x + cos * outerRadius, y + sin * outerRadius, 0).color(ColorUtils.red(BUBBLE_COLOR), ColorUtils.green(BUBBLE_COLOR), ColorUtils.blue(BUBBLE_COLOR), 0).endVertex();
            bufferBuilder.vertex(pose, x + cos * midRadius, y + sin * midRadius, 0).color(ColorUtils.red(BUBBLE_COLOR), ColorUtils.green(BUBBLE_COLOR), ColorUtils.blue(BUBBLE_COLOR), alpha).endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

}