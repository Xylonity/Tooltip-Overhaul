package dev.xylonity.tooltipoverhaul.client.style;

import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.style.background.DefaultBackground;
import dev.xylonity.tooltipoverhaul.client.style.background.DefaultPreviewBackground;
import dev.xylonity.tooltipoverhaul.client.style.divider.GradientDividerLine;
import dev.xylonity.tooltipoverhaul.client.style.divider.StaticDividerLine;
import dev.xylonity.tooltipoverhaul.client.style.icon.DefaultIcon;
import dev.xylonity.tooltipoverhaul.client.style.icon.background.*;
import dev.xylonity.tooltipoverhaul.client.style.inner.GradientInnerOverlay;
import dev.xylonity.tooltipoverhaul.client.style.inner.StaticInnerOverlay;
import dev.xylonity.tooltipoverhaul.client.style.overlay.DefaultOverlay;
import dev.xylonity.tooltipoverhaul.client.style.shadow.DefaultShadow;
import dev.xylonity.tooltipoverhaul.client.style.text.DefaultText;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.client.util.Palette;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;

import java.util.ArrayList;
import java.util.List;

public class StyleFactory {

    public List<ITooltipLayer> create(TooltipContext context, CustomFrameData data) {
        if (context.getStack().isEmpty()) {
            return createEmpty(context);
        }

        if (data != null) {
            return createCustom(context);
        }

        return createForRarity(context);
    }

    private List<ITooltipLayer> createEmpty(TooltipContext context) {

        List<ITooltipLayer> layers = new ArrayList<>();

        layers.add(new DefaultBackground());
        layers.add(new DefaultText());

        int[] colors = Palette.NO_STACK;
        layers.add(new GradientInnerOverlay(colors[0], colors[1], colors[2]));

        return layers;
    }

    private List<ITooltipLayer> createCustom(TooltipContext context) {

        List<ITooltipLayer> layers = new ArrayList<>();

        assignDefaultLayers(context, layers);

        // Inner overlay
        int[] colors = ColorUtils.getInnerOverlayColors(context);
        switch (RenderUtils.getInnerOverlayType(context)) {
            case "glint" -> layers.add(new GradientInnerOverlay(colors[0], colors[1], 0x0));
            case "static" -> layers.add(new StaticInnerOverlay(colors[0]));
            default -> layers.add(new GradientInnerOverlay(colors[0], colors[1], colors[2]));
        }

        return layers;
    }

    private List<ITooltipLayer> createForRarity(TooltipContext context) {

        List<ITooltipLayer> layers = new ArrayList<>();

        assignDefaultLayers(context, layers);

        // Inner overlay
        int[] colors = ColorUtils.getColorsPerRarity(context);
        switch (RenderUtils.getInnerOverlayType(context)) {
            case "glint" -> layers.add(new GradientInnerOverlay(colors[0], colors[1], 0x0));
            case "static" -> layers.add(new StaticInnerOverlay(colors[0]));
            default -> layers.add(new GradientInnerOverlay(colors[0], colors[1], colors[2]));
        }

        return layers;
    }

    private void assignDefaultLayers(TooltipContext context, List<ITooltipLayer> layers) {

        layers.add(new DefaultBackground());
        layers.add(new DefaultText());

        if (RenderUtils.hasShadow(context)) {
            layers.add(new DefaultShadow());
        }

        // If the icon is enabled
        if (context.hasIcon()) {
            String iconBackgroundType = RenderUtils.getIconBackgroundType(context);
            switch (iconBackgroundType) {
                case "focus" -> layers.add(new FocusIconBackground());
                case "void" -> layers.add(new VoidIconBackground());
                case "slot" -> layers.add(new SlotIconBackground());
                case "slot_border" -> layers.add(new SlotBorderIconBackground());
                case "glow" -> layers.add(new GlowingIconBackground());
            }

            layers.add(new DefaultIcon());
        }

        // If the divider line is enabled
        if (context.hasDividerLine() && context.getComponents().size() > 1) {
            String dividerLineType = RenderUtils.getDividerLineType(context);
            switch (dividerLineType) {
                case "gradient" -> layers.add(new GradientDividerLine());
                case "static" -> layers.add(new StaticDividerLine());
            }

        }

        if (RenderUtils.hasPreview(context)) {
            layers.add(new DefaultPreviewBackground());
        }

        layers.add(new DefaultOverlay());

    }

}
