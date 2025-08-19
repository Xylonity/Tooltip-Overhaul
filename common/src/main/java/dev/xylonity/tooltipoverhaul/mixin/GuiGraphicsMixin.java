package dev.xylonity.tooltipoverhaul.mixin;

import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.wrap.TooltipWrapper;
import dev.xylonity.tooltipoverhaul.util.ITooltipOverhaulItemAware;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = GuiGraphics.class, priority = 1)
public class GuiGraphicsMixin {

    /**
     * Main tooltip renderer call. A context is populated with the relevant info needed to render the tooltip. Nothing else
     * is rendered except if explicit specified within the renderer internal logic, thus preventing possible incompats (that
     * shouldn't exist anyways)
     */
    @Inject(method = "renderTooltipInternal", at = @At(value = "HEAD"), cancellable = true)
    private void enhancedtooltips$coreRenderer(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner tooltipPositioner, CallbackInfo ci) {
        int sw = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int sh = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        ItemStack stack = ((ITooltipOverhaulItemAware) this).tooltipsOverhaul$hoveredItem();
        List<ClientTooltipComponent> text = TooltipWrapper.wrap(font, components, sw, stack);

        TooltipContext ctx = TooltipContext.of((GuiGraphics) (Object) this, mouseX, mouseY, sw, sh, text, stack);
        if (TooltipRenderer.render(ctx)) {
            ci.cancel();
        }

    }

}
