package dev.xylonity.tooltipoverhaul.client.util;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.mixin.KeyMappingAccessor;
import dev.xylonity.tooltipoverhaul.registry.TooltipOverhaulKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class EquippedContextCalculator {

    public static @Nullable TooltipContext from(GuiGraphics graphics, Font font, int mouseX, int mouseY, ClientTooltipPositioner tooltipPositioner, ItemStack fromStack, int screenWidth, int screenHeight) {

        InputConstants.Key compareKey = ((KeyMappingAccessor) TooltipOverhaulKeyMappings.COMPARE_TOOLTIP).tooltipoverhaul$key();
        boolean isKeyDown = false;
        if (!compareKey.equals(InputConstants.UNKNOWN)) {
            isKeyDown = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), compareKey.getValue());
        }

        if (!isKeyDown) {
            return null;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (!(fromStack.getItem() instanceof Equipable equippableStack) || player == null) {
            return null;
        }

        ItemStack equippedArmorStack = player.getInventory().getArmor(equippableStack.getEquipmentSlot().getIndex());
        if (equippedArmorStack.isEmpty() || ItemStack.isSameItemSameComponents(fromStack, equippedArmorStack)) {
            return null;
        }

        List<ClientTooltipComponent> componentList = TextUtils.getTooltipComponentsFrom(equippedArmorStack, font, screenWidth, 2.2f);

        return new TooltipContext(graphics, font, componentList, mouseX, mouseY, screenWidth, screenHeight, tooltipPositioner, equippedArmorStack, false);
    }

}
