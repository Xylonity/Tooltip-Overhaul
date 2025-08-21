package dev.xylonity.tooltipoverhaul.mixin.apotheosis;

import dev.shadowsoffire.apotheosis.ench.EnchModuleClient;
import dev.xylonity.tooltipoverhaul.compat.apotheosis.ApotheosisHook;
import net.minecraftforge.client.event.ScreenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EnchModuleClient.class, remap = false)
public abstract class EnchModuleClientMixin {

    @Inject(method = "drawAnvilCostBlob", at = @At(value = "INVOKE", target = "Ldev/shadowsoffire/apotheosis/util/DrawsOnLeft;draw(Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;Lnet/minecraft/client/gui/GuiGraphics;Ljava/util/List;I)V"))
    private void tooltipoverhaul$apotheosis$beforeLeftTooltip(ScreenEvent.Render.Post event, CallbackInfo ci) {
        ApotheosisHook.enter();
    }

    @Inject(method = "drawAnvilCostBlob", at = @At(value = "INVOKE", target = "Ldev/shadowsoffire/apotheosis/util/DrawsOnLeft;draw(Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;Lnet/minecraft/client/gui/GuiGraphics;Ljava/util/List;I)V", shift = At.Shift.AFTER))
    private void tooltipoverhaul$apotheosis$afterLeftTooltip(ScreenEvent.Render.Post event, CallbackInfo ci) {
        ApotheosisHook.exit();
    }

}
