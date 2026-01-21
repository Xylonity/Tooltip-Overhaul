package dev.xylonity.tooltipoverhaul.compat.proxy;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

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

    public static boolean isEmiTooltip(List<ClientTooltipComponent> components) {
        if (isEmiScreen()) {
            return true;
        }

        if (components != null) {
            for (ClientTooltipComponent component : components) {
                if (component == null) {
                    continue;
                }

                String packageName = component.getClass().getName().toLowerCase(Locale.ROOT);
                if (packageName.contains("dev.emi") || packageName.contains(".emi.")) {
                    return true;
                }

            }

        }

        return false;
    }

    private static boolean isEmiScreen() {
        Screen screen = Minecraft.getInstance().screen;
        if (screen == null) {
            return false;
        }

        String packageName = screen.getClass().getName().toLowerCase(Locale.ROOT);
        return packageName.contains("dev.emi") || packageName.contains(".emi.");
    }

}
