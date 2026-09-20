package dev.xylonity.tooltipoverhaul.client.screen.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameLoader;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameSource;
import dev.xylonity.tooltipoverhaul.client.frame.FrameTemplates;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.Reader;
import java.io.StringReader;

import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class FrameEditorDocument {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();

    private final Path file;
    private final Path draftFile;
    private JsonObject lastDraft;
    private CustomFrameSource source;
    private final JsonObject root;
    private final List<JsonObject> frames = new ArrayList<>();
    private final List<JsonObject> templates = new ArrayList<>();
    private final boolean loadFailed;

    FrameEditorDocument(Path file) {
        this(file, () -> Files.exists(file) ? Files.newBufferedReader(file, StandardCharsets.UTF_8) : new StringReader("{}"));
    }

    FrameEditorDocument(CustomFrameSource source) {
        this(source.file(), source::openReader);
        this.source = source;
    }

    private FrameEditorDocument(Path file, IoSupplier<Reader> input) {
        this.file = file;
        this.draftFile = file.resolveSibling(file.getFileName() + ".draft.json");

        JsonObject loaded = new JsonObject();
        boolean failed = false;
        try (Reader reader = input.get()) {
            final JsonElement parsed = JsonParser.parseReader(reader);
            loaded = parsed.isJsonNull() ? new JsonObject() : parsed.getAsJsonObject();
            validateStructure(loaded);
            if (Files.exists(draftFile)) {
                try (Reader draftReader = Files.newBufferedReader(draftFile, StandardCharsets.UTF_8)) {
                    final JsonObject savedDraft = JsonParser.parseReader(draftReader).getAsJsonObject();
                    final JsonObject draftRoot = savedDraft.getAsJsonObject("document");

                    validateStructure(draftRoot);

                    for (JsonElement template : savedDraft.getAsJsonArray("editorTemplates")) {
                        if (!template.isJsonObject()) {
                            throw new IllegalArgumentException("Invalid draft template");
                        }

                    }

                    loaded = draftRoot;
                    lastDraft = savedDraft.deepCopy();
                }

            }

        }
        catch (Exception exception) {
            TooltipOverhaul.LOGGER.error("Frame editor could not read {}: {}", file, exception.getMessage());
            failed = true;
        }

        this.root = loaded;
        this.loadFailed = failed;
        readEntries();
        if (!failed && lastDraft != null) {
            templates.clear();
            lastDraft.getAsJsonArray("editorTemplates").forEach(value -> templates.add(value.getAsJsonObject().deepCopy()));
        }

    }

    JsonObject root() {
        return root;
    }

    List<JsonObject> frames() {
        return frames;
    }

    List<JsonObject> templates() {
        return templates;
    }

    boolean loadFailed() {
        return loadFailed;
    }

    boolean hasDraft() {
        return lastDraft != null;
    }

    String saveDraft() {
        if (loadFailed) {
            return "The document or draft could not be read, existing files were not changed";
        }

        syncRoot();
        final JsonObject snapshot = new JsonObject();
        snapshot.add("document", root.deepCopy());
        final JsonArray editorTemplates = new JsonArray();
        templates.forEach(template -> editorTemplates.add(template.deepCopy()));
        snapshot.add("editorTemplates", editorTemplates);
        if (snapshot.equals(lastDraft)) {
            return null;
        }

        try {
            writeAtomically(draftFile, snapshot, false);
            lastDraft = snapshot;
            return null;
        }
        catch (Exception exception)  {
            TooltipOverhaul.LOGGER.error("Frame editor could not write draft {}", draftFile, exception);
            return messageOf(exception);
        }

    }

    void syncRoot() {
        final JsonArray array = new JsonArray();
        frames.forEach(array::add);
        root.add("frames", array);

        final JsonObject serializedTemplates = new JsonObject();
        for (JsonObject template : templates) {
            if (template.has("name")) {
                serializedTemplates.add(template.get("name").getAsString().trim(), FrameTemplates.styleOf(template));
            }

        }

        if (!templates.isEmpty() || root.has("templates")) {
            root.add("templates", serializedTemplates);
        }

    }

    String save() {
        if (loadFailed) {
            return "The document could not be read, the original file was not changed";
        }

        syncRoot();
        try {
            validate();
        }
        catch (RuntimeException exception) {
            return messageOf(exception);
        }

        try {
            if (source != null) {
                source.prepareSave();
            }

            writeAtomically(file, root, true);
            Files.deleteIfExists(draftFile);
            lastDraft = null;
            return null;
        }
        catch (Exception exception) {
            TooltipOverhaul.LOGGER.error("Frame editor could not write {}", file, exception);
            return messageOf(exception);
        }

    }

    private static void writeAtomically(Path target, JsonObject value, boolean backup) throws java.io.IOException {
        Files.createDirectories(target.getParent());
        final Path temporary = Files.createTempFile(target.getParent(), target.getFileName() + ".", ".tmp");
        try {
            Files.writeString(temporary, GSON.toJson(value), StandardCharsets.UTF_8);
            if (backup && Files.exists(target)) {
                Files.copy(target, target.resolveSibling(target.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            }

            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (AtomicMoveNotSupportedException ignored)  {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }

        }
        finally {
            Files.deleteIfExists(temporary);
        }

    }

    private void readEntries() {
        if (loadFailed) {
            return;
        }

        if (root.has("frames") && root.get("frames").isJsonArray()) {
            for (JsonElement element : root.getAsJsonArray("frames")) {
                if (element.isJsonObject()) {
                    frames.add(element.getAsJsonObject());
                }

            }

        }

        frames.sort(Comparator.comparingInt(FrameEditorDocument::priorityOf).reversed());

        if (root.has("templates") && root.get("templates").isJsonObject()) {
            for (final Map.Entry<String, JsonElement> template : root.getAsJsonObject("templates").entrySet()) {
                if (template.getValue().isJsonObject()) {
                    final JsonObject value = template.getValue().getAsJsonObject().deepCopy();
                    value.addProperty("name", template.getKey());
                    templates.add(value);
                }

            }

        }

    }

    private static void validateStructure(JsonObject root) {
        if (root.has("frames")) {
            if (!root.get("frames").isJsonArray()) {
                throw new IllegalArgumentException("frames must be an array");
            }

            for (JsonElement entry : root.getAsJsonArray("frames")) {
                if (!entry.isJsonObject()) {
                    throw new IllegalArgumentException("Every frame must be an object");
                }

            }

        }

        if (root.has("templates")) {
            if (!root.get("templates").isJsonObject()) {
                throw new IllegalArgumentException("templates must be an object");
            }

            for (final Map.Entry<String, JsonElement> entry : root.getAsJsonObject("templates").entrySet()) {
                if (!entry.getValue().isJsonObject()) {
                    throw new IllegalArgumentException("Every template must be an object");
                }

            }

        }

    }

    private void validate() {
        final Set<String> names = new HashSet<>();
        for (JsonObject template : templates) {
            final String name = template.has("name") ? template.get("name").getAsString().trim() : "";
            if (name.isEmpty() || !names.add(name)) {
                throw new IllegalArgumentException("Template names must be non-empty and unique");
            }

            CustomFrameLoader.parseFrame(root, template);
        }

        for (JsonObject frame : frames) {
            CustomFrameLoader.parseFrame(root, frame);
        }

    }

    private static int priorityOf(JsonObject entry) {
        try {
            return entry.has("priority") ? entry.get("priority").getAsInt() : 0;
        }
        catch (RuntimeException ignored) {
            return 0;
        }

    }

    private static String messageOf(Exception exception) {
        return exception.getMessage() != null ? exception.getMessage() : exception.getClass().getSimpleName();
    }

}