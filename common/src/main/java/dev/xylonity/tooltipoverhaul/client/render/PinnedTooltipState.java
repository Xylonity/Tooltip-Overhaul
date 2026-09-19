package dev.xylonity.tooltipoverhaul.client.render;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipLayout;
import dev.xylonity.tooltipoverhaul.client.screen.config.EffectEditorScreen;
import dev.xylonity.tooltipoverhaul.client.screen.config.FrameEditorScreen;
import dev.xylonity.tooltipoverhaul.client.screen.config.TooltipOverhaulConfigScreen;
import dev.xylonity.tooltipoverhaul.client.style.animation.TooltipAnimator;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.client.util.TextUtils;
import dev.xylonity.tooltipoverhaul.client.util.TooltipScrollState;
import dev.xylonity.tooltipoverhaul.compat.proxy.ScreenTypeProxy;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.registry.TooltipOverhaulKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Pinned item tooltips that survive closing and reopening inventory screens
 */
public final class PinnedTooltipState {

    private static final int BADGE_HEIGHT = 16;

    private static Screen owner;

    private static ItemStack hovered = ItemStack.EMPTY;
    private static List<ClientTooltipComponent> hoveredComponents = List.of();

    private static final List<Pin> pins = new ArrayList<>();

    private static long lastHover;
    private static Player pinnedPlayer;
    private static Pin dragging;
    private static int consumedButton = -1;
    private static double dragX, dragY;

    private static boolean allowed(Screen screen) {
        final boolean inventoryLike = screen instanceof AbstractContainerScreen<?> || ScreenTypeProxy.isJei(screen)
                || ScreenTypeProxy.isEmi(screen) || ScreenTypeProxy.isFtbQuests(screen) || ScreenTypeProxy.isFtbLibrary(screen);
        return Minecraft.getInstance().player != null && inventoryLike && !(screen instanceof FrameEditorScreen)
                && !(screen instanceof EffectEditorScreen) && !(screen instanceof TooltipOverhaulConfigScreen);
    }

    private static void attach(Screen screen) {
        if (owner == screen) {
            return;
        }

        owner = screen;
        hovered = ItemStack.EMPTY;
        hoveredComponents = List.of();
        lastHover = 0;
        dragging = null;
        consumedButton = -1;
    }

    public static void capture(TooltipContext context) {
        final Screen screen = Minecraft.getInstance().screen;
        if (!allowed(screen) || context.getStack().isEmpty()) {
            return;
        }

        attach(screen);
        hovered = context.getStack();
        hoveredComponents = context.getComponents();
        lastHover = System.nanoTime();
    }

    public static void clear() {
        owner = null;
        hovered = ItemStack.EMPTY;
        hoveredComponents = List.of();
        pins.clear();
        lastHover = 0;
        pinnedPlayer = null;
        dragging = null;
        consumedButton = -1;
    }

