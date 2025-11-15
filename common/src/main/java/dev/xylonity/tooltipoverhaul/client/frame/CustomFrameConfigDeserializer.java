package dev.xylonity.tooltipoverhaul.client.frame;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.List;

/**
 * JSON core deserializer. The first member name of the json array must be the literal "frames", otherwise the
 * dedicated config file won't be read
 */
public class CustomFrameConfigDeserializer implements JsonDeserializer<CustomFrameConfig> {

    @Override
    public CustomFrameConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

        if (!json.isJsonObject()) {
            throw new JsonParseException("Expected JSON object for CustomFrameConfig");
        }

        JsonObject file = json.getAsJsonObject();

        if (!file.has("frames") || !file.get("frames").isJsonArray()) {
            return new CustomFrameConfig(List.of());
        }

        List<CustomFrameData> frames = file.getAsJsonArray("frames").asList().stream().map(element -> context.<CustomFrameData>deserialize(element, CustomFrameData.class)).toList();
        return new CustomFrameConfig(frames);
    }

}