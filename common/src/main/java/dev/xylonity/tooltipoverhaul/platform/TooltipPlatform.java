package dev.xylonity.tooltipoverhaul.platform;

import dev.xylonity.tooltipoverhaul.compat.glitchcore.GlitchcoreTooltipProxy;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface TooltipPlatform {

    boolean isModLoaded(String modid);

    Path resolveConfigFile(String configFileName);
    Path getConfigPath();

    ItemStack getHoveredItem(GuiGraphics graphics, List<ClientTooltipComponent> components, int mouseX, int mouseY);

    Optional<String> getModDisplayName(String namespace);

    /**
     * Lets loader-specific tooltip event hooks contribute extra {@link ClientTooltipComponent}s before rendering
     */
    default List<ClientTooltipComponent> gatherTooltipComponents(GuiGraphics graphics, ItemStack stack, List<ClientTooltipComponent> components, Font font, int mouseX, int mouseY, int screenWidth, int screenHeight, ClientTooltipPositioner positioner) {
        if (isModLoaded("glitchcore")) {
            return GlitchcoreTooltipProxy.gather(graphics, stack, components, font, mouseX, mouseY, screenWidth, screenHeight, positioner);
        }

        return components;
    }

}
