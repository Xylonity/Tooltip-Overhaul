package dev.xylonity.tooltipoverhaul.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Renderer wrapper that contains the relevant info from the tooltip context.
 */
public class TooltipContext {

    private final GuiGraphics graphics;
    private final int mouseX;
    private final int mouseY;
    private final int screenW;
    private final int screenH;
    private final List<?> components;
    private ItemStack stack;
    private float elapsedSeconds = 0f;

    private TooltipContext(GuiGraphics graphics, int mouseX, int mouseY, int screenW, int screenH, List<ClientTooltipComponent> components, ItemStack stack) {
        this.graphics = graphics;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.screenW = screenW;
        this.screenH = screenH;
        this.components = components;
        this.stack = stack;
    }

    public static TooltipContext of(GuiGraphics graphics, int mouseX, int mouseY, int screenW, int screenH, List<ClientTooltipComponent> components, ItemStack stack) {
        return new TooltipContext(graphics, mouseX, mouseY, screenW, screenH, components, stack);
    }

    public List<?> getComponents() {
        return components;
    }

    public int mouseX() {
        return mouseX;
    }

    public int mouseY() {
        return mouseY;
    }

    public int width() {
        return screenW;
    }

    public int height() {
        return screenH;
    }

    /**
     * Returns the ItemStack in case there is one (or else an EMPTY one)
     */
    public ItemStack stack() {
        return stack;
    }

    public float time() {
        return elapsedSeconds;
    }

    public GuiGraphics graphics() {
        return graphics;
    }

    public PoseStack pose() {
        return graphics.pose();
    }

    public MultiBufferSource buffer() {
        return graphics.bufferSource();
    }

    public void push(Runnable r) {
        pose().pushPose();
        try {
            r.run();
        }
        finally {
            pose().popPose();
        }

    }

    public void translate(float x, float y, float z) {
        pose().translate(x, y, z);
    }

    public void scale(float x, float y, float z) {
        pose().scale(x, y, z);
    }

    public void multiply(Axis axis, float degrees) {
        pose().mulPose(axis.rotationDegrees(degrees));
    }

    public void flush() {
        graphics.flush();
    }

}
