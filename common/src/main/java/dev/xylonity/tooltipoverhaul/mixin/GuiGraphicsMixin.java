package dev.xylonity.tooltipoverhaul.mixin;

import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.render.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.util.ITooltipOverhaulItemAware;
import net.minecraft.client.Minecraft;
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
public class GuiGraphicsMixin {

    @Unique
    private static ItemStack tooltipoverhaul$cachedMainStack = ItemStack.EMPTY;

    @Unique
    private static long tooltipoverhaul$hoverStartTime = 0;

    /**
     * Main tooltip renderer call. A context is populated with the relevant info needed to render the tooltip. Nothing else
     * is rendered except if explicit specified within the renderer internal logic, thus preventing possible incompats (that
     * shouldn't exist anyways)
     */
    @Inject(method = "renderTooltipInternal", at = @At(value = "HEAD"), cancellable = true)
    private void tooltipoverhaul$coreRenderCall(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner tooltipPositioner, CallbackInfo ci) {

        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        ItemStack stack = ((ITooltipOverhaulItemAware) this).tooltipsOverhaul$hoveredItem();

        if (ItemStack.isSameItemSameTags(tooltipoverhaul$cachedMainStack, stack)) {
            long elapsed = System.currentTimeMillis() - tooltipoverhaul$hoverStartTime;
            TooltipRenderer.COUNTER = elapsed / 1000f;
        }
        else {
            tooltipoverhaul$hoverStartTime = System.currentTimeMillis();
            TooltipRenderer.COUNTER = 0;
        }

        tooltipoverhaul$cachedMainStack = stack.copy();

        TooltipContext context = new TooltipContext((GuiGraphics) (Object) this, font, components, mouseX, mouseY, screenWidth, screenHeight, tooltipPositioner, stack, true);
        TooltipRenderer renderer = new TooltipRenderer(context);
        if (renderer.render()) {
            ci.cancel();
        }
    }

}