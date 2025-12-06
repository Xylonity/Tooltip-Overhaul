package dev.xylonity.tooltipoverhaul.platform;

import dev.xylonity.tooltipoverhaul.compat.proxy.EmiProxy;
import dev.xylonity.tooltipoverhaul.compat.proxy.JeiProxy;
import dev.xylonity.tooltipoverhaul.compat.proxy.ScreenTypeProxy;
import dev.xylonity.tooltipoverhaul.mixin.AbstractContainerScreenMixin;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;
import java.util.List;

public class TooltipPlatformFabric implements TooltipPlatform {

    @Override
    public boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }

    @Override
    public Path resolveConfigFile(String configFileName) {
        return FabricLoader.getInstance().getConfigDir().resolve(configFileName);
    }

    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    /**
     * Hovered ItemStack locator. For dedicated mod compatibility, proxies (reflection) are used
     */
    @Override
    public ItemStack getHoveredItem(GuiGraphics graphics, List<ClientTooltipComponent> components, int mouseX, int mouseY) {

        Screen screen = Minecraft.getInstance().screen;



        boolean isContainerLike = (screen instanceof AbstractContainerScreen<?>) || (screen instanceof CreativeModeInventoryScreen) || ScreenTypeProxy.isContainerLikeOrJeiEmi();
        if (!isContainerLike) {
            return ItemStack.EMPTY;
        }

        // Vanilla GUI hovered stack
        if (screen instanceof AbstractContainerScreen<?> container) {
            try {
                Slot slot = ((AbstractContainerScreenMixin) container).getHoveredSlot();
                if (slot != null) {
                    ItemStack stack = slot.getItem();
                    if (!stack.isEmpty()) {
                        return stack;
                    }
                }
            }
            catch (Throwable ignored) {
                ;;

            }

        }

        // Emi compat
        ItemStack emiStack = EmiProxy.getItemStack(mouseX, mouseY);
        if (!emiStack.isEmpty()) {
            return emiStack;
        }

        // Jei compat
        ItemStack jeiStack = JeiProxy.getItemStack();
        if (!jeiStack.isEmpty()) {
            return jeiStack;
        }

        return ItemStack.EMPTY;
    }

}
