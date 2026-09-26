package dev.xylonity.tooltipoverhaul.mixin;

import dev.xylonity.tooltipoverhaul.client.util.TooltipLinesContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Screen.class)
public class ScreenTooltipLinesMixin {

    @Inject(method = "getTooltipFromItem", at = @At("RETURN"))
    private static void tooltipoverhaul$captureLines(Minecraft minecraft, ItemStack stack, CallbackInfoReturnable<List<Component>> cir) {
        TooltipLinesContext.capture(stack, cir.getReturnValue());
    }

}
