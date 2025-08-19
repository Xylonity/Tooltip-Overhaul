package dev.xylonity.tooltipoverhaul.platform;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class TooltipPlatformForge implements TooltipPlatform {

    @Override
    public boolean isModLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }

    @Override
    public Path resolveConfigFile(String config) {
        return FMLPaths.CONFIGDIR.get().resolve(config);
    }

}