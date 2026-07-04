package dev.xylonity.tooltipoverhaul.client.event;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.command.ReloadCommand;
import dev.xylonity.tooltipoverhaul.client.render.TooltipAnimationState;
import dev.xylonity.tooltipoverhaul.registry.TooltipOverhaulKeyMappings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

public class TooltipOverhaulClientEvents {

    @EventBusSubscriber(modid = TooltipOverhaul.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class TooltipOverhaulClientForgeEvents {

        @SubscribeEvent
        public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
            ReloadCommand.register(event.getDispatcher());
        }

        /**
         * Drives the tooltip "out" animation. Render.Post fires after the whole screen (including its
         * tooltips) has been drawn, which is the only point that works for every screen type
         */
        @SubscribeEvent
        public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
            TooltipAnimationState.tick(event.getGuiGraphics());
        }

    }

    @EventBusSubscriber(modid = TooltipOverhaul.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class TooltipOverhaulClientModEvents {

        @SubscribeEvent
        public static void registerKeyMappingsEvent(RegisterKeyMappingsEvent event) {
            event.register(TooltipOverhaulKeyMappings.COMPARE_TOOLTIP);
        }

    }

}
