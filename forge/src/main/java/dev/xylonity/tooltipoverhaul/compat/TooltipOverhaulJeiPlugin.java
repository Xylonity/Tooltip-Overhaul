package dev.xylonity.tooltipoverhaul.compat;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.compat.jei.JeiHoverHolder;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public final class TooltipOverhaulJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(TooltipOverhaul.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        JeiHoverHolder.init(runtime);
    }

}