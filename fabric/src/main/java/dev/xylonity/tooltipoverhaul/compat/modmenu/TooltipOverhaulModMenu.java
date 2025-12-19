package dev.xylonity.tooltipoverhaul.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.xylonity.tooltipoverhaul.client.screen.TooltipOverhaulConfigScreen;

public class TooltipOverhaulModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TooltipOverhaulConfigScreen::new;
    }

}
