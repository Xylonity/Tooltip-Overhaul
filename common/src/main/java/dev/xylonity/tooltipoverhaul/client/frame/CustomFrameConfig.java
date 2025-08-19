package dev.xylonity.tooltipoverhaul.client.frame;

import java.util.List;
import java.util.Objects;

/**
 * Dummy config wrapper used for deserialization
 */
public record CustomFrameConfig(List<CustomFrameData> frames) {

    public CustomFrameConfig {
        frames = Objects.requireNonNullElse(frames, List.of());
    }

    public List<CustomFrameData> getCustomFrames() {
        return frames;
    }

}