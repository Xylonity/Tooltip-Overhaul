package dev.xylonity.tooltipoverhaul.client.frame;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the loading context of the custom_frames.json files
 */
public class CustomFrameManager {

    private static final Map<ResourceLocation, CustomFrameData> customFrames = new ConcurrentHashMap<>();
    private static boolean INIT = false;

    // Set by the frame editor screen so the live preview tooltip uses the frame being edited instead of the loaded ones
    private static volatile CustomFrameData previewOverride = null;

    public static void setPreviewOverride(CustomFrameData data) {
        previewOverride = data;
    }

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
     * Returns the custom frame data for the specified stack (if it's present anywhere). The most specific match wins (item > tag > namespace > rarity)
     */
    public static Optional<CustomFrameData> of(ItemStack stack) {
        if (previewOverride != null) {
            return Optional.of(previewOverride);
        }

        if (!INIT) {
            initialize();
        }

        CustomFrameData best = null;
        int bestScore = 0;
        for (final CustomFrameData customFrameData : customFrames.values()) {
            final int score = customFrameData.matchScore(stack);
            if (score > bestScore) {
                bestScore = score;
                best = customFrameData;
                // An explicit item match can't be beaten
                if (score == 4) {
                    break;
                }

            }

        }

        return Optional.ofNullable(best);
    }

}