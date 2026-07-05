package dev.xylonity.tooltipoverhaul.client.util;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.mixin.KeyMappingAccessor;
import dev.xylonity.tooltipoverhaul.registry.TooltipOverhaulKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;

import javax.annotation.Nullable;
import java.util.List;

public class EquippedContextCalculator {

    public static @Nullable TooltipContext from(GuiGraphics graphics, Font font, int mouseX, int mouseY, ClientTooltipPositioner tooltipPositioner, ItemStack fromStack, int screenWidth, int screenHeight) {

        if (!isComparisonRequested()) {
            return null;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        final Player player = minecraft.player;

        if (player == null) {
            return null;
        }

        // Armor is compared against the piece currently worn in the matching slot, and weapons and tools (any tiered item, such as swords or axes) are
        // compared against the mainhand item, but only when it is itself a tiered item so the comparison stays weapon-weapon
        ItemStack equippedStack;
        if (fromStack.getItem() instanceof Equipable equippableStack) {
            EquipmentSlot slot = equippableStack.getEquipmentSlot();
            // offhand index collides with the leggings armor index lmao
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                equippedStack = player.getInventory().getArmor(slot.getIndex());
            }
            else {
                equippedStack = player.getItemBySlot(slot);
            }

        }
        else if (fromStack.getItem() instanceof TieredItem) {
            equippedStack = player.getMainHandItem();
            if (!(equippedStack.getItem() instanceof TieredItem)) {
                return null;
            }

        }
        else {
            return null;
        }

        if (equippedStack.isEmpty() || ItemStack.isSameItemSameTags(fromStack, equippedStack)) {
            return null;
        }

        final List<ClientTooltipComponent> componentList = TextUtils.getTooltipComponentsFrom(equippedStack, font, screenWidth, 2.2f);
        return new TooltipContext(graphics, font, componentList, mouseX, mouseY, screenWidth, screenHeight, tooltipPositioner, equippedStack, false);
    }

    /**
     * The comparison is requested either while the compare key is held or at all times when the always-show
     * config option is enabled
     */
    public static boolean isComparisonRequested() {
        if (TooltipsConfig.ALWAYS_SHOW_COMPARISON) {
            return true;
        }

        InputConstants.Key compareKey = ((KeyMappingAccessor) TooltipOverhaulKeyMappings.COMPARE_TOOLTIP).tooltipoverhaul$key();
        return !compareKey.equals(InputConstants.UNKNOWN) && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), compareKey.getValue());
    }

}
