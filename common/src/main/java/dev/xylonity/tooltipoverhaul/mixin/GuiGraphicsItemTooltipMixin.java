package dev.xylonity.tooltipoverhaul.mixin;

import dev.xylonity.tooltipoverhaul.compat.emi.EmiStackContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public class GuiGraphicsItemTooltipMixin {

    @Inject(
            method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
            at = @At("HEAD")
    )
    private void tooltipoverhaul$captureStack(Font font, ItemStack stack, int mouseX, int mouseY, CallbackInfo ci) {
        EmiStackContext.set(stack);
    }

    @Inject(
            method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
            at = @At("RETURN")
    )
    private void tooltipoverhaul$clearStack(Font font, ItemStack stack, int mouseX, int mouseY, CallbackInfo ci) {
        EmiStackContext.clear();
    }

}
