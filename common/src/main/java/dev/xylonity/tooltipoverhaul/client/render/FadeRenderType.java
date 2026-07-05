package dev.xylonity.tooltipoverhaul.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderType;

import java.util.IdentityHashMap;
import java.util.Map;

public final class FadeRenderType extends RenderType {

    private static final Map<RenderType, RenderType> CACHE = new IdentityHashMap<>();

    private FadeRenderType(RenderType original) {
        super("tooltipoverhaul_fade_" + original, original.format(), original.mode(), original.bufferSize(), original.affectsCrumbling(), true,
                () -> {
                    original.setupRenderState();
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                },
                () -> {
                    RenderSystem.disableBlend();
                    original.clearRenderState();
                });

    }

    public static RenderType remap(RenderType original) {
        if (original instanceof FadeRenderType) {
            return original;
        }

        // Foil items request the glint layer and the base layer at the same time and this RT cancels the latter,
        // so ignoring the vanilla render type (which also supports transparencies) would do the trick
        if (original.toString().contains("glint")) {
            return original;
        }

        return CACHE.computeIfAbsent(original, FadeRenderType::new);
    }

}
