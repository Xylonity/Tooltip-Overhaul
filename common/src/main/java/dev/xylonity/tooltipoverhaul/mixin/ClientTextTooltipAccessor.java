package dev.xylonity.tooltipoverhaul.mixin;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ClientTextTooltip.class, priority = 1)
public interface ClientTextTooltipAccessor {
    @Accessor("text")
    FormattedCharSequence getText();
}
