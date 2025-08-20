package dev.xylonity.tooltipoverhaul.mixin;

import dev.xylonity.tooltipoverhaul.compat.proxy.EmiProxy;
import dev.xylonity.tooltipoverhaul.compat.proxy.JeiProxy;
import dev.xylonity.tooltipoverhaul.util.ITooltipOverhaulItemAware;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Hovered ItemStack locator. For dedicated mod compatibility, proxies (reflection) are used
 */
@Mixin(value = GuiGraphics.class, priority = 1)
public class GuiGraphicsItemMixin implements ITooltipOverhaulItemAware {
    @Final
    @Shadow
    private Minecraft minecraft;

    @Unique
    private ItemStack tooltipsOverhaul$currentItemStack = ItemStack.EMPTY;

    @Inject(method = "renderTooltipInternal", at = @At("HEAD"))
    private void tooltipsOverhaul$captureHovered(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner positioner, CallbackInfo ci) {
        tooltipsOverhaul$currentItemStack = ItemStack.EMPTY;

        // Jei compat
        ItemStack jeiStack = JeiProxy.getItemStack();
        if (!jeiStack.isEmpty()) {
            tooltipsOverhaul$currentItemStack = jeiStack;
            return;
        }

        // Emi compat
        ItemStack emiStack = EmiProxy.getItemStack(mouseX, mouseY);
        if (!emiStack.isEmpty()) {
            tooltipsOverhaul$currentItemStack = emiStack;
            return;
        }

        // Vanilla GUI hovered stack
        if (minecraft.screen instanceof AbstractContainerScreen<?> container) {
            try {
                Slot slot = ((AbstractContainerScreenMixin) container).getHoveredSlot();
                if (slot != null) {
                    ItemStack stack = slot.getItem();
                    if (!stack.isEmpty()) {
                        tooltipsOverhaul$currentItemStack = stack;
                    }
                }
            }
            catch (Throwable ignored) {
                ;;
            }
        }

    }

    @Override
    public ItemStack tooltipsOverhaul$hoveredItem() {
        return tooltipsOverhaul$currentItemStack;
    }

}
