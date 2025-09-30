package dev.xylonity.tooltipoverhaul.compat.modernfix;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;

import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * The config entry under the name 'mixin.perf.faster_item_rendering', if set to true, certain unseen faces are culled,
 * so when the rendered stack starts rotating, some faces are seen invisible
 *
 * This should also fix Flerovium (Forge only) if 'itemBackFaceCulling' is set to true
 */
public final class ModernFixCompat {

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    public static final boolean SHOULD_RETURN_ORIGINAL_RENDER;

    static {
        SHOULD_RETURN_ORIGINAL_RENDER = isModernFixEntryEnabled();
    }

    public static void push() {
        DEPTH.set(DEPTH.get() + 1);
    }

    public static void pop() {
        int depth = DEPTH.get();

        if (depth <= 1) {
            DEPTH.remove();
        }
        else {
            DEPTH.set(depth - 1);
        }

    }

    public static boolean isEnabled() {
        return DEPTH.get() > 0;
    }

    private static boolean isModernFixEntryEnabled() {
        try {
            // Dedicated Flerovium compat (as it adds face culling optimization)
            if (TooltipOverhaul.PLATFORM.isModLoaded("flerovium")) {
                Path path = TooltipOverhaul.PLATFORM.resolveConfigFile("flerovium.json");
                if (Files.exists(path)) {
                    try (Reader reader = Files.newBufferedReader(path)) {
                        JsonElement element = JsonParser.parseReader(reader);
                        if (element != null && element.isJsonObject()) {
                            JsonObject item = element.getAsJsonObject();
                            if (item.has("itemBackFaceCulling")) {
                                JsonElement value = item.get("itemBackFaceCulling");
                                if (value.isJsonPrimitive()) {
                                    JsonPrimitive bool = value.getAsJsonPrimitive();
                                    if (bool.isBoolean() && bool.getAsBoolean()) return true;
                                    if (bool.isString() && Boolean.parseBoolean(bool.getAsString())) return true;
                                }
                            }
                        }

                    }

                }

            }

            // Default modernfix checks
            if (!TooltipOverhaul.PLATFORM.isModLoaded("modernfix")) return false;

            Path path = TooltipOverhaul.PLATFORM.resolveConfigFile("modernfix-mixins.properties");
            if (!Files.exists(path)) {
                return false;
            }

            Properties properties = new Properties();
            try (InputStream in = Files.newInputStream(path)) {
                properties.load(in);
            }

            return "true".equalsIgnoreCase(properties.getProperty("mixin.perf.faster_item_rendering"));
        }
        catch (Exception ignore) {
            return false;
        }

    }

}