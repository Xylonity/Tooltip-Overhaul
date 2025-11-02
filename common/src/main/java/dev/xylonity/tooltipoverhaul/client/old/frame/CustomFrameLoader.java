package dev.xylonity.tooltipoverhaul.client.old.frame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom JSON parser with Optional wrapper compat
 */
public class CustomFrameLoader {

    private static final String CONFIG_PATH = "custom_frames.json";
    private static final String CONFIG_SUBDIR = "tooltipoverhaul";

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(CustomFrameData.class, new CustomFrameDataDeserializer())
            .registerTypeAdapter(CustomFrameConfig.class, new CustomFrameConfigDeserializer())
            .setPrettyPrinting()
            .create();

    /**
     * Loads every single json file from every single mod that defines it,
     * plus the config file from Tooltip Overhaul itself
     */
    public static Map<ResourceLocation, CustomFrameData> loadCustomFrames(ResourceManager resourceManager, Path configDir) {
        Map<ResourceLocation, CustomFrameData> frames = new ConcurrentHashMap<>();

        // Loads Tooltip Overhaul config-based custom_frames.json
        loadTooltipOverhaulConfigFrames(frames, configDir, resourceManager);

        // saves all json occurrences
        Map<ResourceLocation, Resource> resources = resourceManager.listResources(TooltipOverhaul.MOD_ID, path -> path.getPath().endsWith(CONFIG_PATH));

        TooltipOverhaul.LOGGER.info("Found {} custom frame config files from resource packs", resources.size());

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation location = entry.getKey();
            // Skips Tooltip Overhaul resource file since it's loaded through the config folder instead
            if (TooltipOverhaul.MOD_ID.equals(location.getNamespace())) {
                continue;
            }

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

    /**
     * Loads Tooltip Overhaul custom_frames.json from the config directory
     * If it doesn't exist, extracts it from resources
     */
    private static void loadTooltipOverhaulConfigFrames(Map<ResourceLocation, CustomFrameData> frames, Path configDir, ResourceManager resourceManager) {
        Path configSubDir = configDir.resolve(CONFIG_SUBDIR);
        Path configFile = configSubDir.resolve(CONFIG_PATH);
        try {
            // Ensures config directory exists
            if (!Files.exists(configSubDir)) {
                Files.createDirectories(configSubDir);
                TooltipOverhaul.LOGGER.info("Created config directory: {}", configSubDir);
            }

            // If the config file doesn't exist, extracts it from resources
            if (!Files.exists(configFile)) {
                extractDefaultConfigFile(configFile, resourceManager);
            }

            // Loads the config file
            if (Files.exists(configFile)) {
                try (BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                    CustomFrameConfig config = GSON.fromJson(reader, CustomFrameConfig.class);

                    if (config == null || config.getCustomFrames() == null) {
                        TooltipOverhaul.LOGGER.warn("Empty or invalid custom frames config: {}", configFile);
                        return;
                    }

                    List<CustomFrameData> customFrames = config.getCustomFrames();

                    TooltipOverhaul.LOGGER.info("Found {} frame configs in TooltipOverhaul config", customFrames.size());

                    processFrameData(frames, customFrames, TooltipOverhaul.MOD_ID);
                }
            }
            else {
                TooltipOverhaul.LOGGER.warn("Could not find or create config file at {}", configFile);
            }

        }
        catch (IOException e) {
            TooltipOverhaul.LOGGER.error("Failed to load Tooltip Overhaul config frames: {}", e.getMessage());
        }
        catch (JsonSyntaxException e) {
            TooltipOverhaul.LOGGER.error("Invalid JSON syntax in {}: {}", configFile, e.getMessage());
        }
        catch (Exception e) {
            TooltipOverhaul.LOGGER.error("Unexpected error loading custom frames from {}: {}", e.getMessage(), e);
        }

    }

    /**
     * Extracts the default custom_frames.json from (this) mod resources to the config dir
     */
    private static void extractDefaultConfigFile(Path targetFile, ResourceManager resourceManager) {
        try {
            Optional<Resource> resource = resourceManager.getResource(new ResourceLocation(TooltipOverhaul.MOD_ID, TooltipOverhaul.MOD_ID + "/" + CONFIG_PATH));
            if (resource.isPresent()) {
                try (InputStream stream = resource.get().open()) {
                    Files.copy(stream, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    TooltipOverhaul.LOGGER.info("Extracted default custom_frames.json to config: {}", targetFile);
                }
            }
            else {
                TooltipOverhaul.LOGGER.warn("Could not find default custom_frames.json in mod resources");
                createEmptyConfigFile(targetFile);
            }
        }
        catch (Exception e) {
            TooltipOverhaul.LOGGER.error("Failed to extract default config file: {}", e.getMessage());
            createEmptyConfigFile(targetFile);
        }

    }

    /**
     * Creates an empty custom_frames.json if the extraction fails
     */
    private static void createEmptyConfigFile(Path targetFile) {
        try {
            Files.writeString(targetFile, "{\n  \"custom_frames\": []\n}", StandardCharsets.UTF_8);
            TooltipOverhaul.LOGGER.info("Created an empty custom_frames.json as a fallback at {}", targetFile);
        }
        catch (Exception e) {
            TooltipOverhaul.LOGGER.error("Failed to create empty custom_frames.json config file: {}", e.getMessage());
        }

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

            processFrameData(frames, customFrames, namespace);
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

    /**
     * Processes and registers the frame data into the frames map. This method is now refactored in an isolated method (as the logic was previously inside `loadFramesFromResource`)
     */
    private static void processFrameData(Map<ResourceLocation, CustomFrameData> frames, List<CustomFrameData> customFrames, String namespace) {
        // Memoizing the data read
        for (CustomFrameData frameData : customFrames) {
            List<ResourceLocation> itemLocations = frameData.getItemLocations();
            // Debug logs for verbose people :imp:
            for (ResourceLocation item : itemLocations) {
                if (frames.containsKey(item)) {
                    TooltipOverhaul.LOGGER.debug("Duplicate frame for item {} in namespace {}, overwriting previous", item, namespace);
                }
                frames.put(item, frameData);
            }

            // If there is no items but there are tags, still registers the entry using a synthetic key so that it
            // exists in the values channel. This (should) ensure tag-only entries to participate in the iteration of the main
            // map that caches all entries
            if (itemLocations.isEmpty() && !frameData.tags().isEmpty()) {
                ResourceLocation key = new ResourceLocation(namespace, "tag_only/" + Integer.toUnsignedString(System.identityHashCode(frameData), 36));

                frames.put(key, frameData);

                TooltipOverhaul.LOGGER.debug("Registered tag-only entry {} with tags {}", key, frameData.tags());
            }
            else if (!frameData.tags().isEmpty()) {
                TooltipOverhaul.LOGGER.debug("Entry will also match tags (mixed): {}", frameData.tags());
            }

            // Caches namespace-only entries in the isolated map
            if (itemLocations.isEmpty() && frameData.tags().isEmpty() && frameData.namespace().isPresent() && !frameData.namespace().get().trim().isEmpty()) {
                String currNamespace = frameData.namespace().get().trim();
                ResourceLocation key = new ResourceLocation(namespace, "namespace_only/" + currNamespace + "/" + Integer.toUnsignedString(System.identityHashCode(frameData), 36));

                frames.put(key, frameData);

                TooltipOverhaul.LOGGER.debug("Registered namespace-only entry {} for namespace '{}'", key, currNamespace);
            }

        }

    }

}