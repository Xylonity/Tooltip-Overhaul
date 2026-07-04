package dev.xylonity.tooltipoverhaul.compat.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ScreenTypeProxy {

    public static boolean isFtbQuests(Screen screen) {
        return nameOf(screen).contains("ftbquests");
    }

    public static boolean isFtbLibrary(Screen screen) {
        return nameOf(screen).contains("ftblibrary");
    }

    public static boolean isJei(Screen screen) {
        return nameOf(screen).contains("mezz.jei");
    }

    public static boolean isEmi(Screen screen) {
        return nameOf(screen).contains("dev.emi.emi");
    }

    public static boolean isContainerLikeOrJeiEmi() {
        final Screen screen = Minecraft.getInstance().screen;
        if (screen == null) {
            return false;
        }

        return isJei(screen) || isEmi(screen);
    }

    private static String nameOf(Screen screen) {
        return (screen == null ? "" : screen.getClass().getName()).toLowerCase();
    }

}
