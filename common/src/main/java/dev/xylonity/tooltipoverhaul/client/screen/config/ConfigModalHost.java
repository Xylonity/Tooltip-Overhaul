package dev.xylonity.tooltipoverhaul.client.screen.config;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Owns the single modal layer of a screen and centralizes all modal input routing
 */
final class ConfigModalHost {

    private ConfigModal modal;

    void open(ConfigModal modal) {
        this.modal = modal;
    }

    void clear() {
        modal = null;
    }

    boolean isOpen() {
        return modal != null;
    }

    void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (modal != null) {
            modal.render(graphics, mouseX, mouseY, partialTick);
            closeFinished();
        }

    }

    void tick() {
        if (modal != null) {
            modal.tick();
            closeFinished();
        }

    }

    boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (modal == null) {
            return false;
        }

        modal.mouseClicked(mouseX, mouseY, button);
        closeFinished();

        return true;
    }

    boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (modal == null) {
            return false;
        }

        modal.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        closeFinished();

        return true;
    }

    boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (modal == null) {
            return false;
        }

        modal.mouseReleased(mouseX, mouseY, button);
        closeFinished();

        return true;
    }

    boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (modal == null) {
            return false;
        }

        modal.mouseScrolled(mouseX, mouseY, delta);
        closeFinished();

        return true;
    }

    boolean keyPressed(int key, int scanCode, int modifiers) {
        if (modal == null) {
            return false;
        }

        modal.keyPressed(key, scanCode, modifiers);
        closeFinished();

        return true;
    }

    boolean charTyped(char character, int modifiers) {
        if (modal == null) {
            return false;
        }

        modal.charTyped(character, modifiers);
        closeFinished();

        return true;
    }

    private void closeFinished() {
        if (modal != null && modal.closed()) {
            modal = null;
        }

    }

}
