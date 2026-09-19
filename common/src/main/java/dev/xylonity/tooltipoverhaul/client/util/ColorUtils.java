package dev.xylonity.tooltipoverhaul.client.util;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ColorUtils {

    public static int[] rgb(int argb) {
        return new int[] {red(argb), green(argb), blue(argb)};
    }

    public static int withAlpha(int color, int alpha) {
        return (AnimationUtils.clamp255(alpha) << 24) | (color & 0x00FFFFFF);
    }

    public static int mixRgb(int from, int to, float progress) {
        final float time = AnimationUtils.clamp01(progress);
        final int red = (int) (red(from) + (red(to) - red(from)) * time);
        final int green = (int) (green(from) + (green(to) - green(from)) * time);
        final int blue = (int) (blue(from) + (blue(to) - blue(from)) * time);
        return (red << 16) | (green << 8) | blue;
    }

    public static int dimAccent(int accent) {
        return ((red(accent) * 11 / 20) << 16) | ((green(accent) * 11 / 20) << 8) | (blue(accent) * 11 / 20);
    }

    public static int getIconBackgroundColor(TooltipContext context, int fallback) {
        String color = Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getIconBackgroundColor).orElse(TooltipsConfig.ICON_BACKGROUND_COLOR);
        return color.isBlank() || color.equalsIgnoreCase("default") ? fallback : ConfigColorParser.parseColor(color);
    }

    public static int getIconBorderColor(TooltipContext context, int fallback) {
        String color = Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getIconBorderColor).orElse(TooltipsConfig.ICON_BORDER_COLOR);
        return color.isBlank() || color.equalsIgnoreCase("default") ? fallback : ConfigColorParser.parseColor(color);
    }

    public static int getCompactModNameColor(TooltipContext context) {
        return ConfigColorParser.parseColor(Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getCompactModNameColor).orElse(TooltipsConfig.COMPACT_MOD_NAME_COLOR));
    }

    public static int getDividerLineColor(TooltipContext context) {
        return parseDividerLineColor(Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getDividerLineColor).orElse(TooltipsConfig.DIVIDER_LINE_COLOR), context);
    }

    public static int getBackgroundColor(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getBackgroundColor).orElse(Palette.PANEL_BG);
    }

    public static int[] getInnerOverlayColors(TooltipContext context) {
        int[] colors;
        final CustomFrameData data = context.getFrameData();
        if (data != null) {
            final CustomFrameData.GradientType gradientType = data.getGradientType();
            if (gradientType != CustomFrameData.GradientType.CUSTOM) {
                colors = Arrays.copyOf(Palette.of(gradientType), 3);
            }
            else {
                if (data.getBorderType().startsWith("auto") && data.hasCustomTexture()) {
                    colors = ColorExtractor.getOverlayGradient(TooltipOverhaul.rawPathOf(data.getTextureLocation()));
                }
                else {
                    final int[] configuredColors = data.getGradientColors(context);
                    colors = configuredColors.length == 3 ? configuredColors : Arrays.copyOf(getColorsPerRarity(context), 3);
                }

            }

        }
        else {
            colors = Arrays.copyOf(getColorsPerRarity(context), 3);
        }

        return colors;
    }

    public static int[] getRenderedInnerOverlayColors(TooltipContext context) {
        int[] colors = getInnerOverlayColors(context);
        switch (RenderUtils.getInnerOverlayType(context)) {
            case "static", "auto_static" -> Arrays.fill(colors, colors[0]);
            case "glint", "auto_glint" -> colors[2] = 0;
        }

        return colors;
    }

    public static int getInnerOverlayColorAtY(TooltipContext context, int[] colors, float y) {
        final int startY = (int) (context.getTooltipPosition().y - 2) + 1;
        final int height = (int) (context.getTooltipSize().y + 4) - 2;
        final int middle = height / 2;
        final float offset = y - startY;
        if (offset < middle) {
            return lerpColor(colors[0], colors[1], offset / Math.max(1, middle));
        }

        return lerpColor(colors[1], colors[2], (offset - middle) / Math.max(1, height - middle));
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

    /**
     * Formats a color as #RRGGBB
     */
    public static String formatHex(int argb, boolean forceAlpha) {
        return forceAlpha || alpha(argb) != 0xFF ? String.format("#%08X", argb) : String.format("#%06X", argb & 0x00FFFFFF);
    }

    public static List<String> colorEntries(String raw) {
        List<String> colors = new ArrayList<>();
        if (raw == null) {
            return colors;
        }

        for (String token : raw.split("[,;]")) {
            colors.add(token.trim());
        }

        return colors;
    }

    public static List<String> colorTokens(String raw) {
        List<String> colors = colorEntries(raw);
        colors.removeIf(String::isBlank);
        return colors;
    }

    public static int hsvToRgb(float hue, float saturation, float value) {
        final float chroma = value * saturation;
        final float sector = ((hue % 360f) + 360f) % 360f / 60f;
        final float intermediate = chroma * (1f - Math.abs(sector % 2f - 1f));

        float red = 0;
        float green = 0;
        float blue = 0;
        if (sector < 1f) {
            red = chroma;
            green = intermediate;
        }
        else if (sector < 2f) {
            red = intermediate;
            green = chroma;
        }
        else if (sector < 3f) {
            green = chroma;
            blue = intermediate;
        }
        else if (sector < 4f) {
            green = intermediate;
            blue = chroma;
        }
        else if (sector < 5f) {
            red = intermediate;
            blue = chroma;
        }
        else {
            red = chroma;
            blue = intermediate;
        }

        final float match = value - chroma;
        return Math.round((red + match) * 255f) << 16 | Math.round((green + match) * 255f) << 8 | Math.round((blue + match) * 255f);
    }

    public static float[] rgbToHsv(int rgb) {
        final float red = red(rgb) / 255f;
        final float green = green(rgb) / 255f;
        final float blue = blue(rgb) / 255f;
        final float maximum = Math.max(red, Math.max(green, blue));
        final float minimum = Math.min(red, Math.min(green, blue));
        final float delta = maximum - minimum;

        float hue = 0;
        if (delta > 0) {
            if (maximum == red) {
                hue = 60f * (((green - blue) / delta) % 6f);
            }
            else if (maximum == green) {
                hue = 60f * ((blue - red) / delta + 2f);
            }
            else {
                hue = 60f * ((red - green) / delta + 4f);
            }

        }

        if (hue < 0) {
            hue += 360f;
        }

        return new float[] {hue, maximum == 0 ? 0 : delta / maximum, maximum};
    }

    private static int parseDividerLineColor(String matcher, TooltipContext context) {
        final ItemStack stack = context.getStack();
        switch (matcher) {
            case "match_inner_frame_color" -> {
                if (context.getFrameData() != null) {
                    return getInnerOverlayColors(context)[0];
                }

                return getFirstColorOfRarity(context);
            }

            case "match_item_name_color" -> {
                final TextColor color = stack.getHoverName().getStyle().getColor();
                final TextColor rarityColor = TextColor.fromLegacyFormat(stack.getRarity().color);
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
        final Rarity rarity = context.getStack().getRarity();
        int[] colors = Palette.CUSTOM_RARITY;

        if (rarity == Rarity.COMMON) {
            colors = Palette.COMMON;
        }

        if (rarity == Rarity.UNCOMMON) {
            colors = Palette.UNCOMMON;
        }

        if (rarity == Rarity.RARE) {
            colors = Palette.RARE;
        }

        if (rarity == Rarity.EPIC) {
            colors = Palette.EPIC;
        }

        return Arrays.copyOf(colors, 3);
    }

    public static int getFirstColorOfRarity(TooltipContext context) {
        return getColorsPerRarity(context)[0];
    }

    public static int mulAlpha(int color, float scale) {
        int alpha = (color >>> 24) & 0xFF;
        final int newA = Math.max(0, Math.min(255, Math.round(alpha * scale)));
        return (color & 0x00FFFFFF) | (newA << 24);
    }

    public static int lerpColor(int c0, int c1, float time) {
        time = AnimationUtils.clamp01(time);

        final int alpha = (int) (ColorUtils.alpha(c0) + (ColorUtils.alpha(c1) - ColorUtils.alpha(c0)) * time);
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
        final int red = (argb >>> 16) & 0xFF;
        final int green = (argb >>> 8) & 0xFF;
        final int blue = (argb) & 0xFF;

        final float[] hsv = Color.RGBtoHSB(red, green, blue, null);

        final float hue = hsv[0];
        final float saturation = AnimationUtils.clamp01(hsv[1] * saturationMul);
        final float value = AnimationUtils.clamp01(hsv[2] * valueMul);

        final int rgb = Color.HSBtoRGB(hue, saturation, value) & 0x00FFFFFF;
        return 0xFF000000 | rgb;
    }

}