package dev.xylonity.tooltipoverhaul.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameManager;
import dev.xylonity.tooltipoverhaul.client.style.StyleFactory;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextAxis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

/**
 * Renderer wrapper that contains the relevant info from the tooltip context.
 */
public class TooltipContext {

    private final GuiGraphics graphics;
    private final Font font;
    private final List<ClientTooltipComponent> components;
    private final int mouseX;
    private final int mouseY;
    private final int screenWidth;
    private final int screenHeight;
    private final ClientTooltipPositioner tooltipPositioner;

    private final ItemStack stack;

    private Vec2 tooltipSize;
    private Vec2 tooltipPosition;
    private LayerDepth layerDepth;

    private final List<ITooltipLayer> layers;
    private final @Nullable CustomFrameData frameData;

    private final boolean isMainTooltip;
    private final boolean isEmptyTooltip;
    private final boolean hasIcon;

    private final int paddingX;
    private final int paddingY;

    public TooltipContext(GuiGraphics graphics, Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, int screenWidth, int screenHeight, ClientTooltipPositioner tooltipPositioner, @NotNull ItemStack stack, boolean isMainTooltip) {
        this.graphics = graphics;
        this.font = font;
        this.components = components;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.tooltipPositioner = tooltipPositioner;
        this.stack = stack;
        this.isMainTooltip = isMainTooltip;
        this.frameData = CustomFrameManager.of(stack).orElse(null);
        this.isEmptyTooltip = stack.isEmpty();
        this.hasIcon = RenderUtils.hasIcon(this);
        this.paddingX = RenderUtils.calculatePadding(this, TextAxis.X);
        this.paddingY = RenderUtils.calculatePadding(this, TextAxis.Y);
        this.layers = new StyleFactory().create(this, frameData);
    }

    public GuiGraphics getGraphics() {
        return graphics;
    }

    public Font getFont() {
        return font;
    }

    public List<ClientTooltipComponent> getComponents() {
        return components;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public ClientTooltipPositioner getTooltipPositioner() {
        return tooltipPositioner;
    }

    public MultiBufferSource getBuffer() {
        return graphics.bufferSource();
    }

    public ItemStack getStack() {
        return stack;
    }

    public boolean isMainTooltip() {
        return isMainTooltip;
    }

    public void setTooltipSize(Vec2 tooltipSize) {
        this.tooltipSize = tooltipSize;
    }

    public Vec2 getTooltipSize() {
        return tooltipSize;
    }

    public void setTooltipPosition(Vec2 tooltipPosition) {
        this.tooltipPosition = tooltipPosition;
    }

    public void setLayerDepth(LayerDepth layerDepth) {
        this.layerDepth = layerDepth;
    }

    public LayerDepth getLayerDepth() {
        return layerDepth;
    }

    public Vec2 getTooltipPosition() {
        return tooltipPosition;
    }

    public List<ITooltipLayer> getTooltipLayers() {
        return layers;
    }

    public boolean isEmptyTooltip() {
        return isEmptyTooltip;
    }

    public boolean hasIcon() {
        return hasIcon;
    }

    public int getPaddingX() {
        return paddingX;
    }

    public int getPaddingY() {
        return paddingY;
    }

    @Nullable
    public CustomFrameData getFrameData() {
        return frameData;
    }

    public void flush() {
        this.graphics.flush();
    }

    public PoseStack getPose() {
        return graphics.pose();
    }

    public void push(Runnable r) {
        getPose().pushPose();
        try {
            r.run();
        }
        finally {
            getPose().popPose();
        }

    }

    public void translate(float x, float y, float z) {
        getPose().translate(x, y, z);
    }

    public void scale(float x, float y, float z) {
        getPose().scale(x, y, z);
    }

    public void multiply(Axis axis, float degrees) {
        getPose().mulPose(axis.rotationDegrees(degrees));
    }

}