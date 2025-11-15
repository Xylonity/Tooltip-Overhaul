package dev.xylonity.tooltipoverhaul.client.event;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.command.ReloadCommand;
import dev.xylonity.tooltipoverhaul.registry.TooltipOverhaulKeyMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class TooltipOverhaulClientEvents {

    @Mod.EventBusSubscriber(modid = TooltipOverhaul.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class TooltipOverhaulClientModEvents {

        @SubscribeEvent
        public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
            ReloadCommand.register(event.getDispatcher());
        }

    }

    @Mod.EventBusSubscriber(modid = TooltipOverhaul.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class TooltipOverhaulClientGameEvents {

        @SubscribeEvent
        public static void registerKeyMappingsEvent(RegisterKeyMappingsEvent event) {
            event.register(TooltipOverhaulKeyMappings.COMPARE_TOOLTIP);
        }

    }

}
