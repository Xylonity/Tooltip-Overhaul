package dev.xylonity.tooltipoverhaul;

import dev.xylonity.tooltipoverhaul.config.ConfigManager;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(TooltipOverhaul.MOD_ID)
public class TooltipOverhaulForge {

    public TooltipOverhaulForge() {
        if (FMLLoader.getDist().isClient()) {
            ClientEntrypoint.init(FMLJavaModLoadingContext.get().getModEventBus());
        } else {
            TooltipOverhaul.LOGGER.warn("Won't load as the mod should be initialized on the client side.");
        }

    }

    private static final class ClientEntrypoint {

        public static void init(IEventBus modBus) {
            modBus.addListener(ClientEntrypoint::onClientSetup);
            modBus.addListener(ClientEntrypoint::onRegisterClientReloads);
        }

        private static void onClientSetup(final FMLClientSetupEvent event) {
            // Loads a simplex wrapper of nightconfig, impl derived from knightlib
            ConfigManager.init(FMLPaths.CONFIGDIR.get(), TooltipsConfig.class);
            event.enqueueWork(() -> CustomFrameManager.initialize());
        }

        private static void onRegisterClientReloads(final RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(new SimplePreparableReloadListener<Void>() {
                @Override
                protected Void prepare(ResourceManager rm, ProfilerFiller profiler) {
                    return null;
                }

                @Override
                protected void apply(Void v, ResourceManager rm, ProfilerFiller profiler) {
                    CustomFrameManager.reset();
                    CustomFrameManager.initialize(rm);
                }
            });
        }

    }

}
