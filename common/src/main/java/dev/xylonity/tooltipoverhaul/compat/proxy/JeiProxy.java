package dev.xylonity.tooltipoverhaul.compat.proxy;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

/**
 * Proxy used for dedicated JEI compat, in order to 'detect' the hovered stack on JEI categories
 */
public final class JeiProxy {

    private static final String CLASS_LOCATION = "dev.xylonity.tooltipoverhaul.compat.jei.JeiHoverHolder";
    private static final String CLASS_METHOD = "getItemStack";

    public static ItemStack getItemStack() {
        if (!TooltipOverhaul.PLATFORM.isModLoaded("jei")) return ItemStack.EMPTY;
        try {
            Class<?> holder = Class.forName(CLASS_LOCATION, false, JeiProxy.class.getClassLoader());
            Method method = holder.getMethod(CLASS_METHOD);
            Object stack = method.invoke(null);
            return (stack instanceof ItemStack s) ? s : ItemStack.EMPTY;
        }
        catch (Exception ignored) {
            return ItemStack.EMPTY;
        }
    }

}
