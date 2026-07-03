package dev.xylonity.tooltipoverhaul.client.event;

import com.mojang.brigadier.CommandDispatcher;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameManager;
import dev.xylonity.tooltipoverhaul.client.render.TooltipAnimationState;
import dev.xylonity.tooltipoverhaul.registry.TooltipOverhaulKeyMappings;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class TooltipOverhaulClientEvents {

    public static void init() {
        KeyBindingHelper.registerKeyBinding(TooltipOverhaulKeyMappings.COMPARE_TOOLTIP);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext) ->
                registerClientReloadCommand(dispatcher)
        );

        // Drives the tooltip "out" animation. Render.Post fires after the whole screen (including its
        // tooltips) has been drawn, which is the only point that works for every screen type
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) ->
                ScreenEvents.afterRender(screen).register((renderedScreen, graphics, mouseX, mouseY, tickDelta) ->
                        TooltipAnimationState.tick(graphics)
                )

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
