package dev.xylonity.tooltipoverhaul.client.util;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.awt.*;
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
                if (data.getBorderType().startsWith("auto") && data.hasCustomTexture()) {
                    colors = ColorExtractor.getOverlayGradient(TooltipOverhaul.rawPathOf(data.getTextureLocation()));
                }
                else {
                    int[] configuredColors = data.getGradientColors(context);
                    colors = configuredColors.length == 3 ? configuredColors : Arrays.copyOf(getColorsPerRarity(context), 3);
                }

            }

        }
        else {
            colors = Arrays.copyOf(getColorsPerRarity(context), 3);
        }

        return colors;
    }

    public static int alpha(int argb) {
        return (argb >>> 24) & 0xFF;
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

    private static int parseDividerLineColor(String matcher, TooltipContext context) {
        ItemStack stack = context.getStack();
        switch (matcher) {
            case "match_inner_frame_color" -> {
                if (context.getFrameData() != null) {
                    return getInnerOverlayColors(context)[0];
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

    public static int mulAlpha(int color, float scale) {
        int alpha = (color >>> 24) & 0xFF;
        int newA = Math.max(0, Math.min(255, Math.round(alpha * scale)));
        return (color & 0x00FFFFFF) | (newA << 24);
    }

    public static int lerpColor(int c0, int c1, float time) {
        time = AnimationUtils.clamp01(time);

        int alpha = (int) (ColorUtils.alpha(c0) + (ColorUtils.alpha(c1) - ColorUtils.alpha(c0)) * time);
        int red = (int) (ColorUtils.red(c0) + (ColorUtils.red(c1) - ColorUtils.red(c0)) * time);
        int green = (int) (ColorUtils.green(c0) + (ColorUtils.green(c1) - ColorUtils.green(c0)) * time);
        int blue = (int) (ColorUtils.blue(c0) + (ColorUtils.blue(c1) - ColorUtils.blue(c0)) * time);

        return (AnimationUtils.clamp255(alpha) << 24) | (AnimationUtils.clamp255(red) << 16) | (AnimationUtils.clamp255(green) << 8) | AnimationUtils.clamp255(blue);
    }

    public static double srgbToLinear(double value) {
        return value <= 0.04045 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
    }

    public static double linearToSrgb(double value) {
        return value <= 0.0031308 ? value * 12.92 : 1.055 * Math.pow(value, 1f / 2.4) - 0.055;
    }

    public static int tweakHSV(int argb, float saturationMul, float valueMul) {
        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = (argb) & 0xFF;

        float[] hsv = Color.RGBtoHSB(red, green, blue, null);

        float h = hsv[0];
        float s = AnimationUtils.clamp01(hsv[1] * saturationMul);
        float v = AnimationUtils.clamp01(hsv[2] * valueMul);

        int rgb = Color.HSBtoRGB(h, s, v) & 0x00FFFFFF;
        return 0xFF000000 | rgb;
    }

}
