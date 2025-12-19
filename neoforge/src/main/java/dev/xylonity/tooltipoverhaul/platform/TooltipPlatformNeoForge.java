package dev.xylonity.tooltipoverhaul.platform;

import dev.xylonity.tooltipoverhaul.mixin.GuiGraphicsAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.List;

public class TooltipPlatformNeoForge implements TooltipPlatform {

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
    public ItemStack getHoveredItem(GuiGraphics graphics, List<ClientTooltipComponent> components, int mouseX, int mouseY) {
        return ((GuiGraphicsAccessor) graphics).tooltipoverhaul$getTooltipStack();
    }

}