package dev.xylonity.tooltipoverhaul.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.xylonity.tooltipoverhaul.client.screen.config.TooltipOverhaulConfigScreen;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;

public class TooltipOverhaulModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new TooltipOverhaulConfigScreen(parent, TooltipsConfig.class);
    }

}
