package dev.xylonity.tooltipoverhaul.client.event;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.command.ReloadCommand;
import dev.xylonity.tooltipoverhaul.registry.TooltipOverhaulKeyMappings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class TooltipOverhaulClientEvents {

    @EventBusSubscriber(modid = TooltipOverhaul.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class TooltipOverhaulClientForgeEvents {

        @SubscribeEvent
        public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
            ReloadCommand.register(event.getDispatcher());
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
