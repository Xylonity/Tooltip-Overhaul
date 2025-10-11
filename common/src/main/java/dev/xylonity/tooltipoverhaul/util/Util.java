package dev.xylonity.tooltipoverhaul.util;

import dev.xylonity.tooltipoverhaul.client.Palette;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameManager;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.awt.*;

public class Util {

    public static float calcRotY(double time) {
        return (float) ((System.currentTimeMillis() / time) * 360 % 360);
    }

    public static boolean shouldDisableIcon(ItemStack stack) {
        return !stack.isEmpty() && CustomFrameManager.of(stack).map(CustomFrameData::shouldDisableIcon).orElse(TooltipsConfig.DISABLE_ICON);
    }

    public static int getSecondPanelOffset(TooltipContext ctx, TextAxis axis) {
        if (axis == TextAxis.X) {
            return ctx.data().map(CustomFrameData::getSecondPanelX).orElse(TooltipsConfig.SECOND_PANEL_X);
        }
        else {
            return ctx.data().map(CustomFrameData::getSecondPanelY).orElse(TooltipsConfig.SECOND_PANEL_Y);
        }

    }

    public static int getMainPanelPadding(TooltipContext ctx, TextAxis axis) {
        if (axis == TextAxis.X) {
            return ctx.data().map(CustomFrameData::getMainPanelPaddingX).orElse(TooltipsConfig.MAIN_PANEL_PADDING_X);
        }
        else {
            return ctx.data().map(CustomFrameData::getMainPanelPaddingY).orElse(TooltipsConfig.MAIN_PANEL_PADDING_Y);
        }

    }

    public static boolean isScrollingDisabled(TooltipContext ctx) {
        return ctx.data().map(CustomFrameData::shouldDisableScrolling).orElse(TooltipsConfig.DISABLE_TOOLTIP_SCROLLING);
    }

    public static int getBackgroundColor(TooltipContext ctx) {
        return ctx.data().map(CustomFrameData::getBackgroundColor).orElse(Palette.PANEL_BG);
    }

    public static boolean shouldShowRating(ItemStack stack) {
        return !stack.isEmpty() && CustomFrameManager.of(stack).map(CustomFrameData::shouldShowRating).orElse(TooltipsConfig.SHOW_RATING);
    }

    public static boolean shouldDisableDividerLine(TooltipContext ctx) {
        return ctx.data().map(CustomFrameData::shouldDisableDividerLine).orElse(TooltipsConfig.DISABLE_DIVIDER_LINE);
    }

    public static String getIconAppearAnimation(TooltipContext context) {
        return context.data().map(CustomFrameData::getIconAppearAnimation).orElse(TooltipsConfig.ICON_APPEAR_ANIMATION);
    }

    public static float getIconRotatingSpeed(TooltipContext context) {
        return context.data().isPresent() ? context.data().get().getIconRotatingSpeed() : TooltipsConfig.ICON_ROTATING_SPEED;
    }

    public static int getDividerLineColor(TooltipContext ctx) {
        return parseDividerLineColor(ctx.data().map(CustomFrameData::getDividerLineColor).orElse(TooltipsConfig.DIVIDER_LINE_COLOR), ctx);
    }

    private static int parseDividerLineColor(String matcher, TooltipContext ctx) {
        ItemStack stack = ctx.stack();
        switch (matcher) {
            case "match_inner_frame_color" -> {
                if (ctx.data().isPresent() && ctx.data().get().hasGradientColors()) {
                    return ConfigColorParser.parseColor(ctx.data().get().getGradientColors().get(0));
                }

                return getColorPerRarity(stack);
            }
            case "match_item_name_color" -> {
                TextColor color = stack.getHoverName().getStyle().getColor();
                TextColor rarityColor = TextColor.fromLegacyFormat(stack.getRarity().color);
                if (color != null) {
                    return color.getValue();
                }
                else if (rarityColor != null) {
                    return rarityColor.getValue();
                }
            }
            default -> {
                if (matcher.startsWith("0x") || matcher.startsWith("0X") || matcher.startsWith("#")) {
                    return ConfigColorParser.parseColor(matcher);
                }
            }

        }

        return 0xFFFFFFFF;
    }

