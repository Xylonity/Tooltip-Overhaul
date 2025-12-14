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

public class WhiteDustEffect implements EffectLayer {

    private static final int RUNE_COLOR = 0xDDFFFFFF;
    private static final int GLYPH_COLOR = 0xD0E0B35C;
    private static final int PARTICLE_COLOR = 0xDDFFFFFF;

    private static final int RUNE_COUNT = 8;
    private static final int PARTICLE_COUNT = 32;

    private static final float[][] RUNES = new float[RUNE_COUNT][4];
    private static final float[][] PARTICLES = new float[PARTICLE_COUNT][5];

    static {
        Random random = new Random(33333L);

        for (int i = 0; i < RUNE_COUNT; i++) {
            RUNES[i][0] = (float) i / RUNE_COUNT; // Angle seed
            RUNES[i][1] = 0.55f + random.nextFloat() * 0.35f; // Relative distance
            RUNES[i][2] = 0.5f + random.nextFloat() * 0.7f; // Speed
            RUNES[i][3] = random.nextFloat() * (float) Math.PI * 2.0f; // Phase
        }

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            PARTICLES[i][0] = random.nextFloat(); // Angle seed
            PARTICLES[i][1] = 0.45f + random.nextFloat() * 0.65f; // Radial seed
            PARTICLES[i][2] = 0.5f + random.nextFloat() * 1.5f; // Size
            PARTICLES[i][3] = 0.4f + random.nextFloat() * 0.9f; // Speed
            PARTICLES[i][4] = random.nextFloat() * (float) Math.PI * 2.0f; // Phase
        }

    }

    @Override
    public void render(TooltipContext context, Vec2 position) {
        int positionX = (int) position.x;
        int positionY = (int) position.y;
        int tooltipWidth = (int) context.getTooltipSize().x;
        int tooltipHeight = (int) context.getTooltipSize().y;

        long now = System.currentTimeMillis();
        float time = (now - context.getStartTime()) / 12000f;
        float timeLoop = time % 1f;

        float centerX = positionX + tooltipWidth * 0.5f;
        float centerY = positionY + tooltipHeight * 0.5f;
        float radius = (float) Math.hypot(tooltipWidth, tooltipHeight) * 0.55f;

        context.push(() -> {

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
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tesselator.getBuilder();

            // Dust
            renderDust(bufferBuilder, tesselator, pose, centerX, centerY, radius, time);

            // Stars
            renderStars(bufferBuilder, tesselator, pose, centerX, centerY, radius, time);

            // Glyphs
            renderGlyphs(bufferBuilder, tesselator, pose, centerX, centerY, radius, timeLoop);

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

    private void renderDust(BufferBuilder buf, Tesselator tess, Matrix4f pose, float centerX, float centerY, float radius, float tGlobal) {

        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (float[] particle : PARTICLES) {
            float angleSeed = particle[0];
            float radialSeed = particle[1];
            float sizeSeed = particle[2];
            float speed = particle[3];
            float phase = particle[4];

            float angle = angleSeed * (float) Math.PI * 2.0f + tGlobal * speed * (float) Math.PI * 2.0f;

            float fadeRadius = 0.6f + 0.4f * (float) Math.sin(tGlobal * Math.PI * 2.4f + phase);
            float starRadius = radius * radialSeed * fadeRadius;

            float starX = centerX + (float) Math.cos(angle) * starRadius;
            float starY = centerY + (float) Math.sin(angle) * starRadius;

            float twinkleBase = (float) Math.sin((tGlobal * 6.0f + phase) * Math.PI);
            float twinkle = 0.35f + 0.65f * Math.abs(twinkleBase);

            float size = sizeSeed * (0.6f + 0.4f * twinkle);

            int alphaCenter = AnimationUtils.clamp255((int) (255 * twinkle));
            int alphaEdge = AnimationUtils.clamp255((int) (200 * twinkle));

            float half = size * 0.5f;

            buf.vertex(pose, starX, starY - half, 0).color(ColorUtils.red(PARTICLE_COLOR), ColorUtils.green(PARTICLE_COLOR), ColorUtils.blue(PARTICLE_COLOR), alphaEdge).endVertex();
            buf.vertex(pose, starX + half, starY, 0).color(ColorUtils.red(PARTICLE_COLOR), ColorUtils.green(PARTICLE_COLOR), ColorUtils.blue(PARTICLE_COLOR), alphaCenter).endVertex();
            buf.vertex(pose, starX, starY + half, 0).color(ColorUtils.red(PARTICLE_COLOR), ColorUtils.green(PARTICLE_COLOR), ColorUtils.blue(PARTICLE_COLOR), alphaEdge).endVertex();
            buf.vertex(pose, starX - half, starY, 0).color(ColorUtils.red(PARTICLE_COLOR), ColorUtils.green(PARTICLE_COLOR), ColorUtils.blue(PARTICLE_COLOR), alphaCenter).endVertex();
        }

        BufferUploader.drawWithShader(buf.end());
    }

    private void renderStars(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float radius, float time) {
        for (int i = 0; i < RUNE_COUNT; i++) {
            float[] rune = RUNES[i];
            float angleSeed  = rune[0];
            float radial = rune[1];
            float orbitSpeed = rune[2];
            float phase = rune[3];

            float angle = angleSeed * (float) Math.PI * 2.0f + time * orbitSpeed * (float) Math.PI * 2.0f;

            float orbitRadius = radius * radial * (0.9f + 0.1f * (float) Math.sin(time * Math.PI * 3.5f + phase));

            float dustX = centerX + (float) Math.cos(angle) * orbitRadius;
            float dustY = centerY + (float) Math.sin(angle) * orbitRadius;

            float runeRotation = phase + time * (float) Math.PI * 1.7f;
            float glow = 0.65f + 0.35f * (float) Math.sin((time * 4.0f + phase) * Math.PI * 2.0f);
            float size = 5.0f * (0.8f + 0.4f * glow);

            int type = (i % 4 == 3) ? 3 : (i % 3);
            star(bufferBuilder, tesselator, pose, dustX, dustY, size, runeRotation, glow, type);
        }

    }

    private void star(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float size, float rotation, float glow, int type) {
        if (type == 3) {
            float glowSize = size * 1.6f;
            float coreSize = size * 0.9f;

            drawStar(bufferBuilder, tesselator, pose, centerX, centerY, glowSize, RUNE_COLOR, 0.5f * glow);
            drawStar(bufferBuilder, tesselator, pose, centerX, centerY, coreSize, RUNE_COLOR, glow);
        }

    }

    private void drawStar(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float size, int color, float alphaMult) {

        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        int alpha = AnimationUtils.clamp255((int) (ColorUtils.alpha(color) * alphaMult));
        bufferBuilder.vertex(pose, centerX, centerY, 0).color(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), alpha).endVertex();

        for (int i = 0; i <= 8; i++) {
            float angle = (i / 8.0f) * (float) Math.PI * 2.0f;
            float radius = size * (i % 2 == 0 ? 1.0f : 0.4f);

            float starX = centerX + (float) Math.cos(angle) * radius;
            float starY = centerY + (float) Math.sin(angle) * radius;

            int edgeAlpha = AnimationUtils.clamp255((int) (alpha * 0.2f));
            bufferBuilder.vertex(pose, starX, starY, 0).color(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), edgeAlpha).endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private void renderGlyphs(BufferBuilder bufferBuilder, Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float radius, float time) {

        float envelope = (float) Math.sin(time * Math.PI);
        if (envelope < 0.05f) {
            return;
        }

        float rawV = (float) Math.pow(envelope, 1.2f);

        float size = radius * 0.24f * (0.9f + 0.2f * rawV);

        int alphaMain = AnimationUtils.clamp255((int) (ColorUtils.alpha(GLYPH_COLOR) * rawV));
        int alphaSoft = AnimationUtils.clamp255((int) (ColorUtils.alpha(GLYPH_COLOR) * 0.4f * rawV));

        bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.vertex(pose, centerX - size, centerY, 0)
                .color(ColorUtils.red(GLYPH_COLOR), ColorUtils.green(GLYPH_COLOR), ColorUtils.blue(GLYPH_COLOR), alphaMain)
                .endVertex();
        bufferBuilder.vertex(pose, centerX + size, centerY, 0)
                .color(ColorUtils.red(GLYPH_COLOR), ColorUtils.green(GLYPH_COLOR), ColorUtils.blue(GLYPH_COLOR), alphaMain)
                .endVertex();

        bufferBuilder.vertex(pose, centerX, centerY - size, 0)
                .color(ColorUtils.red(GLYPH_COLOR), ColorUtils.green(GLYPH_COLOR), ColorUtils.blue(GLYPH_COLOR), alphaMain)
                .endVertex();
        bufferBuilder.vertex(pose, centerX, centerY + size, 0)
                .color(ColorUtils.red(GLYPH_COLOR), ColorUtils.green(GLYPH_COLOR), ColorUtils.blue(GLYPH_COLOR), alphaMain)
                .endVertex();

        BufferUploader.drawWithShader(bufferBuilder.end());

        bufferBuilder.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        int innerSeg = 24;
        float innerRad = size * 0.7f;
        for (int i = 0; i <= innerSeg; i++) {
            float alpha = (i / (float) innerSeg) * (float) Math.PI * 2.0f;
            float x = centerX + (float) Math.cos(alpha) * innerRad;
            float y = centerY + (float) Math.sin(alpha) * innerRad;

            bufferBuilder.vertex(pose, x, y, 0)
                    .color(ColorUtils.red(GLYPH_COLOR), ColorUtils.green(GLYPH_COLOR), ColorUtils.blue(GLYPH_COLOR), alphaSoft)
                    .endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

}
