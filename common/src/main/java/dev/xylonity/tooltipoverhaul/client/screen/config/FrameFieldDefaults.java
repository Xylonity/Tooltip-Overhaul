package dev.xylonity.tooltipoverhaul.client.screen.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.xylonity.tooltipoverhaul.client.frame.FrameTemplates;
import dev.xylonity.tooltipoverhaul.client.util.Palette;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.Arrays;
import java.util.stream.Collectors;

import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.formatHex;

final class FrameFieldDefaults {

    static String read(JsonObject root, JsonObject entry, String key, ItemStack sample) {
        JsonObject effective = entry;
        try {
            effective = FrameTemplates.resolve(root, entry);
        }
        catch (RuntimeException ignored) {
            ;;
        }

        final JsonElement value = effective.get(key);
        if (value == null || value.isJsonNull()) {
            return text(key, sample);
        }

        if (value.isJsonArray()) return value.getAsJsonArray().asList().stream()
                .filter(JsonElement::isJsonPrimitive).map(JsonElement::getAsString).collect(Collectors.joining(", "));
        if (!value.isJsonPrimitive()) {
            return "";
        }

        if (key.equals("backgroundColor") && value.getAsJsonPrimitive().isNumber()) {
            return formatHex(value.getAsInt(), true);
        }

        return value.getAsString();
    }

    static String text(String key, ItemStack sample) {
        return switch (key) {
            case "priority", "durabilityMin" -> "0";
            case "durabilityMax" -> "100";
            case "texture" -> TooltipsConfig.GLOBAL_FRAME_OVERLAY_LOCATION;
            case "backgroundColor" -> formatHex(Palette.PANEL_BG, true);
            case "iconBackgroundColor" -> TooltipsConfig.ICON_BACKGROUND_COLOR;
            case "iconBorderColor" -> TooltipsConfig.ICON_BORDER_COLOR;
            case "gradientColors" -> Arrays.stream(palette(sample)).mapToObj(c -> formatHex(c, true)).collect(Collectors.joining(", "));
            case "iconSize" -> "1.0";
            case "iconRotatingSpeed" -> Float.toString(TooltipsConfig.ICON_ROTATING_SPEED);
            case "itemRating" -> "Common";
            case "colorItemRating" -> formatHex(palette(sample)[0], true);
            case "compactModNameColor" -> TooltipsConfig.COMPACT_MOD_NAME_COLOR;
            case "tooltipPositionX" -> Integer.toString(TooltipsConfig.TOOLTIP_POSITION_X);
            case "tooltipPositionY" -> Integer.toString(TooltipsConfig.TOOLTIP_POSITION_Y);
            case "mainPanelPaddingX" -> Integer.toString(TooltipsConfig.MAIN_PANEL_PADDING_X);
            case "mainPanelPaddingY" -> Integer.toString(TooltipsConfig.MAIN_PANEL_PADDING_Y);
            case "dividerLineColor" -> TooltipsConfig.DIVIDER_LINE_COLOR;
            case "dividerLineTopPadding" -> Integer.toString(TooltipsConfig.DIVIDER_LINE_TOP_PADDING);
            case "dividerLineBottomPadding" -> Integer.toString(TooltipsConfig.DIVIDER_LINE_BOTTOM_PADDING);
            case "secondPanelX" -> Integer.toString(TooltipsConfig.SECOND_PANEL_X);
            case "secondPanelY" -> Integer.toString(TooltipsConfig.SECOND_PANEL_Y);
            case "secondPanelSizeX" -> Integer.toString(TooltipsConfig.SECOND_PANEL_SIZE_X);
            case "secondPanelSizeY" -> Integer.toString(TooltipsConfig.SECOND_PANEL_SIZE_Y);
            case "secondPanelRendererSpeed" -> Float.toString(TooltipsConfig.SECOND_PANEL_RENDERER_SPEED);
            case "effectSpeed" -> Float.toString(TooltipsConfig.EFFECT_SPEED);
            case "effectIntensity" -> Float.toString(TooltipsConfig.EFFECT_INTENSITY);
            case "effectDensity" -> Float.toString(TooltipsConfig.EFFECT_DENSITY);
            case "tooltipAnimationDuration" -> Float.toString(TooltipsConfig.TOOLTIP_ANIMATION_DURATION);
            default -> "";
        };
    }

    private static int[] palette(ItemStack stack) {
        final Rarity rarity = stack.getRarity();
        if (rarity == Rarity.COMMON) {
            return Palette.COMMON;
        }

        if (rarity == Rarity.UNCOMMON) {
            return Palette.UNCOMMON;
        }

        if (rarity == Rarity.RARE) {
            return Palette.RARE;
        }

        if (rarity == Rarity.EPIC) {
            return Palette.EPIC;
        }

        return Palette.CUSTOM_RARITY;
    }

}