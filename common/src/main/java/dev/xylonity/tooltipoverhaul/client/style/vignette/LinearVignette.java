package dev.xylonity.tooltipoverhaul.client.style.vignette;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.xylonity.tooltipoverhaul.client.layer.impl.VignetteLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.style.vignette.parser.VignetteEntry;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

public final class LinearVignette implements VignetteLayer {

    private final VignetteEntry entry;

    public LinearVignette(VignetteEntry entry) {
        this.entry = entry;
    }

    @Override
    public void render(TooltipContext context, Vec2 position) {
        final float width = context.getTooltipSize().x, height = context.getTooltipSize().y;
        if (!Float.isFinite(entry.radius()) || entry.radius() <= 0) {
            return;
        }

        final float left = position.x - context.getPaddingX() - 1, top = position.y - context.getPaddingY();
        final float right = position.x + width + context.getPaddingX(), bottom = position.y + height + context.getPaddingY();
        final float offsetX = height * entry.extraPositionX() / 100f, offsetY = width * entry.extraPositionY() / 100f;

        context.enableScissor((int) left, (int) top, (int) right, (int) bottom);
        context.flush();

        RenderSystem.enableBlend();

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        try {
            final Matrix4f pose = context.getPose().last().pose();
            final BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            final String anchor = entry.position();
            final float depthY = Math.min(bottom - top, (bottom - top) * entry.radius());
            final float depthX = Math.min(right - left, (right - left) * entry.radius());
            if (anchor.startsWith("top") || anchor.equals("middle")) {
                float edge = top + offsetY;
                vertical(buffer, pose, left + offsetX, right + offsetX, edge, edge + depthY);
            }

            if (anchor.startsWith("bottom") || anchor.equals("middle")) {
                float edge = bottom + offsetY;
                vertical(buffer, pose, left + offsetX, right + offsetX, edge, edge - depthY);
            }

            if (anchor.equals("left")) {
                float edge = left + offsetX;
                horizontal(buffer, pose, top + offsetY, bottom + offsetY, edge, edge + depthX);
            }

            if (anchor.equals("right")) {
                final float edge = right + offsetX;
                horizontal(buffer, pose, top + offsetY, bottom + offsetY, edge, edge - depthX);
            }

            final MeshData mesh = buffer.build();
            if (mesh != null) {
                BufferUploader.drawWithShader(mesh);
            }

        }
        finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();

            RenderSystem.disableBlend();

            context.getGraphics().disableScissor();
        }

    }

    private void vertical(BufferBuilder buffer, Matrix4f pose, float x0, float x1, float edgeY, float innerY) {
        int alpha = ColorUtils.alpha(entry.color());
        vertex(buffer, pose, x0, edgeY, alpha);
        vertex(buffer, pose, x0, innerY, 0);
        vertex(buffer, pose, x1, innerY, 0);
        vertex(buffer, pose, x1, edgeY, alpha);
    }

    private void horizontal(BufferBuilder buffer, Matrix4f pose, float y0, float y1, float edgeX, float innerX) {
        final int alpha = ColorUtils.alpha(entry.color());
        vertex(buffer, pose, edgeX, y0, alpha);
        vertex(buffer, pose, innerX, y0, 0);
        vertex(buffer, pose, innerX, y1, 0);
        vertex(buffer, pose, edgeX, y1, alpha);
    }

    private void vertex(BufferBuilder buffer, Matrix4f pose, float x, float y, int alpha) {
        buffer.addVertex(pose, x, y, 0).setColor(ColorUtils.red(entry.color()), ColorUtils.green(entry.color()), ColorUtils.blue(entry.color()), alpha);
    }

}