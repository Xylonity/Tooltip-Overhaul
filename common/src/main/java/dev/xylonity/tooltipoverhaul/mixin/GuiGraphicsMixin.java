package dev.xylonity.tooltipoverhaul.mixin;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.render.TooltipHoverTracker;
import dev.xylonity.tooltipoverhaul.client.render.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.util.EquippedContextCalculator;
import dev.xylonity.tooltipoverhaul.client.util.TextUtils;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.util.ITooltipOverhaulItemAware;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.jetbrains.annotations.Nullable;
import java.util.List;

@Mixin(value = GuiGraphics.class, priority = 1500)
public class GuiGraphicsMixin {

    /**
     * Main tooltip renderer call. A context is populated with the relevant info needed to render the tooltip. Nothing else
     * is rendered except if explicit specified within the renderer internal logic, thus preventing possible incompats (that
     * shouldn't exist anyways)
     */
    @Inject(method = "renderTooltipInternal", at = @At(value = "HEAD"), cancellable = true)
    private void tooltipoverhaul$coreRenderCall(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner tooltipPositioner, CallbackInfo ci) {

        final int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        final int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        final ItemStack stack = ((ITooltipOverhaulItemAware) this).tooltipsOverhaul$hoveredItem();

        tooltipoverhaul$calculateCounterValue(stack);

        // We create the context and the renderer for the equipped stack here, as this is the highest priority when computing certain values a posteriori
        final TooltipContext equippedStackContext = EquippedContextCalculator.from((GuiGraphics) (Object) this, font, mouseX, mouseY, tooltipPositioner, stack, screenWidth, screenHeight);
        final TooltipRenderer equippedStackRenderer = new TooltipRenderer(equippedStackContext);

        // The original lines of the original tooltip are rewrapped if the comparison exists, to prevent the content of the main tooltip from going beyond
        // the margins of the screen when forcing the screen scale under extreme circumstances
        List<ClientTooltipComponent> componentList;

        if (equippedStackContext != null) {
            componentList = TextUtils.getTooltipComponentsFrom(stack, font, screenWidth, 2.2f);
        }
        else {
            componentList = TooltipOverhaul.PLATFORM.gatherTooltipComponents((GuiGraphics) (Object) this, stack, components, font, mouseX, mouseY, screenWidth, screenHeight, tooltipPositioner);
        }

        // Then, the main renderer is computed here
        final TooltipContext context = new TooltipContext((GuiGraphics) (Object) this, font, componentList, mouseX, mouseY, screenWidth, screenHeight, tooltipPositioner, stack, true);
        final TooltipRenderer renderer = new TooltipRenderer(context);

        // Parity is assigned here so that both contexts inherit from each other
        if (equippedStackContext != null) {
            context.setOtherTooltipContext(equippedStackContext);
            equippedStackContext.setOtherTooltipContext(context);
        }

        // Original sizes and positions are initialized
        equippedStackRenderer.init();
        renderer.init();

        // The sizes and positions are recalculated after having been previously calculated, in order to readjust the layout of the tooltips and fit them
        // to the margins of the screen
        if (equippedStackContext != null) {
            equippedStackRenderer.adjustLayout();
            renderer.adjustLayout();
        }

        // Cancelling renderTooltipInternal at head also skips the loader's pre-render tooltip event, so it's replayed here
        // Only fired when the renderer is going to take over
        if (renderer.canRender() && TooltipOverhaul.PLATFORM.fireRenderTooltipPre((GuiGraphics) (Object) this, stack, componentList, font, mouseX, mouseY, screenWidth, screenHeight, tooltipPositioner)) {
            ci.cancel();
            return;
        }

        // If the rendering is correct, the rest of the call is canceled
        if (renderer.render()) {
            if (equippedStackContext != null) {
                equippedStackRenderer.render();
            }

            ci.cancel();
        }

    }

    @Unique
    private void tooltipoverhaul$calculateCounterValue(ItemStack stack) {
        final float delay = Math.max(0, TooltipsConfig.TOOLTIP_APPEAR_DELAY);
        final TooltipHoverTracker.Timing timing = TooltipHoverTracker.timing(stack, tooltipoverhaul$hoveredSlot(), delay, !TooltipsConfig.TOOLTIP_ANIMATE_ON_SWITCH);
        TooltipRenderer.COUNTER = timing.tooltipSeconds();
        TooltipRenderer.ICON_COUNTER = timing.iconSeconds();
    }

    @Unique
    @Nullable
    private static Slot tooltipoverhaul$hoveredSlot() {
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> container) {
            try {
                return ((AbstractContainerScreenMixin) container).getHoveredSlot();
            }
            catch (Throwable ignored) {
                ;;
            }

        }

        return null;
    }

}