package dev.xylonity.tooltipoverhaul.mixin;

import dev.xylonity.tooltipoverhaul.client.util.TooltipScrollState;
import dev.xylonity.tooltipoverhaul.client.render.PinnedTooltipState;
import dev.xylonity.tooltipoverhaul.registry.TooltipOverhaulKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.glfw.GLFW;

@Mixin(value = MouseHandler.class, priority = 1)
public abstract class MouseHandlerMixin {

    @Unique
    private static double tooltipoverhaul$deferredDy = Double.NaN;

    private static double tooltipoverhaul$mouseX(Minecraft client) {
        return client.mouseHandler.xpos() * client.getWindow().getGuiScaledWidth() / client.getWindow().getScreenWidth();
    }

    private static double tooltipoverhaul$mouseY(Minecraft client) {
        return client.mouseHandler.ypos() * client.getWindow().getGuiScaledHeight() / client.getWindow().getScreenHeight();
    }

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    private void tooltipoverhaul$onPress(long window, int button, int action, int modifiers, CallbackInfo ci) {
        final Minecraft client = Minecraft.getInstance();
        if (window == client.getWindow().getWindow() && PinnedTooltipState.mouseButton(tooltipoverhaul$mouseX(client), tooltipoverhaul$mouseY(client), button, action)) {
            ci.cancel();
            return;
        }

        if (window == client.getWindow().getWindow() && action == GLFW.GLFW_PRESS && TooltipOverhaulKeyMappings.handleConfigMouseShortcut(client, button)) {
            ci.cancel();
        }

    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void tooltipoverhaul$onScroll(long window, double dx, double dy, CallbackInfo ci) {
        final Minecraft minecraft = Minecraft.getInstance();
        tooltipoverhaul$deferredDy = Double.NaN;
        if (window != minecraft.getWindow().getWindow()) {
            return;
        }

        if (PinnedTooltipState.scroll(tooltipoverhaul$mouseX(minecraft), tooltipoverhaul$mouseY(minecraft), dy)) {
            ci.cancel();
            return;
        }

        if (minecraft.screen == null) {
            if (TooltipScrollState.isIsActive()) {
                TooltipScrollState.reset();
            }

            return;
        }

        if (TooltipScrollState.shouldCaptureScroll()) {
            // Container items use the wheel themselves
            if (tooltipoverhaul$hoveredStackHasImage(minecraft.screen)) {
                tooltipoverhaul$deferredDy = dy;
                return;
            }

            TooltipScrollState.onRawScroll(dy);

            ci.cancel();
        }

    }

    @Redirect(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDDD)Z"), require = 0)
    private boolean tooltipoverhaul$screenScrolled(Screen screen, double mouseX, double mouseY, double scrollX, double scrollY) {
        final boolean consumed = screen.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        final double dy = tooltipoverhaul$deferredDy;
        tooltipoverhaul$deferredDy = Double.NaN;
        if (!consumed && !Double.isNaN(dy) && TooltipScrollState.shouldCaptureScroll()) {
            TooltipScrollState.onRawScroll(dy);
        }

        return consumed;
    }

    @Unique
    private static boolean tooltipoverhaul$hoveredStackHasImage(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?> container) || screen instanceof CreativeModeInventoryScreen) {
            return false;
        }

        try {
            final Slot slot = ((AbstractContainerScreenMixin) container).getHoveredSlot();
            return slot != null && slot.hasItem() && slot.getItem().getTooltipImage().isPresent();
        }
        catch (Throwable ignored) {
            return false;
        }

    }

}
