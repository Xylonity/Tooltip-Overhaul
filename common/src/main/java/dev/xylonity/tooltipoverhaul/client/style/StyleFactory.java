package dev.xylonity.tooltipoverhaul.client.style;

import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.old.Palette;
import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.style.background.DefaultBackground;
import dev.xylonity.tooltipoverhaul.client.style.divider.GradientDividerLine;
import dev.xylonity.tooltipoverhaul.client.style.icon.DefaultIcon;
import dev.xylonity.tooltipoverhaul.client.style.icon.background.SlotBorderIconBackground;
import dev.xylonity.tooltipoverhaul.client.style.inner.GradientInnerOverlay;
import dev.xylonity.tooltipoverhaul.client.style.shadow.DefaultShadow;
import dev.xylonity.tooltipoverhaul.client.style.text.DefaultText;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import net.minecraft.world.item.Rarity;

import java.util.ArrayList;
import java.util.List;

public class StyleFactory {

    private final List<ITooltipLayer> layers = new ArrayList<>();

    public List<ITooltipLayer> create(TooltipContext context, CustomFrameData data) {
        if (context.getStack().isEmpty()) {
            return createEmpty(context);
        }

        if (data != null) {
            return createCustom(context);
        }

        return createForRarity(context);
    }

    private List<ITooltipLayer> createCustom(TooltipContext context) {
        return createForRarity(context);
    }

    private List<ITooltipLayer> createEmpty(TooltipContext context) {
        return createForRarity(context);
    }

    private List<ITooltipLayer> createForRarity(TooltipContext context) {

        layers.add(new DefaultBackground());
        layers.add(new DefaultText());
        layers.add(new DefaultShadow());

        // If the icon is enabled
        if (context.hasIcon()) {
            layers.add(new SlotBorderIconBackground());
            layers.add(new DefaultIcon());
        }

        // If the divider line is enabled
        if (context.hasDividerLine() && context.getComponents().size() > 1) {
            layers.add(new GradientDividerLine());
        }

        // Inner overlay
        int[] colors = colorsPerRarity(context.getStack().getRarity());
        switch (RenderUtils.getInnerOverlayType(context)) {
            case "glint" -> layers.add(new GradientInnerOverlay(colors[0], colors[1], 0x0));
            case "static" -> layers.add(new GradientInnerOverlay(colors[0], 0x0, 0x0));
            default -> layers.add(new GradientInnerOverlay(colors[0], colors[1], colors[2]));
        }


        return layers;
    }

    private int[] colorsPerRarity(Rarity rarity) {
        int[] colors = Palette.LEGENDARY;

        if (rarity == Rarity.COMMON) return Palette.COMMON;
        if (rarity == Rarity.UNCOMMON) return Palette.UNCOMMON;
        if (rarity == Rarity.RARE) return Palette.RARE;
        if (rarity == Rarity.EPIC) return Palette.EPIC;

        return colors;
    }

}
