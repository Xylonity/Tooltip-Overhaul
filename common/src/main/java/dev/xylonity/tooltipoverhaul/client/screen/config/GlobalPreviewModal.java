package dev.xylonity.tooltipoverhaul.client.screen.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import org.lwjgl.glfw.GLFW;

final class GlobalPreviewModal implements ConfigModal {

    private final FramePreviewPanel preview;
    private final FlatButton closeButton;
    private final int width;
    private final int height;
    private boolean closed;

    GlobalPreviewModal(int width, int height, int accent) {
        this.width = width;
        this.height = height;
        preview = new FramePreviewPanel(accent);
        preview.refreshGlobal();
        final int margin = Math.min(20, Math.min(width, height) / 10);
        preview.setBounds(margin, margin, width - margin * 2, Math.max(1, height - margin * 2 - 26));
        closeButton = new FlatButton((width - 90) / 2, height - margin - 18, 90, 18, CommonComponents.GUI_BACK, button -> close(), accent, true);
    }

    private void close() {
        closed = true;
        preview.close();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xDB050507);
        preview.render(graphics, Minecraft.getInstance().font, width, height, mouseX, mouseY, "");
        closeButton.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return closeButton.mouseClicked(mouseX, mouseY, button) || preview.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return preview.mouseDragged(deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        preview.mouseReleased();
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return preview.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) {
            close();
        }

        return true;
    }

    @Override
    public boolean closed() {
        return closed;
    }

}