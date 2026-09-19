package dev.xylonity.tooltipoverhaul.client.render;

import dev.xylonity.tooltipoverhaul.client.util.TooltipScrollState;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tracks how long each tooltip on the current frame has been hovered. Several tooltips can be rendered in the same
 * frame so each one keeps its own tracking session instead of resetting a single shared counter every call
 */
public final class TooltipHoverTracker {

    private static final List<Session> SESSIONS = new ArrayList<>();

    /**
     * Seconds since the given tooltip was first hovered
     */
    public static float seconds(ItemStack stack, @Nullable Slot slot, float delay, boolean settleOnSwitch) {
        final long frame = TooltipAnimationState.frame();
        final long now = System.currentTimeMillis();

        Session match = null;
        Session previous = null;
        for (Iterator<Session> iterator = SESSIONS.iterator(); iterator.hasNext(); ) {
            final Session session = iterator.next();
            if (frame - session.lastFrame > 1) {
                iterator.remove();
            }
            else if (match == null && session.matches(stack, slot)) {
                match = session;
            }
            else if (frame - session.lastFrame == 1 && (now - session.start) / 1000f >= delay && (previous == null || session.start < previous.start)) {
                previous = session;
            }

        }

        if (match == null) {
            TooltipScrollState.reset();
            if (SESSIONS.size() >= 8) {
                SESSIONS.remove(0);
            }

            match = new Session(stack.copy(), settleOnSwitch && previous != null ? previous.start : now);
            SESSIONS.add(match);
        }

        match.slot = slot;
        match.lastFrame = frame;

        return (now - match.start) / 1000f;
    }

    public static void clear() {
        SESSIONS.clear();
    }

    private static final class Session {

        private final ItemStack stack;
        private final long start;
        @Nullable
        private Slot slot;
        private long lastFrame;

        private Session(ItemStack stack, long start) {
            this.stack = stack;
            this.start = start;
        }

        private boolean matches(ItemStack other, @Nullable Slot otherSlot) {
            // Workaround for stacks updating their nbt info on hovering from replaying the same animation
            return ItemStack.isSameItemSameTags(stack, other) || (otherSlot != null && otherSlot == slot && ItemStack.isSameItem(stack, other));
        }

    }

}
