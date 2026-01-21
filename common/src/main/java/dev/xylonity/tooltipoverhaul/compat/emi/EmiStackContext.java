package dev.xylonity.tooltipoverhaul.compat.emi;

import net.minecraft.world.item.ItemStack;

public final class EmiStackContext {

    private static final ThreadLocal<ItemStack> STACK = ThreadLocal.withInitial(() -> ItemStack.EMPTY);

    public static void set(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            STACK.set(ItemStack.EMPTY);
            return;
        }

        STACK.set(stack.copy());
    }

    public static ItemStack get() {
        ItemStack stack = STACK.get();
        return (stack == null) ? ItemStack.EMPTY : stack;
    }

    public static void clear() {
        STACK.set(ItemStack.EMPTY);
    }

}
