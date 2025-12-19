package dev.xylonity.tooltipoverhaul.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.render.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.util.EquippedContextCalculator;
import dev.xylonity.tooltipoverhaul.client.util.TextUtils;
import dev.xylonity.tooltipoverhaul.registry.TooltipOverhaulKeyMappings;
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

        tooltipoverhaul$calculateCounterValue(stack);

        tooltipoverhaul$cachedMainStack = stack.copy();

        // We create the context and the renderer for the equipped stack here, as this is the highest priority when computing certain values a posteriori
        TooltipContext equippedStackContext = EquippedContextCalculator.from((GuiGraphics) (Object) this, font, mouseX, mouseY, tooltipPositioner, stack, screenWidth, screenHeight);
        TooltipRenderer equippedStackRenderer = new TooltipRenderer(equippedStackContext);

        // The original lines of the original tooltip are rewrapped if the comparison exists, to prevent the content of the main tooltip from going beyond
        // the margins of the screen when forcing the screen scale under extreme circumstances
        List<ClientTooltipComponent> componentList;

        InputConstants.Key compareKey = ((KeyMappingAccessor) TooltipOverhaulKeyMappings.COMPARE_TOOLTIP).tooltipoverhaul$key();
        boolean isKeyDown = false;
        if (!compareKey.equals(InputConstants.UNKNOWN)) {
            isKeyDown = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), compareKey.getValue());
        }

        if (isKeyDown && equippedStackContext != null) {
            componentList = TextUtils.getTooltipComponentsFrom(stack, font, screenWidth, 2.2f);
        }
        else {
            componentList = components;
        }

        // Then, the main renderer is computed here
        TooltipContext context = new TooltipContext((GuiGraphics) (Object) this, font, componentList, mouseX, mouseY, screenWidth, screenHeight, tooltipPositioner, stack, true);
        TooltipRenderer renderer = new TooltipRenderer(context);

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

        // If the rendering is correct, the rest of the call is canceled
        if (renderer.render()) {
            if (equippedStackContext != null) {
                equippedStackRenderer.render();
            }

            ci.cancel();
        }

    }

    @Unique
    private void tooltipoverhaul$calculateCounterValue(ItemStack of) {
        if (ItemStack.isSameItemSameComponents(tooltipoverhaul$cachedMainStack, of)) {
            long elapsed = System.currentTimeMillis() - tooltipoverhaul$hoverStartTime;
            TooltipRenderer.COUNTER = elapsed / 1000f;
        }
        else {
            tooltipoverhaul$hoverStartTime = System.currentTimeMillis();
            TooltipRenderer.COUNTER = 0;
        }

    }

}