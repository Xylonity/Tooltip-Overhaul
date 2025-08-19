package dev.xylonity.tooltipoverhaul.client.style.effect;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipEffect;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;

public class SonarEffect implements ITooltipEffect {

    private static final int RINGS = 1;
    private static final long PER_PULSE = 1200L;
    private static final int LIFETIME = 1400;
    private static final float THICKNESS = 10f;
    private static final float GLOW = 2.6f;
    private static final int color = 0x88A0D8FF;

    private long lastSpawn = 0;
    private final Deque<Pulse> pulses = new ArrayDeque<>();

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        int x = (int) pos.x;
        int y = (int) pos.y;
        int w = size.x;
        int h = size.y;

        if (w <= 2 || h <= 2) return;

        long now = System.currentTimeMillis();
        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f;

        if (now - lastSpawn >= PER_PULSE && pulses.size() < RINGS) {
            pulses.addLast(new Pulse(now, LIFETIME, (float) Math.hypot(w, h) * 0.6f, 0, color));
            lastSpawn = now;
        }

        ctx.push(() -> {
            ctx.graphics().enableScissor(x - 3, y - 3, x + w + 3, y + h + 3);
            ctx.translate(0, 0, depth.getZ());

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            pulses.removeIf(p -> !p.render(ctx.pose().last().pose(), now, cx, cy));

            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            RenderSystem.disableBlend();

            ctx.graphics().disableScissor();
        });
    }

    private static void draw(Matrix4f pose, float cx, float cy, float innerR, float outerR, int r, int g, int b, int alphaPeak) {
        if (outerR <= 1f) return;
        if (innerR < 0f) innerR = 0f;
        if (outerR - innerR <= 0.5f) outerR = innerR + 0.5f;

        int segments = Math.max(16, (int) (outerR * 0.8f));
        float midR = innerR + (outerR - innerR) * 0.5f;

        Tesselator tesselator = Tesselator.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // inner
        BufferBuilder buf = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double rot = i * (Math.PI * 2.0 / segments);
            float func = (float) Math.cos(rot), sin = (float) Math.sin(rot);

            buf.addVertex(pose, cx + func * midR, cy + sin * midR, 0).setColor(r, g, b, alphaPeak);
            buf.addVertex(pose, cx + func * innerR, cy + sin * innerR, 0).setColor(r, g, b, 0);
        }

        try (MeshData data = buf.buildOrThrow()) {
            BufferUploader.drawWithShader(data);
        }

        // outer
        buf = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double rot = i * (Math.PI * 2.0 / segments);
            float func = (float) Math.cos(rot), sin = (float) Math.sin(rot);

            buf.addVertex(pose, cx + func * outerR, cy + sin * outerR, 0).setColor(r, g, b, 0);
            buf.addVertex(pose, cx + func * midR, cy + sin * midR, 0).setColor(r, g, b, alphaPeak);
        }

        try (MeshData data = buf.buildOrThrow()) {
            BufferUploader.drawWithShader(data);
        }
    }

    private record Pulse(long start, int lifetime, float radiusS, float radiusE, int color) {

        boolean render(Matrix4f pose, long now, float cx, float cy) {
                float time = (now - start) / (float) lifetime;
                if (time >= 1f) return false;

                float radius = lerp(radiusS, radiusE, easeInOutCubic(time));

                float innerCore = Math.max(0f, radius - THICKNESS * 0.5f);
                float innerGlow = Math.max(0f, radius - (THICKNESS * 0.5f * GLOW));
                float outerGlow = Math.max(innerGlow + 0.5f, radius + (THICKNESS * 0.5f * GLOW));

                if (outerGlow <= 1f) {
                    return true;
                }

                int red = (color >>> 16) & 0xFF;
                int green = (color >>> 8) & 0xFF;
                int blue = color & 0xFF;
                float alpha = (float) Math.sin(Math.PI * clamp(time));

                // Glow
                draw(pose, cx, cy, innerGlow, outerGlow, red, green, blue, (int) (((int) (alpha * 90)) * 0.65f));
                // core
                draw(pose, cx, cy, innerCore, Math.max(innerCore + 0.5f, radius + THICKNESS * 0.5f), red, green, blue, (int) (alpha * 170));

                return true;
            }

            private static float lerp(float a, float b, float t) {
                return a + (b - a) * t;
            }

            private static float clamp(float v) {
                return v < 0f ? 0f : (Math.min(v, 1f));
            }

            private static float easeInOutCubic(float t) {
                return t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
            }

        }

}
