package dev.xylonity.tooltipoverhaul.client.render;

import dev.xylonity.tooltipoverhaul.mixin.ClientTextTooltipAccessor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks the tooltips within the current frame
 *
 * Vanilla draws every tooltip at the same depth, so when two of them share a frame the later one simply paints over the earlier one (preventing a bug
 * where 2 tooltips were cast on the screen when hovering over a status effect icon)
 */
public final class TooltipFrameStack {

    /**
     * Depth added to the second tooltip of a frame
     */
    public static final int EXTRA_DEPTH = 1000;
    private static final int MAX_INDEX = 1;

    private static long frame = -1;

    private static List<Drawn> current = new ArrayList<>();
    private static List<Drawn> previous = List.of();

    /**
     * Registers a tooltip about to be cast and returns how many were drawn before it this frame
     */
    public static int register(TooltipContext context) {
        roll();
        if (current.size() > 32) {
            current.clear();
        }

        current.add(new Drawn(context.getMouseX(), context.getMouseY(), lines(context.getComponents())));

        return Math.min(MAX_INDEX, current.size() - 1);
    }

    /**
     * JEED draws its extended effect tooltip on both Pre and Post, so this comparation is done
     */
    public static boolean superseded(TooltipContext context) {
        roll();
        final List<String> lines = lines(context.getComponents());
        return supersededIn(current, context, lines, true) || supersededIn(previous, context, lines, false);
    }

    private static boolean supersededIn(List<Drawn> drawn, TooltipContext context, List<String> lines, boolean orEqual) {
        for (final Drawn entry : drawn) {
            if (entry.mouseX != context.getMouseX() || entry.mouseY != context.getMouseY() || entry.lines.size() < lines.size()) {
                continue;
            }

            if ((orEqual || entry.lines.size() > lines.size()) && entry.lines.subList(0, lines.size()).equals(lines)) {
                return true;
            }

        }

        return false;
    }

    private static void roll() {
        final long now = TooltipAnimationState.frame();
        if (now != frame) {
            previous = current;
            current = new ArrayList<>();
            frame = now;
        }

    }

    private static List<String> lines(List<ClientTooltipComponent> components) {
        final List<String> result = new ArrayList<>(components.size());
        for (ClientTooltipComponent component : components) {
            if (component instanceof ClientTextTooltip text) {
                final StringBuilder builder = new StringBuilder();
                ((ClientTextTooltipAccessor) text).getText().accept((index, style, codePoint) -> {
                    builder.appendCodePoint(codePoint);
                    return true;
                });

                result.add(builder.toString());
            }
            else {
                result.add(component.getClass().getName());
            }

        }

        return result;
    }

    private record Drawn(
            int mouseX,
            int mouseY,
            List<String> lines
    ) {
        ;;
    }


}