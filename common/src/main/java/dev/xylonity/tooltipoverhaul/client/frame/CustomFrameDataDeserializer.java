package dev.xylonity.tooltipoverhaul.client.frame;

import com.google.gson.*;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.resources.ResourceLocation;

public class CustomFrameDataDeserializer implements JsonDeserializer<CustomFrameData> {

    @Override
    public CustomFrameData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

        if (!json.isJsonObject()) {
            throw new JsonParseException("Failed loading custom_frames JSON, invalid object for CustomFrameData");
        }

        final JsonObject entry = json.getAsJsonObject();

        final List<String> items = parseStringList(entry, "items");
        final List<String> tags = parseStringList(entry, "tags");
        for (String id : Stream.concat(items.stream(), tags.stream()).toList()) {
            if (ResourceLocation.tryParse(id) == null) {
                throw new JsonParseException("Invalid item or tag ID: " + id);
            }

        }

        final Optional<String> namespace = parseString(entry, "namespace");
        List<String> rarities = parseStringOrList(entry, "rarity");
        if (rarities.isEmpty()) {
            rarities = parseStringOrList(entry, "rarities");
        }

        final Optional<String> texture = parseString(entry, "texture");
        final Optional<Integer> backgroundColor = parseInt(entry, "backgroundColor");
        final Optional<String> borderType = parseString(entry, "borderType");
        final Optional<String> innerFrameCornerType = parseString(entry, "innerFrameCornerType");
        final Optional<String> backgroundCornerType = parseString(entry, "backgroundCornerType");
        final Optional<CustomFrameData.GradientType> gradientType = parseEnum(entry, "gradientType", CustomFrameData.GradientType.class);
        final Optional<List<String>> gradientColors = parseOptionalStringList(entry, "gradientColors");
        final Optional<String> itemRating = parseString(entry, "itemRating");
        final Optional<String> colorItemRating = parseString(entry, "colorItemRating");

        final Optional<String> ratingAlignment = parseString(entry, "ratingAlignment");
        final Optional<String> titleAlignment = parseString(entry, "titleAlignment");
        final Optional<String> tooltipLayout = parseString(entry, "tooltipLayout");
        final Optional<Boolean> compactShowModName = parseBool(entry, "compactShowModName");
        final Optional<String> compactModNameColor = parseString(entry, "compactModNameColor");
        final Optional<Integer> tooltipPositionX = parseInt(entry, "tooltipPositionX");
        final Optional<Integer> tooltipPositionY = parseInt(entry, "tooltipPositionY");
        final Optional<Integer> mainPanelPaddingX = parseInt(entry, "mainPanelPaddingX");
        final Optional<Integer> mainPanelPaddingY = parseInt(entry, "mainPanelPaddingY");
        final Optional<Integer> dividerLineTopPadding = parseInt(entry, "dividerLineTopPadding");
        final Optional<Integer> dividerLineBottomPadding = parseInt(entry, "dividerLineBottomPadding");
        final Optional<Float> iconSize = parseFloat(entry, "iconSize");
        final Optional<Float> iconRotatingSpeed = parseFloat(entry, "iconRotatingSpeed");
        final Optional<String> iconAppearAnimation = parseString(entry, "iconAppearAnimation");
        final Optional<String> tooltipAppearAnimation = parseString(entry, "tooltipAppearAnimation");
        final Optional<String> tooltipDisappearAnimation = parseString(entry, "tooltipDisappearAnimation");
        final Optional<Float> tooltipAnimationDuration = parseFloat(entry, "tooltipAnimationDuration");
        final Optional<Integer> secondPanelX = parseInt(entry, "secondPanelX");
        final Optional<Integer> secondPanelY = parseInt(entry, "secondPanelY");
        final Optional<Integer> secondPanelSizeX = parseInt(entry, "secondPanelSizeX");
        final Optional<Integer> secondPanelSizeY = parseInt(entry, "secondPanelSizeY");
        final Optional<Float> secondPanelRendererSpeed = parseFloat(entry, "secondPanelRendererSpeed");
        final Optional<String> dividerLineType = parseString(entry, "dividerLineType");
        final Optional<String> dividerLineColor = parseString(entry, "dividerLineColor");

        final Optional<String> particles = parseString(entry, "particles");
        final Optional<String> specialEffect = parseString(entry, "specialEffect");
        final List<String> vignettes = parseStringList(entry, "vignettes");

        final Optional<String> iconBackgroundType = parseString(entry, "iconBackgroundType");

        final Optional<Boolean> showSecondPanel = parseBool(entry, "showSecondPanel");
        final Optional<Boolean> showRating = parseBool(entry, "showRating");

        final Optional<Boolean> showShadow = parseBool(entry, "showShadow");
        final Optional<Boolean> effectsBehindText = parseBool(entry, "effectsBehindText");

        final Optional<Boolean> usePlayerSkinInPreview = parseBool(entry, "usePlayerSkinInPreview");
        final Optional<String> previewPanelModel = parseString(entry, "previewPanelModel");

