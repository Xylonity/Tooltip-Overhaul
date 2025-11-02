package dev.xylonity.tooltipoverhaul.client.old.layer;

import dev.xylonity.tooltipoverhaul.client.old.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.old.style.TooltipStyle;
import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameData;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

@FunctionalInterface
public interface ITooltipLayer {
    void render(TooltipContext ctx, Vec2 pos, Point size, TooltipStyle style, Component rarity, Font font, @Nullable CustomFrameData customFrame);
}