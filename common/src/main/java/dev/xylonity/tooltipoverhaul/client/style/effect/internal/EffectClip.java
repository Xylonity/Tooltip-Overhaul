package dev.xylonity.tooltipoverhaul.client.style.effect.internal;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexBuffer;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipLayout;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import net.minecraft.world.phys.Vec2;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class EffectClip {

    private final TooltipContext context;
    private final List<Rect> regions = new ArrayList<>();

    private final Rect bounds;

    public EffectClip(TooltipContext context, Vec2 position) {
        this.context = context;
        final int x = (int) position.x;
        final int y = (int) position.y;
        final Rect main = new Rect(x - context.getPaddingX() - 1, y - context.getPaddingY(), x + (int) context.getTooltipSize().x + context.getPaddingX(), y + (int) context.getTooltipSize().y + context.getPaddingY());

        add(main);

        if (context.getLayoutStyle() == TooltipLayout.Style.BADGE) {
            final int iconX = x + TooltipLayout.iconX(context);
            final int iconY = y + TooltipLayout.iconY(context);
            final int size = Constants.getIconSize(context);

            addOutside(new Rect(iconX, iconY - 1, iconX + size, iconY), main);
            addOutside(new Rect(iconX - 1, iconY, iconX + size + 1, iconY + size), main);
            addOutside(new Rect(iconX, iconY + size, iconX + size, iconY + size + 1), main);
        }

        int left = main.left, top = main.top, right = main.right, bottom = main.bottom;
        for (final Rect region : regions) {
            left = Math.min(left, region.left);
            top = Math.min(top, region.top);
            right = Math.max(right, region.right);
            bottom = Math.max(bottom, region.bottom);
        }

        bounds = new Rect(left, top, right, bottom);
    }

    private void add(Rect rect) {
        if (rect.right > rect.left && rect.bottom > rect.top) {
            regions.add(rect);
        }

    }

    private void addOutside(Rect rect, Rect main) {
        final int left = Math.max(rect.left, main.left);
        final int right = Math.min(rect.right, main.right);
        final int top = Math.max(rect.top, main.top);
        final int bottom = Math.min(rect.bottom, main.bottom);
        if (left >= right || top >= bottom) {
            add(rect);
            return;
        }

        add(new Rect(rect.left, rect.top, rect.right, top));
        add(new Rect(rect.left, bottom, rect.right, rect.bottom));
        add(new Rect(rect.left, top, left, bottom));
        add(new Rect(right, top, rect.right, bottom));
    }

    public int left() {
        return bounds.left;
    }

    public int top() {
        return bounds.top;
    }

    public int width() {
        return bounds.right - bounds.left;
    }

    public int height() {
        return bounds.bottom - bounds.top;
    }

    public boolean hasExtension() {
        return regions.size() > 1;
    }

    public void draw(VertexBuffer buffer, Matrix4f modelView, ShaderInstance shader) {
        try {
            for (final Rect region : regions) {
                context.enableScissor(region.left, region.top, region.right, region.bottom);
                try {
                    RenderSystem.disableDepthTest();
                    buffer.bind();
                    buffer.drawWithShader(modelView, RenderSystem.getProjectionMatrix(), shader);
                }
                finally {
                    context.getGraphics().disableScissor();
                }

            }

        }
        finally {
            VertexBuffer.unbind();
        }

    }

    public void draw(BufferBuilder.RenderedBuffer mesh) {
        if (mesh.isEmpty()) {
            mesh.release();
            return;
        }

        final VertexBuffer buffer = mesh.drawState().format().getImmediateDrawVertexBuffer();
        boolean uploaded = false;
        try {
            for (final Rect region : regions) {
                context.enableScissor(region.left, region.top, region.right, region.bottom);
                try {
                    RenderSystem.disableDepthTest();
                    if (!uploaded) {
                        uploaded = true;
                        BufferUploader.drawWithShader(mesh);
                    }
                    else {
                        buffer.bind();
                        buffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
                    }

                }
                finally {
                    context.getGraphics().disableScissor();
                }

            }

        }
        finally {
            if (!uploaded) {
                mesh.release();
            }

            if (hasExtension()) {
                VertexBuffer.unbind();
            }

        }

    }

    private record Rect(
            int left,
            int top,
            int right,
            int bottom
    ) {
        ;;
    }

}
