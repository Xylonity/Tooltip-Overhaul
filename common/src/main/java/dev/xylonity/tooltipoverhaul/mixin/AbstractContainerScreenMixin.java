package dev.xylonity.tooltipoverhaul.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AbstractContainerScreen.class, priority = 1)
public interface AbstractContainerScreenMixin {
    @Accessor("hoveredSlot")
    Slot getHoveredSlot();
}