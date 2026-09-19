package dev.xylonity.tooltipoverhaul.client.style.vignette.parser;

import com.google.common.collect.Lists;
import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;

import java.util.List;
import java.util.Locale;

public record VignetteEntry(
        String type,
        String position,
        int color,
        float radius,
        int extraPositionX,
        int extraPositionY
) {

    private static final int valueAmount = 6;

    public static final List<String> TYPES = List.of("circular", "hole", "ellipse", "diamond", "ring", "linear");
    public static final List<String> POSITIONS = List.of("top_left", "top_middle", "top_right", "left", "middle", "right", "bottom_left", "bottom_middle", "bottom_right");

    public String serialize() {
        return String.format(Locale.ROOT, "(%s, %s, #%08X, %s, %d, %d)", type, position, color, Float.toString(radius), extraPositionX, extraPositionY);
    }

    public static final class Parser {

        public static List<VignetteEntry> from(String entry) {

            final List<VignetteEntry> vignetteEntries = Lists.newArrayList();

            final String[] rawEntries = entry.split("\\),");

            for (String key : rawEntries) {

                final String sanitized = key.replace("(", "").replace(")", "").trim();

                if (sanitized.isBlank()) {
                    continue;
                }

                final String[] parts = sanitized.split("\\s*,\\s*");
                if (parts.length != valueAmount) {
                    continue;
                }

                String type;
                String position;
                int color;
                float radius;
                int extraPositionX;
                int extraPositionY;
                try {
                    type = parts[0].toLowerCase(Locale.ROOT);
                    position = parts[1].toLowerCase(Locale.ROOT);
                    if (position.equals("center")) {
                        position = "middle";
                    }

                    color = ConfigColorParser.parseColor(parts[2]);
                    radius = Float.parseFloat(parts[3]);
                    if (!Float.isFinite(radius) || radius < 0) {
                        continue;
                    }

                    extraPositionX = Integer.parseInt(parts[4]);
                    extraPositionY = Integer.parseInt(parts[5]);
                }
                catch (Exception e) {
                    continue;
                }

                vignetteEntries.add(new VignetteEntry(type, position, color, radius, extraPositionX, extraPositionY));

            }

            return vignetteEntries;
        }

    }

}
