package dev.xylonity.tooltipoverhaul.client.old.frame;

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

        JsonObject entry = json.getAsJsonObject();

        List<String> items = parseStringList(entry, "items");
        List<String> tags = parseStringList(entry, "tags");
        Optional<String> namespace = parseString(entry, "namespace");

        Optional<String> texture = parseString(entry, "texture");
        Optional<Integer> backgroundColor = parseInt(entry, "backgroundColor");
        Optional<String> borderType = parseString(entry, "borderType");
        Optional<CustomFrameData.GradientType> gradientType = parseEnum(entry, "gradientType", CustomFrameData.GradientType.class);
        Optional<List<String>> gradientColors = parseOptionalStringList(entry);
        Optional<String> itemRating = parseString(entry, "itemRating");
        Optional<Integer> colorItemRating = parseInt(entry, "colorItemRating");

        Optional<String> ratingAlignment = parseString(entry, "ratingAlignment");
        Optional<String> titleAlignment = parseString(entry, "titleAlignment");
        Optional<Integer> titlePositionX = parseInt(entry, "titlePositionX");
        Optional<Integer> titlePositionY = parseInt(entry, "titlePositionY");
        Optional<Integer> ratingPositionX = parseInt(entry, "ratingPositionX");
        Optional<Integer> ratingPositionY = parseInt(entry, "ratingPositionY");
        Optional<Integer> tooltipDescriptionPositionX = parseInt(entry, "tooltipDescriptionPositionX");
        Optional<Integer> tooltipDescriptionPositionY = parseInt(entry, "tooltipDescriptionPositionY");
        Optional<Integer> mainPanelPaddingX = parseInt(entry, "mainPanelPaddingX");
        Optional<Integer> mainPanelPaddingY = parseInt(entry, "mainPanelPaddingY");
        Optional<Float> iconSize = parseFloat(entry, "iconSize");
        Optional<Float> iconRotatingSpeed = parseFloat(entry, "iconRotatingSpeed");
        Optional<String> iconAppearAnimation = parseString(entry, "iconAppearAnimation");
        Optional<Integer> secondPanelX = parseInt(entry, "secondPanelX");
        Optional<Integer> secondPanelY = parseInt(entry, "secondPanelY");
        Optional<Float> secondPanelRendererSize = parseFloat(entry, "secondPanelRendererSize");
        Optional<Float> secondPanelRendererSpeed = parseFloat(entry, "secondPanelRendererSpeed");
        Optional<CustomFrameData.DividerLineType> dividerLineType = parseEnum(entry, "dividerLineType", CustomFrameData.DividerLineType.class);
        Optional<String> dividerLineColor = parseString(entry, "dividerLineColor");

        Optional<String> particles = parseString(entry, "particles");
        Optional<String> specialEffect = parseString(entry, "specialEffect");

        Optional<String> iconBackgroundType = parseString(entry, "iconBackgroundType");

        Optional<Boolean> showSecondPanel = parseBool(entry, "showSecondPanel");
        Optional<Boolean> showRating = parseBool(entry, "showRating");

        Optional<Boolean> disableIcon = parseBool(entry, "disableIcon");
        Optional<Boolean> disableScrolling = parseBool(entry, "disableScrolling");
        Optional<Boolean> disableTooltip = parseBool(entry, "disableTooltip");
        Optional<Boolean> disableDividerLine = parseBool(entry, "disableDividerLine");

        return new CustomFrameData(
                items,
                tags,
                namespace,
                texture,
                backgroundColor,
                borderType,
                gradientType,
                gradientColors,
                itemRating,
                colorItemRating,
                ratingAlignment,
                titleAlignment,
                titlePositionX,
                titlePositionY,
                ratingPositionX,
                ratingPositionY,
                tooltipDescriptionPositionX,
                tooltipDescriptionPositionY,
                mainPanelPaddingX,
                mainPanelPaddingY,
                iconSize,
                iconRotatingSpeed,
                iconAppearAnimation,
                secondPanelX,
                secondPanelY,
                secondPanelRendererSize,
                secondPanelRendererSpeed,
                dividerLineType,
                dividerLineColor,
                particles,
                specialEffect,
                iconBackgroundType,
                showSecondPanel,
                showRating,
                disableIcon,
                disableScrolling,
                disableTooltip,
                disableDividerLine
        );
    }

    private List<String> parseStringList(JsonObject content, String field) {
        if (!content.has(field)) return List.of();

        JsonElement ele = content.get(field);
        if (ele.isJsonArray()) {
            return ele.getAsJsonArray().asList().stream().filter(JsonElement::isJsonPrimitive).map(JsonElement::getAsString).toList();
        }

        return List.of();
    }

    private Optional<Boolean> parseBool(JsonObject content, String field) {
        if (!content.has(field) || content.get(field).isJsonNull()) return Optional.empty();

        JsonElement elem = content.get(field);
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

    private Optional<Integer> parseInt(JsonObject content, String field) {
        if (!content.has(field) || content.get(field).isJsonNull()) return Optional.empty();

        JsonElement elem = content.get(field);
        if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isNumber()) {
            return Optional.of(elem.getAsInt());
        }

        return Optional.empty();
    }

    private Optional<Float> parseFloat(JsonObject content, String field) {
        if (!content.has(field) || content.get(field).isJsonNull()) return Optional.empty();

        JsonElement elem = content.get(field);
        if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isNumber()) {
            return Optional.of(elem.getAsFloat());
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