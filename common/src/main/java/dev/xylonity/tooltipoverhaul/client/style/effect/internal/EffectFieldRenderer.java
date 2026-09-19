package dev.xylonity.tooltipoverhaul.client.style.effect.internal;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.vertex.*;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectParameter.*;

public final class EffectFieldRenderer {

    public static final ResourceLocation SHADER_ID = new ResourceLocation(TooltipOverhaul.MOD_ID, "effect_field");
    private static final int MAX_MESHES = 8;
    private static final Map<EffectField.Grid, VertexBuffer> MESHES = new LinkedHashMap<>(16, 0.75f, true);
    private static ShaderInstance shader;

    private static final BlendMode OPAQUE_BLEND = new BlendMode();

    private static final int[][] COLORS = {
            {0xFF77BBB5, 0xFFA69BD3, 0xFFD5B596, 0xFF7F88A9, 0xFF77BBB5},
            {0xFF70CFC6, 0xFF9390DF, 0xFFE6BC8B, 0xFF70CFC6, 0xFF70CFC6},
            {0xFF6CDFB8, 0xFF947ADC, 0xFFB1EAD5, 0xFF6CDFB8, 0xFF6CDFB8},
            {0xFF5ABBB5, 0xFF879CDB, 0xFFD9A6BF, 0xFFE5DFC9, 0xFF5ABBB5},
            {0xFF659DCF, 0xFFB2A5E6, 0xFFE4C49A, 0xFFD3A2C6, 0xFFD8E8F1}
    };

    private static final int[][] CHANNELS = { {0,1,2,0,0}, {0,1,2,0,0}, {0,1,2,0,0}, {0,1,2,2,0}, {0,1,2,1,1} };

    public static void setShader(ShaderInstance loaded) {
        clearMeshes();
        shader = loaded;
    }

    public static void reset() {
        shader = null;
        clearMeshes();
    }

    private static void clearMeshes() {
        MESHES.values().forEach(VertexBuffer::close);
        MESHES.clear();
    }

    public static boolean available() {
        return shader != null;
    }

    public static void draw(EffectField field, TooltipContext context, EffectClip clip, double time, float interiorVisibility) {
        final EffectField.Grid grid = field.grid(clip.width(), clip.height());
        final VertexBuffer mesh = mesh(grid);

        configure(shader, field, grid, time, interiorVisibility);

        final Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(context.getPose().last().pose()).translate(clip.left(), clip.top(), 0);
        try {
            clip.draw(mesh, modelView, shader);
        }
        finally {
            restoreGuiShader(GameRenderer.getPositionColorShader());
        }

    }

    static void restoreGuiShader(ShaderInstance regular) {
        OPAQUE_BLEND.apply();
        regular.apply();
        regular.clear();
    }

    static VertexBuffer mesh(EffectField.Grid grid) {
        VertexBuffer mesh = MESHES.get(grid);
        if (mesh == null) {
            mesh = createMesh(grid);
            MESHES.put(grid, mesh);
            if (MESHES.size() > MAX_MESHES) {
                final Iterator<Map.Entry<EffectField.Grid, VertexBuffer>> oldest = MESHES.entrySet().iterator();
                oldest.next().getValue().close();
                oldest.remove();
            }

        }

        return mesh;
    }

    static void configure(ShaderInstance shader, EffectField field, EffectField.Grid grid, double time, float interiorVisibility) {
        shader.safeGetUniform("FieldKind").set(field.ordinal());
        shader.safeGetUniform("FieldTime").set((float) time);
        shader.safeGetUniform("CanvasSize").set(grid.width(), grid.height());
        shader.safeGetUniform("FieldScale").set(grid.scale(), EffectRuntime.value(FIELD_SCALE), grid.pixelSize());
        shader.safeGetUniform("FieldControls").set(EffectRuntime.value(DISTORTION), EffectRuntime.value(SHARPNESS), EffectRuntime.value(CURTAIN_HEIGHT), EffectRuntime.intensity());
        shader.safeGetUniform("InteriorVisibility").set(interiorVisibility);
        shader.safeGetUniform("Variation").set((int) EffectRuntime.value(VARIATION));
        shader.safeGetUniform("PatternPhase").set(EffectCanvas.patternPhase(80), EffectCanvas.patternPhase(81), EffectCanvas.patternPhase(82));
        for (int i = 0; i < 5; i++) {
            final int color = EffectRuntime.color(CHANNELS[field.ordinal()][i], COLORS[field.ordinal()][i]);
            shader.safeGetUniform("Palette" + i).set((float) ((color >>> 16) & 255), (float) ((color >>> 8) & 255), (float) (color & 255));
        }

    }

    private static VertexBuffer createMesh(EffectField.Grid grid) {
        final BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int row = 0; row < grid.rows(); row++) {
            final float y0 = grid.y(row), y1 = grid.y(row + 1);
            for (int column = 0; column < grid.columns(); column++) {
                final float x0 = grid.x(column), x1 = grid.x(column + 1);
                builder.vertex(x0, y0, 0).endVertex();
                builder.vertex(x1, y0, 0).endVertex();
                builder.vertex(x1, y1, 0).endVertex();
                builder.vertex(x0, y1, 0).endVertex();
            }

        }

        final VertexBuffer mesh = new VertexBuffer(VertexBuffer.Usage.STATIC);
        try {
            mesh.bind();
            mesh.upload(builder.end());
            return mesh;
        }
        catch (final RuntimeException failure) {
            mesh.close();
            throw failure;
        }
        finally {
            VertexBuffer.unbind();
        }

    }

}