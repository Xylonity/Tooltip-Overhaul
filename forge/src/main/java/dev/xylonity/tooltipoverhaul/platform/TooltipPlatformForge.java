package dev.xylonity.tooltipoverhaul.platform;

import dev.xylonity.tooltipoverhaul.compat.emi.EmiStackContext;
import dev.xylonity.tooltipoverhaul.compat.proxy.EmiProxy;
import dev.xylonity.tooltipoverhaul.compat.proxy.FtbLibraryProxy;
import dev.xylonity.tooltipoverhaul.mixin.GuiGraphicsAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class TooltipPlatformForge implements TooltipPlatform {

    @Override
    public boolean isModLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }

    @Override
    public Path resolveConfigFile(String config) {
        return FMLPaths.CONFIGDIR.get().resolve(config);
    }

    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public Optional<String> getModDisplayName(String namespace) {
        return ModList.get().getModContainerById(namespace).map(container -> container.getModInfo().getDisplayName());
    }

    @Override
    public ItemStack getHoveredItem(GuiGraphics graphics, List<ClientTooltipComponent> components, int mouseX, int mouseY) {

        ItemStack stack = ((GuiGraphicsAccessor) graphics).tooltipoverhaul$getTooltipStack();
        if (stack != null && !stack.isEmpty()) {
            return stack;
        }

        stack = EmiStackContext.get();
        if (!stack.isEmpty()) {
            return stack;
        }

        if (isModLoaded("emi") && EmiProxy.isEmiTooltip(components)) {
            ItemStack emi = EmiProxy.getItemStack(mouseX, mouseY);
            if (emi != null && !emi.isEmpty()) {
                return emi;
            }

        }

        // Ftb Library compat
        final ItemStack ftbStack = FtbLibraryProxy.getItemStack();
        if (!ftbStack.isEmpty()) {
            return ftbStack;
        }

        return ItemStack.EMPTY;
    }

}