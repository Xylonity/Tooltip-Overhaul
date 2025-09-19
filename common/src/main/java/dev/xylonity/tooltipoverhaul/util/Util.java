package dev.xylonity.tooltipoverhaul.util;

import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameManager;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
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