package dev.xylonity.tooltipoverhaul.compat.proxy;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Deque;
import java.util.Optional;

/**
 * Proxy for the FTB Library based screens (FTB Quests primarily)
 * <p>
 * BaseScreen#getIngredientUnderMouse only iterates its regular widgets and skips the modal panels deque entirely
 * (the opened quest view is a ModalPanel, so querying the base gui would solve the quest node buried under the popup
 * instead), and RewardButton wraps its reward ingredient inside a nested Optional
 */
public final class FtbLibraryProxy {

    private static Class<?> cachedScreenClass;

    private static Method getGui;                    // ScreenWrapper#getGui() -> BaseScreen
    private static Method getIngredientUnderMouse;   // Widget#getIngredientUnderMouse() -> Optional<PositionedIngredient>
    private static Method ingredient;                // PositionedIngredient#ingredient() -> Object
    private static Method tooltip;                   // PositionedIngredient#tooltip() -> boolean
    private static Field modalPanels;                // BaseScreen#modalPanels -> Deque<ModalPanel>

    private static boolean BROKEN = false;
    private static boolean MODAL_BROKEN = false;

    public static ItemStack getItemStack() {
        if (BROKEN || !TooltipOverhaul.PLATFORM.isModLoaded("ftblibrary")) {
            return ItemStack.EMPTY;
        }

        final Screen screen = Minecraft.getInstance().screen;
        if (screen == null || !ScreenTypeProxy.isFtbLibrary(screen)) {
            return ItemStack.EMPTY;
        }

        try {
            if (cachedScreenClass != screen.getClass()) {
                getGui = screen.getClass().getMethod("getGui");
                cachedScreenClass = screen.getClass();
            }

            final Object gui = getGui.invoke(screen);
            if (gui == null) {
                return ItemStack.EMPTY;
            }

            if (getIngredientUnderMouse == null) {
                getIngredientUnderMouse = gui.getClass().getMethod("getIngredientUnderMouse");
            }

            // The top modal panel takes priority, as the base gui search skips modal panels and would match whatever
            // widget lies buried under the task popup
            Object positioned = null;
            final Object modalPanel = topModalPanel(gui);
            if (modalPanel != null) {
                positioned = unwrapOptional(getIngredientUnderMouse.invoke(modalPanel));
            }

            if (positioned == null) {
                positioned = unwrapOptional(getIngredientUnderMouse.invoke(gui));
            }

            if (positioned == null) {
                return ItemStack.EMPTY;
            }

            if (ingredient == null) {
                ingredient = positioned.getClass().getMethod("ingredient");
                tooltip = positioned.getClass().getMethod("tooltip");
            }

            return resolveStack(positioned);
        }
        catch (Throwable throwable) {
            // Never retries against an incompatible FTB Library version
            BROKEN = true;
        }

        return ItemStack.EMPTY;
    }

    /**
     * Walks a PositionedIngredient down to an ItemStack
     */
    private static ItemStack resolveStack(Object positioned) throws Exception {
        Object current = positioned;
        boolean showTooltip = false;
        // Capped to 5, believe
        for (int i = 0; i < 5 && current != null; i++) {
            if (current instanceof ItemStack stack) {
                return (showTooltip && !stack.isEmpty()) ? stack : ItemStack.EMPTY;
            }

            if (current instanceof Optional<?> optional) {
                current = optional.orElse(null);
            }
            else if (ingredient != null && ingredient.getDeclaringClass().isInstance(current)) {
                showTooltip = (boolean) tooltip.invoke(current);
                current = ingredient.invoke(current);
            }
            else if (current.getClass().getName().endsWith(".ItemIcon")) {
                current = current.getClass().getMethod("getStack").invoke(current);
            }
            else {
                break;
            }

        }

        return ItemStack.EMPTY;
    }

    private static Object topModalPanel(Object gui) {
        if (MODAL_BROKEN) {
            return null;
        }

        try {
            if (modalPanels == null) {
                Class<?> clazz = gui.getClass();
                while (clazz != null && modalPanels == null) {
                    try {
                        modalPanels = clazz.getDeclaredField("modalPanels");
                        modalPanels.setAccessible(true);
                    }
                    catch (NoSuchFieldException exception) {
                        clazz = clazz.getSuperclass();
                    }

                }

            }

            if (modalPanels != null && modalPanels.get(gui) instanceof Deque<?> deque) {
                return deque.peek();
            }

        }
        catch (Throwable ignored) {
            ;;
        }

        // Degrades to the base gui search only instead of disabling the whole proxy
        MODAL_BROKEN = true;

        return null;
    }

    private static Object unwrapOptional(Object object) {
        return (object instanceof Optional<?> optional) ? optional.orElse(null) : object;
    }

}
