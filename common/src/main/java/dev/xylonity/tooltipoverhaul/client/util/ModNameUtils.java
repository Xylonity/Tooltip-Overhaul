package dev.xylonity.tooltipoverhaul.client.util;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.mixin.ClientTextTooltipAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the display name of the mod that adds an item, so it can be appended as the last tooltip line
 */
public final class ModNameUtils {

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    public static String getModName(ItemStack stack) {
        final String namespace = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
        return CACHE.computeIfAbsent(namespace, vNamespace -> {
            if ("minecraft".equals(vNamespace)) {
                return "Minecraft";
            }

            return TooltipOverhaul.PLATFORM.getModDisplayName(vNamespace).orElseGet(() -> capitalize(vNamespace));
        });

    }

    /**
     * Returns a new list with the mod name appended as a blue italic last line (ignored if the component list already includes such line)
     */
    public static List<ClientTooltipComponent> appendModName(List<ClientTooltipComponent> components, ItemStack stack) {
        if (!TooltipsConfig.SHOW_MOD_NAME || stack.isEmpty()) {
            return components;
        }

        final String name = getModName(stack);
        if (!components.isEmpty() && name.equalsIgnoreCase(plainTextOf(components.get(components.size() - 1)))) {
            return components;
        }

        final List<ClientTooltipComponent> result = new ArrayList<>(components);
        result.add(ClientTooltipComponent.create(Component.literal(name).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC).getVisualOrderText()));

        return result;
    }

    private static String plainTextOf(ClientTooltipComponent component) {
        if (!(component instanceof ClientTextTooltip text)) {
            return "";
        }

        final StringBuilder stringBuilder = new StringBuilder();
        ((ClientTextTooltipAccessor) text).getText().accept((index, style, codePoint) -> {
            stringBuilder.appendCodePoint(codePoint);
            return true;
        });

        return stringBuilder.toString();
    }

    private static String capitalize(String namespace) {
        return namespace.isEmpty() ? namespace : Character.toUpperCase(namespace.charAt(0)) + namespace.substring(1);
    }

}
