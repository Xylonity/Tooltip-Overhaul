package dev.xylonity.tooltipoverhaul.platform;

import java.nio.file.Path;

public interface TooltipPlatform {
    boolean isModLoaded(String modid);
    Path resolveConfigFile(String configFileName);
}
