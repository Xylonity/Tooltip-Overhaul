package dev.xylonity.tooltipoverhaul.client.util;

import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class TextUtils {

    public static Component computeRatingText(TooltipContext context) {
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
                    return base.withStyle(Style.EMPTY.withColor(data.getItemRatingColor(context.getStack())));
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