    /**
     * Changes input ownership without discarding the pinned items
     */
    public static void screenChanged(Screen screen) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            clear();
            return;
        }

        attach(allowed(screen) ? screen : null);
    }

    private static void unpin(Pin pin) {
        pins.remove(pin);
        if (dragging == pin) {
            dragging = null;
        }

        if (pins.isEmpty()) {
            pinnedPlayer = null;
        }

    }

    private static void pinHovered() {
        final Minecraft client = Minecraft.getInstance();
        while (pins.size() >= Math.max(1, TooltipsConfig.MAX_PINNED_TOOLTIPS)) {
            unpin(pins.get(0));
        }

        pinnedPlayer = client.player;
        pins.add(new Pin(hovered.copy(), List.copyOf(hoveredComponents), 24 + pins.size() * 16, 40 + pins.size() * 16));
    }

    private static Pin pinAt(double mx, double my) {
        final Minecraft client = Minecraft.getInstance();
        if (!allowed(client.screen)) {
            return null;
        }

        for (int i = pins.size() - 1; i >= 0; i--) {
            if (pins.get(i).contains(mx, my)) {
                return pins.get(i);
            }

        }

        return null;
    }

    private static boolean toggle(double mx, double my) {
        final Minecraft client = Minecraft.getInstance();
        if (!allowed(client.screen)) {
            return false;
        }

        attach(client.screen);

        final Pin over = pinAt(mx, my);
        if (over != null) {
            unpin(over);
            return true;
        }

        if (!hovered.isEmpty() && System.nanoTime() - lastHover <= 250_000_000L) {
            pinHovered();
            return true;
        }

        if (!pins.isEmpty()) {
            unpin(pins.get(pins.size() - 1));
            return true;
        }

        return false;
    }

    private static double mouseX(Minecraft client) {
        return client.mouseHandler.xpos() * client.getWindow().getGuiScaledWidth() / client.getWindow().getScreenWidth();
    }

    private static double mouseY(Minecraft client) {
        return client.mouseHandler.ypos() * client.getWindow().getGuiScaledHeight() / client.getWindow().getScreenHeight();
    }

    public static boolean key(int key, int scanCode, int action) {
        final Minecraft client = Minecraft.getInstance();
        if (!allowed(client.screen) || action != GLFW.GLFW_PRESS) {
            return false;
        }

        if (!TooltipOverhaulKeyMappings.PIN_TOOLTIP.matches(key, scanCode)) {
            return false;
        }

        if (client.screen.getFocused() instanceof EditBox) {
            return false;
        }

        return toggle(mouseX(client), mouseY(client));
    }

    public static boolean mouseButton(double mx, double my, int button, int action) {
        if (action == GLFW.GLFW_RELEASE && consumedButton == button) {
            consumedButton = -1;
            dragging = null;
            return true;
        }

        if (action != GLFW.GLFW_PRESS) {
            return false;
        }

        if (TooltipOverhaulKeyMappings.PIN_TOOLTIP.matchesMouse(button) && toggle(mx, my)) {
            consumedButton = button;
            return true;
        }

        final Pin pin = pinAt(mx, my);
        if (pin == null) {
            return false;
        }

        consumedButton = button;

        if (button == 0 && my < pin.top) {
            if (mx >= pin.right - 16) {
                unpin(pin);
            }
            else {
                pins.remove(pin);
                pins.add(pin);
                dragging = pin;
                dragX = mx - pin.x;
                dragY = my - pin.y;
            }

        }

        return true;
    }

    public static boolean scroll(double mx, double my, double delta) {
        final Pin pin = pinAt(mx, my);
        if (pin == null) {
            return false;
        }

        final TooltipScrollState.Snapshot previous = TooltipScrollState.snapshot();
        try {
            TooltipScrollState.restore(pin.scrolling);
            TooltipScrollState.onRawScroll(delta);
            pin.scrolling = TooltipScrollState.snapshot();
        }
        finally {
            TooltipScrollState.restore(previous);
        }

        return true;
    }

    public static void render(GuiGraphics graphics, int mouseX, int mouseY) {
        final Minecraft client = Minecraft.getInstance();
        if (pins.isEmpty() || !allowed(client.screen)) {
            return;
        }

        if (pinnedPlayer != null && pinnedPlayer != client.player) {
            pins.clear();
            pinnedPlayer = null;
            dragging = null;
            return;
        }

        attach(client.screen);

        if (dragging != null) {
            dragging.x = (float) (mouseX - dragX);
            dragging.y = (float) (mouseY - dragY);
        }

        final List<Pin> visiblePins = new ArrayList<>(pins);
        final float depthScale = 1f / visiblePins.size();
        for (int index = 0; index < visiblePins.size(); index++) {
            render(graphics, visiblePins.get(index), index, depthScale);
        }

    }

    private static void render(GuiGraphics graphics, Pin pin, int index, float depthScale) {
        final Minecraft client = Minecraft.getInstance();
        final int width = client.getWindow().getGuiScaledWidth(), height = client.getWindow().getGuiScaledHeight();
        final TooltipScrollState.Snapshot previous = TooltipScrollState.snapshot();
        final float previousCounter = TooltipRenderer.COUNTER;
        final float previousIconCounter = TooltipRenderer.ICON_COUNTER;

        graphics.pose().pushPose();
        try {
            graphics.pose().translate(0, 0, 3000 + index * 6000f * depthScale);
            graphics.pose().last().pose().scale(1f, 1f, depthScale);

            TooltipScrollState.restore(pin.scrolling);

            TooltipRenderer.COUNTER = Math.max(0, System.nanoTime() - pin.pinnedAtNano) / 1_000_000_000f;
            TooltipRenderer.ICON_COUNTER = TooltipRenderer.COUNTER;
            final List<ClientTooltipComponent> components = liveComponents(graphics, pin, width, height);
            final TooltipContext context = new TooltipContext(graphics, client.font, components, 0, 0, width,
                    Math.max(30, height - 24), DefaultTooltipPositioner.INSTANCE, pin.stack, false);

            context.setPinned(true);

            final TooltipRenderer renderer = new TooltipRenderer(context);
            renderer.init();
            if (!renderer.canRender()) {
                return;
            }

            final float tooltipWidth = context.getTooltipSize().x;
            final float tooltipHeight = context.getTooltipSize().y;
            for (int pass = 0; pass < 2; pass++) {
                context.setTooltipPosition(new Vec2(pin.x, pin.y));
                bounds(context, pin, tooltipWidth, tooltipHeight);
                pin.x += Math.max(4 - pin.left, Math.min(0, width - 4 - pin.right));
                pin.y += Math.max(BADGE_HEIGHT + 6 - pin.top, Math.min(0, height - 4 - pin.bottom));
            }

            context.setTooltipPosition(new Vec2(pin.x, pin.y));
            bounds(context, pin, tooltipWidth, tooltipHeight);

            graphics.flush();

            TooltipAnimator.render(context, false, 1);

            graphics.flush();

            graphics.pose().translate(0, 0, 2600);

            // Badge above the tooltip
            final int badgeTop = (int) pin.top - BADGE_HEIGHT - 2;
            final int badgeBottom = (int) pin.top - 2;
            graphics.fill((int) pin.left, badgeTop, (int) pin.right, badgeBottom, 0xF0343B4A);
            graphics.fill((int) pin.left + 1, badgeTop + 1, (int) pin.right - 1, badgeBottom - 1, 0xF0222833);
            graphics.drawString(client.font, client.font.plainSubstrByWidth(Component.translatable("tooltipoverhaul.pinned.title").getString(), Math.max(1, (int) (pin.right - pin.left - 25))), (int) pin.left + 4, badgeTop + 4, 0xFFBFD7F4, false);
            graphics.drawString(client.font, "x", (int) pin.right - 11, badgeTop + 4, 0xFFFFFFFF, false);

            pin.scrolling = TooltipScrollState.snapshot();
        }
        catch (RuntimeException exception) {
            TooltipOverhaul.LOGGER.warn("Could not render pinned tooltip", exception);
            unpin(pin);
        }
        finally {
            graphics.flush();
            graphics.pose().popPose();
            TooltipRenderer.COUNTER = previousCounter;
            TooltipRenderer.ICON_COUNTER = previousIconCounter;
            TooltipScrollState.restore(previous);
        }

    }

    /**
     * Updates the pinned tooltip per tick
     */
    private static List<ClientTooltipComponent> liveComponents(GuiGraphics graphics, Pin pin, int width, int height) {
        try {
            List<ClientTooltipComponent> components = TextUtils.getTooltipComponentsFrom(pin.stack, Minecraft.getInstance().font, width, 2.2f);
            components = TooltipOverhaul.PLATFORM.gatherTooltipComponents(graphics, pin.stack, components, Minecraft.getInstance().font, (int) pin.x, (int) pin.y, width, height, DefaultTooltipPositioner.INSTANCE);
            if (!components.isEmpty()) {
                pin.components = List.copyOf(components);
                pin.refreshWarningShown = false;
                return components;
            }

        }
        catch (RuntimeException exception) {
            if (!pin.refreshWarningShown) {
                TooltipOverhaul.LOGGER.warn("Could not refresh pinned tooltip contents", exception);
                pin.refreshWarningShown = true;
            }

        }

        return pin.components;
    }

    private static void bounds(TooltipContext context, Pin pin, float width, float height) {
        pin.left = pin.x - 4; pin.top = pin.y - 4; pin.right = pin.x + width + 4; pin.bottom = pin.y + height + 4;
        if (TooltipLayout.hasExternalIcon(context)) {
            final float ix = pin.x + TooltipLayout.iconX(context), iy = pin.y + TooltipLayout.iconY(context);
            pin.left = Math.min(pin.left, ix - 4); pin.top = Math.min(pin.top, iy - 4);
            pin.right = Math.max(pin.right, ix + Constants.getIconSize(context) + 4);
            pin.bottom = Math.max(pin.bottom, iy + Constants.getIconSize(context) + 4);
        }

    }

    private static final class Pin {

        private final ItemStack stack;
        private List<ClientTooltipComponent> components;
        private final long pinnedAtNano;
        private boolean refreshWarningShown;
        private float x, y;
        private float left, top, right, bottom;
        private TooltipScrollState.Snapshot scrolling = new TooltipScrollState.Snapshot(0, 0, false, 0, 0, 0);

        private Pin(ItemStack stack, List<ClientTooltipComponent> components, float x, float y) {
            this.stack = stack;
            this.components = components;
            this.pinnedAtNano = System.nanoTime();
            this.x = x;
            this.y = y;
        }

        private boolean contains(double mx, double my) {
            return mx >= left && mx <= right && my >= top - BADGE_HEIGHT - 2 && my <= bottom;
        }

    }

}
