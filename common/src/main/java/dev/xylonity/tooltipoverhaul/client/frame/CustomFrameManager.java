package dev.xylonity.tooltipoverhaul.client.frame;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the loading context of the custom_frames.json files
 */
public class CustomFrameManager {

    private static final Map<ResourceLocation, CustomFrameData> customFrames = new ConcurrentHashMap<>();
    private static boolean INIT = false;

    public static void initialize() {
        if (INIT) return;

        try {
            customFrames.clear();
            customFrames.putAll(CustomFrameLoader.loadCustomFrames(Minecraft.getInstance().getResourceManager(), TooltipOverhaul.PLATFORM.getConfigPath()));

            INIT = true;

            TooltipOverhaul.LOGGER.info("{} frames have been loaded!", customFrames.size());
        }
        catch (Exception e) {
            TooltipOverhaul.LOGGER.error("Failed to initialize custom frames loader: {}", e.getMessage());
        }

    }

    public static void initialize(ResourceManager resourceManager) {
        if (INIT) return;

        try {
            customFrames.clear();
            customFrames.putAll(CustomFrameLoader.loadCustomFrames(resourceManager, TooltipOverhaul.PLATFORM.getConfigPath()));

            INIT = true;

            TooltipOverhaul.LOGGER.info("{} frames have been loaded!", customFrames.size());
        }
        catch (Exception e) {
            TooltipOverhaul.LOGGER.error("Failed to initialize custom frames loader: {}", e.getMessage());
        }

    }

    /**
     * Per resource reload
     */
    public static void reset() {
        customFrames.clear();
        INIT = false;
    }

    /**
     * Returns the custom frame data for the specified stack (if it's present anywhere)
     */
    public static Optional<CustomFrameData> of(ItemStack stack) {
        if (!INIT) initialize();

        return customFrames.values().stream().filter(cfg -> cfg.matches(stack)).findFirst();
    }

}