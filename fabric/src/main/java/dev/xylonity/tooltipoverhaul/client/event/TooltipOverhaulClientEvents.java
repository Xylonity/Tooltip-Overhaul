package dev.xylonity.tooltipoverhaul.client.event;

import dev.xylonity.tooltipoverhaul.registry.TooltipOverhaulKeyMappings;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class TooltipOverhaulClientEvents {

    public static void init() {
        KeyBindingHelper.registerKeyBinding(TooltipOverhaulKeyMappings.COMPARE_TOOLTIP);
    }

}
