package dev.xylonity.tooltipoverhaul.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class TooltipOverhaulKeyMappings {

    public static final KeyMapping COMPARE_TOOLTIP = new KeyMapping(
            "tooltipoverhaul.binds.compare_bind",
            InputConstants.KEY_LSHIFT,
            "tooltipoverhaul.binds.category"
    );

}
