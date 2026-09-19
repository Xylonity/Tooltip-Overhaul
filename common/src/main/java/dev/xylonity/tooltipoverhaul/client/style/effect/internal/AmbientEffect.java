package dev.xylonity.tooltipoverhaul.client.style.effect.internal;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.xylonity.tooltipoverhaul.client.layer.impl.EffectLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec2;

/**
 * Shared setup for every effect, which owns the render state so the effects themselves only ever have to write down geometry
 */
public abstract class AmbientEffect implements EffectLayer {

    @Override
    public final void render(TooltipContext context, Vec2 position) {
        final Vec2 size = context.getTooltipSize();
        if (size == null || size.x <= 0 || size.y <= 0) {
            return;
        }

        // Finishing buffered gui layers before switching their blend mode
        context.flush();

        final boolean clipped = clipToTooltip();
        final EffectClip clip = clipped ? new EffectClip(context, position) : null;

        float left = position.x - 4;
        float top = position.y - 3;
        float width = size.x + 7;
        float height = size.y + 6;
        if (clipped) {
            left = clip.left();
            top = clip.top();
            width = clip.width();
            height = clip.height();
        }

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        final BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        try {
            final double time = EffectRuntime.seconds(context);
            final boolean gpuField = field() != null && clip != null && EffectFieldRenderer.available();
            if (gpuField) {
                EffectFieldRenderer.draw(field(), context, clip, time, interiorVisibility());
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
            }

            if (hasMaterial()) {
                RenderSystem.defaultBlendFunc();

                buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
                try {
                    drawMaterial(new EffectCanvas(buffer, context.getPose().last().pose(), left, top, width, height, time, 1, clipped));
                }
                finally {
                    if (clip != null) {
                        clip.draw(buffer.end());
                    }
                    else {
                        BufferUploader.drawWithShader(buffer.end());
                    }

                }

            }

            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

            try {
                final EffectCanvas canvas = new EffectCanvas(buffer, context.getPose().last().pose(), left, top, width, height, time, interiorVisibility(), clipped);
                if (gpuField) {
                    drawAfterField(canvas);
                }
                else {
                    draw(canvas);
                }

            }
            finally {
                if (clip != null) {
                    clip.draw(buffer.end());
                }
                else {
                    BufferUploader.drawWithShader(buffer.end());
                }

            }

        }
        finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();

        }

    }

    protected abstract void draw(EffectCanvas canvas);

    protected EffectField field() {
        return null;
    }

    protected void drawAfterField(EffectCanvas canvas) {
        ;;
    }

    protected boolean clipToTooltip() {
        return false;
    }

    protected boolean hasMaterial() {
        return false;
    }

    protected void drawMaterial(EffectCanvas canvas) {
        ;;
    }

    /**
     * Transparency when the effect is within the tooltip margins, so it doesn't interfere with text reading
     */
    protected float interiorVisibility() {
        return 0.18f;
    }

}
