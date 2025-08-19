package dev.xylonity.tooltipoverhaul.mixin;

import dev.xylonity.tooltipoverhaul.client.TooltipScrollState;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MouseHandler.class, priority = 1)
public abstract class MouseHandlerMixin {

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void tooltipsoverhaul$onScroll(long window, double dx, double dy, CallbackInfo ci) {
        if (TooltipScrollState.shouldCaptureScroll()) {
            TooltipScrollState.onRawScroll(dy);
            ci.cancel();
        }

    }

}