package dev.xylonity.tooltipoverhaul;

import dev.xylonity.tooltipoverhaul.platform.TooltipPlatform;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public class TooltipOverhaul {

    public static final String MOD_ID = "tooltipoverhaul";
    public static final Logger LOGGER = LoggerFactory.getLogger("Tooltip Overhaul");

    public static final TooltipPlatform PLATFORM = ServiceLoader.load(TooltipPlatform.class).findFirst().orElseThrow();

    public static ResourceLocation locationOf(String path) {
        return new ResourceLocation(path);
    }

}