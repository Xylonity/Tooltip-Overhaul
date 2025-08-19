package dev.xylonity.tooltipoverhaul.client.frame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom JSON parser with Optional wrapper compat
 */
public class CustomFrameLoader {

    private static final String CONFIG_PATH = "custom_frames.json";

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(CustomFrameData.class, new CustomFrameDataDeserializer())
            .registerTypeAdapter(CustomFrameConfig.class, new CustomFrameConfigDeserializer())
            .setPrettyPrinting()
            .create();

    /**
     * Loads every single json file from every single mod that defines it
     */
    public static Map<ResourceLocation, CustomFrameData> loadCustomFrames(ResourceManager resourceManager) {
        Map<ResourceLocation, CustomFrameData> frames = new ConcurrentHashMap<>();

        // saves all json occurrences
        Map<ResourceLocation, Resource> resources = resourceManager.listResources(TooltipOverhaul.MOD_ID, path -> path.getPath().endsWith(CONFIG_PATH));

        TooltipOverhaul.LOGGER.info("Found {} custom frame config files", resources.size());

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation location = entry.getKey();
            try {
                loadFramesFromResource(entry.getValue(), frames, location);
            }
            catch (Exception e) {
                TooltipOverhaul.LOGGER.error("Error loading custom frames from {}: {}", location, e.getMessage());
            }
        }

        TooltipOverhaul.LOGGER.info("Loaded {} custom frame entries total", frames.size());

        return frames;
    }

    private static void loadFramesFromResource(Resource resource, Map<ResourceLocation, CustomFrameData> frames, ResourceLocation configLocation) {
        // For each json loaded
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {

            TooltipOverhaul.LOGGER.info("Loading custom frames from: {}", configLocation);

            CustomFrameConfig config = GSON.fromJson(reader, CustomFrameConfig.class);
            if (config == null || config.getCustomFrames() == null) {
                TooltipOverhaul.LOGGER.warn("Empty or invalid custom frames config at {}", configLocation);
                return;
            }

            List<CustomFrameData> customFrames = config.getCustomFrames();
            String namespace = configLocation.getNamespace();

            TooltipOverhaul.LOGGER.debug("Found {} frame configs in {}", customFrames.size(), namespace);

            // Memoizing the data read
            for (CustomFrameData frameData : customFrames) {
                // Debug logs for verbose people :imp:
                for (ResourceLocation item : frameData.getItemLocations()) {
                    if (frames.containsKey(item)) {
                        TooltipOverhaul.LOGGER.debug("Duplicate frame for item {} in {}, overwriting previous", item, configLocation);
                    }

                    frames.put(item, frameData);
                }

                if (!frameData.tags().isEmpty()) {
                    TooltipOverhaul.LOGGER.debug("Frame will also match tags: {}", frameData.tags());
                }
            }

        }
        catch (IOException e) {
            TooltipOverhaul.LOGGER.error("Failed to read custom frames file {}: {}", configLocation, e.getMessage());
        }
        catch (JsonSyntaxException e) {
            TooltipOverhaul.LOGGER.error("Invalid JSON syntax in {}: {}", configLocation, e.getMessage());
        }
        catch (Exception e) {
            TooltipOverhaul.LOGGER.error("Unexpected error loading custom frames from {}: {}", configLocation, e.getMessage(), e);
        }

    }

}