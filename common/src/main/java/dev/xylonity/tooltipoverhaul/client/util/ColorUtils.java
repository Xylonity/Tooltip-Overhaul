package dev.xylonity.tooltipoverhaul.client.util;

import dev.xylonity.tooltipoverhaul.client.old.Palette;
import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.Optional;

public class ColorUtils {

    public static int getDividerLineColor(TooltipContext context) {
        return parseDividerLineColor(Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getDividerLineColor).orElse(TooltipsConfig.DIVIDER_LINE_COLOR), context);
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
                    return ConfigColorParser.parseColor(context.getFrameData().getGradientColors().get(0));
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

}
