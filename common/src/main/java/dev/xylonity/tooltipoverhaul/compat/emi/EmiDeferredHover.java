package dev.xylonity.tooltipoverhaul.compat.emi;

import net.minecraft.world.item.ItemStack;

public final class EmiDeferredHover {

    private static ItemStack LAST = ItemStack.EMPTY;

    public static void set(ItemStack stack) {
        LAST = (stack == null || stack.isEmpty()) ? ItemStack.EMPTY : stack.copy();
    }

    public static ItemStack pop() {
        ItemStack stack = LAST;
        LAST = ItemStack.EMPTY;
        return stack;
    }

}
