package dev.xylonity.tooltipoverhaul.client.style.vignette;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.xylonity.tooltipoverhaul.client.layer.impl.VignetteLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectClip;
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

        context.flush();

        final EffectClip clip = new EffectClip(context, position);
        final float x0 = clip.left() + offsetX, x1 = clip.left() + clip.width() + offsetX;
        final float y0 = clip.top() + offsetY, y1 = clip.top() + clip.height() + offsetY;

        RenderSystem.enableBlend();

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        try {
            final Matrix4f pose = context.getPose().last().pose();
            final BufferBuilder buffer = Tesselator.getInstance().getBuilder();

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            final String anchor = entry.position();
            final int alpha = ColorUtils.alpha(entry.color());
            final float depthY = Math.min(bottom - top, (bottom - top) * entry.radius());
            final float depthX = Math.min(right - left, (right - left) * entry.radius());
            if (anchor.startsWith("top") || anchor.equals("middle")) {
                final float edge = top + offsetY;
                vertical(buffer, pose, x0, x1, y0, edge, alpha);
                vertical(buffer, pose, x0, x1, edge, edge + depthY, 0);
            }

            if (anchor.startsWith("bottom") || anchor.equals("middle")) {
                final float edge = bottom + offsetY;
                vertical(buffer, pose, x0, x1, y1, edge, alpha);
                vertical(buffer, pose, x0, x1, edge, edge - depthY, 0);
            }

            if (anchor.equals("left")) {
                final float edge = left + offsetX;
                horizontal(buffer, pose, y0, y1, x0, edge, alpha);
                horizontal(buffer, pose, y0, y1, edge, edge + depthX, 0);
            }

            if (anchor.equals("right")) {
                final float edge = right + offsetX;
                horizontal(buffer, pose, y0, y1, x1, edge, alpha);
                horizontal(buffer, pose, y0, y1, edge, edge - depthX, 0);
            }

            clip.draw(buffer.end());
        }
        finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();

            RenderSystem.disableBlend();
        }

    }

    private void vertical(BufferBuilder buffer, Matrix4f pose, float x0, float x1, float edgeY, float innerY, int innerAlpha) {
        final int alpha = ColorUtils.alpha(entry.color());
        vertex(buffer, pose, x0, edgeY, alpha);
        vertex(buffer, pose, x0, innerY, innerAlpha);
        vertex(buffer, pose, x1, innerY, innerAlpha);
        vertex(buffer, pose, x1, edgeY, alpha);
    }

    private void horizontal(BufferBuilder buffer, Matrix4f pose, float y0, float y1, float edgeX, float innerX, int innerAlpha) {
        final int alpha = ColorUtils.alpha(entry.color());
        vertex(buffer, pose, edgeX, y0, alpha);
        vertex(buffer, pose, innerX, y0, innerAlpha);
        vertex(buffer, pose, innerX, y1, innerAlpha);
        vertex(buffer, pose, edgeX, y1, alpha);
    }

    private void vertex(BufferBuilder buffer, Matrix4f pose, float x, float y, int alpha) {
        buffer.vertex(pose, x, y, 0).color(ColorUtils.red(entry.color()), ColorUtils.green(entry.color()), ColorUtils.blue(entry.color()), alpha).endVertex();
    }

}