package dev.xylonity.tooltipoverhaul.client.frame;

import com.google.gson.*;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class CustomFrameDataDeserializer implements JsonDeserializer<CustomFrameData> {

    @Override
    public CustomFrameData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

        if (!json.isJsonObject()) {
            throw new JsonParseException("Failed loading custom_frames JSON, invalid object for CustomFrameData");
        }

        JsonObject content = json.getAsJsonObject();

        List<String> items = parseStringList(content, "items");
        List<String> tags = parseStringList(content, "tags");

        Optional<String> texture = parseString(content, "texture");
        Optional<CustomFrameData.InnerBorderType> borderType = parseEnum(content, "borderType", CustomFrameData.InnerBorderType.class);
        Optional<CustomFrameData.GradientType> gradientType = parseEnum(content, "gradientType", CustomFrameData.GradientType.class);
        Optional<List<String>> gradientColors = parseOptionalStringList(content);
        Optional<String> itemRating = parseString(content, "itemRating");
        Optional<Integer> colorItemRating = parseInt(content);
        Optional<String> particles = parseString(content, "particles");
        Optional<String> specialEffect = parseString(content, "specialEffect");
        Optional<Boolean> disableTooltip = parseBool(content);

        return new CustomFrameData(items, tags, texture, borderType, gradientType, gradientColors, itemRating, colorItemRating, particles, specialEffect, disableTooltip);
    }

    private List<String> parseStringList(JsonObject content, String field) {
        if (!content.has(field)) return List.of();

        JsonElement ele = content.get(field);
        if (ele.isJsonArray()) {
            return ele.getAsJsonArray().asList().stream().filter(JsonElement::isJsonPrimitive).map(JsonElement::getAsString).toList();
        }

        return List.of();
    }

    private Optional<Boolean> parseBool(JsonObject content) {
        if (!content.has("disableTooltip") || content.get("disableTooltip").isJsonNull()) return Optional.empty();

        JsonElement elem = content.get("disableTooltip");
        if (elem.isJsonPrimitive()) {
            if (elem.getAsJsonPrimitive().isBoolean()) return Optional.of(elem.getAsBoolean());

            String s = elem.getAsString().trim().toLowerCase();
            if ("true".equals(s)) return Optional.of(true);
            if ("false".equals(s)) return Optional.of(false);
        }

        return Optional.empty();
    }

    private Optional<String> parseString(JsonObject content, String field) {
        if (!content.has(field) || content.get(field).isJsonNull()) return Optional.empty();

        JsonElement elem = content.get(field);
        if (elem.isJsonPrimitive()) {
            String value = elem.getAsString();
            return value.trim().isEmpty() ? Optional.empty() : Optional.of(value);
        }

        return Optional.empty();
    }

    private Optional<List<String>> parseOptionalStringList(JsonObject content) {
        if (!content.has("gradientColors") || content.get("gradientColors").isJsonNull()) return Optional.empty();

        JsonElement elem = content.get("gradientColors");
        if (elem.isJsonArray()) {
            List<String> list = elem.getAsJsonArray().asList().stream().filter(JsonElement::isJsonPrimitive).map(JsonElement::getAsString).toList();
            return list.isEmpty() ? Optional.empty() : Optional.of(list);
        }

        return Optional.empty();
    }

    private Optional<Integer> parseInt(JsonObject content) {
        if (!content.has("colorItemRating") || content.get("colorItemRating").isJsonNull()) return Optional.empty();

        JsonElement elem = content.get("colorItemRating");
        if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isNumber()) {
            return Optional.of(elem.getAsInt());
        }

        return Optional.empty();
    }

    private <T extends Enum<T>> Optional<T> parseEnum(JsonObject content, String field, Class<T> enumClass) {
        if (!content.has(field) || content.get(field).isJsonNull()) return Optional.empty();

        JsonElement elem = content.get(field);
        if (elem.isJsonPrimitive()) {
            try {
                return Optional.of(Enum.valueOf(enumClass, elem.getAsString().toUpperCase().replace('-', '_')));
            }
            catch (Exception e) {
                TooltipOverhaul.LOGGER.warn("Invalid enum value for {}: {}", field, elem.getAsString());
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

}