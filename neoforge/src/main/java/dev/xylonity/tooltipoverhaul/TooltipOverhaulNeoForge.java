package dev.xylonity.tooltipoverhaul;

import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameManager;
import dev.xylonity.tooltipoverhaul.client.screen.config.TooltipOverhaulConfigScreen;
import dev.xylonity.tooltipoverhaul.client.util.Palette;
import dev.xylonity.tooltipoverhaul.config.ConfigManager;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectFieldRenderer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.LoggerFactory;

@Mod(TooltipOverhaul.MOD_ID)
public class TooltipOverhaulNeoForge {

    public TooltipOverhaulNeoForge(IEventBus eventBus) {
        if (FMLLoader.getDist().isClient()) {
            ClientEntrypoint.init(eventBus);
        }
        else {
            LoggerFactory.getLogger("Tooltip Overhaul").warn("Tooltip Overhaul is a client-side mod and does nothing on a dedicated server.");
        }

    }

    private static final class ClientEntrypoint {

        public static void init(IEventBus modBus) {
            ModLoadingContext.get().registerExtensionPoint(
                    IConfigScreenFactory.class,
                    () -> (mc, parent) -> new TooltipOverhaulConfigScreen(parent, TooltipsConfig.class)
            );

            modBus.addListener(ClientEntrypoint::onClientSetup);
            modBus.addListener(ClientEntrypoint::onRegisterClientReloads);
            modBus.addListener(ClientEntrypoint::onRegisterShaders);

            NeoForge.EVENT_BUS.addListener(ClientEntrypoint::onShutdown);
        }

        private static void onShutdown(GameShuttingDownEvent event) {
            EffectFieldRenderer.reset();
        }

        private static void onRegisterShaders(RegisterShadersEvent event) {
            EffectFieldRenderer.reset();
            try {
                event.registerShader(new ShaderInstance(event.getResourceProvider(), EffectFieldRenderer.SHADER_ID,
                        DefaultVertexFormat.POSITION), EffectFieldRenderer::setShader);
            }
            catch (Exception failure)  {
                TooltipOverhaul.LOGGER.warn("Could not load effect shader...", failure);
            }

        }

        private static void onClientSetup(final FMLClientSetupEvent event) {
            // Loads a simplex wrapper of nightconfig, impl derived from knightlib
            ConfigManager.init(FMLPaths.CONFIGDIR.get(), TooltipsConfig.class);
            ConfigManager.onReload(TooltipsConfig.class, Palette::reload);
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
