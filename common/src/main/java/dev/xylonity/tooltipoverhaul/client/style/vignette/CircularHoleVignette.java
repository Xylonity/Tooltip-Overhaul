package dev.xylonity.tooltipoverhaul.client.style.vignette;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.xylonity.tooltipoverhaul.client.layer.impl.VignetteLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.style.vignette.parser.VignetteEntry;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.client.util.PositionUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextAxis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

public class CircularHoleVignette implements VignetteLayer {

    private static final int SEGMENTS = 64;

    private final VignetteEntry vignetteEntry;

    public CircularHoleVignette(VignetteEntry vignetteEntry) {
        this.vignetteEntry = vignetteEntry;
    }

    @Override
    public void render(TooltipContext context, Vec2 position) {

        int positionX = (int) position.x;
        int positionY = (int) position.y;
        int tooltipWidth = (int) context.getTooltipSize().x;
        int tooltipHeight = (int) context.getTooltipSize().y;
        int paddingX = context.getPaddingX();
        int paddingY = context.getPaddingY();

        float anchorPositionX = positionX + PositionUtils.getVignettePosition(context, vignetteEntry, TextAxis.X) + (tooltipHeight * (vignetteEntry.extraPositionX() / 100f));
        float anchorPositionY = positionY + PositionUtils.getVignettePosition(context, vignetteEntry, TextAxis.Y) + (tooltipWidth * (vignetteEntry.extraPositionY() / 100f));

        float innerRadius = tooltipWidth * vignetteEntry.radius();
        float outerRadius = Math.max(tooltipWidth, tooltipHeight) * 1.2f;

        int color = vignetteEntry.color();

        context.translate(anchorPositionX, anchorPositionY, 0);

        context.getGraphics().enableScissor(
                positionX - paddingX - 1,
                positionY - paddingY,
                positionX + tooltipWidth + paddingX,
                positionY + tooltipHeight + paddingY
        );

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
        BufferBuilder buffer = tesselator.getBuilder();

        // Inversed circular ring
        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        int alpha = ColorUtils.alpha(color);
        int red = ColorUtils.red(color);
        int green = ColorUtils.green(color);
        int blue = ColorUtils.blue(color);

        for (int i = 0; i <= SEGMENTS; i++) {
            float angle = (i / (float) SEGMENTS) * (float) Math.PI * 2.0f;

            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float innerX = cos * innerRadius;
            float innerY = sin * innerRadius;
            float outerX = cos * outerRadius;
            float outerY = sin * outerRadius;

            // Outer
            buffer.vertex(pose, outerX, outerY, 0)
                    .color(red, green, blue, alpha)
                    .endVertex();

            // Circle border
            buffer.vertex(pose, innerX, innerY, 0)
                    .color(red, green, blue, 0)
                    .endVertex();
        }

        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();

        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );

        RenderSystem.disableBlend();
        RenderSystem.enableCull();

        context.getGraphics().disableScissor();
    }

}