        final Optional<Boolean> disableIcon = parseBool(entry, "disableIcon");
        final Optional<Boolean> disableScrolling = parseBool(entry, "disableScrolling");
        final Optional<Boolean> disableTooltip = parseBool(entry, "disableTooltip");
        final Optional<Boolean> disableDividerLine = parseBool(entry, "disableDividerLine");

        return new CustomFrameData(
                parseInt(entry, "priority").orElse(0),
                FrameConditions.parse(entry),
                dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectSettings.parse(entry),
                items,
                tags,
                namespace,
                rarities,
                texture,
                backgroundColor,
                borderType,
                innerFrameCornerType,
                backgroundCornerType,
                gradientType,
                gradientColors,
                itemRating,
                colorItemRating,
                ratingAlignment,
                titleAlignment,
                tooltipLayout,
                compactShowModName,
                compactModNameColor,
                tooltipPositionX,
                tooltipPositionY,
                mainPanelPaddingX,
                mainPanelPaddingY,
                dividerLineTopPadding,
                dividerLineBottomPadding,
                iconSize,
                iconRotatingSpeed,
                iconAppearAnimation,
                tooltipAppearAnimation,
                tooltipDisappearAnimation,
                tooltipAnimationDuration,
                secondPanelX,
                secondPanelY,
                secondPanelSizeX,
                secondPanelSizeY,
                secondPanelRendererSpeed,
                dividerLineType,
                dividerLineColor,
                particles,
                specialEffect,
                vignettes,
                iconBackgroundType,
                parseString(entry, "iconBorderStyle"),
                parseString(entry, "iconBackgroundColor"),
                parseString(entry, "iconBorderColor"),
                usePlayerSkinInPreview,
                previewPanelModel,
                parseString(entry, "previewPanelSideTriangles"),
                parseString(entry, "previewPanelCornerType"),
                parseString(entry, "previewPanelBackgroundCornerType"),
                showSecondPanel,
                showRating,
                showShadow,
                effectsBehindText,
                disableIcon,
                disableScrolling,
                disableTooltip,
                disableDividerLine
        );

    }

    private List<String> parseStringList(JsonObject content, String field) {
        if (!content.has(field)) {
            return List.of();
        }

        final JsonElement ele = content.get(field);
        if (ele.isJsonArray()) {
            return ele.getAsJsonArray().asList().stream().filter(JsonElement::isJsonPrimitive).map(JsonElement::getAsString).toList();
        }

        return List.of();
    }

    private List<String> parseStringOrList(JsonObject content, String field) {
        if (!content.has(field) || content.get(field).isJsonNull()) {
            return List.of();
        }

        final JsonElement element = content.get(field);
        if (element.isJsonPrimitive()) {
            final String value = element.getAsString().trim();
            return value.isEmpty() ? List.of() : List.of(value);
        }

        return parseStringList(content, field);
    }

    private Optional<Boolean> parseBool(JsonObject content, String field) {
        if (!content.has(field) || content.get(field).isJsonNull()) {
            return Optional.empty();
        }

        JsonElement elem = content.get(field);
        if (elem.isJsonPrimitive()) {
            if (elem.getAsJsonPrimitive().isBoolean()) {
                return Optional.of(elem.getAsBoolean());
            }

            final String text = elem.getAsString().trim().toLowerCase();
            if ("true".equals(text)) {
                return Optional.of(true);
            }

            if ("false".equals(text)) {
                return Optional.of(false);
            }

        }

        return Optional.empty();
    }

    private Optional<String> parseString(JsonObject content, String field) {
        if (!content.has(field) || content.get(field).isJsonNull()) {
            return Optional.empty();
        }

        JsonElement elem = content.get(field);
        if (elem.isJsonPrimitive()) {
            final String value = elem.getAsString();
            return value.trim().isEmpty() ? Optional.empty() : Optional.of(value);
        }

        return Optional.empty();
    }

    private Optional<List<String>> parseOptionalStringList(JsonObject content, String field) {
        if (!content.has(field) || content.get(field).isJsonNull()) {
            return Optional.empty();
        }

        JsonElement elem = content.get(field);
        if (elem.isJsonArray()) {
            final List<String> list = elem.getAsJsonArray().asList().stream().filter(JsonElement::isJsonPrimitive).map(JsonElement::getAsString).toList();
            return list.isEmpty() ? Optional.empty() : Optional.of(list);
        }

        return Optional.empty();
    }

    private Optional<Integer> parseInt(JsonObject content, String field) {
        if (!content.has(field) || content.get(field).isJsonNull()) {
            return Optional.empty();
        }

        JsonElement elem = content.get(field);
        if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isNumber()) {
            return Optional.of(elem.getAsInt());
        }

        return Optional.empty();
    }

    private Optional<Float> parseFloat(JsonObject content, String field) {
        if (!content.has(field) || content.get(field).isJsonNull()) {
            return Optional.empty();
        }

        JsonElement elem = content.get(field);
        if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isNumber()) {
            return Optional.of(elem.getAsFloat());
        }

        return Optional.empty();
    }

    private <T extends Enum<T>> Optional<T> parseEnum(JsonObject content, String field, Class<T> enumClass) {
        if (!content.has(field) || content.get(field).isJsonNull()) {
            return Optional.empty();
        }

        final JsonElement elem = content.get(field);
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
