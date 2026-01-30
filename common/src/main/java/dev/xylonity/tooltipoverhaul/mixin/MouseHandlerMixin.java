package dev.xylonity.tooltipoverhaul.mixin;

import dev.xylonity.tooltipoverhaul.client.util.TooltipScrollState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MouseHandler.class, priority = 1)
public abstract class MouseHandlerMixin {

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void tooltipoverhaul$onScroll(long window, double dx, double dy, CallbackInfo ci) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen == null) {
            if (TooltipScrollState.isIsActive()) {
                TooltipScrollState.reset();
            }

            return;
        }

        if (TooltipScrollState.shouldCaptureScroll()) {
            TooltipScrollState.onRawScroll(dy);
            ci.cancel();
        }

    }

}