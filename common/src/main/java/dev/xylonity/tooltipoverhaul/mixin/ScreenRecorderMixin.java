package dev.xylonity.tooltipoverhaul.mixin;

import dev.xylonity.tooltipoverhaul.client.render.TooltipAnimationState;
import dev.xylonity.tooltipoverhaul.client.render.TooltipHoverTracker;
import dev.xylonity.tooltipoverhaul.client.util.TooltipScrollState;
import dev.xylonity.tooltipoverhaul.compat.emi.EmiDeferredHover;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.List;

@Mixin(value = Screen.class)
abstract class ScreenRecorderMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void to$emiRecord(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        final Object self = this;
        try {
            final ClassLoader classLoader = self.getClass().getClassLoader();
            final Class<?> recipeScreenInstance = Class.forName("dev.emi.emi.screen.RecipeScreen", false, classLoader);
            if (!recipeScreenInstance.isInstance(self)) {
                return;
            }

            final Method getStack = recipeScreenInstance.getMethod("getHoveredStack");
            final Object emiIngredient = getStack.invoke(self);
            if (emiIngredient == null) {
                return;
            }

            final Method emiStacks = emiIngredient.getClass().getMethod("getEmiStacks");
            final Object listt = emiStacks.invoke(emiIngredient);
            if (!(listt instanceof List<?> list) || list.isEmpty()) {
                return;
            }

            for (final Object object : list) {
                if (object == null) {
                    continue;
                }

                final Method realItemStack = object.getClass().getMethod("getItemStack");
                final Object is = realItemStack.invoke(object);
                if (is instanceof ItemStack stack && !stack.isEmpty()) {
                    EmiDeferredHover.set(stack);
                    break;
                }

            }

        }
        catch (Throwable ignored) {
            ;;
        }

    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void onScreenRemoved(CallbackInfo ci) {
        TooltipScrollState.reset();
        TooltipAnimationState.clear();
        TooltipHoverTracker.clear();
    }

}
