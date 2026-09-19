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
     * Animation times after the appear delay. The icon always starts anew for a new hover session.
     */
    public static Timing timing(ItemStack stack, @Nullable Slot slot, float delay, boolean settleOnSwitch) {
        return timing(stack, slot, delay, settleOnSwitch, TooltipAnimationState.frame(), System.currentTimeMillis());
    }

    static Timing timing(ItemStack stack, @Nullable Slot slot, float delay, boolean settleOnSwitch, long frame, long now) {

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

            final boolean inheritStart = settleOnSwitch && previous != null;
            match = new Session(stack.copy(), inheritStart ? previous.start : now, now, inheritStart ? 0 : delay);
            SESSIONS.add(match);
        }

        match.slot = slot;
        match.lastFrame = frame;

        return new Timing((now - match.start) / 1000f - delay, (now - match.iconStart) / 1000f - match.iconDelay);
    }

    public static void clear() {
        SESSIONS.clear();
    }

    private static final class Session {

        private final ItemStack stack;
        private final long start;
        private final long iconStart;
        private final float iconDelay;
        @Nullable
        private Slot slot;
        private long lastFrame;

        private Session(ItemStack stack, long start, long iconStart, float iconDelay) {
            this.stack = stack;
            this.start = start;
            this.iconStart = iconStart;
            this.iconDelay = iconDelay;
        }

        private boolean matches(ItemStack other, @Nullable Slot otherSlot) {
            // Workaround for stacks updating their nbt info on hovering from replaying the same animation
            return ItemStack.isSameItemSameTags(stack, other) || (otherSlot != null && otherSlot == slot && ItemStack.isSameItem(stack, other));
        }

    }


    public record Timing(
            float tooltipSeconds,
            float iconSeconds
    ) {
        ;;
    }


}
