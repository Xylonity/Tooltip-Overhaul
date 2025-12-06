package dev.xylonity.tooltipoverhaul.mixin;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.util.ITooltipOverhaulItemAware;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = GuiGraphics.class, priority = 1)
public class GuiGraphicsItemMixin implements ITooltipOverhaulItemAware {

    @Unique
    private ItemStack tooltipsOverhaul$currentItemStack = ItemStack.EMPTY;

    @Inject(method = "renderTooltipInternal", at = @At("HEAD"))
    private void tooltipsOverhaul$captureHovered(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner positioner, CallbackInfo ci) {
        tooltipsOverhaul$currentItemStack = TooltipOverhaul.PLATFORM.getHoveredItem((GuiGraphics) (Object) this, components, mouseX, mouseY);
    }

    @Inject(method = "renderTooltipInternal", at = @At("RETURN"))
    private void tooltipsOverhaul$clearHovered(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner positioner, CallbackInfo ci) {
        tooltipsOverhaul$currentItemStack = ItemStack.EMPTY;
    }

    @Override
    public ItemStack tooltipsOverhaul$hoveredItem() {
        return tooltipsOverhaul$currentItemStack;
    }

}
