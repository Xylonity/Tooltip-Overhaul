package dev.xylonity.tooltipoverhaul.client.util;

import com.google.common.collect.Lists;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class TextUtils {

    public static List<ClientTooltipComponent> getTooltipComponentsFrom(ItemStack stack, Font font, int screenWidth, float screenSplit) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        List<ClientTooltipComponent> componentList = Lists.newArrayList();

        List<Component> originalComponentLines = stack.getTooltipLines(player, minecraft.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL);
        for (Component originalComponentLine : originalComponentLines) {
            List<FormattedCharSequence> wrappedLines = font.split(originalComponentLine, Math.max((int)(screenWidth / screenSplit), 200));

            if (wrappedLines.size() == 1) {
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
                int idx = originalComponentLines.size() > 1 ? 1 : componentList.size();
                componentList.add(idx, ClientTooltipComponent.create(bundleTooltip));
            }

        });

        return componentList;
    }

    public static Component getRatingText(TooltipContext context) {
        // Computes the default color per rarity
        // Defaults to a simulated legendary rarity with gold color
        // Can't use enum checking here as it would crash on certain rarity enum injections
        ChatFormatting color = ChatFormatting.GOLD;
        Rarity rarity = context.getStack().getRarity();
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

        CustomFrameData data = context.getFrameData();
        // If the curent stack is declared in a custom frame format
        if (data != null) {
            // If the stack has a custom rating
            if (data.hasCustomItemRating()) {

                // Computes the rating, either as a translatable key or a literal component
                String raw = String.valueOf(data.getItemRating(context.getStack()));
                MutableComponent base = raw.startsWith("key.tooltipoverhaul") ? Component.translatable(raw) : Component.literal(raw);

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
