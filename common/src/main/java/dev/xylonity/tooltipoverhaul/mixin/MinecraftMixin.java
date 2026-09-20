package dev.xylonity.tooltipoverhaul.mixin;

import dev.xylonity.tooltipoverhaul.client.render.PinnedTooltipState;
import dev.xylonity.tooltipoverhaul.client.render.FadeRenderType;
import dev.xylonity.tooltipoverhaul.client.style.preview.renderer.PreviewEntityCache;
import dev.xylonity.tooltipoverhaul.client.util.TooltipScrollState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void tooltipoverhaul$onSetScreen(Screen screen, CallbackInfo ci) {
        PinnedTooltipState.screenChanged(screen);
        TooltipScrollState.reset();
        PreviewEntityCache.clear();
        FadeRenderType.reset();
    }

    @Inject(method = "updateLevelInEngines", at = @At("HEAD"))
    private void tooltipoverhaul$onLevelChanged(ClientLevel level, CallbackInfo ci) {
        PreviewEntityCache.clear();
        FadeRenderType.reset();
    }

}
