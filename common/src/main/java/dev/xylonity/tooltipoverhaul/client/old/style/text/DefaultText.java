package dev.xylonity.tooltipoverhaul.client.old.style.text;

import dev.xylonity.tooltipoverhaul.client.old.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.old.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.old.TooltipScrollState;
import dev.xylonity.tooltipoverhaul.client.old.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.old.layer.bridge.ITooltipText;
import dev.xylonity.tooltipoverhaul.util.TextAxis;
import dev.xylonity.tooltipoverhaul.util.TextType;
import dev.xylonity.tooltipoverhaul.util.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

/**
 * Handles the core rendering of both text and images per se
 */
public class DefaultText implements ITooltipText {

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size, Component rarity, Font font) {
        ctx.push(() -> {
            ctx.translate(0, 0, depth.getZ());

            boolean hasStack = !ctx.stack().isEmpty();
            boolean shouldShowRating = Util.shouldShowRating(ctx.stack());
            int firstLineOffset = TooltipRenderer.PADDING_X + (hasStack ? 26 : 0 ) - (Util.shouldDisableIcon(ctx.stack()) ? 26 : 0);

            // If there is a stack present adds padding to the left
            if (hasStack && rarity != null && !rarity.getString().isEmpty() && shouldShowRating) {
                int rx = Util.getRatingAlignmentX((int) pos.x + Util.getExtraTextPosition(ctx, TextType.RATING, TextAxis.X), firstLineOffset, size, rarity, font, ctx);
                int ry = (int) pos.y + TooltipRenderer.PADDING_Y + 13 + Util.getExtraTextPosition(ctx, TextType.RATING, TextAxis.Y);

                // Rating text
                ctx.graphics().drawString(font, rarity, rx, ry, 0xEDDE76, false);
            }

            if (!TooltipScrollState.isIsActive()) {
                int y = (int) pos.y + TooltipRenderer.PADDING_Y + 3 + (shouldShowRating || !hasStack ? 0 : 6) + Util.getExtraTextPosition(ctx, TextType.TITLE, TextAxis.Y);
                for (int i = 0; i < ctx.getComponents().size(); i++) {
                    ClientTooltipComponent component = (ClientTooltipComponent) ctx.getComponents().get(i);

                    if (i == 1) {
                        y += (hasStack ? 3 : 0) - (shouldShowRating || !hasStack ? 0 : 6) - Util.getExtraTextPosition(ctx, TextType.TITLE, TextAxis.Y) + Util.getExtraTextPosition(ctx, TextType.DESCRIPTION, TextAxis.Y);
                    }

                    if (i == 1) {
                        if (hasStack || Util.shouldDisableIcon(ctx.stack())) {
                            y += 12;
                        }
                        if (Util.shouldDisableDividerLine(ctx)) {
                            y -= 6;
                        }
                    }

                    int x = (int) pos.x;

                    if (i == 0) {
                        x = Util.getTitleAlignmentX(x + Util.getExtraTextPosition(ctx, TextType.TITLE, TextAxis.X) - (!hasStack ? 1 : 0), firstLineOffset, size, component, font, ctx);
                    }
                    else {
                        x += TooltipRenderer.PADDING_X + Util.getExtraTextPosition(ctx, TextType.DESCRIPTION, TextAxis.X);
                    }

                    component.renderText(font, x, y, ctx.pose().last().pose(), ctx.graphics().bufferSource());

                    y += component.getHeight();

                    if (hasStack && i == 0 && ctx.getComponents().size() > 1) {
                        y += 6;
                    }
                }

                y = (int) pos.y + TooltipRenderer.PADDING_Y + 6;
                for (int i = 0; i < ctx.getComponents().size(); i++) {
                    ClientTooltipComponent component = (ClientTooltipComponent) ctx.getComponents().get(i);

                    if (hasStack && i == 1) {
                        y += 12;
                    }

                    int x = (int) pos.x + (i == 0 ? firstLineOffset : TooltipRenderer.PADDING_X);
                    component.renderImage(font, x, y, ctx.graphics());

                    y += component.getHeight();

                    if (hasStack && i == 0 && ctx.getComponents().size() > 1) {
                        y += 6;
                    }
                }

                return;
            }

            int yTitle = (int) pos.y + TooltipRenderer.PADDING_Y + 3;
            if (!ctx.getComponents().isEmpty()) {
                ClientTooltipComponent title = (ClientTooltipComponent) ctx.getComponents().get(0);
                int xTitle = (int) pos.x + firstLineOffset;
                title.renderText(font, xTitle, yTitle, ctx.pose().last().pose(), ctx.graphics().bufferSource());
                // Hotfix for invisible tooltip stack title name on certain scrollable items (so the title isn't affected by the scissor)
                ctx.flush();
            }

            int toLeft = (int) pos.x + TooltipRenderer.PADDING_X;
            int toTop = TooltipRenderer.LAST_POS_YI + TooltipRenderer.LAST_HEADER_ABS + 7;
            int toRight = (int) pos.x + size.x - TooltipRenderer.PADDING_X;
            int toBottom = Math.min(TooltipRenderer.LAST_POS_YI + size.y - TooltipRenderer.PADDING_Y - 1, ctx.height() - 4);
            ctx.graphics().enableScissor(toLeft, toTop, toRight, toBottom);

            int scroll = TooltipScrollState.getScroll();

            int y = TooltipRenderer.LAST_POS_YI + TooltipRenderer.LAST_HEADER_ABS - scroll;
            for (int i = 1; i < ctx.getComponents().size(); i++) {
                ClientTooltipComponent component = (ClientTooltipComponent) ctx.getComponents().get(i);
                int x = (int) pos.x + TooltipRenderer.PADDING_X;
                int h = component.getHeight();
                if (y + h >= toTop && y <= toBottom) {
                    component.renderText(font, x, y, ctx.pose().last().pose(), ctx.graphics().bufferSource());
                }

                y += h;
            }

            // Image rendering
            y = TooltipRenderer.LAST_POS_YI + TooltipRenderer.LAST_HEADER_ABS - scroll;
            for (int i = 1; i < ctx.getComponents().size(); i++) {
                ClientTooltipComponent component = (ClientTooltipComponent) ctx.getComponents().get(i);

                int x = (int) pos.x + TooltipRenderer.PADDING_X;
                int h = component.getHeight();
                if (y + h >= toTop && y <= toBottom) {
                    component.renderImage(font, x, y, ctx.graphics());
                }
                y += h;
            }

            ctx.graphics().disableScissor();
        });

    }

}
