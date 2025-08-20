package dev.xylonity.tooltipoverhaul.mixin;

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

@Mixin(value = Screen.class, remap = false)
abstract class ScreenRecorderMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void to$emiRecord(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Object self = this;
        try {
            ClassLoader classL = self.getClass().getClassLoader();
            Class<?> recipeScreenInst = Class.forName("dev.emi.emi.screen.RecipeScreen", false, classL);
            if (!recipeScreenInst.isInstance(self)) return;

            Method getStack = recipeScreenInst.getMethod("getHoveredStack");
            Object emiIngredient = getStack.invoke(self);
            if (emiIngredient == null) return;

            Method emiStacks = emiIngredient.getClass().getMethod("getEmiStacks");
            Object listt = emiStacks.invoke(emiIngredient);
            if (!(listt instanceof List<?> list) || list.isEmpty()) return;

            for (Object object : list) {
                if (object == null) continue;
                Method realItemStack = object.getClass().getMethod("getItemStack");
                Object is = realItemStack.invoke(object);
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

}
