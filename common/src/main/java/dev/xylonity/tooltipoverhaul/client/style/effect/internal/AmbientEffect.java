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

        float left = position.x - 4;
        float top = position.y - 3;
        float width = size.x + 7;
        float height = size.y + 6;
        if (clipped) {
            final int x = (int) position.x;
            final int y = (int) position.y;
            final int x0 = x - context.getPaddingX() - 1;
            final int y0 = y - context.getPaddingY();
            final int x1 = x + (int) size.x + context.getPaddingX();
            final int y1 = y + (int) size.y + context.getPaddingY();

            context.enableScissor(x0, y0, x1, y1);

            left = x0;
            top = y0;
            width = x1 - x0;
            height = y1 - y0;
        }

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        final BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        try {
            final double time = Math.max(0, System.currentTimeMillis() - context.getStartTime()) / 1000.0;
            if (hasMaterial()) {
                RenderSystem.defaultBlendFunc();

                buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
                try {
                    drawMaterial(new EffectCanvas(buffer, context.getPose().last().pose(), left, top, width, height, time, 1));
                }
                finally {
                    BufferUploader.drawWithShader(buffer.end());
                }

            }

            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

            try {
                draw(new EffectCanvas(buffer, context.getPose().last().pose(), left, top, width, height, time, interiorVisibility()));
            }
            finally {
                BufferUploader.drawWithShader(buffer.end());
            }

        }
        finally {
            try {
                if (clipped) {
                    context.getGraphics().disableScissor();
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

    }

    protected abstract void draw(EffectCanvas canvas);

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