    private static int getColorPerRarity(ItemStack stack) {
        final Rarity r = stack.getRarity();
        // Computes the default color per rarity
        // Defaults to a simulated legendary rarity
        int palette = Palette.LEGENDARY[0];
        if (r == Rarity.COMMON) palette = Palette.COMMON[0];
        if (r == Rarity.UNCOMMON) palette = Palette.UNCOMMON[0];
        if (r == Rarity.RARE) palette = Palette.RARE[0];
        if (r == Rarity.EPIC) palette = Palette.EPIC[0];
        return palette;
    }

    public static int getExtraTextPosition(TooltipContext ctx, TextType type, TextAxis axis) {
        return switch (axis) {
            case X -> switch (type) {
                case TITLE -> ctx.data().map(CustomFrameData::getTitlePositionX).orElse(TooltipsConfig.TITLE_POSITION_X);
                case RATING -> ctx.data().map(CustomFrameData::getRatingPositionX).orElse(TooltipsConfig.RATING_POSITION_X);
                case DESCRIPTION -> ctx.data().map(CustomFrameData::getTooltipDescriptionPositionX).orElse(TooltipsConfig.TOOLTIP_DESCRIPTION_POSITION_X);
            };
            case Y -> switch (type) {
                case TITLE -> ctx.data().map(CustomFrameData::getTitlePositionY).orElse(TooltipsConfig.TITLE_POSITION_Y);
                case RATING -> ctx.data().map(CustomFrameData::getRatingPositionY).orElse(TooltipsConfig.RATING_POSITION_Y);
                case DESCRIPTION -> ctx.data().map(CustomFrameData::getTooltipDescriptionPositionY).orElse(TooltipsConfig.TOOLTIP_DESCRIPTION_POSITION_Y);
            };
        };

    }

    public static int getTitleAlignmentX(int posx, int offset, Point size, ClientTooltipComponent component, Font font, TooltipContext ctx) {
        int startX = posx + offset;
        int rightX = posx + size.x - TooltipRenderer.PADDING_X;

        int textWidth = component.getWidth(font);
        int available = rightX - startX;

        int result;
        switch (ctx.data().map(CustomFrameData::getTitleAlignment).orElse(TooltipsConfig.TITLE_X_ALIGNMENT)) {
            case "middle" -> result = startX + (available - textWidth) / 2;
            case "right" -> result = rightX - textWidth;
            default -> // left
                    result = startX;
        }

        if (available <= 0) {
            return startX;
        }

        result = Math.max(startX, Math.min(result, rightX - Math.min(textWidth, available)));

        return result;
    }

    public static int getRatingAlignmentX(int posx, int offset, Point size, Component rarity, Font font, TooltipContext ctx) {
        int startX = posx + offset;
        int rightX = posx + size.x - TooltipRenderer.PADDING_X;
        int available = Math.max(0, rightX - startX);

        int compWidth = font.width(rarity);

        if (available > 0 && compWidth > available) {
            int maxLine = 0;
            for (FormattedCharSequence line : font.split(rarity, available)) {
                maxLine = Math.max(maxLine, font.width(line));
            }

            compWidth = Math.min(maxLine, available);
        }

        int result;
        switch (ctx.data().map(CustomFrameData::getRatingAlignment).orElse(TooltipsConfig.RATING_X_ALIGNMENT)) {
            case "middle" -> result = startX + (available - compWidth) / 2;
            case "right"  -> result = rightX - compWidth;
            default -> // left
                    result = startX;
        }

        if (available <= 0) return startX;

        return Math.max(startX, Math.min(result, rightX - compWidth));
    }

    public static Component getDefaultRarity(ItemStack stack) {
        Rarity r = stack.getRarity();
        String string = r.toString();
        if (r == Rarity.COMMON || r == Rarity.UNCOMMON || r == Rarity.RARE || r == Rarity.EPIC) {
            return Component.translatable("tooltipoverhaul." + string.trim().toLowerCase() + "_rarity");
        }

        if (string.contains("alexscaves")) {
            return Component.translatable("rarity.alexscaves." + string.split(":")[1] + ".name");
        }

        return Component.translatable(r.name().trim().toLowerCase());
    }

}