package dev.xylonity.tooltipoverhaul.config.parser;

public class ConfigColorParser {

    public static int[] parsePalette(String key) {
        String[] rawColors = key.split("[,;\\s]+");
        int[] colors = new int[3];

        int length = Math.min(3, rawColors.length);
        int lastColor = 0xFFFFFFFF;
        for (int i = 0; i < length; i++) {
            lastColor = parseColor(rawColors[i].trim());
            colors[i] = lastColor;
        }

        // If the array is not populated entirely
        for (int i = length; i < 3; i++) {
            colors[i] = lastColor;
        }

        return colors;
    }

    public static int parseColor(String rawKey) {
        String key = rawKey.trim();
        if (key.startsWith("#")) {
            return parseHex(key.substring(1));
        }

        if (key.startsWith("0x") || key.startsWith("0X")) {
            return parseHex(key.substring(2));
        }

        // If it's normal RGB, injects 0xFF alpha
        long value = Long.parseLong(key);
        long argb = (value <= 0x00FF_FFFFL) ? (0xFF00_0000L | value ) : value;
        return (int) (argb & 0xFFFF_FFFFL);
    }

    private static int parseHex(String rawHex) {
        String hex = rawHex.trim();
        if (hex.isEmpty()) {
            throw new IllegalArgumentException("Empty hex");
        }

        int length = hex.length();
        long value = Long.parseLong(hex, 16);
        if (length == 6) {
            value |= 0xFF00_0000L;
        }
        else if (length != 8) {
            return 0xFFFFFFFF;
            //throw new IllegalArgumentException("Hex length must be 6 or 8: " + rawHex);
        }

        return (int) (value & 0xFFFF_FFFFL);
    }

}
