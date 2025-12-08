package dev.xylonity.tooltipoverhaul.client.style.effect;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.xylonity.tooltipoverhaul.client.layer.impl.EffectLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.Deque;

public class SonarEffect implements EffectLayer {

    private static final int RINGS = 1;
    private static final long PER_PULSE = 1200L;
    private static final int LIFETIME = 1400;
    private static final float THICKNESS = 10f;
    private static final float GLOW = 2.6f;
    private static final int COLOR = 0x88A0D8FF;

    private static long lastSpawn = 0;
    private static final Deque<Pulse> pulses = new ArrayDeque<>();

    @Override
    public void render(TooltipContext context, Vec2 position) {
        int positionX = (int) position.x;
        int positionY = (int) position.y;
        int tooltipWidth = (int) context.getTooltipSize().x;
        int tooltipHeight = (int) context.getTooltipSize().y;

        long now = System.currentTimeMillis();
        float centerX = positionX + tooltipWidth * 0.5f;
        float centerY = positionY + tooltipHeight * 0.5f;

        if (now - lastSpawn >= PER_PULSE && pulses.size() < RINGS) {
            pulses.addLast(new Pulse(now, LIFETIME, (float) Math.hypot(tooltipWidth, tooltipHeight) * 0.6f, 0, COLOR));
            lastSpawn = now;
        }

        context.push(() -> {
            context.getGraphics().enableScissor(
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
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            pulses.removeIf(p -> !p.render(context.getPose().last().pose(), now, centerX, centerY));

            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            RenderSystem.disableBlend();

            context.getGraphics().disableScissor();
        });

    }

    private static void draw(Matrix4f pose, float cx, float cy, float innerR, float outerR, int r, int g, int b, int alphaPeak) {
        if (outerR <= 1f) {
            return;
        }

        if (innerR < 0f) {
            innerR = 0f;
        }
        if (outerR - innerR <= 0.5f) {
            outerR = innerR + 0.5f;
        }

        int segments = Math.max(16, (int) (outerR * 0.8f));
        float midR = innerR + (outerR - innerR) * 0.5f;

        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // inner
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= segments; i++) {
            double rot = i * (Math.PI * 2.0 / segments);
            float function = (float) Math.cos(rot), sin = (float) Math.sin(rot);

            buf.vertex(pose, cx + function * midR, cy + sin * midR, 0)
                    .color(r, g, b, alphaPeak)
                    .endVertex();
            buf.vertex(pose, cx + function * innerR, cy + sin * innerR, 0)
                    .color(r, g, b, 0)
                    .endVertex();
        }

        BufferUploader.drawWithShader(buf.end());

        // outer
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= segments; i++) {
            double rot = i * (Math.PI * 2.0 / segments);
            float function = (float) Math.cos(rot), sin = (float) Math.sin(rot);

            buf.vertex(pose, cx + function * outerR, cy + sin * outerR, 0)
                    .color(r, g, b, 0)
                    .endVertex();
            buf.vertex(pose, cx + function * midR, cy + sin * midR, 0)
                    .color(r, g, b, alphaPeak)
                    .endVertex();
        }

        BufferUploader.drawWithShader(buf.end());
    }

    private record Pulse(long start, int lifetime, float radiusS, float radiusE, int color) {

        boolean render(Matrix4f pose, long now, float centerX, float centerY) {
            float time = (now - start) / (float) lifetime;
            if (time >= 1f) return false;

            float radius = AnimationUtils.lerp(radiusS, radiusE, AnimationUtils.easeInOutCubic(time));

            float innerCore = Math.max(0f, radius - THICKNESS * 0.5f);
            float innerGlow = Math.max(0f, radius - (THICKNESS * 0.5f * GLOW));
            float outerGlow = Math.max(innerGlow + 0.5f, radius + (THICKNESS * 0.5f * GLOW));

            if (outerGlow <= 1f) {
                return true;
            }

            int red = (color >>> 16) & 0xFF;
            int green = (color >>> 8) & 0xFF;
            int blue = color & 0xFF;
            float alpha = (float) Math.sin(Math.PI * AnimationUtils.clamp01(time));

            // Glow
            draw(pose, centerX, centerY, innerGlow, outerGlow, red, green, blue, (int) (((int) (alpha * 90)) * 0.65f));

            // core
            draw(pose, centerX, centerY, innerCore, Math.max(innerCore + 0.5f, radius + THICKNESS * 0.5f), red, green, blue, (int) (alpha * 170));

            return true;
        }

    }

}
