package dev.xylonity.tooltipoverhaul.client.style.divider;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.xylonity.tooltipoverhaul.client.layer.impl.DividerLineLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

public class StaticDividerLine implements DividerLineLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {

        int y = (int) position.y;
        int x = (int) position.x;
        int paddingX = context.getPaddingX();
        int width = (int) context.getTooltipSize().x;

        PoseStack pose = context.getGraphics().pose();
        Matrix4f matrix = pose.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buf = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        int baseColor = ColorUtils.getDividerLineColor(context);
        int r = (baseColor >>> 16) & 0xFF;
        int g = (baseColor >>> 8) & 0xFF;
        int b = baseColor & 0xFF;

        buf.addVertex(matrix, x - paddingX - 2, y + 1, 0).setColor(r, g, b, 255);
        buf.addVertex(matrix, x + width - paddingX + 1, y + 1, 0).setColor(r, g, b, 255);
        buf.addVertex(matrix, x + width - paddingX + 1, y, 0).setColor(r, g, b, 255);
        buf.addVertex(matrix, x - paddingX - 2, y, 0).setColor(r, g, b, 255);

        try (MeshData data = buf.buildOrThrow()) {
            BufferUploader.drawWithShader(data);
        }
    }
}