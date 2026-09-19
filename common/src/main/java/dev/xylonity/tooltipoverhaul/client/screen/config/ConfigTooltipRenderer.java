package dev.xylonity.tooltipoverhaul.client.screen.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.drawCard;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.*;

/**
 * Positions and renders a tooltip card
 */
final class ConfigTooltipRenderer {

    static void render(GuiGraphics graphics, Font font, List<Section> sections, int mouseX, int mouseY, int screenWidth, int screenHeight, int minimumY, int accent, float fade) {
        final List<Section> visible = sections.stream().filter(section -> !section.lines().isEmpty()).toList();
        if (visible.isEmpty()) {
            return;
        }

        final int alpha = (int) (0xFF * fade);
        if (alpha < 0x10) {
            return;
        }

        int textWidth = 0;
        int lineCount = 0;
        for (Section section : visible) {
            lineCount += section.lines().size();
            for (FormattedCharSequence line : section.lines()) {
                textWidth = Math.max(textWidth, font.width(line));
            }

        }

        final int paddingX = 6;
        final int paddingY = 5;
        final int sectionGaps = Math.max(0, visible.size() - 1) * 3;
        final int boxWidth = textWidth + paddingX * 2;
        final int boxHeight = lineCount * (font.lineHeight + 1) - 1 + sectionGaps + paddingY * 2;
        final int x = Math.min(mouseX + 10, screenWidth - 4 - boxWidth);
        int y = mouseY - boxHeight - 4;
        if (y < minimumY) {
            y = mouseY + 12;
        }

        y = Math.min(y, screenHeight - 4 - boxHeight);

        graphics.pose().pushPose();

        graphics.pose().translate(0, (1f - fade) * 3f, 400);
        drawCard(graphics, x, y, x + boxWidth, y + boxHeight, ((int) (0xF2 * fade) << 24) | 0x121214, withAlpha(mixRgb(0x2E2E32, dimAccent(accent), 0.4f), alpha));

        int lineY = y + paddingY;
        for (int sectionIndex = 0; sectionIndex < visible.size(); sectionIndex++) {
            final Section section = visible.get(sectionIndex);
            if (sectionIndex > 0) {
                lineY += 3;
            }

            for (FormattedCharSequence line : section.lines()) {
                graphics.drawString(font, line, x + paddingX, lineY, withAlpha(section.color(), alpha), false);
                lineY += font.lineHeight + 1;
            }

        }

        graphics.pose().popPose();
    }

    record Section(
            List<FormattedCharSequence> lines,
            int color
    ) {
        ;;
    }

}
