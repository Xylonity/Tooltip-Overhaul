package dev.xylonity.tooltipoverhaul.config.parser;

public class ConfigColorParser {

    public static int[] parsePalette(String key) {
        String[] rawColors = key.split("[,;\\s]+");

        int[] colors = new int[3];
        for (int i = 0; i < rawColors.length; i++) {
            colors[i] = parseColor(rawColors[i].trim());
        }

        return colors;
    }

    public static int parseColor(String key) {
        if (key.startsWith("#")) {
            return parseHex(key.substring(1));
        }

        if (key.startsWith("0x") || key.startsWith("0X")) {
            return parseHex(key.substring(2));
        }

        return Integer.parseInt(key);
    }

    private static int parseHex(String hex) {
        String s = hex.trim();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("Empty hex");
        }

        if (s.length() == 8) {
            return (int) Long.parseLong(s, 16);
        }
        else if (s.length() == 6) {
            return (int) (((0xFF) << 24) | ((int) Long.parseLong(s, 16) & 0x00FFFFFFL));
        }
        else {
            throw new IllegalArgumentException("Hex length must be 6 or 8: " + hex);
        }

    }

}
