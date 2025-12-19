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

public class CircularVignette implements VignetteLayer {

    private static final int SEGMENTS = 64;

    private final VignetteEntry vignetteEntry;

    public CircularVignette(VignetteEntry vignetteEntry) {
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
        float radius = tooltipWidth * vignetteEntry.radius();

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

        // Circle
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        // Completely opaque center
        buffer.addVertex(pose, 0, 0, 0)
                .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), ColorUtils.alpha(color));

        // Transparent border along the chunk amount
        for (int i = 0; i <= SEGMENTS; i++) {
            float angle = (i / (float) SEGMENTS) * (float) Math.PI * 2.0f;
            float vertexPositionX = (float) Math.cos(angle) * radius;
            float vertexPositionY = (float) Math.sin(angle) * radius;

            buffer.addVertex(pose, vertexPositionX, vertexPositionY, 0)
                    .setColor(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), 0);
        }

        try (MeshData data = buffer.buildOrThrow()) {
            BufferUploader.drawWithShader(data);
        }

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