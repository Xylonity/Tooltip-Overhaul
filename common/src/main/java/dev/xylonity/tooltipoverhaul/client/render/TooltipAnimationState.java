package dev.xylonity.tooltipoverhaul.client.render;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.style.animation.TooltipAnimator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * Holds a snapshot of the last main tooltip so an "out" animation can keep drawing it for a short time after the hover ends
 */
public final class TooltipAnimationState {

    @Nullable
    private static Font font;
    @Nullable
    private static List<ClientTooltipComponent> components;
    @Nullable
    private static ItemStack stack;
    @Nullable
    private static ClientTooltipPositioner positioner;

    private static int mouseX;
    private static int mouseY;
    private static int screenWidth;
    private static int screenHeight;

    private static long lastAliveNano;

    private static long frameCounter;
    private static long lastAliveFrame;

    private static boolean suppressCapture = false;

    /**
     * Used by the live preview
     */
    public static void setSuppressCapture(boolean suppress) {
        suppressCapture = suppress;
    }

    /**
     * Stores the data needed to replay the given (main) tooltip
     */
    public static void capture(TooltipContext context) {
        if (suppressCapture) {
            return;
        }

        font = context.getFont();
        components = context.getComponents();
        stack = context.getStack();
        positioner = context.getTooltipPositioner();
        mouseX = context.getMouseX();
        mouseY = context.getMouseY();
        screenWidth = context.getScreenWidth();
        screenHeight = context.getScreenHeight();
        lastAliveNano = System.nanoTime();
        lastAliveFrame = frameCounter;
    }

    public static void clear() {
        font = null;
        components = null;
        stack = null;
        positioner = null;
    }

    public static void tick(GuiGraphics graphics) {
        frameCounter++;

        if (font == null || positioner == null || stack == null || components == null || components.isEmpty()) {
            return;
        }
        if (frameCounter - lastAliveFrame <= 1) {
            return;
        }

        final float elapsed = (System.nanoTime() - lastAliveNano) / 1_000_000_000f;

        try {
            // The context resolves the stack's custom frame, so the replay honors per-frame appear anims
            final TooltipContext context = new TooltipContext(graphics, font, components, mouseX, mouseY, screenWidth, screenHeight, positioner, stack, true);
            if (!TooltipAnimator.animation(context).isAnimated()) {
                clear();
                return;
            }

            final float duration = TooltipAnimator.duration(context);
            if (elapsed >= duration) {
                clear();
                return;
            }

            final TooltipRenderer renderer = new TooltipRenderer(context);
            renderer.init();
            renderer.renderOut(elapsed / duration);
        }
        catch (Throwable throwable) {
            TooltipOverhaul.LOGGER.warn("Dropped tooltip out-animation replay for {}: {}", stack, throwable.toString());
            clear();
        }

    }

}
