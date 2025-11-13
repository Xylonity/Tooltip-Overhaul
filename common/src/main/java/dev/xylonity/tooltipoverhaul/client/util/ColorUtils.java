package dev.xylonity.tooltipoverhaul.client.util;

import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.Arrays;
import java.util.Optional;

public class ColorUtils {

    public static int getDividerLineColor(TooltipContext context) {
        return parseDividerLineColor(Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getDividerLineColor).orElse(TooltipsConfig.DIVIDER_LINE_COLOR), context);
    }

    public static int getBackgroundColor(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getBackgroundColor).orElse(Palette.PANEL_BG);
    }

    public static int[] getInnerOverlayColors(TooltipContext context) {
        int[] colors;
        CustomFrameData data = context.getFrameData();
        if (data != null) {
            CustomFrameData.GradientType gradientType = data.getGradientType();
            if (gradientType != CustomFrameData.GradientType.CUSTOM) {
                colors = Arrays.copyOf(Palette.of(gradientType), 3);
            }
            else {
                int[] configuredColors = data.getGradientColors(context);
                colors = configuredColors.length == 3 ? configuredColors : Arrays.copyOf(getColorsPerRarity(context), 3);
            }

        }
        else {
            colors = Arrays.copyOf(getColorsPerRarity(context), 3);
        }

        return colors;
    }

    public static int red(int argb) {
        return (argb >>> 16) & 0xFF;
    }

    public static int green(int argb) {
        return (argb >>> 8) & 0xFF;
    }

    public static int blue(int argb) {
        return argb & 0xFF;
    }

    public static int alpha(int argb) {
        return (argb >>> 24) & 0xFF;
    }

    private static int parseDividerLineColor(String matcher, TooltipContext context) {
        ItemStack stack = context.getStack();
        switch (matcher) {
            case "match_inner_frame_color" -> {
                if (context.getFrameData() != null && context.getFrameData().hasGradientColors()) {
                    return context.getFrameData().getGradientColors(context)[0];
                }

                return getFirstColorOfRarity(context);
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

    public static int[] getColorsPerRarity(TooltipContext context) {
        Rarity rarity = context.getStack().getRarity();
        int[] colors = Palette.LEGENDARY;

        if (rarity == Rarity.COMMON) colors = Palette.COMMON;
        if (rarity == Rarity.UNCOMMON) colors = Palette.UNCOMMON;
        if (rarity == Rarity.RARE) colors = Palette.RARE;
        if (rarity == Rarity.EPIC) colors = Palette.EPIC;

        return Arrays.copyOf(colors, 3);
    }

    public static int getFirstColorOfRarity(TooltipContext context) {
        return getColorsPerRarity(context)[0];
    }

}
