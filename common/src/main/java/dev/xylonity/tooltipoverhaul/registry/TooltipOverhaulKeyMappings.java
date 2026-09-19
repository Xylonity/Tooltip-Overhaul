package dev.xylonity.tooltipoverhaul.registry;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xylonity.tooltipoverhaul.client.screen.config.TooltipOverhaulConfigScreen;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class TooltipOverhaulKeyMappings {

    public static final KeyMapping PIN_TOOLTIP = new KeyMapping(
            "tooltipoverhaul.binds.pin_tooltip", InputConstants.UNKNOWN.getValue(), "tooltipoverhaul.binds.category");

    public static final KeyMapping COMPARE_TOOLTIP = new KeyMapping(
            "tooltipoverhaul.binds.compare_bind",
            InputConstants.KEY_LSHIFT,
            "tooltipoverhaul.binds.category"
    );

    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "tooltipoverhaul.binds.open_config",
            InputConstants.UNKNOWN.getValue(),
            "tooltipoverhaul.binds.category"
    );

    public static boolean handleConfigShortcut(Minecraft client, int key, int scanCode) {
        if (client.screen != null || !OPEN_CONFIG.matches(key, scanCode)) {
            return false;
        }

        client.setScreen(new TooltipOverhaulConfigScreen(null, TooltipsConfig.class));

        return true;
    }

    public static boolean handleConfigMouseShortcut(Minecraft client, int button) {
        if (client.screen != null || !OPEN_CONFIG.matchesMouse(button)) {
            return false;
        }

        client.setScreen(new TooltipOverhaulConfigScreen(null, TooltipsConfig.class));

        return true;
    }

}
