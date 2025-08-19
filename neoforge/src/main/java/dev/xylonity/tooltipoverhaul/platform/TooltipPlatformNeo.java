package dev.xylonity.tooltipoverhaul.platform;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class TooltipPlatformNeo implements TooltipPlatform {

    @Override
    public boolean isModLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }

    @Override
    public Path resolveConfigFile(String config) {
        return FMLPaths.CONFIGDIR.get().resolve(config);
    }

}