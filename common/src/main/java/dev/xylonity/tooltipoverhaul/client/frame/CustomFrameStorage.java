package dev.xylonity.tooltipoverhaul.client.frame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;

/**
 * Keeps previous defaults separated from the editable copy
 */
final class CustomFrameStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
    private static final String LEGACY_DEFAULTS = "/assets/tooltipoverhaul/tooltipoverhaul/defaults/custom_frames-1.5.2.json";

    static Path defaultsFile(CustomFrameSource source) {
        return source.file().resolveSibling(source.file().getFileName() + ".defaults.json");
    }

    static void synchronize(CustomFrameSource source) throws IOException {
        if (source.resource() == null) {
            source.initializePrimary();
            return;
        }

        // External resources remain live until Save and only replacements need migration metadata
        if (!source.primary() && !source.customized()) {
            return;
        }

        final JsonObject current;
        try (final Reader reader = source.resource().openAsReader()) {
            current = read(reader);
        }

        final Path defaults = defaultsFile(source);
        if (!source.customized()) {
            write(defaults, current);
            write(source.file(), current);
            return;
        }

        final JsonObject local;
        try (Reader reader = Files.newBufferedReader(source.file(), StandardCharsets.UTF_8)) {
            local = read(reader);
        }

        final JsonObject previous;
        if (Files.exists(defaults)) {
            try (final Reader reader = Files.newBufferedReader(defaults, StandardCharsets.UTF_8)) {
                previous = read(reader);
            }

        }
        else if (source.primary())  {
            previous = legacyDefaults();
        }
        else {
            previous = current;
        }

        final JsonObject merged = CustomFrameDefaults.merge(previous, local, current);
        if (!merged.equals(local)) {
            Files.copy(source.file(), source.file().resolveSibling(source.file().getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            write(source.file(), merged);
        }

        if (!Files.exists(defaults) || !previous.equals(current)) {
            write(defaults, current);
        }

    }

    static void prepareSave(CustomFrameSource source) throws IOException {
        if (source.resource() == null) {
            return;
        }

        final Path defaults = defaultsFile(source);
        if (!source.customized() || !Files.exists(defaults)) {
            if (source.primary() && source.customized()) {
                write(defaults, legacyDefaults());
            }
            else {
                try (Reader reader = source.resource().openAsReader()) {
                    write(defaults, read(reader));
                }

            }

        }

    }

    private static JsonObject legacyDefaults() throws IOException {
        try (final InputStream input = CustomFrameStorage.class.getResourceAsStream(LEGACY_DEFAULTS)) {
            if (input == null) {
                throw new IOException("Missing legacy custom frame defaults");
            }

            try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                return read(reader);
            }

        }

    }

    static JsonObject read(Reader reader) throws IOException {
        try {
            final JsonElement parsed = JsonParser.parseReader(reader);
            final JsonObject root = parsed.isJsonNull() ? new JsonObject() : parsed.getAsJsonObject();
            if (root.has("frames")) {
                if (!root.get("frames").isJsonArray()) {
                    throw new IOException("frames must be an array");
                }

                for (final JsonElement frame : root.getAsJsonArray("frames")) {
                    if (!frame.isJsonObject()) {
                        throw new IOException("Every frame must be an object");
                    }

                }

            }

            if (root.has("templates")) {
                if (!root.get("templates").isJsonObject()) {
                    throw new IOException("templates must be an object");
                }

                for (final Map.Entry<String, JsonElement> template : root.getAsJsonObject("templates").entrySet()) {
                    if (!template.getValue().isJsonObject()) {
                        throw new IOException("Every template must be an object");
                    }

                }

            }

            return root;
        }
        catch (RuntimeException failure)  {
            throw new IOException("Invalid custom frames document", failure);
        }

    }

    private static void write(Path file, JsonObject root) throws IOException {
        Files.createDirectories(file.getParent());
        final Path temporary = Files.createTempFile(file.getParent(), file.getFileName() + ".", ".tmp");
        try {
            Files.writeString(temporary, GSON.toJson(root) + "\n", StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (Exception e)  {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }

        }
        finally {
            Files.deleteIfExists(temporary);
        }

    }

}