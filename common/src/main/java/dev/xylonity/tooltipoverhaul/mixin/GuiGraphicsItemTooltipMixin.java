package dev.xylonity.tooltipoverhaul.mixin;

import dev.xylonity.tooltipoverhaul.client.util.TooltipLinesContext;
import dev.xylonity.tooltipoverhaul.compat.emi.EmiStackContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

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

    @Inject(
            method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V",
            at = @At("HEAD")
    )
    private void tooltipoverhaul$captureLinesStack(Font font, List<Component> lines, Optional<TooltipComponent> image, int mouseX, int mouseY, CallbackInfo ci) {
        TooltipLinesContext.begin(lines);
    }

    @Inject(
            method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V",
            at = @At("RETURN")
    )
    private void tooltipoverhaul$clearLinesStack(Font font, List<Component> lines, Optional<TooltipComponent> image, int mouseX, int mouseY, CallbackInfo ci) {
        TooltipLinesContext.clear();
    }

}
