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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class CinderEffect implements EffectLayer {

    private static final int CINDERS_CAP = 18;
    private static final long PER_PARTICLE = 110L;

    private static final float MIN_DISP = -1;
    private static final float MAX_DISP = -4;
    private static final float TOKYO_DRIFT = 8f;

    private static final int COLOR_START = 0xCCFFE2A8;
    private static final int COLOR_END = 0x99FFA060;

    private static final Deque<Cinder> CINDERS = new ArrayDeque<>();
    private static long lastSpawn = 0L;

    @Override
    public void render(TooltipContext context, Vec2 position) {
        int positionX = (int) position.x;
        int positionY = (int) position.y;
        int tooltipWidth = (int) context.getTooltipSize().x;
        int tooltipHeight = (int) context.getTooltipSize().y;

        long now = System.currentTimeMillis();
        if (now - lastSpawn >= PER_PARTICLE && CINDERS.size() < CINDERS_CAP) {
            spawn(tooltipWidth, tooltipHeight, now);
            lastSpawn = now;
        }

        context.push(() -> {
            context.enableScissor(
                    positionX - context.getPaddingX() - 1,
                    positionY - context.getPaddingY(),
                    positionX + tooltipWidth + context.getPaddingX(),
                    positionY + tooltipHeight + context.getPaddingY()
            );

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE
            );

            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            Matrix4f pose = context.getPose().last().pose();

            CINDERS.removeIf(c -> !c.render(pose, 1 / 60f, now, positionX, positionY, tooltipWidth, tooltipHeight));

            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            RenderSystem.disableBlend();

            context.getGraphics().disableScissor();
        });

    }

    private static void spawn(int w, int h, long now) {
        Random random = new Random();
        float marginX = 6f;

        float localX = marginX + random.nextFloat() * Math.max(2f, w - 2f * marginX);
        float bottomY = h - 3f;
        float localY = bottomY - random.nextFloat() * 4f + 5;

        float vy = MIN_DISP + random.nextFloat() * (MAX_DISP - MIN_DISP);
        float life = 450f + random.nextFloat() * 350f;
        float size = 0.8f + random.nextFloat() * 1.2f;
        float ax = (random.nextFloat() * 2f - 1f) * TOKYO_DRIFT;
        float rot = random.nextFloat() * (float) Math.PI;
        float twinklePhase = random.nextFloat() * (float) (Math.PI * 2.0);

        if (CINDERS.size() >= CINDERS_CAP) {
            CINDERS.pollFirst();
        }

        CINDERS.addLast(new Cinder(localX, localY, vy, ax, size, now, life, rot, twinklePhase));
    }

    private static final class Cinder {

        private float localX;
        private float localY;

        private final float vy;
        private final float ax;
        private final float size;
        private final float baseRot;
        private final long start;
        private final float lifetime;
        private final float twinklePhase;

        Cinder(float localX, float localY, float vy, float ax, float size, long start, float lifetime, float baseRot, float twinklePhase) {
            this.localX = localX;
            this.localY = localY;
            this.vy = vy;
            this.ax = ax;
            this.size = size;
            this.start = start;
            this.lifetime = lifetime;
            this.baseRot = baseRot;
            this.twinklePhase = twinklePhase;
        }

        boolean render(Matrix4f pose, float dt, long now, float tooltipX, float tooltipY, float tooltipW, float tooltipH) {

            float age = now - start;
            if (age >= lifetime) {
                return false;
            }

            localX += ax * dt * 0.25f;
            localY += vy * dt;

            float topLimit = tooltipH * 0.1f;
            if (localY < topLimit) {
                return false;
            }

            float bottomFactor = (localY - topLimit) / Math.max(1f, (tooltipH - topLimit));
            bottomFactor = AnimationUtils.clamp01(bottomFactor);

            float k = AnimationUtils.clamp01(age / lifetime);

            float lifeFade = (float) Math.pow(1f - k, 1.3f);
            float verticalFade = 0.4f + 0.6f * bottomFactor;
            float visibility = lifeFade * verticalFade;
            if (visibility <= 0f) {
                return false;
            }

            float twinkle = 0.85f + 0.15f * (float) Math.sin((now - start) * 0.006f + twinklePhase);
            float drop = 0.75f + 0.25f * (float) Math.sin(now * 0.015f + start * 0.003f);

            int alphaCore = (int) (210 * visibility * twinkle * drop);
            int alphaGlow = (int) (140 * visibility * twinkle * drop);

            if (alphaCore <= 0 && alphaGlow <= 0) {
                return true;
            }

            float colorT = 0.25f + 0.75f * (1f - k);
            int coreColor = ColorUtils.lerpColor(COLOR_END, COLOR_START, colorT);
            int glowColor = ColorUtils.lerpColor(COLOR_END, COLOR_START, 0.5f * colorT);

            int cr = (coreColor >>> 16) & 0xFF;
            int cg = (coreColor >>> 8) & 0xFF;
            int cb = coreColor & 0xFF;

            int gr = (glowColor >>> 16) & 0xFF;
            int gg = (glowColor >>> 8) & 0xFF;
            int gb = glowColor & 0xFF;

            float rot = baseRot + (float) Math.sin((start * 0.0017f) + (now * 0.0011f) + localX * 0.03f) * 0.18f;

            float baseLen = 0f + size * 2.2f;
            float coreThick = 1f + size * 0.55f;
            float glowThick = coreThick * 2f;
            float lenGlow = baseLen * 1.2f;

            float cx = tooltipX + localX;
            float cy = tooltipY + localY;

            // halo
            drawStar(pose, cx, cy, lenGlow, glowThick, rot, gr, gg, gb, alphaGlow);
            // internal
            drawStar(pose, cx, cy, baseLen, coreThick, rot, cr, cg, cb, alphaCore);

            return true;
        }

    }

    private static void drawStar(Matrix4f pose, float cx, float cy, float len, float thickness, float rot, int r, int g, int b, int a) {
        for (int i = 0; i < 4; i++) {
            float rot2 = rot + i * (float) (Math.PI * 0.5);

            float x2 = cx + (float) Math.cos(rot2) * len;
            float y2 = cy + (float) Math.sin(rot2) * len;

            drawSegment(pose, cx, cy, x2, y2, thickness, Math.max(0.6f, thickness * 0.5f), r, g, b, a);
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
