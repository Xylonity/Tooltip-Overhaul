package dev.xylonity.tooltipoverhaul.mixin;

import dev.xylonity.tooltipoverhaul.client.render.PinnedTooltipState;
import dev.xylonity.tooltipoverhaul.registry.TooltipOverhaulKeyMappings;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.glfw.GLFW;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void tooltipoverhaul$pin(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (window == Minecraft.getInstance().getWindow().getWindow() && PinnedTooltipState.key(key, scanCode, action)) {
            ci.cancel();
            return;
        }

        if (window == Minecraft.getInstance().getWindow().getWindow() && action == GLFW.GLFW_PRESS && TooltipOverhaulKeyMappings.handleConfigShortcut(Minecraft.getInstance(), key, scanCode)) {
            ci.cancel();
        }

    }

}
