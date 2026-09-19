package dev.xylonity.tooltipoverhaul.client.frame;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handles the loading context of the custom_frames.json files
 */
public class CustomFrameManager {

    private static final Map<ResourceLocation, CustomFrameData> customFrames = new LinkedHashMap<>();

    private static boolean INIT = false;

    // Set by the frame editor screen so the live preview tooltip uses the frame being edited instead of the loaded ones
    private static volatile CustomFrameData previewOverride = null;

    public static void setPreviewOverride(CustomFrameData data) {
        previewOverride = data;
    }

    public static void initialize() {
        if (INIT) {
            return;
        }

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
        if (INIT) {
            return;
        }

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

    public static Optional<CustomFrameData> of(ItemStack stack) {
        if (previewOverride != null) {
            return Optional.of(previewOverride);
        }

        if (!INIT) {
            initialize();
        }

        return findMatch(customFrames.values(), stack);
    }

    /**
     * priority > specificity > order
     */
    public static Optional<CustomFrameData> findMatch(Iterable<CustomFrameData> frames, ItemStack stack) {
        CustomFrameData best = null;
        int bestScore = 0;
        for (final CustomFrameData customFrameData : frames) {
            final int score = customFrameData.matchScore(stack);
            if (score > 0 && (best == null || customFrameData.priority() > best.priority() || customFrameData.priority() == best.priority() && score > bestScore)) {
                bestScore = score;
                best = customFrameData;

            }

        }

        return Optional.ofNullable(best);
    }

}