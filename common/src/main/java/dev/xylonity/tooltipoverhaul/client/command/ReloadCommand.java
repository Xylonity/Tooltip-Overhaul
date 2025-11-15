package dev.xylonity.tooltipoverhaul.client.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ReloadCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal(TooltipOverhaul.MOD_ID)
                        .then(Commands.literal("reload")
                                .executes(context -> {
                                    try {
                                        TooltipOverhaul.LOGGER.info("Manual resources reload triggered by command");

                                        CustomFrameManager.reset();
                                        CustomFrameManager.initialize();

                                        context.getSource().sendSuccess(() -> Component.translatable("tooltipoverhaul.reload_resources.success"), false);

                                        return 1;
                                    }
                                    catch (Exception e) {
                                        TooltipOverhaul.LOGGER.error("Failed to reload via command: {}", e.getMessage(), e);

                                        context.getSource().sendFailure(Component.translatable("tooltipoverhaul.reload_resources.fail"));

                                        return 0;
                                    }
                                })
                        )
        );

    }

}