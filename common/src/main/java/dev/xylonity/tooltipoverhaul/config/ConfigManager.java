package dev.xylonity.tooltipoverhaul.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import dev.xylonity.tooltipoverhaul.config.wrapper.AutoConfig;
import dev.xylonity.tooltipoverhaul.config.wrapper.ConfigEntry;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private static final Map<Path, Long> IGNORE_UNTIL = new ConcurrentHashMap<>();

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

        CommentedFileConfig cfg = CommentedFileConfig
                .builder(tomlPath, TomlFormat.instance())
                .autosave()
                .preserveInsertionOrder()
                .sync()
                .build();

        cfg.load();

        OPEN.put(clazz, cfg);
        FILE2CLASS.put(tomlPath, clazz);

        apply(clazz, cfg, true);
        fireReload(clazz);

        cfg.save();
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

                        final long now = System.currentTimeMillis();
                        final long until = IGNORE_UNTIL.getOrDefault(file, 0L);
                        if (now < until) {
                            continue;
                        }

                        final ScheduledFuture<?> old = PENDING.remove(file);
                        if (old != null) {
                            old.cancel(false);
                        }

                        PENDING.put(file, SCHEDULED.schedule(() -> {
                            final CommentedFileConfig cfg = OPEN.get(clazz);
                            if (cfg == null) {
                                return;
                            }

                            try {
                                cfg.load();
                                apply(clazz, cfg, false);
                                fireReload(clazz);
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

        IGNORE_UNTIL.put(tomlPath, System.currentTimeMillis() + 800L);

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

        try {
            config.save();
        }
        catch (Throwable ignored) {
            ;;
        }

        fireReload(clazz);
    }

}