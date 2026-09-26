package dev.xylonity.tooltipoverhaul.client.style.vignette;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.xylonity.tooltipoverhaul.client.layer.impl.VignetteLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectClip;
import dev.xylonity.tooltipoverhaul.client.style.vignette.parser.VignetteEntry;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.client.util.PositionUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextAxis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

public final class ShapedVignette implements VignetteLayer {

    private final VignetteEntry entry;

    public ShapedVignette(VignetteEntry entry) {
        this.entry = entry;
    }

    @Override
    public void render(TooltipContext context, Vec2 position) {
        final float width = context.getTooltipSize().x, height = context.getTooltipSize().y;
        final float radius = width * entry.radius();
        if (!Float.isFinite(radius) || radius <= 0) {
            return;
        }

        final float x = position.x + PositionUtils.getVignettePosition(context, entry, TextAxis.X) + height * entry.extraPositionX() / 100f;
        final float y = position.y + PositionUtils.getVignettePosition(context, entry, TextAxis.Y) + width * entry.extraPositionY() / 100f;

        context.flush();

        RenderSystem.enableBlend();

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        try {
            final Matrix4f pose = new Matrix4f(context.getPose().last().pose()).translate(x, y, 0);
            final BufferBuilder buffer = Tesselator.getInstance().getBuilder();

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            if (entry.type().equals("ring")) {
                band(buffer, pose, radius, 0.6f, 0.8f, 0, ColorUtils.alpha(entry.color()));
                band(buffer, pose, radius, 0.8f, 1f, ColorUtils.alpha(entry.color()), 0);
            }
            else {
                band(buffer, pose, radius, 0, 1, ColorUtils.alpha(entry.color()), 0);
            }

            new EffectClip(context, position).draw(buffer.end());
        }
        finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();

            RenderSystem.disableBlend();
        }

    }

    private void band(BufferBuilder buffer, Matrix4f pose, float radius, float inner, float outer, int innerAlpha, int outerAlpha) {
        final int segments = entry.type().equals("diamond") ? 4 : 64;
        final float aspect = entry.type().equals("ellipse") ? 0.5f : 1f;
        for (int i = 0; i < segments; i++) {
            final double from = i * Math.PI * 2 / segments, to = (i + 1) * Math.PI * 2 / segments;
            final float ax = (float) Math.cos(from) * radius, ay = (float) Math.sin(from) * radius * aspect;
            final float bx = (float) Math.cos(to) * radius, by = (float) Math.sin(to) * radius * aspect;
            vertex(buffer, pose, ax * inner, ay * inner, innerAlpha);
            vertex(buffer, pose, ax * outer, ay * outer, outerAlpha);
            vertex(buffer, pose, bx * outer, by * outer, outerAlpha);
            vertex(buffer, pose, bx * inner, by * inner, innerAlpha);
        }

    }

    private void vertex(BufferBuilder buffer, Matrix4f pose, float x, float y, int alpha) {
        buffer.vertex(pose, x, y, 0).color(ColorUtils.red(entry.color()), ColorUtils.green(entry.color()), ColorUtils.blue(entry.color()), alpha).endVertex();
    }

}