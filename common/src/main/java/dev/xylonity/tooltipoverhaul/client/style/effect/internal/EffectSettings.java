package dev.xylonity.tooltipoverhaul.client.style.effect.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record EffectSettings(
        Float speed,
        Float intensity,
        Float density,
        List<Integer> colors,
        Map<String, EffectSettings> overrides,
        Map<EffectParameter, Float> parameters
) {

    public static final float MAX_SPEED = 10;
    public static final float MAX_DENSITY = 5;

    public static final EffectSettings INHERIT = new EffectSettings(null, null, null, List.of(), Map.of());

    public EffectSettings(Float speed, Float intensity, Float density, List<Integer> colors, Map<String, EffectSettings> overrides) {
        this(speed, intensity, density, colors, overrides, Map.of());
    }

    public EffectSettings(Float speed, Float intensity, Float density, Map<String, EffectSettings> overrides) {
        this(speed, intensity, density, List.of(), overrides);
    }

    public static EffectSettings parse(JsonObject entry) {
        final Map<String, EffectSettings> overrides = new LinkedHashMap<>();
        if (entry.has("effectSettings")) {
            if (!entry.get("effectSettings").isJsonObject()) {
                throw new JsonParseException("effectSettings must be an object");
            }

            for (final Map.Entry<String, JsonElement> field : entry.getAsJsonObject("effectSettings").entrySet()) {
                final JsonObject values = field.getValue().getAsJsonObject();
                overrides.put(EffectCatalog.canonical(field.getKey()), new EffectSettings(number(values, "speed", MAX_SPEED),
                        number(values, "intensity", 1), number(values, "density", MAX_DENSITY), colors(values), Map.of(), parameters(values)));
            }

        }

        return new EffectSettings(number(entry, "effectSpeed", MAX_SPEED), number(entry, "effectIntensity", 1), number(entry, "effectDensity", MAX_DENSITY), Map.copyOf(overrides));
    }

    private static Map<EffectParameter, Float> parameters(JsonObject values) {
        final Map<EffectParameter, Float> result = new EnumMap<>(EffectParameter.class);
        for (final EffectParameter parameter : EffectParameter.values()) {
            if (!values.has(parameter.key())) {
                continue;
            }

            final Float value = number(values, parameter.key(), parameter.maximum());
            if (value < parameter.minimum() || parameter.integer() && value != Math.round(value)) {
                throw new JsonParseException(parameter.key() + " must be " + (parameter.integer() ? "an integer " : "") + "between " + parameter.minimum() + " and " + parameter.maximum());
            }

            result.put(parameter, value);
        }

        return Map.copyOf(result);
    }

    public float value(EffectParameter parameter) {
        return parameters.getOrDefault(parameter, parameter.defaultValue());
    }

    private static List<Integer> colors(JsonObject values) {
        if (!values.has("colors")) {
            return List.of();
        }

        if (!values.get("colors").isJsonArray() || values.getAsJsonArray("colors").size() != 3) {
            throw new JsonParseException("Effect colors must contain exactly three RGB hex colors");
        }

        final List<Integer> result = new ArrayList<>();
        for (final JsonElement element : values.getAsJsonArray("colors")) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("Effect colors must use #RRGGBB strings");
            }

            final String hex = element.getAsString().replaceFirst("^(#|0[xX])", "");
            if (!hex.matches("[0-9a-fA-F]{6}")) {
                throw new JsonParseException("Effect colors must use #RRGGBB");
            }

            result.add(0xFF000000 | Integer.parseInt(hex, 16));
        }

        return List.copyOf(result);
    }

    private static Float number(JsonObject object, String key, float max) {
        if (!object.has(key)) {
            return null;
        }

        final float value = object.get(key).getAsFloat();
        if (!Float.isFinite(value) || value < 0 || value > max) {
            throw new JsonParseException(key + " must be between 0 and " + max);
        }

        return value;
    }

    public EffectSettings forEffect(String id) {
        return forEffect(id, true);
    }

    public EffectSettings forEffect(String id, boolean respectReducedMotion) {
        final EffectSettings child = overrides.getOrDefault(EffectCatalog.canonical(id), INHERIT);
        return new EffectSettings(respectReducedMotion && TooltipsConfig.REDUCED_MOTION ? 0 :
                fallback(child.speed, speed, TooltipsConfig.EFFECT_SPEED),
                fallback(child.intensity, intensity, TooltipsConfig.EFFECT_INTENSITY),
                fallback(child.density, density, TooltipsConfig.EFFECT_DENSITY), child.colors, Map.of(), child.parameters);
    }

    private static float fallback(Float specific, Float general, float global) {
        return specific != null ? specific : general != null ? general : global;
    }

}