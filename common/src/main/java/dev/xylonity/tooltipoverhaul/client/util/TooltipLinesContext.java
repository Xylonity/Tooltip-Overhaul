package dev.xylonity.tooltipoverhaul.client.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Resolves the stack of tooltips drawn from plain lines, to avoid showing up the same itemstack icon on nested tooltips
 */
public final class TooltipLinesContext {

    private static Lines lastLines;
    private static ItemStack stack = ItemStack.EMPTY;

    public static void capture(ItemStack source, List<Component> lines) {
        lastLines = new Lines(lines, source);
    }

    /**
     * Called before some lines get rendered as a tooltip
     */
    public static void begin(List<Component> lines) {
        final Lines captured = lastLines;
        stack = captured != null && captured.lines() == lines ? captured.stack() : ItemStack.EMPTY;
    }

    /**
     * The stack is consumed on read so it can't leak into another tooltip
     */
    public static ItemStack consume() {
        final ItemStack result = stack;
        stack = ItemStack.EMPTY;
        return result;
    }

    public static void clear() {
        stack = ItemStack.EMPTY;
    }

    private record Lines(
            List<Component> lines,
            ItemStack stack
    ) {
        ;;
    }

}