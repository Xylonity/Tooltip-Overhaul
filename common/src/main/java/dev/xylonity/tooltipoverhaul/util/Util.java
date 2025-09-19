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

import java.awt.*;

public class Util {

    public static float calcRotY(double time) {
        return (float) ((System.currentTimeMillis() / time) * 360 % 360);
    }

    public static boolean shouldDisableIcon(ItemStack stack) {
        return !stack.isEmpty() && CustomFrameManager.of(stack).map(CustomFrameData::shouldDisableIcon).orElse(TooltipsConfig.DISABLE_ICON);
    }

    public static boolean shouldDisableRating(ItemStack stack) {
        return !stack.isEmpty() && CustomFrameManager.of(stack).map(CustomFrameData::shouldDisableRating).orElse(TooltipsConfig.DISABLE_RATING);
    }

    public static String getIconAppearAnimation(TooltipContext context) {
        return context.data().isPresent() ? context.data().get().getIconAppearAnimation() : TooltipsConfig.ICON_APPEAR_ANIMATION;
    }

    public static float getIconRotatingSpeed(TooltipContext context) {
        return context.data().isPresent() ? context.data().get().getIconRotatingSpeed() : TooltipsConfig.ICON_ROTATING_SPEED;
    }

    public static int getDividerLineColor(TooltipContext ctx) {
        if (ctx.data().isPresent()) {
            return parseDividerLineColor(ctx.data().get().getDividerLineColor(), ctx.stack());
        }

        return parseDividerLineColor(TooltipsConfig.DIVIDER_LINE_COLOR, ctx.stack());
    }

    private static int parseDividerLineColor(String matcher, ItemStack stack) {
        switch (matcher) {
            case "match_inner_frame_color" -> {
                return switch (stack.getRarity()) {
                    case COMMON -> Palette.COMMON[0];
                    case UNCOMMON -> Palette.UNCOMMON[0];
                    case RARE -> Palette.RARE[0];
                    case EPIC -> Palette.EPIC[0];
                    default -> Palette.LEGENDARY[0];
                };
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

    public static int getTitleAlignmentX(int posx, int offset, Point size, ClientTooltipComponent component, Font font) {
        int startX = posx + offset;
        int rightX = posx + size.x - TooltipRenderer.PADDING_X;

        int textWidth = component.getWidth(font);
        int available = rightX - startX;

        int result;
        switch (TooltipsConfig.TITLE_X_ALIGNMENT) {
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

    public static int getRatingAlignmentX(int posx, int offset, Point size, Component rarity, Font font) {
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
        switch (TooltipsConfig.RATING_X_ALIGNMENT) {
            case "middle" -> result = startX + (available - compWidth) / 2;
            case "right"  -> result = rightX - compWidth;
            default -> // left
                    result = startX;
        }

        if (available <= 0) return startX;

        return Math.max(startX, Math.min(result, rightX - compWidth));
    }

    public static String getDefaultRarity(ItemStack stack) {
        return Component.translatable("tooltipoverhaul." + stack.getRarity().toString().toLowerCase() + "_rarity").getString();
    }

}