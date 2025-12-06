package dev.xylonity.tooltipoverhaul.client.event;

import com.mojang.brigadier.CommandDispatcher;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameManager;
import dev.xylonity.tooltipoverhaul.registry.TooltipOverhaulKeyMappings;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class TooltipOverhaulClientEvents {

    public static void init() {
        KeyBindingHelper.registerKeyBinding(TooltipOverhaulKeyMappings.COMPARE_TOOLTIP);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext) ->
                registerClientReloadCommand(dispatcher)
        );

    }

    private static void registerClientReloadCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal(TooltipOverhaul.MOD_ID)
                        .then(literal("reload")
                                .executes(context -> {
                                    try {
                                        TooltipOverhaul.LOGGER.info("Manual resources reload triggered by *client* command");

                                        CustomFrameManager.reset();
                                        CustomFrameManager.initialize();

                                        context.getSource().sendFeedback(Component.translatable("tooltipoverhaul.reload_resources.success"));

                                        return 1;
                                    }
                                    catch (Exception exception) {
                                        TooltipOverhaul.LOGGER.error("Failed to reload via client command", exception);

                                        context.getSource().sendError(Component.translatable("tooltipoverhaul.reload_resources.fail"));

                                        return 0;
                                    }

                                })
                        )
        );

    }

}
