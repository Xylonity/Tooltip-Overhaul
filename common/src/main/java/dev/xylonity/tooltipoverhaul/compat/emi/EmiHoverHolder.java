package dev.xylonity.tooltipoverhaul.compat.emi;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

/**
 * This is a compat layer for EMI. It's only referenced through it's proxy
 */
public final class EmiHoverHolder {

    private static final String RECIPESCREEN_CLASS_LOCATION = "dev.emi.emi.screen.RecipeScreen";

    /**
     * Hard bypass for tag categories (containing the 'tag' word)
     */
    private static boolean isTagCategory() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen == null) return false;

            Class<?> recipeScreen = Class.forName(RECIPESCREEN_CLASS_LOCATION, false, EmiHoverHolder.class.getClassLoader());
            if (!recipeScreen.isInstance(minecraft.screen)) return false;

            Method method = recipeScreen.getMethod("getFocusedCategory");
            Object category = method.invoke(minecraft.screen);
            if (category == null) return false;

            Method getId = category.getClass().getMethod("getId");
            Object idValue = getId.invoke(category);
            String id = (idValue == null) ? null : idValue.toString();
            if (id == null) return false;

            String ss = id.toLowerCase(Locale.ROOT);
            return ss.contains("tag");
        }
        catch (Exception ignored) {
            return false;
        }

    }

    private static boolean isTagIngredient(EmiIngredient ei) {
        try {
            String isIng = ei.getClass().getName().toLowerCase(Locale.ROOT);
            return isIng.contains("tag");
        }
        catch (Throwable ignored) {
            ;;
        }

        return false;
    }

    public static ItemStack getItemStack() {
        if (isTagCategory()) return ItemStack.EMPTY;

        ItemStack fromCache = EmiDeferredHover.pop();
        if (!fromCache.isEmpty()) {
            return fromCache;
        }

        ItemStack itemStack = fromRecipeScreen();
        if (!itemStack.isEmpty()) {
            return itemStack;
        }

        EmiStackInteraction internal = EmiApi.getHoveredStack(true);
        itemStack = getStack(internal);
        if (!itemStack.isEmpty()) {
            return itemStack;
        }

        return ItemStack.EMPTY;
    }

    public static ItemStack getItemStack(int mouseX, int mouseY) {
        if (isTagCategory()) return ItemStack.EMPTY;

        ItemStack fromCache = EmiDeferredHover.pop();
        if (!fromCache.isEmpty()) {
            return fromCache;
        }

        ItemStack itemStack = fromRecipeScreen();
        if (!itemStack.isEmpty()) {
            return itemStack;
        }

        EmiStackInteraction internal = EmiApi.getHoveredStack(mouseX, mouseY, true);
        itemStack = getStack(internal);
        if (!itemStack.isEmpty()) {
            return itemStack;
        }

        internal = EmiApi.getHoveredStack(mouseX, mouseY, false);
        itemStack = getStack(internal);
        if (!itemStack.isEmpty()) {
            return itemStack;
        }

        internal = EmiApi.getHoveredStack(true);
        itemStack = getStack(internal);
        if (!itemStack.isEmpty()) {
            return itemStack;
        }

        return ItemStack.EMPTY;
    }

    private static ItemStack fromRecipeScreen() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen == null) return ItemStack.EMPTY;

            Class<?> resource = Class.forName(RECIPESCREEN_CLASS_LOCATION, false, EmiHoverHolder.class.getClassLoader());
            if (!resource.isInstance(minecraft.screen)) return ItemStack.EMPTY;

            Method method = resource.getMethod("getHoveredStack");
            Object ingred = method.invoke(minecraft.screen);
            if (!(ingred instanceof EmiIngredient s)) {
                return ItemStack.EMPTY;
            }

            if (isTagIngredient(s)) {
                return ItemStack.EMPTY;
            }

            return getStack(s);
        }
        catch (Exception ignore) {
            ;;
        }

        return ItemStack.EMPTY;
    }

    private static ItemStack getStack(EmiStackInteraction hovered) {
        if (hovered == null || hovered.isEmpty()) return ItemStack.EMPTY;

        EmiIngredient ingred = hovered.getStack();
        if (ingred == null) return ItemStack.EMPTY;

        if (isTagIngredient(ingred)) return ItemStack.EMPTY;

        return getStack(ingred);
    }

    private static ItemStack getStack(EmiIngredient ingred) {
        if (ingred == null) return ItemStack.EMPTY;

        List<EmiStack> list = ingred.getEmiStacks();
        if (list == null || list.isEmpty()) return ItemStack.EMPTY;

        for (EmiStack es : list) {
            if (es == null) continue;
            ItemStack st = es.getItemStack();
            if (st != null && !st.isEmpty()) return st;
        }

        return ItemStack.EMPTY;
    }

}