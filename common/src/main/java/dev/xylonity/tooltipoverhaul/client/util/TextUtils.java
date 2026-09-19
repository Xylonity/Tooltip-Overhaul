package dev.xylonity.tooltipoverhaul.client.util;

import com.google.common.collect.Lists;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.mixin.ClientTextTooltipAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TextUtils {

    public static boolean shouldDisableScrolling(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::shouldDisableScrolling).orElse(TooltipsConfig.DISABLE_TOOLTIP_SCROLLING);
    }

    /**
     * Adds vanilla's durability line without enabling the item id and component count
     */
    public static List<ClientTooltipComponent> appendDurability(List<ClientTooltipComponent> components, ItemStack stack) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (!TooltipsConfig.SHOW_ITEM_DURABILITY || stack.isEmpty() || !stack.isDamaged() || minecraft.options.advancedItemTooltips) {
            return components;
        }

        final List<ClientTooltipComponent> result = new ArrayList<>(components);
        final int insertionIndex = ModNameUtils.hasTrailingModName(result, stack) ? result.size() - 1 : result.size();

        result.add(insertionIndex, ClientTooltipComponent.create(Component.translatable("item.durability",
                stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage()).getVisualOrderText()));

        return result;
    }

    /**
     * Adds the item id, skipped when F3+H already shows it
     */
    public static List<ClientTooltipComponent> appendRegistryName(List<ClientTooltipComponent> components, ItemStack stack) {
        if (!TooltipsConfig.SHOW_REGISTRY_NAME || stack.isEmpty() || Minecraft.getInstance().options.advancedItemTooltips) {
            return components;
        }

        final List<ClientTooltipComponent> result = new ArrayList<>(components);
        final int insertionIndex = ModNameUtils.hasTrailingModName(result, stack) ? result.size() - 1 : result.size();

        result.add(insertionIndex, ClientTooltipComponent.create(Component.literal(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText()));

        return result;
    }

    public static List<ClientTooltipComponent> getTooltipComponentsFrom(ItemStack stack, Font font, int screenWidth, float screenSplit) {
        final Minecraft minecraft = Minecraft.getInstance();
        final Player player = minecraft.player;
        final List<ClientTooltipComponent> componentList = Lists.newArrayList();

        final List<Component> originalComponentLines = stack.getTooltipLines(player, minecraft.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL);
        for (Component originalComponentLine : originalComponentLines) {
            final List<FormattedCharSequence> wrappedLines = font.split(originalComponentLine, Math.max((int)(screenWidth / screenSplit), 200));

            if (wrappedLines.isEmpty()) {
                componentList.add(ClientTooltipComponent.create(FormattedCharSequence.EMPTY));
            }
            else if (wrappedLines.size() == 1) {
                componentList.add(ClientTooltipComponent.create(originalComponentLine.getVisualOrderText()));
            }
            else {
                for (FormattedCharSequence wrappedLine : wrappedLines) {
                    componentList.add(ClientTooltipComponent.create(wrappedLine));
                }

            }

        }

        stack.getTooltipImage().ifPresent(component -> {
            if (component instanceof BundleTooltip bundleTooltip) {
                final int idx = originalComponentLines.size() > 1 ? 1 : componentList.size();
                componentList.add(idx, ClientTooltipComponent.create(bundleTooltip));
            }

        });

        return componentList;
    }

    public static List<ClientTooltipComponent> wrapToWidth(List<ClientTooltipComponent> components, Font font, int firstLineWidth, int lineWidth) {
        List<ClientTooltipComponent> result = null;
        for (int i = 0; i < components.size(); i++) {
            final ClientTooltipComponent component = components.get(i);
            final int limit = Math.max(40, i == 0 ? firstLineWidth : lineWidth);
            if (!(component instanceof ClientTextTooltip text) || component.getWidth(font) <= limit) {
                if (result != null) {
                    result.add(component);
                }

                continue;
            }

            if (result == null) {
                result = new ArrayList<>(components.subList(0, i));
            }

            for (FormattedCharSequence line : font.split(toText(((ClientTextTooltipAccessor) text).getText()), limit)) {
                result.add(ClientTooltipComponent.create(line));
            }

        }

        return result == null ? components : result;
    }

    /**
     * Rebuilds a component from an already ordered sequence keeping each style run so the font splitter can rewrap it
     */
    private static Component toText(FormattedCharSequence sequence) {
        final MutableComponent result = Component.empty();
        final StringBuilder run = new StringBuilder();
        final Style[] current = {null};
        sequence.accept((index, style, codePoint) -> {
            if (current[0] != null && !style.equals(current[0])) {
                result.append(Component.literal(run.toString()).withStyle(current[0]));
                run.setLength(0);
            }

            current[0] = style;
            run.appendCodePoint(codePoint);

            return true;
        });

        if (run.length() > 0 && current[0] != null) {
            result.append(Component.literal(run.toString()).withStyle(current[0]));
        }

        return result;
    }

    public static Component getRatingText(TooltipContext context) {
        // Computes the default color per rarity
        // Defaults to a simulated legendary rarity with gold color
        // Can't use enum checking here as it would crash on certain rarity enum injections
        ChatFormatting color = ChatFormatting.GOLD;
        final Rarity rarity = context.getStack().getRarity();
        if (rarity == Rarity.COMMON) {
            color = ChatFormatting.GRAY;
        }
        else if (rarity == Rarity.UNCOMMON) {
            color = ChatFormatting.YELLOW;
        }
        else if (rarity == Rarity.RARE) {
            color = ChatFormatting.BLUE;
        }
        else if (rarity == Rarity.EPIC) {
            color = ChatFormatting.DARK_PURPLE;
        }

        final CustomFrameData data = context.getFrameData();
        // If the curent stack is declared in a custom frame format
        if (data != null) {
            // If the stack has a custom rating
            if (data.hasCustomItemRating()) {

                // Computes the rating, either as a translatable key or a literal component
                final String raw = data.getItemRating(context.getStack());
                final MutableComponent base = raw.startsWith("key.tooltipoverhaul") ? Component.translatable(raw) : Component.literal(raw);

                if (data.hasCustomColorItemRating()) {
                    return base.withStyle(Style.EMPTY.withColor(data.getItemRatingColor(context)));
                }
                // If the frame doesn't provide a color, uses rarity color by default
                else {
                    return base.withStyle(color);
                }

            }

        }

        return getDefaultRarity(context.getStack()).copy().withStyle(color);
    }

    private static Component getDefaultRarity(ItemStack stack) {
        final Rarity rarity = stack.getRarity();
        final String string = rarity.toString();
        if (rarity == Rarity.COMMON || rarity == Rarity.UNCOMMON || rarity == Rarity.RARE || rarity == Rarity.EPIC) {
            return Component.translatable("tooltipoverhaul." + string.trim().toLowerCase() + "_rarity");
        }

        if (string.contains("alexscaves")) {
            return Component.translatable("rarity.alexscaves." + string.split(":")[1] + ".name");
        }

        return Component.translatable(rarity.name().trim().toLowerCase());
    }

}
