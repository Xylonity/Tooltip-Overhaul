package dev.xylonity.tooltipoverhaul.client.screen.config;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Common screen shared by the complete configuration screen family
 */
abstract class AbstractConfigScreen extends Screen {

    private static final int MODAL_LAYER_Z = 4000;

    protected final Screen parent;
    protected final int accent;

    protected final Minecraft minecraft = Minecraft.getInstance();

    protected final long openedAt = Util.getMillis();

    protected final ConfigModalHost modals = new ConfigModalHost();

    protected AbstractConfigScreen(Component title, Screen parent, int accent) {
        super(title);
        this.parent = parent;
        this.accent = accent;
    }

    @Override
    protected final void init() {
        modals.clear();
        initScreen();
    }

    /**
     * Builds the concrete screen after shared transient state has been reset
     */
    protected abstract void initScreen();

    protected final void renderScreenShell(GuiGraphics graphics, String description) {
        ConfigScreenStyle.renderBackground(graphics, width, height, ConfigScreenStyle.HEADER_HEIGHT, accent);
        ConfigScreenStyle.renderBrandedHeader(graphics, font, width, title.getString(), description, openedAt);
    }

    protected final void returnToParent() {
        minecraft.setScreen(parent);
    }

    protected final void addCenteredFooterButtons(int buttonWidth, int gap, FooterAction... actions) {
        final int totalWidth = buttonWidth * actions.length + gap * Math.max(0, actions.length - 1);
        final int startX = (width - totalWidth) / 2;
        final int buttonY = height - 26;
        for (int index = 0; index < actions.length; index++) {
            final FooterAction action = actions[index];
            final FlatButton button = new FlatButton(startX + index * (buttonWidth + gap), buttonY, buttonWidth, 18, action.label(),
                    ignored -> action.press().run(), accent, action.primary()).withEntranceDelay(action.entranceDelay());
            addRenderableWidget(button);
        }

    }

    /**
     * Renders the modal above the screen own overlays
     */
    protected final void renderModalLayer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.pose().pushPose();

        graphics.pose().translate(0, 0, MODAL_LAYER_Z);
        try {
            modals.render(graphics, mouseX, mouseY, partialTick);
        }
        finally {
            graphics.pose().popPose();
        }

    }

    @Override
    public final boolean mouseClicked(double mouseX, double mouseY, int button) {
        return modals.mouseClicked(mouseX, mouseY, button) || handleMouseClicked(mouseX, mouseY, button);
    }

    protected boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public final boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return modals.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) || handleMouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    protected boolean handleMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public final boolean mouseReleased(double mouseX, double mouseY, int button) {
        return modals.mouseReleased(mouseX, mouseY, button) || handleMouseReleased(mouseX, mouseY, button);
    }

    protected boolean handleMouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public final boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return modals.mouseScrolled(mouseX, mouseY, delta) || handleMouseScrolled(mouseX, mouseY, delta);
    }

    protected boolean handleMouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public final boolean keyPressed(int key, int scanCode, int modifiers) {
        return modals.keyPressed(key, scanCode, modifiers) || handleKeyPressed(key, scanCode, modifiers);
    }

    protected boolean handleKeyPressed(int key, int scanCode, int modifiers) {
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public final boolean charTyped(char character, int modifiers) {
        return modals.charTyped(character, modifiers) || handleCharTyped(character, modifiers);
    }

    protected boolean handleCharTyped(char character, int modifiers) {
        return super.charTyped(character, modifiers);
    }

    @Override
    public final void tick() {
        tickBeforeModals();
        modals.tick();
        tickScreen();
    }

    protected void tickBeforeModals() {
        ;;
    }

    protected void tickScreen() {
        super.tick();
    }

    protected record FooterAction(
            Component label,
            Runnable press,
            boolean primary,
            int entranceDelay
    ) {

        protected FooterAction(Component label, Runnable press, boolean primary) {
            this(label, press, primary, 0);
        }

    }

}