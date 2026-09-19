package dev.xylonity.tooltipoverhaul.client.frame;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.function.Function;

final class CustomFrameDefaults {

    private static final List<String> SELECTORS = List.of("items", "tags", "namespace", "rarity", "rarities",
            "durabilityMin", "durabilityMax", "enchanted", "enchantments", "hasCustomName", "customName");

    static JsonObject merge(JsonObject previous, JsonObject local, JsonObject current) {
        return mergeValue(previous, local, current, "").getAsJsonObject();
    }

    private static JsonElement mergeValue(JsonElement previous, JsonElement local, JsonElement current, String key) {
        if (Objects.equals(local, previous)) {
            return copy(current);
        }

        if (Objects.equals(previous, current) || local == null || current == null) {
            return copy(local);
        }

        if ((previous == null || previous.isJsonObject()) && local.isJsonObject() && current.isJsonObject()) {
            final JsonObject old = previous == null ? new JsonObject() : previous.getAsJsonObject();
            final JsonObject user = local.getAsJsonObject(), next = current.getAsJsonObject(), merged = new JsonObject();
            final Set<String> keys = new LinkedHashSet<>(user.keySet());
            keys.addAll(next.keySet());
            for (final String field : keys) {
                final JsonElement value = mergeValue(old.get(field), user.get(field), next.get(field), field);
                if (value != null) {
                    merged.add(field, value);
                }

            }

            return merged;
        }

        if (previous != null && previous.isJsonArray() && local.isJsonArray() && current.isJsonArray()) {
            final JsonArray old = previous.getAsJsonArray(), user = local.getAsJsonArray(), next = current.getAsJsonArray();
            if (key.equals("frames")) {
                return mergeFrames(old, user, next);
            }

            // Palette slots have fixed meanings as selector/effect lists are userowned as a whole
            if ((key.equals("gradientColors") || key.equals("colors")) && old.size() == user.size() && old.size() == next.size()) {
                final JsonArray merged = new JsonArray();
                for (int i = 0; i < old.size(); i++) {
                    merged.add(mergeValue(old.get(i), user.get(i), next.get(i), ""));
                }

                return merged;
            }

        }

        return copy(local);
    }

    private static JsonArray mergeFrames(JsonArray previous, JsonArray local, JsonArray current) {
        final int[] userMatches = match(previous, local), nextMatches = match(previous, current);
        final Map<Integer, JsonElement> replacements = new HashMap<>();
        final Set<Integer> matchedNext = new HashSet<>();
        for (int i = 0; i < previous.size(); i++) {
            final int user = userMatches[i], next = nextMatches[i];
            if (next >= 0) {
                matchedNext.add(next);
            }

            if (user >= 0) {
                replacements.put(user, copy(previous.get(i).equals(local.get(user)) ? next < 0 ? null : current.get(next) : local.get(user)));
            }

        }

        final List<JsonElement> merged = new ArrayList<>();
        final Map<Integer, JsonElement> anchors = new HashMap<>();
        for (int i = 0; i < local.size(); i++) {
            final JsonElement value = replacements.containsKey(i) ? replacements.get(i) : copy(local.get(i));
            if (value != null) {
                merged.add(value);
                for (int j = 0; j < previous.size(); j++) {
                    if (userMatches[j] == i && nextMatches[j] >= 0) {
                        anchors.put(nextMatches[j], value);
                    }

                }

            }

        }

        boolean reordered = false;
        int last = -1;
        for (int user : userMatches) {
            if (user >= 0) {
                if (user < last) {
                    reordered = true;
                }

                last = user;
            }

        }

        if (!reordered) {
            final Set<JsonElement> movable = Collections.newSetFromMap(new IdentityHashMap<>());

            movable.addAll(anchors.values());

            final Iterator<JsonElement> ordered = new TreeMap<>(anchors).values().iterator();
            for (int i = 0; i < merged.size(); i++) {
                if (movable.contains(merged.get(i))) {
                    merged.set(i, ordered.next());
                }

            }

        }

        final int[] existing = match(current, local);
        for (int i = 0; i < current.size(); i++) {
            if (matchedNext.contains(i) || existing[i] >= 0) {
                continue;
            }

            boolean ambiguous = false;
            for (int j = 0; j < previous.size(); j++) {
                if (nextMatches[j] < 0 && sameIdentity(previous.get(j), current.get(i))) {
                    ambiguous = true;
                }

            }

            for (JsonElement entry : local) {
                if (sameIdentity(entry, current.get(i))) {
                    ambiguous = true;
                }

            }

            if (ambiguous) {
                continue;
            }

            final JsonElement added = copy(current.get(i));
            int insertion = merged.size();
            for (int j = i + 1; j < current.size(); j++) {
                if (anchors.containsKey(j)) {
                    insertion = identityIndex(merged, anchors.get(j));
                    break;
                }

            }

            merged.add(insertion, added);
            anchors.put(i, added);
        }

        final JsonArray result = new JsonArray();
        merged.forEach(result::add);

        return result;
    }

