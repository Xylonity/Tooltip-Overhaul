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

public class StarsEffect implements ITooltipEffect {

    private static final int STARS_CAP = 3;
    private static final long SPAWN_INTERVAL = 620L;
    private static final int MIN_LIFE = 420;
    private static final int MAX_LIFE = 680;

    private static final float MIN_SIZE = 28f;
    private static final float MAX_SIZE = 68f;
    private static final float CORe_SIZE = 6.5f;
    private static final float GLOW_SIZE = 2.4f;
    private static final float OFFSET = 0.8f;

    private final Deque<Star> stars = new ArrayDeque<>();
    private final int[] colors;
    private long lastSpawn = 0;

    private static final int[] DEFAULT_COLORS = {
            0x88A0D8FF,
            0x88FFCFE7,
            0x88FFFFFF
    };

    public StarsEffect() {
        this(DEFAULT_COLORS);
    }

    public StarsEffect(int[] colors) {
        this.colors = (colors == null || colors.length == 0) ? DEFAULT_COLORS : colors.clone();
    }

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        int x = (int) pos.x;
        int y = (int) pos.y;
        int w = size.x;
        int h = size.y;

        if (w <= 2 || h <= 2) return;

        long now = System.currentTimeMillis();
        if (now - lastSpawn >= SPAWN_INTERVAL && stars.size() < STARS_CAP) {
            spawnStar(x, y, w, h, now);
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

            stars.removeIf(s -> !s.updateAndRender(ctx.pose().last().pose(), now));

            // reset
            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            RenderSystem.disableBlend();

            ctx.graphics().disableScissor();
        });
    }

    private void spawnStar(int x, int y, int w, int h, long now) {
        Random random = new Random();

        float margin = 10;

        float cx = x + margin + random.nextFloat() * Math.max(1f, w - 2 * margin);
        float cy = y + margin + random.nextFloat() * Math.max(1f, h - 2 * margin);

        float rot = random.nextFloat() * (float) Math.PI;
        stars.addLast(new Star(cx, cy, lerp(random.nextFloat()), now, randomBetween(), colors[random.nextInt(colors.length)], rot));

        if (stars.size() > STARS_CAP) {
            stars.pollFirst();
        }

    }

    private static void draw(Matrix4f pose, float x1, float y1, float x2, float y2, float tStart, float tEnd, int r, int g, int b, int a) {
        float dx = x2 - x1;
        float dy = y2 - y1;

        float len = (float) Math.max(0.001, Math.hypot(dx, dy));

        float x = -dy / len;
        float y = dx / len;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        buf.addVertex(pose, x1 - (x * tStart * 0.5f), y1 - (y * tStart * 0.5f), 0).setColor(r, g, b, a);
        buf.addVertex(pose, x1 + (x * tStart * 0.5f), y1 + (y * tStart * 0.5f), 0).setColor(r, g, b, a);
        buf.addVertex(pose, x2 - (x * tEnd * 0.5f), y2 - (y * tEnd * 0.5f), 0).setColor(r, g, b, a);
        buf.addVertex(pose, x2 + (x * tEnd * 0.5f), y2 + (y * tEnd * 0.5f), 0).setColor(r, g, b, a);

        try (MeshData data = buf.buildOrThrow()) {
            BufferUploader.drawWithShader(data);
        }
    }

    private static int randomBetween() {
        return StarsEffect.MIN_LIFE + (int) Math.floor(Math.random() * (StarsEffect.MAX_LIFE - StarsEffect.MIN_LIFE + 1));
    }

    private static float lerp(float t) {
        return StarsEffect.MIN_SIZE + (StarsEffect.MAX_SIZE - StarsEffect.MIN_SIZE) * t;
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static final class Star {

        private final float cx;
        private final float cy;
        private final float maxLen;
        private final float baseRot;
        private final long birth;
        private final int lifetime;
        private final int argb;

        public Star(float cx, float cy, float maxLen, long birth, int life, int color, float baseRot) {
            this.cx = cx; this.cy = cy; this.maxLen = maxLen;
            this.birth = birth; this.lifetime = life; this.argb = color; this.baseRot = baseRot;
        }

        boolean updateAndRender(Matrix4f pose, long now) {
            float time = (now - birth) / (float) lifetime;
            if (time >= 1f) return false;

            float s = (float) Math.sin(Math.PI * clamp(time));
            float rot = baseRot + (float) Math.sin((now + birth) * 0.0023) * 0.08f;

            float alpha = (float) Math.pow(1f - time, 1.15f);
            int red = (argb >>> 16) & 0xFF;
            int green = (argb >>> 8) & 0xFF;
            int blue = argb & 0xFF;

            float len = 4f + s * maxLen;
            float tco = CORe_SIZE * (0.9f + 0.2f * new Random().nextFloat());
            float tti  = (0.8f + 0.4f * (1f - s));
            for (int i = 0; i < 4; i++) {
                float ax = (float) Math.cos(rot + i * (float)(Math.PI * 0.5));
                float ay = (float) Math.sin(rot + i * (float)(Math.PI * 0.5));
                float x2 = cx + ax * len;
                float y2 = cy + ay * len;

                // halo
                draw(pose, cx, cy, x2, y2, tco * GLOW_SIZE, tti * GLOW_SIZE, red, green, blue, (int)(90 * alpha));

                // core
                draw(pose, cx, cy, x2, y2, tco, tti, red, green, blue, (int) (200 * alpha));

                // external border
                float nx = -ay * OFFSET;
                float ny = ax * OFFSET;
                draw(pose, cx + nx, cy + ny, x2 + nx, y2 + ny, tco * 0.9f, tti * 0.9f, 255, 80, 80, (int)(110 * alpha));
                draw(pose, cx - nx, cy - ny, x2 - nx, y2 - ny, tco * 0.9f, tti * 0.9f, 80, 120, 255, (int)(110 * alpha));
            }

            return true;
        }

    }

}
