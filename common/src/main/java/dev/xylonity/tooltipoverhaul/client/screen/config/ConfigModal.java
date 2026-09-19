package dev.xylonity.tooltipoverhaul.client.screen.config;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Input and render contract for modal overlays used by configuration screens
 */
interface ConfigModal {

    void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);

    boolean mouseClicked(double mouseX, double mouseY, int button);

    default boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return true;
    }

    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        return true;
    }

    default boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return true;
    }

    default boolean keyPressed(int key, int scanCode, int modifiers) {
        return true;
    }

    default boolean charTyped(char character, int modifiers) {
        return true;
    }

    default void tick() {
        ;;
    }

    boolean closed();
}
