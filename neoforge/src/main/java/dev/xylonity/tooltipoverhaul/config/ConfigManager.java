package dev.xylonity.tooltipoverhaul.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import dev.xylonity.tooltipoverhaul.config.wrapper.AutoConfig;
import dev.xylonity.tooltipoverhaul.config.wrapper.ConfigEntry;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigManager {
    private static Path CONFIG_DIR = Path.of("config");
    private static final Set<Class<?>> REGISTERED = new HashSet<>();

    public static void init(Path configDir, Class<?>... configs) {
        CONFIG_DIR = configDir;
        for (Class<?> clazz : configs) {
            loadOrCreate(clazz);
        }
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
        Set<String> seenCats = new HashSet<>();

        for (Field field : clazz.getDeclaredFields()) {
            ConfigEntry e = field.getAnnotation(ConfigEntry.class);
            if (e == null) continue;

            field.setAccessible(true);
            String category = e.category();
            String entry = field.getName();
            String path = category.isEmpty() ? entry : category + "." + entry;
            String target = category.isEmpty() ? entry : category;

            if (seenCats.add(category)) {
                cfg.setComment(target, wrapAndIndent(buildCategoryBanner(category)));
            }

            Object def;
            try {
                def = field.get(null);
            } catch (Exception ex) {
                continue;
            }

            Object raw = cfg.get(path);
            Object oldDefault = parseDefFromComment(cfg.getComment(path), field.getType());

            if (!cfg.contains(path) || (oldDefault != null && same(raw, oldDefault))) {
                cfg.set(path, def);
                raw = cfg.get(path);
            }

            String entryComment = buildEntryComment(e, def);
            cfg.setComment(path, wrapAndIndent(entryComment));

            Object val = clamp(raw, e, field.getType());
            if (val == null) val = def;
            try {
                setPrimitive(field, val);
            } catch (Exception ignored) {
                ;;
            }
        }

        cfg.save();
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
        } catch (NumberFormatException e) {
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
        boolean isDouble = defaultValue instanceof Double;
        String defVal = isDouble ? hasDecimals(((Number) defaultValue).doubleValue(), true) : defaultValue.toString();
        String minVal = hasDecimals(entry.min(), isDouble);
        String maxVal = hasDecimals(entry.max(), isDouble);
        boolean showRange = !(defaultValue instanceof Boolean);

        StringBuilder sb = new StringBuilder(base).append("\n\nDefault: ").append(defVal);

        if (showRange) sb.append("\nRange: ").append(minVal).append(" ~ ").append(maxVal);
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