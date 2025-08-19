package dev.xylonity.tooltipoverhaul;

import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameManager;
import dev.xylonity.tooltipoverhaul.config.ConfigManager;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class TooltipOverhaulFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        // Loads a simplex wrapper of nightconfig, impl derived from knightlib
        ConfigManager.init(FabricLoader.getInstance().getConfigDir(), TooltipsConfig.class);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            CustomFrameManager.initialize();
        });

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return ResourceLocation.fromNamespaceAndPath(TooltipOverhaul.MOD_ID, "custom_frames_reload");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager resourceManager) {
                        CustomFrameManager.reset();
                        CustomFrameManager.initialize();
                    }
                }
        );

    }

}
