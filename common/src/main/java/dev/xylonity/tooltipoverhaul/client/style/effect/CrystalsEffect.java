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

public class CrystalsEffect implements EffectLayer {

    private static final int[][] CRYSTAL_COLORS = {
            { 0xFFA0E8FF, 0xFFE0F8FF },
            { 0xFFC0A0FF, 0xFFE8DCFF },
            { 0xFFA8FFD8, 0xFFE0FFF0 }
    };

    private static final int GLINT_COLOR = 0xFFFFFFFF;
    private static final int CRYSTAL_COUNT = 6;

    private static final float[][] CRYSTALS = new float[CRYSTAL_COUNT][6];

    static {
        final Random random = new Random(22222L);
        for (int i = 0; i < CRYSTAL_COUNT; i++) {
            CRYSTALS[i][0] = (i / (float) CRYSTAL_COUNT) * (float) Math.PI * 2.0f + random.nextFloat() * 0.5f; // Anchor angle
            CRYSTALS[i][1] = 3.0f + random.nextFloat() * 1.8f; // Size
            CRYSTALS[i][2] = 0.7f + random.nextFloat() * 0.7f; // Bob speed
            CRYSTALS[i][3] = (random.nextFloat() - 0.5f) * 0.8f; // Base tilt
            CRYSTALS[i][4] = 0.4f + random.nextFloat() * 0.5f; // Tilt sway speed
            CRYSTALS[i][5] = random.nextFloat() * (float) Math.PI * 2.0f; // Phase
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

        final float centerX = positionX + tooltipWidth * 0.5f;
        final float centerY = positionY + tooltipHeight * 0.5f;
        final float radiusX = tooltipWidth * 0.5f + 9.0f;
        final float radiusY = tooltipHeight * 0.5f + 7.0f;

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
            final Tesselator tesselator = Tesselator.getInstance();

            for (int i = 0; i < CRYSTAL_COUNT; i++) {
                renderCrystal(tesselator, pose, centerX, centerY, radiusX, radiusY, time, i);
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

    private void renderCrystal(Tesselator tesselator, Matrix4f pose, float centerX, float centerY, float radiusX, float radiusY, float time, int index) {
        final float[] crystal = CRYSTALS[index];
        final float anchorAngle = crystal[0];
        final float size = crystal[1];
        final float bobSpeed = crystal[2];
        final float baseTilt = crystal[3];
        final float tiltSwaySpeed = crystal[4];
        final float phase = crystal[5];

        // Anchored around the tooltip, floating up and down in place
        final float x = centerX + (float) Math.cos(anchorAngle) * radiusX;
        final float y = centerY + (float) Math.sin(anchorAngle) * radiusY + (float) Math.sin(time * bobSpeed * (float) Math.PI + phase) * 2.2f;

        // Slight tilting
        final float rotation = baseTilt + (float) Math.sin(time * tiltSwaySpeed * (float) Math.PI + phase) * 0.35f;
        final float cos = (float) Math.cos(rotation);
        final float sin = (float) Math.sin(rotation);

        final int[] palette = CRYSTAL_COLORS[index % CRYSTAL_COLORS.length];
        final int baseColor = palette[0];
        final int highlightColor = palette[1];

        // Hexagon outline
        final float width = size * 0.55f;
        final float[][] outline = {
                { 0.0f, -size },
                { width, -size * 0.4f },
                { width, size * 0.4f },
                { 0.0f, size },
                { -width, size * 0.4f },
                { -width, -size * 0.4f }
        };

        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < outline.length; i++) {
            final float[] a = outline[i];
            final float[] b = outline[(i + 1) % outline.length];

            final int facetColor = (i % 2 == 0) ? baseColor : highlightColor;

            bufferBuilder.addVertex(pose, x, y, 0)
                    .setColor(ColorUtils.red(highlightColor), ColorUtils.green(highlightColor), ColorUtils.blue(highlightColor), 235);
            bufferBuilder.addVertex(pose, x + a[0] * cos - a[1] * sin, y + a[0] * sin + a[1] * cos, 0)
                    .setColor(ColorUtils.red(facetColor), ColorUtils.green(facetColor), ColorUtils.blue(facetColor), 220);
            bufferBuilder.addVertex(pose, x + b[0] * cos - b[1] * sin, y + b[0] * sin + b[1] * cos, 0)
                    .setColor(ColorUtils.red(facetColor), ColorUtils.green(facetColor), ColorUtils.blue(facetColor), 220);
        }

        try (MeshData data = bufferBuilder.buildOrThrow()) {
            BufferUploader.drawWithShader(data);
        }

        final float glintBase = (float) Math.sin(time * 1.6f + phase * 3.0f);
        final float glint = glintBase > 0 ? glintBase * glintBase * glintBase * glintBase : 0.0f;
        if (glint < 0.05f) {
            return;
        }

        final float glintX = x + (-width * 0.4f) * cos - (-size * 0.45f) * sin;
        final float glintY = y + (-width * 0.4f) * sin + (-size * 0.45f) * cos;
        final float glintHalf = 0.7f + 1.1f * glint;
        final int glintAlpha = AnimationUtils.clamp255((int) (245 * glint));

        bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.addVertex(pose, glintX, glintY - glintHalf, 0).setColor(ColorUtils.red(GLINT_COLOR), ColorUtils.green(GLINT_COLOR), ColorUtils.blue(GLINT_COLOR), glintAlpha);
        bufferBuilder.addVertex(pose, glintX + glintHalf, glintY, 0).setColor(ColorUtils.red(GLINT_COLOR), ColorUtils.green(GLINT_COLOR), ColorUtils.blue(GLINT_COLOR), glintAlpha);
        bufferBuilder.addVertex(pose, glintX, glintY + glintHalf, 0).setColor(ColorUtils.red(GLINT_COLOR), ColorUtils.green(GLINT_COLOR), ColorUtils.blue(GLINT_COLOR), glintAlpha);
        bufferBuilder.addVertex(pose, glintX - glintHalf, glintY, 0).setColor(ColorUtils.red(GLINT_COLOR), ColorUtils.green(GLINT_COLOR), ColorUtils.blue(GLINT_COLOR), glintAlpha);

        try (MeshData data = bufferBuilder.buildOrThrow()) {
            BufferUploader.drawWithShader(data);
        }

    }

}