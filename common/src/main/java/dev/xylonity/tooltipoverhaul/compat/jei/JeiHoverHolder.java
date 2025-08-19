package dev.xylonity.tooltipoverhaul.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is a compat layer for JEI. It's only referenced through it's proxy
 */
public final class JeiHoverHolder {

    private static final AtomicReference<IIngredientListOverlay> LIST = new AtomicReference<>();
    private static final AtomicReference<IRecipesGui> RECIPES = new AtomicReference<>();
    private static final AtomicReference<IBookmarkOverlay> BOOKMARKS = new AtomicReference<>();

    public static void init(IJeiRuntime rt) {
        LIST.set(rt.getIngredientListOverlay());
        RECIPES.set(rt.getRecipesGui());
        BOOKMARKS.set(rt.getBookmarkOverlay());
    }

    public static ItemStack getItemStack() {
        try {
            IIngredientListOverlay ingredientList = LIST.get();
            if (ingredientList != null) {
                Optional<ItemStack> optional = Optional.ofNullable(ingredientList.getIngredientUnderMouse(VanillaTypes.ITEM_STACK));
                if (optional.isPresent()) {
                    ItemStack stack = optional.get();
                    if (!stack.isEmpty()) return stack;
                }
            }
        }
        catch (Exception ignored) {
            ;;
        }

        try {
            IRecipesGui recipes = RECIPES.get();
            if (recipes != null) {
                Optional<ItemStack> optional = recipes.getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
                if (optional.isPresent()) {
                    ItemStack stack = optional.get();
                    if (!stack.isEmpty()) return stack;
                }
            }
        }
        catch (Exception ignored) {
            ;;
        }

        try {
            IBookmarkOverlay bookmark = BOOKMARKS.get();
            if (bookmark != null) {
                Optional<ItemStack> optional = Optional.ofNullable(bookmark.getIngredientUnderMouse(VanillaTypes.ITEM_STACK));
                if (optional.isPresent()) {
                    ItemStack stack = optional.get();
                    if (!stack.isEmpty()) return stack;
                }
            }
        }
        catch (Exception ignored) {
            ;;
        }

        return ItemStack.EMPTY;
    }

}
