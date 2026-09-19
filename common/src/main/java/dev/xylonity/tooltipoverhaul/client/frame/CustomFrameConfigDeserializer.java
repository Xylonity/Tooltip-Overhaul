package dev.xylonity.tooltipoverhaul.client.frame;

import com.google.gson.*;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves templates and isolates malformed rules so valid rules in the file still load
 */
public class CustomFrameConfigDeserializer implements JsonDeserializer<CustomFrameConfig> {

    @Override
    public CustomFrameConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

        if (!json.isJsonObject()) {
            throw new JsonParseException("Expected JSON object for CustomFrameConfig");
        }

        final JsonObject file = json.getAsJsonObject();
        if (!file.has("frames") || !file.get("frames").isJsonArray()) {
            return new CustomFrameConfig(List.of());
        }

        final List<CustomFrameData> frames = new ArrayList<>();
        int index = 0;
        for (final JsonElement element : file.getAsJsonArray("frames")) {
            try {
                frames.add(context.deserialize(FrameTemplates.resolve(file, element.getAsJsonObject()), CustomFrameData.class));
            }
            catch (final RuntimeException exception) {
                TooltipOverhaul.LOGGER.warn("Skipping invalid frame #{}: {}", index + 1, exception.getMessage());
            }

            index++;
        }

        return new CustomFrameConfig(frames);
    }

}