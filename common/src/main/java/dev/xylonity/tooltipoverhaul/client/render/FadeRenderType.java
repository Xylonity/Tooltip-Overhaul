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

        return CACHE.computeIfAbsent(original, FadeRenderType::new);
    }

}
