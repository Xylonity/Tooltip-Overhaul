package dev.xylonity.tooltipoverhaul.client.style.effect;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.xylonity.tooltipoverhaul.client.layer.impl.EffectLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class RipplesEffect implements EffectLayer {

    private static final int RIPPLE_AMOUNT = 4;
    private static final int RING_SEGMENTS = 48;
    private static final float THICKNESS = 8f;
    private static final float GLOW = 1.75f;
    private static final float TWEAKING = 1.2f;
    private static final float TWEAKING_MULTIPLIER = 10f;
    private static final long PER_RIPPLE = 220;

    private static final int[] DEFAULT_COLORS = {
            0x66FFFFFF,
            0x88A0D8FF,
            0x66FFD6FF
    };

    private static final Deque<Ripple> RIPPLES = new ArrayDeque<>();
    private static long lastSpawn = 0;

    @Override
    public void render(TooltipContext context, Vec2 position) {
        int positionX = (int) position.x;
        int positionY = (int) position.y;
        int tooltipWidth = (int) context.getTooltipSize().x;
        int tooltipHeight = (int) context.getTooltipSize().y;

        context.push(() -> {
            context.enableScissor(
                    positionX - context.getPaddingX() - 1,
                    positionY - context.getPaddingY(),
                    positionX + tooltipWidth + context.getPaddingX(),
                    positionY + tooltipHeight + context.getPaddingY()
            );
            //context.translate(0, 0, context.getLayerDepth().getZ());

            long now = System.currentTimeMillis();
            if (now - lastSpawn >= PER_RIPPLE && RIPPLES.size() < RIPPLE_AMOUNT) {
                spawnRipple(positionX, positionY, tooltipWidth, tooltipHeight, now);
                lastSpawn = now;
            }

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );

            RIPPLES.removeIf(r -> !r.updateAndRender(context, now));

            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            RenderSystem.disableBlend();

            context.getGraphics().disableScissor();
        });

    }

    private void spawnRipple(int x, int y, int width, int height, long now) {
        Random random = new Random();
        float margin = 10f;
        float centerX = x + margin + random.nextFloat() * (width - 2 * margin);
        float centerY = y + margin + random.nextFloat() * (height - 2 * margin);

        float speed = AnimationUtils.lerp(0.75f, 1.35f, random.nextFloat());
        float thickness = THICKNESS * AnimationUtils.lerp(0.85f, 1.35f, random.nextFloat());
        float tweakingAmount = random.nextFloat() * (float) (Math.PI * 2);

        if (RIPPLES.size() >= RIPPLE_AMOUNT) {
            RIPPLES.pollFirst();
        }

        RIPPLES.addLast(new Ripple(centerX, centerY, 0.5f * (float) Math.hypot(width, height), DEFAULT_COLORS[random.nextInt(DEFAULT_COLORS.length)], now, speed, thickness, tweakingAmount));
    }

    private record Ripple(float centerX, float centerY, float maxRadius, int baseColor, long birth, float speed, float thickness, float tweaking) {

        boolean updateAndRender(TooltipContext context, long nowMs) {
                float lifetime = Math.max(0f, nowMs - birth);

                // animation duration
                float duration = Math.max(800f, 1200f * (maxRadius / 120f));

                float t = lifetime / duration;
                float clamped = Math.min(1f, t);

                float eased = AnimationUtils.easeOutCubic(clamped);
                float rad = eased * maxRadius * speed;

                float alpha = AnimationUtils.clamp01(1.2f - clamped * 1.2f) * 0.85f;

                // main halo
                draw(context, centerX, centerY, rad - thickness * 0.5f, rad + thickness * 0.5f, baseColor, alpha, tweaking, nowMs);

                // external halo
                draw(context, centerX, centerY, rad + thickness * 0.4f, rad + thickness * (0.4f + GLOW), baseColor, alpha * 0.55f, tweaking + 1.3f, nowMs);

                return t < 1.05f;
            }

            private void draw(TooltipContext context, float centerX, float centerY, float innerR, float outerR, int color, float alphaPeak, float wobblePhase, long now) {

                if (outerR <= 1f) {
                    return;
                }

                if (innerR < 0f) {
                    innerR = 0f;
                }
                if (outerR - innerR <= 0.5f) {
                    outerR = innerR + 0.5f;
                }

                int alpha = (color >>> 24) & 0xFF;
                int red = (color >>> 16) & 0xFF;
                int green = (color >>> 8) & 0xFF;
                int blue = color & 0xFF;

                float alphaNorm = AnimationUtils.clamp01(alphaPeak) * (alpha / 255f);

                float time = now / 1000f;
                float tweaking = (float) Math.toRadians(TWEAKING_MULTIPLIER) * (float) Math.sin(2 * Math.PI * TWEAKING * time + wobblePhase);

                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buf = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

                RenderSystem.setShader(GameRenderer::getPositionColorShader);

                int segments = Math.max(16, RING_SEGMENTS);
                float midR = innerR + (outerR - innerR) * 0.5f;

                // inner -> mid
                for (int i = 0; i <= segments; i++) {
                    float theta = (float) (2 * Math.PI * (i / (float) segments));
                    float w = theta + tweaking * (float) Math.sin(theta * 3.0f);
                    float func1 = (float) Math.sin(w);
                    float func2 = (float) Math.cos(w);

                    buf.addVertex(context.getPose().last().pose(), centerX + func2 * midR, centerY + func1 * midR, 0).setColor(red, green, blue, (int) (alphaNorm * 255f));
                    buf.addVertex(context.getPose().last().pose(), centerX + func2 * innerR, centerY + func1 * innerR, 0).setColor(red, green, blue, 0);
                }

                try (MeshData data = buf.buildOrThrow()) {
                    BufferUploader.drawWithShader(data);
                }

                // mid -> out
                BufferBuilder buf2 = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
                for (int i = 0; i <= segments; i++) {
                    float theta = (float) (2 * Math.PI * (i / (float) segments));
                    float w = theta + tweaking * (float) Math.sin(theta * 3.0f);
                    float func1 = (float) Math.sin(w);
                    float func2 = (float) Math.cos(w);

                    buf2.addVertex(context.getPose().last().pose(), centerX + func2 * outerR, centerY + func1 * outerR, 0).setColor(red, green, blue, 0);
                    buf2.addVertex(context.getPose().last().pose(), centerX + func2 * midR, centerY + func1 * midR, 0).setColor(red, green, blue, (int) (alphaNorm * 255f));
                }

                try (MeshData data = buf2.buildOrThrow()) {
                    BufferUploader.drawWithShader(data);
                }
            }

        }

}