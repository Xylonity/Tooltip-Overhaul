package dev.xylonity.tooltipoverhaul.platform;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;
import java.util.List;

public interface TooltipPlatform {

    boolean isModLoaded(String modid);

    Path resolveConfigFile(String configFileName);
    Path getConfigPath();

    ItemStack getHoveredItem(GuiGraphics graphics, List<ClientTooltipComponent> components, int mouseX, int mouseY);
}
