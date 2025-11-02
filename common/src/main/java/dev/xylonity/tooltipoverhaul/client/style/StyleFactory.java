package dev.xylonity.tooltipoverhaul.client.style;

import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.style.background.DefaultBackground;
import dev.xylonity.tooltipoverhaul.client.style.icon.DefaultIcon;
import dev.xylonity.tooltipoverhaul.client.style.icon.background.SlotBorderIconBackground;
import dev.xylonity.tooltipoverhaul.client.style.inner.GradientInnerOverlay;
import dev.xylonity.tooltipoverhaul.client.style.text.DefaultText;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;

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

        if (RenderUtils.hasIcon(context)) {
            layers.add(new SlotBorderIconBackground());
            layers.add(new DefaultIcon());
        }

        layers.add(new GradientInnerOverlay(0xFF969696, 0xFF575757, 0xFF3C3C3C));

        return layers;
    }

}
