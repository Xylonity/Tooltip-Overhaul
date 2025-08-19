package dev.xylonity.tooltipoverhaul.compat.emi;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * This is a compat layer for EMI. It's only referenced through it's proxy
 */
public final class EmiHoverHolder {

    public static ItemStack getItemStack() {
        return extractItemStack(EmiApi.getHoveredStack(true));
    }

    public static ItemStack getItemStack(int mouseX, int mouseY) {
        return extractItemStack(EmiApi.getHoveredStack(mouseX, mouseY, true));
    }

    private static ItemStack extractItemStack(EmiStackInteraction hovered) {
        if (hovered == null || hovered.isEmpty()) return ItemStack.EMPTY;

        EmiIngredient ingred = hovered.getStack();
        if (ingred == null || ingred.isEmpty()) return ItemStack.EMPTY;

        List<EmiStack> list = ingred.getEmiStacks();
        if (list == null || list.isEmpty()) return ItemStack.EMPTY;

        for (EmiStack emiStack : list) {
            if (emiStack == null) continue;

            ItemStack stack = emiStack.getItemStack();
            if (stack != null && !stack.isEmpty()) return stack;
        }

        return ItemStack.EMPTY;
    }

}
