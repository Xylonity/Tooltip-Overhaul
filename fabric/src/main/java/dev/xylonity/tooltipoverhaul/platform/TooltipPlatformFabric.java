package dev.xylonity.tooltipoverhaul.platform;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class TooltipPlatformFabric implements TooltipPlatform {

    @Override
    public boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }

    @Override
    public Path resolveConfigFile(String configFileName) {
        return FabricLoader.getInstance().getConfigDir().resolve(configFileName);
    }

}
