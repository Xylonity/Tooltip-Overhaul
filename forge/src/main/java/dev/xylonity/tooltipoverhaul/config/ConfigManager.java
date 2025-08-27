package dev.xylonity.tooltipoverhaul.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import dev.xylonity.tooltipoverhaul.config.wrapper.AutoConfig;
import dev.xylonity.tooltipoverhaul.config.wrapper.ConfigEntry;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
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

    private static final Map<Class<?>, CommentedFileConfig> OPEN = new ConcurrentHashMap<>();

    private static final Map<Path, Class<?>> FILE2CLASS = new ConcurrentHashMap<>();

    private static WatchService WATCH;
    private static volatile boolean RUN_WATCHER;

    private static final ExecutorService WATCHER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "TooltipOverhaul-Config");
        t.setDaemon(true);
        return t;
    });

    private static final ScheduledExecutorService SCHEDULED = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "TooltipOverhaul-ConfigSchedule");
        t.setDaemon(true);
        return t;
    });

    private static final Map<Path, ScheduledFuture<?>> PENDING = new ConcurrentHashMap<>();
    private static final Map<Path, Long> IGNORE_UNTIL = new ConcurrentHashMap<>();

    public static void init(Path configDir, Class<?>... configs) {
        CONFIG_DIR = configDir;
        for (Class<?> clazz : configs) {
            loadOrCreate(clazz);
        }

        startWatcher();
    }

    private static void loadOrCreate(Class<?> clazz) {
        if (!REGISTERED.add(clazz)) return;

        AutoConfig meta = clazz.getAnnotation(AutoConfig.class);
        if (meta == null) return;

        String fileName = meta.file() + ".toml";
        Path tomlPath = CONFIG_DIR.resolve(fileName);

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

        cfg.save();
    }

    private static void apply(Class<?> clazz, CommentedFileConfig cfg, boolean init) {
        Set<String> seenCats = new HashSet<>();

        for (Field field : clazz.getDeclaredFields()) {
            ConfigEntry e = field.getAnnotation(ConfigEntry.class);
            if (e == null) continue;

            field.setAccessible(true);
            String category = e.category();
            String entry = field.getName();
            String path = category.isEmpty() ? entry : category + "." + entry;
            String target = category.isEmpty() ? entry : category;

            Object def;
            try {
                def = field.get(null);
            }
            catch (Exception ex) {
                continue;
            }

            if (init) {
                if (seenCats.add(category)) {
                    cfg.setComment(target, wrapAndIndent(buildCategoryBanner(category)));
                }

                Object rawInit = cfg.get(path);
                Object oldDefault = parseDefFromComment(cfg.getComment(path), field.getType());

                if (!cfg.contains(path) || (oldDefault != null && same(rawInit, oldDefault))) {
                    cfg.set(path, def);
                }

                cfg.setComment(path, wrapAndIndent(buildEntryComment(e, def)));
            }

            Object raw = cfg.get(path);
            Object val = clamp(raw, e, field.getType());
            if (val == null) val = def;
            try {
                setPrimitive(field, val);
            }
            catch (Exception ignored) {
                ;;
            }

        }

    }

    private static void startWatcher() {
        try {
            if (WATCH != null) {
                return;
            }

            WATCH = FileSystems.getDefault().newWatchService();
            CONFIG_DIR.register(WATCH, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
        }
        catch (Exception exception) {
            return;
        }

        RUN_WATCHER = true;
        WATCHER.submit(() -> {
            try {
                while (RUN_WATCHER && !Thread.currentThread().isInterrupted()) {
                    WatchKey key = WATCH.take();
                    Path direct = (Path) key.watchable();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW) continue;

                        Path file = direct.resolve((Path) event.context());
                        Class<?> clazz = FILE2CLASS.get(file);
                        if (clazz == null || !file.toString().endsWith(".toml")) continue;

                        long now = System.currentTimeMillis();
                        long until = IGNORE_UNTIL.getOrDefault(file, 0L);
                        if (now < until) {
                            continue;
                        }

                        ScheduledFuture<?> old = PENDING.remove(file);
                        if (old != null) {
                            old.cancel(false);
                        }

                        PENDING.put(file, SCHEDULED.schedule(() -> {
                            CommentedFileConfig cfg = OPEN.get(clazz);
                            if (cfg == null) {
                                return;
                            }

                            try {
                                cfg.load();
                                apply(clazz, cfg, false);
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

    private static Object parseDefFromComment(String s, Class<?> clazz) {
        if (s == null) return null;
        Matcher m = Pattern.compile("Default:\\s*([^\\|\\n]+)").matcher(s);
        if (!m.find()) return null;
        String raw = m.group(1).trim();

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
        if (a == null || b == null) return false;
        if (a instanceof Number n1 && b instanceof Number n2) {
            return Math.abs(n1.doubleValue() - n2.doubleValue()) < 1e-9;
        }

        return a.equals(b);
    }

    private static String buildCategoryBanner(String category) {
        String title = (category.isEmpty() ? "GENERAL" : category.toUpperCase()) + " SETTINGS";
        return title.toLowerCase().replace(" settings", "") + " §§";
    }

    private static String buildEntryComment(ConfigEntry entry, Object defaultValue) {
        String base = entry.comment().trim();
        String note = entry.note().trim();

        boolean isNumber = defaultValue instanceof Number;
        boolean isFloating = defaultValue instanceof Double || defaultValue instanceof Float;

        String defVal = isNumber && isFloating
                ? hasDecimals(((Number) defaultValue).doubleValue(), true)
                : String.valueOf(defaultValue);

        StringBuilder sb = new StringBuilder(base).append("\n\nDefault: ").append(defVal);

        if (isNumber) {
            String minVal = hasDecimals(entry.min(), isFloating);
            String maxVal = hasDecimals(entry.max(), isFloating);
            sb.append("\nRange: ").append(minVal).append(" ~ ").append(maxVal);
        }
        if (!note.isEmpty()) sb.append("\n\nNote: ").append(note);

        return sb.toString();
    }

    private static String hasDecimals(double d, boolean forceDecimal) {
        if (Double.isInfinite(d) || Double.isNaN(d)) return Double.toString(d);
        long asLong = (long) d;
        if (d == asLong) {
            return forceDecimal ? asLong + ".0" : Long.toString(asLong);
        }

        return Double.toString(d);
    }

    private static String wrapText(String text) {
        StringBuilder out = new StringBuilder();
        for (String paragraph : text.split("\n")) {
            String[] words = paragraph.split(" ");
            int col = 0;
            for (String w : words) {
                if (col + w.length() > 130) {
                    out.append("\n");
                    col = 0;
                } else if (col > 0) {
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
        String wrapped = wrapText(comment);
        StringBuilder s = new StringBuilder();
        for (String line : wrapped.split("\n")) {
            s.append(" ").append(line).append("\n");
        }

        return s.substring(0, s.length() - 1);
    }

    private static Object clamp(Object raw, ConfigEntry e, Class<?> type) {
        if (!(raw instanceof Number num)) return raw;
        double d = Math.max(e.min(), Math.min(e.max(), num.doubleValue()));
        return switch (type.getName()) {
            case "int" -> (int) d;
            case "long" -> (long) d;
            case "float" -> (float) d;
            case "double" -> d;
            default -> raw;
        };

    }

    private static void setPrimitive(Field f, Object v) throws Exception {
        switch (f.getType().getName()) {
            case "int" -> f.setInt(null, (Integer) v);
            case "long" -> f.setLong(null, (Long) v);
            case "float" -> f.setFloat(null, (Float) v);
            case "double" -> f.setDouble(null, (Double) v);
            case "boolean" -> f.setBoolean(null, (Boolean) v);
            default -> f.set(null, v);
        }

    }

}