    private static int identityIndex(List<JsonElement> values, JsonElement target) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) == target) {
                return i;
            }

        }

        return values.size();
    }

    private static int[] match(JsonArray previous, JsonArray other) {
        final int[] matches = new int[previous.size()];
        Arrays.fill(matches, -1);

        final boolean[] used = new boolean[other.size()];

        matchUnique(previous, other, matches, used, value -> value.get("id"));
        matchUnique(previous, other, matches, used, value -> value);
        matchUnique(previous, other, matches, used, value -> value.get("name"));
        matchUnique(previous, other, matches, used, CustomFrameDefaults::selectors);
        matchUnique(previous, other, matches, used, value -> value.get("texture"));

        return matches;
    }

    private static void matchUnique(JsonArray previous, JsonArray other, int[] matches, boolean[] used, Function<JsonObject, JsonElement> key) {
        final Map<JsonElement, List<Integer>> oldKeys = index(previous, key), newKeys = index(other, key);
        for (final Map.Entry<JsonElement, List<Integer>> entry : oldKeys.entrySet()) {
            final List<Integer> candidates = newKeys.get(entry.getKey());
            if (entry.getValue().size() != 1 || candidates == null || candidates.size() != 1) {
                continue;
            }

            final int old = entry.getValue().get(0), next = candidates.get(0);
            final JsonObject previousObject = previous.get(old).getAsJsonObject(), otherObject = other.get(next).getAsJsonObject();
            if (matches[old] < 0 && !used[next] && (!previousObject.has("id") || !otherObject.has("id") || previousObject.get("id").equals(otherObject.get("id")))) {
                matches[old] = next;
                used[next] = true;
            }

        }

    }

    private static Map<JsonElement, List<Integer>> index(JsonArray array, Function<JsonObject, JsonElement> key) {
        final Map<JsonElement, List<Integer>> result = new HashMap<>();
        for (int i = 0; i < array.size(); i++) {
            if (!array.get(i).isJsonObject()) {
                continue;
            }

            final JsonElement value = key.apply(array.get(i).getAsJsonObject());
            if (value != null && !value.isJsonNull()) {
                result.computeIfAbsent(value, ignored -> new ArrayList<>()).add(i);
            }

        }

        return result;
    }

    private static JsonElement selectors(JsonObject frame) {
        final JsonObject result = new JsonObject();
        for (String field : SELECTORS) {
            if (frame.has(field)) {
                result.add(field, frame.get(field));
            }

        }

        return result.size() == 0 ? null : result;
    }

    private static boolean sameIdentity(JsonElement first, JsonElement second) {
        if (!first.isJsonObject() || !second.isJsonObject()) {
            return first.equals(second);
        }

        final JsonObject firstObject = first.getAsJsonObject(), secondObject = second.getAsJsonObject();
        if (firstObject.has("id") && secondObject.has("id")) {
            return firstObject.get("id").equals(secondObject.get("id"));
        }

        for (final String field : List.of("name", "texture")) {
            if (firstObject.has(field) && firstObject.get(field).equals(secondObject.get(field))) {
                return true;
            }

        }

        final JsonElement selector = selectors(firstObject);
        return firstObject.equals(secondObject) || selector != null && selector.equals(selectors(secondObject));
    }

    private static JsonElement copy(JsonElement value) {
        return value == null ? null : value.deepCopy();
    }

}
