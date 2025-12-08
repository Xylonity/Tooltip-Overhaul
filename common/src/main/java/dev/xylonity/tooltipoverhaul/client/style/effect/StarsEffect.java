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

public class StarsEffect implements EffectLayer {

    private static final int STARS_CAP = 1;
    private static final long SPAWN_INTERVAL = 3500L;
    private static final int MIN_LIFE = 550;
    private static final int MAX_LIFE = 800;

    private static final float MIN_SIZE = 20f;
    private static final float MAX_SIZE = 50f;
    private static final float CORE_SIZE = 11f;
    private static final float GLOW_SIZE = 2f;
    private static final float OFFSET = 0f;

    private static final int[] DEFAULT_COLORS = {
            0x88ffffff,
            0x88ffffff,
            0x88ffffff
    };

    private static final Deque<Star> stars = new ArrayDeque<>();
    private final int[] colors;
    private long lastSpawn = 0L;

    public StarsEffect() {
        this(DEFAULT_COLORS);
    }

    public StarsEffect(int[] colors) {
        this.colors = (colors == null || colors.length == 0) ? DEFAULT_COLORS : colors.clone();
    }

    @Override
    public void render(TooltipContext context, Vec2 position) {
        int positionX = (int) position.x;
        int positionY = (int) position.y;
        int tooltipWidth = (int) context.getTooltipSize().x;
        int tooltipHeight = (int) context.getTooltipSize().y;

        long now = System.currentTimeMillis();

        if (now - lastSpawn >= SPAWN_INTERVAL && stars.size() < STARS_CAP) {
            spawnStar(tooltipWidth, tooltipHeight, now);
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

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();

            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            Matrix4f pose = context.getPose().last().pose();

            stars.removeIf(star -> !star.updateAndRender(pose, now, positionX, positionY));

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );

            RenderSystem.disableBlend();
            RenderSystem.enableCull();

            context.getGraphics().disableScissor();
        });

    }

    private void spawnStar(int width, int height, long now) {
        Random random = new Random(0x5A77BEEFL);
        float margin = 10f;

        float localX = margin + random.nextFloat() * Math.max(1f, width - 2f * margin);
        float localY = margin + random.nextFloat() * Math.max(1f, height - 2f * margin);

        float rot = random.nextFloat() * (float) Math.PI;
        float maxLength = AnimationUtils.lerp(MIN_SIZE, MAX_SIZE, random.nextFloat());
        int life = AnimationUtils.randomBetween(random, MIN_LIFE, MAX_LIFE);
        int color = colors[random.nextInt(colors.length)];

        float coreScale = 0.9f + 0.2f * random.nextFloat();
        float twinklePhase = random.nextFloat() * (float) (Math.PI * 2.0);

        stars.addLast(new Star(localX, localY, maxLength, now, life, color, rot, coreScale, twinklePhase));

        if (stars.size() > STARS_CAP) {
            stars.pollFirst();
        }

    }

    private static void draw(Matrix4f pose, float x1, float y1, float x2, float y2, float tStart, float tEnd, int r, int g, int b, int a) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.max(0.001, Math.hypot(dx, dy));

        float nx = -dy / len;
        float ny = dx / len;

        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(pose, x1 - (nx * tStart * 0.5f), y1 - (ny * tStart * 0.5f), 0).color(r, g, b, a).endVertex();
        buf.vertex(pose, x1 + (nx * tStart * 0.5f), y1 + (ny * tStart * 0.5f), 0).color(r, g, b, a).endVertex();
        buf.vertex(pose, x2 - (nx * tEnd * 0.5f), y2 - (ny * tEnd * 0.5f), 0).color(r, g, b, a).endVertex();
        buf.vertex(pose, x2 + (nx * tEnd * 0.5f), y2 + (ny * tEnd * 0.5f), 0).color(r, g, b, a).endVertex();

        BufferUploader.drawWithShader(buf.end());
    }

    private record Star(float localX, float localY, float maxLen, long birth, int lifetime, int argb, float baseRot, float coreScale, float twinklePhase) {

        boolean updateAndRender(Matrix4f pose, long now, float nowX, float nowY) {
            float time = (now - birth) / (float) lifetime;
            if (time >= 1f) return false;

            float centerX = nowX + localX;
            float centerY = nowY + localY;

            float s = (float) Math.sin(Math.PI * AnimationUtils.clamp(time, 0, 1));

            float rot = baseRot + (float) Math.sin((now - birth) * 0.0023f) * 0.08f;

            float alphaFactor = (float) Math.pow(1f - time, 1.15f);

            int red = ColorUtils.red(argb);
            int green = ColorUtils.green(argb);
            int blue = ColorUtils.blue(argb);

            float length = 4f + s * maxLen;

            float twinkle = 0.85f + 0.15f * (float) Math.sin((now - birth) * 0.006f + twinklePhase);

            float tCoreBase = CORE_SIZE * coreScale * twinkle;
            float tInner = (0.8f + 0.4f * (1f - s));

            for (int i = 0; i < 4; i++) {
                float angle = rot + i * (float) (Math.PI * 0.5);
                float ax = (float) Math.cos(angle);
                float ay = (float) Math.sin(angle);
                float x2 = centerX + ax * length;
                float y2 = centerY + ay * length;

                // halo
                float haloStart = tCoreBase * GLOW_SIZE * 0.7f;
                float haloEnd = tInner * GLOW_SIZE * 0.7f;
                draw(pose, centerX, centerY, x2, y2, haloStart, haloEnd, red, green, blue, (int) (90 * alphaFactor));

                // core
                float coreStart = tCoreBase * 0.7f;
                float coreEnd = tInner * 0.9f;
                draw(pose, centerX, centerY, x2, y2, coreStart, coreEnd, red, green, blue, (int) (200 * alphaFactor));

                float nx = -ay * OFFSET;
                float ny = ax * OFFSET;

                float borderStart = tCoreBase * 0.6f;
                float borderEnd = tInner * 0.8f;

                draw(pose, centerX + nx, centerY + ny, x2 + nx, y2 + ny, borderStart, borderEnd, 255, 80, 80, (int) (110 * alphaFactor));

                draw(pose, centerX - nx, centerY - ny, x2 - nx, y2 - ny, borderStart, borderEnd, 80, 120, 255, (int) (110 * alphaFactor));
            }

            return true;
        }

    }

}
