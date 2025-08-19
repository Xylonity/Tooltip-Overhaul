package dev.xylonity.tooltipoverhaul.client.wrap;

import dev.xylonity.tooltipoverhaul.client.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.mixin.ClientTextTooltipAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles a simplex text wrapping, ensuring that tooltip lines do not exceed the maximum width of
 * the screen, automatically splitting long lines into multiple ones
 */
public final class TooltipWrapper {

    /**
     * Core wrapping call
     */
    public static List<ClientTooltipComponent> wrap(Font font, List<ClientTooltipComponent> orig, int screenWidth, ItemStack stack) {
        if (orig == null || orig.isEmpty()) return orig;

        boolean hasIcon = !stack.isEmpty();
        // Base padding of the tooltip (extra padding + sides)
        int basePadding = TooltipRenderer.PADDING_X * 2 + 4;
        int iconPadding = hasIcon ? 26 : 0;

        // Allowed text width (3/4 of the screen width)
        int maxAllowed = Math.max(60, (int) (screenWidth * 0.75f) - basePadding - iconPadding);

        // Checks if wrapping is needed
        boolean shouldWrap = false;
        for (ClientTooltipComponent c : orig) {
            if (c instanceof ClientTextTooltip && c.getWidth(font) > maxAllowed) {
                shouldWrap = true;
                break;
            }

        }

        if (!shouldWrap) return orig;

        // Process each component indiv
        List<ClientTooltipComponent> text = new ArrayList<>(orig.size() + 8);
        for (ClientTooltipComponent component : orig) {
            // only text is legible
            if (!(component instanceof ClientTextTooltip)) {
                text.add(component);
                continue;
            }

            // no wrapping if text fits
            if (component.getWidth(font) <= maxAllowed) {
                text.add(component);
                continue;
            }

            // extractor
            FormattedCharSequence sequence = getSequence(component);
            if (sequence == null) {
                text.add(component);
                continue;
            }

            // Splits into multiple lines within allowed width
            for (FormattedText part : font.getSplitter().splitLines(toFormattedText(sequence), maxAllowed, Style.EMPTY)) {
                text.add(new ClientTextTooltip(Language.getInstance().getVisualOrder(part)));
            }
        }

        return text;
    }

    /**
     * Extrats the actual text sequence from the clienttooltipcomponent
     */
    private static FormattedCharSequence getSequence(ClientTooltipComponent c) {
        if (c instanceof ClientTextTooltip t) {
            return ((ClientTextTooltipAccessor)t).getText();
        }

        return null;
    }

    /**
     * Converts a FormattedCharSequence into a FormattedText, preserving the diff from formating styling
     */
    private static FormattedText toFormattedText(FormattedCharSequence sequence) {
        List<String> chunks = new ArrayList<>();
        List<Style> styles = new ArrayList<>();
        StringBuilder string = new StringBuilder();
        Style[] current = { Style.EMPTY };

        // Iterator that goes through group of chars with the same style
        sequence.accept((pos, style, cp) -> {
            if (!style.equals(current[0])) {
                // commits previous chunk before starting a new one
                if (!string.isEmpty()) {
                    chunks.add(string.toString());
                    styles.add(current[0]);
                    string.setLength(0);
                }

                current[0] = style;
            }

            string.appendCodePoint(cp);

            return true;
        });

        if (!string.isEmpty()) {
            chunks.add(string.toString());
            styles.add(current[0]);
        }

        return new FormattedText() {
            @Override
            public <T> Optional<T> visit(ContentConsumer<T> consumer) {
                for (String part : chunks) {
                    consumer.accept(part);
                }

                return Optional.empty();
            }
            @Override
            public <T> Optional<T> visit(StyledContentConsumer<T> consumer, Style base) {
                for (int i = 0; i < chunks.size(); i++) {
                    consumer.accept(styles.get(i).applyTo(base), chunks.get(i));
                }

                return Optional.empty();
            }

        };

    }

}