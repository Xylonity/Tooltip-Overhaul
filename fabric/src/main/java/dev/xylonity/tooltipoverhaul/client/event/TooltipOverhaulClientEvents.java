package dev.xylonity.tooltipoverhaul.client.event;

import dev.xylonity.tooltipoverhaul.client.render.PinnedTooltipState;
import dev.xylonity.tooltipoverhaul.client.render.TooltipAnimationState;
import dev.xylonity.tooltipoverhaul.registry.TooltipOverhaulKeyMappings;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

public class TooltipOverhaulClientEvents {

    public static void init() {
        KeyBindingHelper.registerKeyBinding(TooltipOverhaulKeyMappings.COMPARE_TOOLTIP);
        KeyBindingHelper.registerKeyBinding(TooltipOverhaulKeyMappings.OPEN_CONFIG);
        KeyBindingHelper.registerKeyBinding(TooltipOverhaulKeyMappings.PIN_TOOLTIP);

        // Drives the tooltip "out" animation. Render.Post fires after the whole screen (including its
        // tooltips) has been drawn, which is the only point that works for every screen type
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) ->
                ScreenEvents.afterRender(screen).register((renderedScreen, graphics, mouseX, mouseY, tickDelta) ->
                        {
                            TooltipAnimationState.tick(graphics);
                            PinnedTooltipState.render(graphics, mouseX, mouseY);
                        }

                )

        );

        // Keeps the frame counter going while no screen is open as hud tooltips also rely on it
        HudRenderCallback.EVENT.register((graphics, deltaTracker) -> TooltipAnimationState.hudTick());

    }

}
