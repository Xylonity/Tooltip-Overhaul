package dev.xylonity.tooltipoverhaul.client.frame;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Resolves templates
 */
public final class FrameTemplates {

    private static final Set<String> SELECTORS = Set.of("id", "name", "items", "tags", "namespace", "rarity", "rarities", "priority");

    public static JsonObject resolve(JsonObject root, JsonObject entry) {
        final JsonElement value = root.get("templates");
        if (value != null && !value.isJsonObject()) {
            throw new JsonParseException("templates must be an object");
        }

        return resolve(value == null ? new JsonObject() : value.getAsJsonObject(), entry, new LinkedHashSet<>());
    }

    private static JsonObject resolve(JsonObject templates, JsonObject entry, Set<String> path) {
        JsonObject result = new JsonObject();
        if (entry.has("extends")) {
            final JsonElement parent = entry.get("extends");
            if (!parent.isJsonPrimitive() || !parent.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("extends must be a template name");
            }

            final String name = parent.getAsString().trim();
            if (!path.add(name) || path.size() > 64) {
                throw new JsonParseException("Template cycle or excessive depth: " + path + " -> " + name);
            }

            if (!templates.has(name) || !templates.get(name).isJsonObject()) {
                throw new JsonParseException("Unknown template: " + name);
            }

            result = resolve(templates, templates.getAsJsonObject(name), path);
            SELECTORS.forEach(result::remove);
            path.remove(name);
        }

        for (final Map.Entry<String, JsonElement> field : entry.entrySet()) {
            if (!field.getKey().equals("extends")) {
                if (result.has(field.getKey()) && result.get(field.getKey()).isJsonObject() && field.getValue().isJsonObject()) {
                    merge(result.getAsJsonObject(field.getKey()), field.getValue().getAsJsonObject());
                }
                else {
                    result.add(field.getKey(), field.getValue().deepCopy());
                }

            }

        }

        return result;
    }

    public static JsonObject styleOf(JsonObject entry) {
        final JsonObject style = entry.deepCopy();
        SELECTORS.forEach(style::remove);
        return style;
    }

    private static void merge(JsonObject target, JsonObject source) {
        for (final Map.Entry<String, JsonElement> field : source.entrySet()) {
            if (target.has(field.getKey()) && target.get(field.getKey()).isJsonObject() && field.getValue().isJsonObject()) {
                merge(target.getAsJsonObject(field.getKey()), field.getValue().getAsJsonObject());
            }
            else {
                target.add(field.getKey(), field.getValue().deepCopy());
            }

        }

    }

}