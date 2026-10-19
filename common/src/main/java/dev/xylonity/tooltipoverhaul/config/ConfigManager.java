package dev.xylonity.tooltipoverhaul.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.config.wrapper.AutoConfig;
import dev.xylonity.tooltipoverhaul.config.wrapper.ConfigEntry;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigManager {

    private static Path CONFIG_DIR = Path.of("config");
    private static final Set<Class<?>> REGISTERED = new HashSet<>();

    private static final Map<Field, Object> DEFAULT_VALUES = new ConcurrentHashMap<>();

    private static final Map<Class<?>, CommentedFileConfig> OPEN = new ConcurrentHashMap<>();

    private static final Map<Path, Class<?>> FILE2CLASS = new ConcurrentHashMap<>();

    private static final Map<Path, WatchKey> WATCHED_DIRS = new ConcurrentHashMap<>();
    private static WatchService WATCH;
    private static volatile boolean RUN_WATCHER;

    private static final ExecutorService WATCHER = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "TooltipOverhaul-Config");
        thread.setDaemon(true);
        return thread;
    });

    private static final ScheduledExecutorService SCHEDULED = Executors.newScheduledThreadPool(1, r -> {
        final Thread thread = new Thread(r, "TooltipOverhaul-ConfigSchedule");
        thread.setDaemon(true);
        return thread;
    });

    private static final Map<Path, ScheduledFuture<?>> PENDING = new ConcurrentHashMap<>();

    // In-memory config values to match the real edits
    private static final Map<Path, String> SYNCED_TEXT = new ConcurrentHashMap<>();
    private static final Object LOCK = new Object();

    private static final Map<Class<?>, CopyOnWriteArrayList<Runnable>> RELOAD_LISTENERS = new ConcurrentHashMap<>();

    public static void onReload(Class<?> clazz, Runnable listener) {
        RELOAD_LISTENERS.computeIfAbsent(clazz, c -> new CopyOnWriteArrayList<>()).add(listener);
        if (REGISTERED.contains(clazz)) {
            listener.run();
        }

    }

    private static void fireReload(Class<?> clazz) {
        final CopyOnWriteArrayList<Runnable> listeners = RELOAD_LISTENERS.get(clazz);
        if (listeners == null) {
            return;
        }

        for (final Runnable listener : listeners) {
            try {
                listener.run();
            }
            catch (Throwable ignored) {
                ;;
            }

        }

    }

    public static void init(Path configDir, Class<?>... configs) {
        CONFIG_DIR = configDir;
        for (Class<?> clazz : configs) {
            loadOrCreate(clazz);
        }

        startWatcher();
    }

    private static void loadOrCreate(Class<?> clazz) {
        if (!REGISTERED.add(clazz)) {
            return;
        }

        AutoConfig meta = clazz.getAnnotation(AutoConfig.class);
        if (meta == null) {
            return;
        }

        String fileName = meta.file();
        final Path subDir = CONFIG_DIR.resolve(fileName);
        Path tomlPath = subDir.resolve(fileName + ".toml");

        try {
            Files.createDirectories(subDir);
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // No autosave cuzz it used to break config files
        CommentedFileConfig cfg = CommentedFileConfig
                .builder(tomlPath, TomlFormat.instance())
                .preserveInsertionOrder()
                .sync()
                .build();

        synchronized (LOCK) {
            final boolean writable = read(tomlPath, cfg);

            OPEN.put(clazz, cfg);
            FILE2CLASS.put(tomlPath, clazz);

            apply(clazz, cfg, true);

            // A broken file that couldn't be backed up is left as is instead of being replaced by the salvaged copy
            if (writable) {
                write(tomlPath, cfg);
            }

        }

        fireReload(clazz);
    }

    private static boolean read(Path tomlPath, CommentedFileConfig config) {
        final String text = readText(tomlPath);
        if (text == null) {
            return true;
        }

        String usable = text;
        boolean writable = true;
        if (!parses(text)) {
            usable = salvage(text);

            String outcome = "The original was copied next to it as .broken";
            try {
                Files.copy(tomlPath, tomlPath.resolveSibling(tomlPath.getFileName() + ".broken"), StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException ignored) {
                writable = false;
                outcome = "The original couldn't be backed up so it was left untouched";
            }

            TooltipOverhaul.LOGGER.warn("{} is malformed, kept every valid line and reset the rest to defaults. {}", tomlPath, outcome);
        }

        TomlFormat.instance().createParser().parse(new StringReader(usable), config, ParsingMode.REPLACE);
        return writable;
    }

    private static String salvage(String text) {
        final StringBuilder kept = new StringBuilder();
        for (final String line : text.split("\n")) {
            if (parses(kept + line + "\n")) {
                kept.append(line).append("\n");
            }

        }

        return kept.toString();
    }

    private static boolean parses(String text) {
        try {
            TomlFormat.instance().createParser().parse(new StringReader(text));
            return true;
        }
        catch (RuntimeException ignored) {
            return false;
        }

    }

    private static String readText(Path tomlPath) {
        final String text;
        try {
            text = new String(Files.readAllBytes(tomlPath), StandardCharsets.UTF_8);
        }
        catch (NoSuchFileException ignored) {
            return null;
        }
        catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }

        // No bom
        return text.startsWith("\uFEFF") ? text.substring(1) : text;
    }

    private static void write(Path tomlPath, CommentedFileConfig config) {
        final String text = TomlFormat.instance().createWriter().writeToString(config);
        final Path temp = tomlPath.resolveSibling(tomlPath.getFileName() + ".tmp");

        try {
            // Written aside and swapped in, so a save cut halfway never leaves a broken file behind
            writeSynced(temp, text);
            Files.move(temp, tomlPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (IOException swapFailure) {
            // Windows can refuse the swap while something holds the file open
            try {
                writeSynced(tomlPath, text);
            }
            catch (IOException writeFailure) {
                TooltipOverhaul.LOGGER.error("Could not save {}", tomlPath, writeFailure);
                return;
            }

            try {
                Files.deleteIfExists(temp);
            }
            catch (IOException ignored) {
                ;;
            }

        }

        SYNCED_TEXT.put(tomlPath, text);
    }

    private static void writeSynced(Path path, String text) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            final ByteBuffer buffer = ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8));
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }

            channel.force(true);
        }

    }

    private static void apply(Class<?> clazz, CommentedFileConfig config, boolean init) {
        final Set<String> seenCats = new HashSet<>();
        final Set<String> validPaths = new HashSet<>();

        for (Field field : clazz.getDeclaredFields()) {
            final ConfigEntry configEntry = field.getAnnotation(ConfigEntry.class);
            if (configEntry == null) {
                continue;
            }

            field.setAccessible(true);
            String category = configEntry.category();
            String entry = field.getName();
            String path = category.isEmpty() ? entry : category + "." + entry;
            final String target = category.isEmpty() ? entry : category;

            validPaths.add(path);

            // Migrates values saved under the old key to the categorized path
            if (init && !category.isEmpty() && !config.contains(path) && config.contains(entry) && !(config.get(entry) instanceof CommentedConfig)) {
                config.set(path, config.<Object>get(entry));
                config.remove(entry);
            }

            // The preview triangle toggle is now a three-way selector, so existing choices are kept
            if (clazz == TooltipsConfig.class && entry.equals("PREVIEW_PANEL_SIDE_TRIANGLES") && config.get(path) instanceof Boolean enabled) {
                config.set(path, enabled ? "style_2" : "none");
            }

            if (clazz == TooltipsConfig.class && entry.equals("PREVIEW_PANEL_SIDE_TRIANGLES") && "style_3".equals(config.get(path))) {
                config.set(path, "none");
            }

            Object def;
            try {
                def = field.get(null);
                DEFAULT_VALUES.putIfAbsent(field, def);
            }
            catch (Exception ex) {
                continue;
            }

            if (init) {
                if (seenCats.add(category)) {
                    config.setComment(target, wrapAndIndent(buildCategoryBanner(category)));
                }

                final Object rawInit = config.get(path);
                final Object oldDefault = parseDefFromComment(config.getComment(path), field.getType());

                if (!config.contains(path) || (oldDefault != null && same(rawInit, oldDefault))) {
                    config.set(path, def);
                }

                config.setComment(path, wrapAndIndent(buildEntryComment(configEntry, def)));
            }

            Object raw = config.get(path);
            Object val = clamp(raw, configEntry, field.getType());
            if (val == null) {
                val = def;
            }

            try {
                setPrimitive(field, val);
            }
            catch (Exception ignored) {
                ;;
            }

        }

        removeNonExistent(config, validPaths);
    }

    private static void removeNonExistent(CommentedFileConfig cfg, Set<String> validPaths) {
        removeNonExistent(cfg, "", validPaths);
    }

    private static void removeNonExistent(CommentedConfig node, String prefix, Set<String> validPaths) {
        final Set<String> keys = new HashSet<>(node.valueMap().keySet());

        for (String key : keys) {
            final String fullPath = prefix.isEmpty() ? key : prefix + "." + key;

            Object value = node.get(key);

            if (value instanceof CommentedConfig nested) {
                removeNonExistent(nested, fullPath, validPaths);

                final boolean hasAnyValidChild = validPaths.stream().anyMatch(p -> p.equals(fullPath) || p.startsWith(fullPath + "."));

                if (nested.valueMap().isEmpty() && !hasAnyValidChild) {
                    node.remove(key);
                }

            }
            else {
                if (!validPaths.contains(fullPath)) {
                    node.remove(key);
                }

            }

        }

    }

    private static void startWatcher() {
        try {
            if (WATCH != null) {
                return;
            }

            WATCH = FileSystems.getDefault().newWatchService();
        }
        catch (Exception exception) {
            return;
        }

        RUN_WATCHER = true;
        WATCHER.submit(() -> {
            try {
                for (Path configFile : FILE2CLASS.keySet()) {
                    final Path parent = configFile.getParent();
                    if (parent != null && !WATCHED_DIRS.containsKey(parent)) {
                        try {
                            WatchKey key = parent.register(WATCH, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
                            WATCHED_DIRS.put(parent, key);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                }

                while (RUN_WATCHER && !Thread.currentThread().isInterrupted()) {
                    final WatchKey key = WATCH.take();
                    final Path direct = (Path) key.watchable();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        final WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }

                        final Path file = direct.resolve((Path) event.context());
                        final Class<?> clazz = FILE2CLASS.get(file);

                        if (clazz == null || !file.toString().endsWith(".toml")) {
                            continue;
                        }

                        final ScheduledFuture<?> old = PENDING.remove(file);
                        if (old != null) {
                            old.cancel(false);
                        }

                        PENDING.put(file, SCHEDULED.schedule(() -> {
                            try {
                                reload(clazz, file);
                            }
                            catch (Throwable ignored) {
                                ;;
                            }

                        }, 300, TimeUnit.MILLISECONDS));

                    }

                    key.reset();
                }

            }
            catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            finally {
                try {
                    WATCH.close();
                }
                catch (IOException ignored) {
                    ;;
                }

            }

        });

    }

    private static void reload(Class<?> clazz, Path tomlPath) {
        final CommentedFileConfig cfg = OPEN.get(clazz);
        if (cfg == null) {
            return;
        }

        synchronized (LOCK) {
            final String text = readText(tomlPath);

            // Skips saves and files an editor is still writing, as the next change event picks those up
            if (text == null || text.equals(SYNCED_TEXT.get(tomlPath)) || !parses(text)) {
                return;
            }

            TomlFormat.instance().createParser().parse(new StringReader(text), cfg, ParsingMode.REPLACE);
            SYNCED_TEXT.put(tomlPath, text);
            apply(clazz, cfg, false);
        }

        fireReload(clazz);
    }

    private static Object parseDefFromComment(String comment, Class<?> clazz) {
        if (comment == null) {
            return null;
        }

        final Matcher matcher = Pattern.compile("Default:\\s*([^\\|\\n]+)").matcher(comment);
        if (!matcher.find()) {
            return null;
        }

        final String raw = matcher.group(1).trim();
        try {
            return switch (clazz.getName()) {
                case "int" -> Integer.parseInt(raw);
                case "long" -> Long.parseLong(raw);
                case "float" -> Float.parseFloat(raw);
                case "double" -> Double.parseDouble(raw);
                case "boolean" -> Boolean.parseBoolean(raw);
                default -> raw;
            };
        }
        catch (NumberFormatException e) {
            return null;
        }

    }

    private static boolean same(Object a, Object b) {
        if (a == null || b == null) {
            return false;
        }

        if (a instanceof Number n1 && b instanceof Number n2) {
            return Math.abs(n1.doubleValue() - n2.doubleValue()) < 1e-9;
        }

        return a.equals(b);
    }

    private static String buildCategoryBanner(String category) {
        final String title = (category.isEmpty() ? "GENERAL" : category.toUpperCase()) + " SETTINGS";
        return title.toLowerCase().replace(" settings", "") + " §§";
    }

    private static String buildEntryComment(ConfigEntry entry, Object defaultValue) {
        final String base = entry.comment().trim();
        final String note = entry.note().trim();

        final boolean isNumber = defaultValue instanceof Number;
        final boolean isFloating = defaultValue instanceof Double || defaultValue instanceof Float;

        String defVal = isNumber && isFloating
                ? hasDecimals(((Number) defaultValue).doubleValue(), true)
                : String.valueOf(defaultValue);

        final StringBuilder sb = new StringBuilder(base).append("\n\nDefault: ").append(defVal);
        if (isNumber) {
            final String minVal = hasDecimals(entry.min(), isFloating);
            final String maxVal = hasDecimals(entry.max(), isFloating);
            sb.append("\nRange: ").append(minVal).append(" ~ ").append(maxVal);
        }

        if (!note.isEmpty()) {
            sb.append("\n\nNote: ").append(note);
        }

        return sb.toString();
    }

    private static String hasDecimals(double value, boolean forceDecimal) {
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            return Double.toString(value);
        }

        final long asLong = (long) value;
        if (value == asLong) {
            return forceDecimal ? asLong + ".0" : Long.toString(asLong);
        }

        return Double.toString(value);
    }

    private static String wrapText(String text) {
        final StringBuilder out = new StringBuilder();
        for (String paragraph : text.split("\n")) {
            final String[] words = paragraph.split(" ");
            int col = 0;
            for (String w : words) {
                if (col + w.length() > 130) {
                    out.append("\n");
                    col = 0;
                }
                else if (col > 0) {
                    out.append(" "); col++;
                }

                out.append(w);
                col += w.length();
            }

            out.append("\n");
        }

        return out.toString().trim();
    }

    private static String wrapAndIndent(String comment) {
        final String wrapped = wrapText(comment);
        final StringBuilder builder = new StringBuilder();
        for (String line : wrapped.split("\n")) {
            builder.append(" ").append(line).append("\n");
        }

        return builder.substring(0, builder.length() - 1);
    }

    private static Object clamp(Object raw, ConfigEntry entry, Class<?> type) {
        if (!(raw instanceof Number num)) {
            return raw;
        }

        final double clamped = Math.max(entry.min(), Math.min(entry.max(), num.doubleValue()));
        return switch (type.getName()) {
            case "int" -> (int) clamped;
            case "long" -> (long) clamped;
            case "float" -> (float) clamped;
            case "double" -> clamped;
            default -> raw;
        };

    }

    public static void setPrimitive(Field f, Object v) throws Exception {
        switch (f.getType().getName()) {
            case "int" -> f.setInt(null, ((Number) v).intValue());
            case "long" -> f.setLong(null, ((Number) v).longValue());
            case "float" -> f.setFloat(null, ((Number) v).floatValue());
            case "double" -> f.setDouble(null, ((Number) v).doubleValue());
            case "boolean" -> f.setBoolean(null, (Boolean) v);
            default -> f.set(null, v);
        }

    }

    public static Object getCodeDefault(Field field) {
        return DEFAULT_VALUES.get(field);
    }

    public static void save(Class<?> clazz) {
        final CommentedFileConfig config = OPEN.get(clazz);
        if (config == null) {
            return;
        }

        final AutoConfig meta = clazz.getAnnotation(AutoConfig.class);
        if (meta == null) {
            return;
        }

        final String fileName = meta.file();
        final Path tomlPath = CONFIG_DIR.resolve(fileName).resolve(fileName + ".toml");

        synchronized (LOCK) {
            for (Field field : clazz.getDeclaredFields()) {
                final ConfigEntry rawEntry = field.getAnnotation(ConfigEntry.class);
                if (rawEntry == null) {
                    continue;
                }

                field.setAccessible(true);

                final String category = rawEntry.category();
                final String entry = field.getName();
                final String path = category.isEmpty() ? entry : category + "." + entry;

                try {
                    final Object value = field.get(null);
                    config.set(path, value);
                }
                catch (Exception ignored) {
                    ;;
                }

            }

            write(tomlPath, config);
        }

        fireReload(clazz);
    }

}