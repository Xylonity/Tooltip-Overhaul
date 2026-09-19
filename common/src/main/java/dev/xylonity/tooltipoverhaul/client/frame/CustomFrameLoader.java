package dev.xylonity.tooltipoverhaul.client.frame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.Reader;
import java.nio.file.Path;
import java.util.*;
import java.util.Map;

/**
 * Discovers frame documents and loads each original or its local (from the config dir) replacement exactly once
 */
public class CustomFrameLoader {

    private static final String CONFIG_PATH = "custom_frames.json";
    private static final String CONFIG_SUBDIR = "tooltipoverhaul";
    public static final ResourceLocation DEFAULT_SOURCE = ResourceLocation.fromNamespaceAndPath(TooltipOverhaul.MOD_ID, CONFIG_SUBDIR + "/" + CONFIG_PATH);

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(CustomFrameData.class, new CustomFrameDataDeserializer())
            .registerTypeAdapter(CustomFrameConfig.class, new CustomFrameConfigDeserializer())
            .setPrettyPrinting()
            .create();

    public static CustomFrameData parseFrame(JsonObject entry) {
        return GSON.fromJson(entry, CustomFrameData.class);
    }

    public static CustomFrameData parseFrame(JsonObject root, JsonObject entry) {
        return parseFrame(FrameTemplates.resolve(root, entry));
    }

    public static Path getUserConfigFile(Path configDir) {
        return configDir.resolve(CONFIG_SUBDIR).resolve(CONFIG_PATH);
    }

    /**
     * Preserves the relative resource path so multiple documents from one mod cannot collide
     */
    public static Path getOverrideFile(Path configDir, ResourceLocation source) {
        final String prefix = CONFIG_SUBDIR + "/";
        if (!source.getPath().startsWith(prefix) || !source.getPath().endsWith(CONFIG_PATH)) {
            throw new IllegalArgumentException("Not a custom frames resource: " + source);
        }

        for (final String segment : source.getPath().substring(prefix.length()).split("/", -1)) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                throw new IllegalArgumentException("Non canonical custom frames path: " + source);
            }

        }

        final Path base = configDir.resolve(CONFIG_SUBDIR).resolve("overrides").toAbsolutePath().normalize();
        final Path namespace = base.resolve(source.getNamespace()).normalize();
        final Path target = namespace.resolve(source.getPath().substring(prefix.length())).normalize();
        if (namespace.equals(base) || !namespace.startsWith(base) || target.equals(namespace) || !target.startsWith(namespace)) {
            throw new IllegalArgumentException("Custom frames path escapes its namespace: " + source);
        }

        return target;
    }

    /**
     * Only active resources are listed so idle overrides from removed mods or packs are not loaded
     */
    public static List<CustomFrameSource> discoverSources(ResourceManager resources, Path configDir) {
        final List<CustomFrameSource> sources = new ArrayList<>();
        sources.add(new CustomFrameSource(DEFAULT_SOURCE, getUserConfigFile(configDir), resources.getResource(DEFAULT_SOURCE).orElse(null), true));
        final TreeMap<ResourceLocation, Resource> found = new TreeMap<>(resources.listResources(CONFIG_SUBDIR, id -> id.getPath().endsWith(CONFIG_PATH)));
        for (final Map.Entry<ResourceLocation, Resource> entry : found.entrySet()) {
            if (entry.getKey().equals(DEFAULT_SOURCE)) {
                continue;
            }

            try {
                sources.add(new CustomFrameSource(entry.getKey(), getOverrideFile(configDir, entry.getKey()),
                        entry.getValue(), false));
            }
            catch (IllegalArgumentException e)  {
                TooltipOverhaul.LOGGER.warn("Skipping unsafe custom frames resource {}", entry.getKey(), e);
            }

        }

        return List.copyOf(sources);
    }

    public static Map<ResourceLocation, CustomFrameData> loadCustomFrames(ResourceManager resources, Path configDir) {
        return loadSources(discoverSources(resources, configDir));
    }

    /**
     * Replacements retain the original source precedence and never loads alongside the original
     */
    static Map<ResourceLocation, CustomFrameData> loadSources(List<CustomFrameSource> sources) {
        final Map<ResourceLocation, CustomFrameData> frames = new LinkedHashMap<>();
        for (CustomFrameSource source : sources) {
            try {
                try {
                    CustomFrameStorage.synchronize(source);
                }
                catch (Exception e)  {
                    TooltipOverhaul.LOGGER.warn("Could not update custom frame defaults for {}: {}", source.location(), e.getMessage());
                }

                try (final Reader reader = source.openReader()) {
                    final CustomFrameConfig config = GSON.fromJson(reader, CustomFrameConfig.class);
                    if (config == null || config.getCustomFrames() == null) {
                        TooltipOverhaul.LOGGER.warn("Empty custom frames document: {}", source.location());
                        continue;
                    }

                    for (final CustomFrameData frame : config.getCustomFrames()) {
                        frames.put(ResourceLocation.fromNamespaceAndPath(source.location().getNamespace(), "rule/" + frames.size()), frame);
                    }

                }

            }
            catch (Exception e)  {
                TooltipOverhaul.LOGGER.error("Could not load custom frames {} (local file {}): {}", source.location(), source.file(), e.getMessage());
            }

        }

        TooltipOverhaul.LOGGER.info("Loaded {} frame entries from {} documents", frames.size(), sources.size());

        return frames;
    }

}
