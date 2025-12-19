package dev.xylonity.tooltipoverhaul.client.style.vignette.parser;

import com.google.common.collect.Lists;
import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;

import java.util.List;

public record VignetteEntry(
        String type,
        String position,
        int color,
        float radius,
        int extraPositionX,
        int extraPositionY
) {

    private static final int valueAmount = 6;

    public static final class Parser {

        public static List<VignetteEntry> from(String entry) {

            List<VignetteEntry> vignetteEntries = Lists.newArrayList();

            String[] rawEntries = entry.split("\\),");

            for (String key : rawEntries) {

                String sanitized = key.replace("(", "").replace(")", "").trim();

                if (sanitized.isBlank()) {
                    continue;
                }

                String[] parts = sanitized.split("\\s*,\\s*");
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
                    type = parts[0];
                    position = parts[1];
                    color = ConfigColorParser.parseColor(parts[2]);
                    radius = Float.parseFloat(parts[3]);
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
