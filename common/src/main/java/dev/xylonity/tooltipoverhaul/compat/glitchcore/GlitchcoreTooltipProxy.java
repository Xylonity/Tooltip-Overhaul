package dev.xylonity.tooltipoverhaul.compat.glitchcore;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Replays GlitchCore's tooltip event so its components (the bug was reproduced with Tough As Nails' thirst droplets on
 * water bottles) survive the fact that I cancel renderTooltipInternal at head
 */
public final class GlitchcoreTooltipProxy {

    private static final String EVENT_CLASS = "glitchcore.event.client.RenderTooltipEvent";
    private static final String BASE_EVENT_CLASS = "glitchcore.event.Event";
    private static final String MANAGER_CLASS = "glitchcore.event.EventManager";

    @SuppressWarnings("unchecked")
    public static List<ClientTooltipComponent> gather(GuiGraphics graphics, ItemStack stack, List<ClientTooltipComponent> components, Font font, int mouseX, int mouseY, int screenWidth, int screenHeight, ClientTooltipPositioner positioner) {
        try {
            final List<ClientTooltipComponent> mutable = new ArrayList<>(components);

            final ClassLoader loader = GlitchcoreTooltipProxy.class.getClassLoader();
            final Class<?> eventClass = Class.forName(EVENT_CLASS, false, loader);
            final Class<?> baseEventClass = Class.forName(BASE_EVENT_CLASS, false, loader);
            final Class<?> managerClass = Class.forName(MANAGER_CLASS, false, loader);

            final Constructor<?> constructor = eventClass.getConstructor(
                    ItemStack.class, GuiGraphics.class, int.class, int.class, int.class, int.class,
                    List.class, Font.class, ClientTooltipPositioner.class
            );
            final Object event = constructor.newInstance(stack, graphics, mouseX, mouseY, screenWidth, screenHeight, mutable, font, positioner);

            final Method fire = managerClass.getMethod("fire", baseEventClass);
            fire.invoke(null, event);

            final Object result = eventClass.getMethod("getComponents").invoke(event);
            if (result instanceof List<?>) {
                return (List<ClientTooltipComponent>) result;
            }

            return mutable;
        }
        catch (Throwable ignored) {
            return components;
        }

    }

}