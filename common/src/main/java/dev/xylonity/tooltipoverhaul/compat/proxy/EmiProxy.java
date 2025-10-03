package dev.xylonity.tooltipoverhaul.compat.proxy;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

/**
 * Proxy used for dedicated EMI compat, in order to 'detect' the hovered stack on EMI categories (and the recipe viewer)
 */
public final class EmiProxy {

    private static final String CLASS_LOCATION = "dev.xylonity.tooltipoverhaul.compat.emi.EmiHoverHolder";
    private static final String CLASS_METHOD = "getItemStack";

    public static ItemStack getItemStack() {
        if (!TooltipOverhaul.PLATFORM.isModLoaded("emi")) return ItemStack.EMPTY;
        try {
            Class<?> holder = Class.forName(CLASS_LOCATION, false, EmiProxy.class.getClassLoader());
            Method method = holder.getMethod(CLASS_METHOD);
            Object stack = method.invoke(null);
            return (stack instanceof ItemStack s) ? s : ItemStack.EMPTY;
        }
        catch (Exception ignored) {
            return ItemStack.EMPTY;
        }

    }

    // Fallbacks to the category viewer if there is no hovered stack in the current coordinates (for the recipe viewer)
    public static ItemStack getItemStack(int mouseX, int mouseY) {
        if (!TooltipOverhaul.PLATFORM.isModLoaded("emi")) return ItemStack.EMPTY;
        try {
            Class<?> holder = Class.forName(CLASS_LOCATION, false, EmiProxy.class.getClassLoader());
            Method method = holder.getMethod(CLASS_METHOD, int.class, int.class);
            Object stack = method.invoke(null, mouseX, mouseY);
            return (stack instanceof ItemStack s) ? s : ItemStack.EMPTY;
        }
        catch (NoSuchMethodException e) {
            return getItemStack();
        }
        catch (Exception ignored) {
            return ItemStack.EMPTY;
        }

    }

}
