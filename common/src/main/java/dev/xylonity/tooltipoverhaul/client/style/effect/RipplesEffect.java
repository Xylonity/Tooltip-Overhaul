package dev.xylonity.tooltipoverhaul.client.style.effect;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipEffect;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec2;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class RipplesEffect implements ITooltipEffect {

    private static final int RIPPLES = 6;
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

    private final Deque<Ripple> ripples = new ArrayDeque<>();
    private final int[] colors;
    private long lastSpawn = 0;

    public RipplesEffect() {
        this(DEFAULT_COLORS);
    }

    public RipplesEffect(int[] rippleColors) {
        this.colors = rippleColors == null || rippleColors.length == 0 ? DEFAULT_COLORS : rippleColors.clone();
    }

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        final int guiX = (int) pos.x;
        final int guiY = (int) pos.y;
        final int guiW = size.x;
        final int guiH = size.y;
        final int x = (int) pos.x;
        final int y = (int) pos.y;
        final int w = size.x;
        final int h = size.y;

        if (w <= 2 || h <= 2) return;

        ctx.push(() -> {
            ctx.graphics().enableScissor(guiX - 3, guiY - 3, guiX + guiW + 3, guiY + guiH + 3);
            ctx.translate(0, 0, depth.getZ());

            long now = System.currentTimeMillis();

            if (now - lastSpawn >= PER_RIPPLE && ripples.size() < RIPPLES) {
                spawnRipple(x, y, w, h, now);
                lastSpawn = now;
            }

            GlStateManager._enableBlend();
            GlStateManager._blendFuncSeparate(770, 1, 1, 771);

            ripples.removeIf(r -> !r.updateAndRender(ctx, now));

            GlStateManager._blendFuncSeparate(770, 771, 770, 771);
            GlStateManager._disableBlend();

            ctx.graphics().disableScissor();
        });

    }

    private void spawnRipple(int x, int y, int w, int h, long now) {
        Random random = new Random();
        float margin = 10f;
        float cx = x + margin + random.nextFloat() * (w - 2 * margin);
        float cy = y + margin + random.nextFloat() * (h - 2 * margin);

        float speed = lerp(0.75f, 1.35f, random.nextFloat());
        float thickness = THICKNESS * lerp(0.85f, 1.35f, random.nextFloat());
        float tweakingAmount = random.nextFloat() * (float) (Math.PI * 2);

        if (ripples.size() >= RIPPLES) {
            ripples.pollFirst();
        }

        ripples.addLast(new Ripple(cx, cy, 0.5f * (float) Math.hypot(w, h), colors[random.nextInt(colors.length)], now, speed, thickness, tweakingAmount));
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float easeOutCubic(float t) {
        float u = 1f - t;
        return 1f - u * u * u;
    }

    private static final class Ripple {
        private final float cx;
        private final float cy;
        private final float maxRadius;
        private final int baseColor;
        private final long birth;
        private final float speed;
        private final float thickness;
        private final float tweaking;

        public Ripple(float cx, float cy, float maxRadius, int baseColor, long birth, float speed, float thickness, float tweaking) {
            this.cx = cx; this.cy = cy;
            this.maxRadius = maxRadius;
            this.baseColor = baseColor;
            this.birth = birth;
            this.speed = speed;
            this.thickness = thickness;
            this.tweaking = tweaking;
        }

        boolean updateAndRender(TooltipContext ctx, long nowMs) {
            float lifetime = Math.max(0f, nowMs - birth);

            float duration = Math.max(800f, 1200f * (maxRadius / 120f));
            float time = Math.min(1f, lifetime / duration);
            float eased = easeOutCubic(time);

            float rad = eased * maxRadius * speed;

            float alpha = clamp01(1.2f - time * 1.2f) * 0.85f;

            // main halo
            draw(ctx, cx, cy, rad - thickness * 0.5f, rad + thickness * 0.5f, baseColor, alpha, tweaking, nowMs);

            // external halo
            draw(ctx, cx, cy, rad + thickness * 0.4f, rad + thickness * (0.4f + GLOW), baseColor, alpha * 0.55f, tweaking + 1.3f, nowMs);

            return rad < (maxRadius + thickness * (0.6f + GLOW));
        }

        private void draw(TooltipContext ctx, float cx, float cy, float innerR, float outerR, int color, float alphaPeak, float wobblePhase, long now) {

            if (outerR <= 1f) return;
            if (innerR < 0f) innerR = 0f;
            if (outerR - innerR <= 0.5f) outerR = innerR + 0.5f;

            int a = (color >>> 24) & 0xFF;
            int r = (color >>> 16) & 0xFF;
            int g = (color >>> 8) & 0xFF;
            int b = color & 0xFF;

            float alphaNorm = clamp01(alphaPeak) * (a / 255f);

            float time = now / 1000f;
            float tweaking = (float) Math.toRadians(TWEAKING_MULTIPLIER) * (float) Math.sin(2 * Math.PI * TWEAKING * time + wobblePhase);

            Tesselator tesselator = Tesselator.getInstance();

            int segments = Math.max(RING_SEGMENTS, (int) (outerR * 2f));
            float midR = innerR + (outerR - innerR) * 0.5f;

            BufferBuilder buf = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            int alphaByte = Math.round(alphaNorm * 255f);
            for (int i = 0; i < segments; i++) {

                float th0 = (float) (2 * Math.PI * i / (float) segments);
                float th1 = (float) (2 * Math.PI * (i + 1) / (float) segments);
                float w0 = th0 + tweaking * (float) Math.sin(th0 * 3.0f);
                float w1 = th1 + tweaking * (float) Math.sin(th1 * 3.0f);

                float mx0 = cx + (float) Math.cos(w0) * midR;
                float my0 = cy + (float) Math.sin(w0) * midR;
                float ix0 = cx + (float) Math.cos(w0) * innerR;
                float iy0 = cy + (float) Math.sin(w0) * innerR;
                float mx1 = cx + (float) Math.cos(w1) * midR;
                float my1 = cy + (float) Math.sin(w1) * midR;
                float ix1 = cx + (float) Math.cos(w1) * innerR;
                float iy1 = cy + (float) Math.sin(w1) * innerR;

                buf.addVertex(ctx.pose().last().pose(), mx0, my0, 0).setColor(r, g, b, alphaByte);
                buf.addVertex(ctx.pose().last().pose(), ix0, iy0, 0).setColor(r, g, b, 0);
                buf.addVertex(ctx.pose().last().pose(), ix1, iy1, 0).setColor(r, g, b, 0);

                buf.addVertex(ctx.pose().last().pose(), mx0, my0, 0).setColor(r, g, b, alphaByte);
                buf.addVertex(ctx.pose().last().pose(), ix1, iy1, 0).setColor(r, g, b, 0);
                buf.addVertex(ctx.pose().last().pose(), mx1, my1, 0).setColor(r, g, b, alphaByte);
            }
            try (MeshData data = buf.buildOrThrow()) {
                RenderType.dragonRays().draw(data);
            }

            // mid -> out
            buf = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i < segments; i++) {
                float th0 = (float) (2 * Math.PI * i / (float) segments);
                float th1 = (float) (2 * Math.PI * (i + 1) / (float) segments);
                float w0 = th0 + tweaking * (float) Math.sin(th0 * 3.0f);
                float w1 = th1 + tweaking * (float) Math.sin(th1 * 3.0f);

                float ox0 = cx + (float) Math.cos(w0) * outerR;
                float oy0 = cy + (float) Math.sin(w0) * outerR;
                float mx0 = cx + (float) Math.cos(w0) * midR;
                float my0 = cy + (float) Math.sin(w0) * midR;
                float ox1 = cx + (float) Math.cos(w1) * outerR;
                float oy1 = cy + (float) Math.sin(w1) * outerR;
                float mx1 = cx + (float) Math.cos(w1) * midR;
                float my1 = cy + (float) Math.sin(w1) * midR;

                buf.addVertex(ctx.pose().last().pose(), ox0, oy0, 0).setColor(r, g, b, 0);
                buf.addVertex(ctx.pose().last().pose(), mx0, my0, 0).setColor(r, g, b, alphaByte);
                buf.addVertex(ctx.pose().last().pose(), mx1, my1, 0).setColor(r, g, b, alphaByte);

                buf.addVertex(ctx.pose().last().pose(), ox0, oy0, 0).setColor(r, g, b, 0);
                buf.addVertex(ctx.pose().last().pose(), mx1, my1, 0).setColor(r, g, b, alphaByte);
                buf.addVertex(ctx.pose().last().pose(), ox1, oy1, 0).setColor(r, g, b, 0);
            }

            try (MeshData data = buf.buildOrThrow()) {
                RenderType.dragonRays().draw(data);
            }

        }

        private static float clamp01(float v) {
            return v < 0f ? 0f : (Math.min(v, 1f));
        }

    }

}