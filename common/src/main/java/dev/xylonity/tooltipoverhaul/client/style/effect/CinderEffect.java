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
import java.util.Random;

public class CinderEffect implements ITooltipEffect {

    private static final int CINDERS_CAP = 14;
    private static final long PER_PARTICLE = 110;
    private static final float MIN_DISP = -12f;
    private static final float MAX_DISP = -22f;
    private static final float TOKYO_DRIFT = 9f;
    private static final int COLOR_START = 0xCCFFE2A8;
    private static final int COLOR_END = 0x99FFA060;

    private final Deque<Cinder> cinders = new ArrayDeque<>();
    private long lastSpawn = 0L;

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        int x = (int) pos.x;
        int y = (int) pos.y;
        int w = size.x;
        int h = size.y;

        long now = System.currentTimeMillis();

        if (now - lastSpawn >= PER_PARTICLE && cinders.size() < CINDERS_CAP) {
            spawn(x, y, w, h, now);
            lastSpawn = now;
        }

        ctx.push(() -> {
            ctx.graphics().enableScissor(x - 3, y - 3, x + w + 3, y + h + 3);

            ctx.translate(0, 0, depth.getZ());

            RenderSystem.enableBlend();

            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE
            );

            cinders.removeIf(c -> !c.render(ctx, 1f/60f, now));

            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );

            RenderSystem.disableBlend();

            ctx.graphics().disableScissor();
        });

    }

    private void spawn(int x, int y, int w, int h, long now) {
        Random random = new Random();
        float sx = x + 4 + random.nextFloat() * (w - 8);
        float sy = y + h - 2 - random.nextFloat() * 2f;
        float vel = MIN_DISP + random.nextFloat() * (MAX_DISP - MIN_DISP);
        float life = 450 + random.nextFloat() * 350f;
        float size = 1 + random.nextFloat();
        float accel = (random.nextFloat()*2f - 1f) * TOKYO_DRIFT;
        float rot = random.nextFloat() * (float) Math.PI;

        if (cinders.size() >= CINDERS_CAP) {
            cinders.pollFirst();
        }

        cinders.addLast(new Cinder(sx, sy, vel, accel, size, now, life, rot));
    }

    private static final class Cinder {
        private float x;
        private float y;
        private float vy;
        private float ax;
        private float size;
        private float baseRot;
        private long start;
        private float lifeitme;

        public Cinder(float x, float y, float vy, float ax, float size, long birth, float lifeitme, float baseRot) {
            this.x = x;
            this.y = y;
            this.vy = vy;
            this.ax = ax;
            this.size = size;
            this.start = birth;
            this.lifeitme = lifeitme;
            this.baseRot = baseRot;
        }

        boolean render(TooltipContext ctx, float dt, long now) {
            float age = now - start;
            if (age >= lifeitme) return false;

            x += ax * dt * 0.25f;
            y += vy * dt;

            float k = age / lifeitme;
            float drop = 0.85f + 0.15f * (float) Math.sin(now * 0.02f + x * 0.1f);
            int startt = (int) ((1f - k) * 200 * drop);
            int endd = (int) ((1f - k) * 120 * drop);

            if (startt <= 0 && endd <= 0) return true;

            float rot = baseRot + (float)Math.sin((start * 0.0017) + (now * 0.0011) + x * 0.03f) * 0.15f;

            Matrix4f pose = ctx.pose().last().pose();

            float length = (2.5f + size * 2.2f) * 0.35f;
            float thick = 0.9f + size * 0.7f;
            float lenGlow = length * 1.35f;
            float thickGlow = thick * 1.35f;

            // glow (end color)
            int rw = (COLOR_END >>> 16) & 0xFF;
            int gw = (COLOR_END >>> 8 ) & 0xFF;
            int bw = COLOR_END & 0xFF;
            drawStar(pose, x, y, lenGlow, thickGlow, rot, rw, gw, bw, endd);

            // core (start color)
            int rh = (COLOR_START >>> 16) & 0xFF;
            int gh = (COLOR_START >>> 8 ) & 0xFF;
            int bh = COLOR_START & 0xFF;
            drawStar(pose, x, y, length, thick, rot, rh, gh, bh, startt);

            return true;
        }

    }

    private static void drawStar(Matrix4f pose, float cx, float cy, float len, float thickness, float rot, int r, int g, int b, int a) {
        for (int i = 0; i < 4; i++) {
            float rot2 = rot + i * (float) (Math.PI * 0.5);
            drawSegment(pose, cx, cy, cx + (float) Math.cos(rot2) * len, cy + (float) Math.sin(rot2) * len, thickness, Math.max(0.6f, thickness * 0.6f), r, g, b, a);
        }

    }

    private static void drawSegment(Matrix4f pose, float x1, float y1, float x2, float y2, float tStart, float tEnd, int r, int g, int b, int a) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.max(0.001, Math.hypot(dx, dy));
        float nx = -dy / len;
        float ny = dx / len;

        float hsx = nx * tStart * 0.5f;
        float hsy = ny * tStart * 0.5f;
        float hex = nx * tEnd * 0.5f;
        float hey = ny * tEnd * 0.5f;

        BufferBuilder buf = Tesselator.getInstance().getBuilder();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(pose, x1 - hsx, y1 - hsy, 0).color(r, g, b, a).endVertex();
        buf.vertex(pose, x1 + hsx, y1 + hsy, 0).color(r, g, b, a).endVertex();
        buf.vertex(pose, x2 - hex, y2 - hey, 0).color(r, g, b, a).endVertex();
        buf.vertex(pose, x2 + hex, y2 + hey, 0).color(r, g, b, a).endVertex();

        BufferUploader.drawWithShader(buf.end());
    }

}
