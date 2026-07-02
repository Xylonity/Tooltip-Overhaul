package dev.xylonity.tooltipoverhaul.client.style;

import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.style.background.DefaultBackground;
import dev.xylonity.tooltipoverhaul.client.style.badge.DefaultEquippedBadge;
import dev.xylonity.tooltipoverhaul.client.style.divider.LinearDividerLine;
import dev.xylonity.tooltipoverhaul.client.style.effect.*;
import dev.xylonity.tooltipoverhaul.client.style.preview.background.DefaultPreviewBackground;
import dev.xylonity.tooltipoverhaul.client.style.divider.GradientDividerLine;
import dev.xylonity.tooltipoverhaul.client.style.divider.StaticDividerLine;
import dev.xylonity.tooltipoverhaul.client.style.icon.DefaultIcon;
import dev.xylonity.tooltipoverhaul.client.style.icon.background.*;
import dev.xylonity.tooltipoverhaul.client.style.inner.GradientInnerOverlay;
import dev.xylonity.tooltipoverhaul.client.style.inner.StaticInnerOverlay;
import dev.xylonity.tooltipoverhaul.client.style.overlay.DefaultOverlay;
import dev.xylonity.tooltipoverhaul.client.style.preview.inner.DefaultPreviewGradientInnerOverlay;
import dev.xylonity.tooltipoverhaul.client.style.preview.renderer.DefaultPreviewArmorStand;
import dev.xylonity.tooltipoverhaul.client.style.preview.renderer.DefaultPreviewPlayerRenderer;
import dev.xylonity.tooltipoverhaul.client.style.preview.renderer.DefaultPreviewStackRenderer;
import dev.xylonity.tooltipoverhaul.client.style.shadow.DefaultShadow;
import dev.xylonity.tooltipoverhaul.client.style.text.DefaultText;
import dev.xylonity.tooltipoverhaul.client.style.vignette.CircularHoleVignette;
import dev.xylonity.tooltipoverhaul.client.style.vignette.CircularVignette;
import dev.xylonity.tooltipoverhaul.client.style.vignette.parser.VignetteEntry;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.client.util.Palette;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;

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

        int[] colors = ColorUtils.getInnerOverlayColors(context);
        assignDefaultLayers(context, layers, colors);

        // Vignette effects
        boolean hasVignette = RenderUtils.hasVignette(context);
        if (hasVignette) {
            List<String> rawVignetteEntries = context.getFrameData().vignettes();
            if (!rawVignetteEntries.isEmpty()) {
                StringBuilder rawKeys = new StringBuilder();
                for (String rawVignette : rawVignetteEntries) {
                    rawKeys.append(rawVignette).append(",");
                }

                parseVignetteEntries(rawKeys.toString(), layers);
            }
            else {
                parseVignetteEntries(TooltipsConfig.VIGNETTES, layers);
            }

        }

        layers.add(new DefaultOverlay());

        assignEffects(context, layers, colors);

        return layers;
    }

    private List<ITooltipLayer> createForRarity(TooltipContext context) {

        List<ITooltipLayer> layers = new ArrayList<>();

        int[] colors = ColorUtils.getColorsPerRarity(context);
        assignDefaultLayers(context, layers, colors);

        // Vignette effects
        boolean hasVignette = RenderUtils.hasVignette(context);
        if (hasVignette) {
            parseVignetteEntries(TooltipsConfig.VIGNETTES, layers);
        }

        layers.add(new DefaultOverlay());

        assignEffects(context, layers, colors);

        return layers;
    }

    private void assignDefaultLayers(TooltipContext context, List<ITooltipLayer> layers, int[] colors) {

        layers.add(new DefaultBackground());
        layers.add(new DefaultText());

        if (RenderUtils.hasShadow(context)) {
            layers.add(new DefaultShadow());
        }

        // Inner overlay
        switch (RenderUtils.getInnerOverlayType(context)) {
            case "glint", "auto_glint" -> layers.add(new GradientInnerOverlay(colors[0], colors[1], 0x0));
            case "static", "auto_static" -> layers.add(new StaticInnerOverlay(colors[0]));
            default -> layers.add(new GradientInnerOverlay(colors[0], colors[1], colors[2]));
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
                case "linear" -> layers.add(new LinearDividerLine());
            }

        }

        boolean hasPreviewOfTieredItem = RenderUtils.hasPreviewOfTieredItem(context);
        boolean hasPreviewOfArmorItem = RenderUtils.hasPreviewOfArmorItem(context);
        if (hasPreviewOfTieredItem || hasPreviewOfArmorItem) {
            layers.add(new DefaultPreviewBackground());
            layers.add(new DefaultPreviewGradientInnerOverlay(colors[0], colors[1], colors[2]));

            if (hasPreviewOfTieredItem) {
                layers.add(new DefaultPreviewStackRenderer());
            }
            else {
                if (RenderUtils.getPreviewPanelModel(context).equals("armor_stand")) {
                    layers.add(new DefaultPreviewArmorStand());
                }
                else {
                    layers.add(new DefaultPreviewPlayerRenderer());
                }
            }

        }

        if (!context.isMainTooltip()) {
            layers.add(new DefaultEquippedBadge(colors[0], colors[1], colors[2]));
        }

    }

    private void parseVignetteEntries(String key, List<ITooltipLayer> layers) {
        List<VignetteEntry> vignetteEntries = VignetteEntry.Parser.from(key);
        for (VignetteEntry entry : vignetteEntries) {
            switch (entry.type()) {
                case "circular" -> layers.add(new CircularVignette(entry));
                case "hole" -> layers.add(new CircularHoleVignette(entry));
            }
        }

    }

    private void assignEffects(TooltipContext context, List<ITooltipLayer> layers, int[] colors) {
        for (String part : RenderUtils.getEffect(context).split("\\s*[;,]\\s*")) {
            switch (part.trim().toLowerCase()) {
                case "bubbles" -> layers.add(new BubblesEffect());
                case "cinder" -> layers.add(new CinderEffect());
                case "crystals" -> layers.add(new CrystalsEffect());
                case "fireflies" -> layers.add(new FirefliesEffect());
                case "echo" -> layers.add(new EchoEffect());
                case "galaxy" -> layers.add(new GalaxyEffect());
                case "magic_orbs" -> layers.add(new MagicOrbsEffect());
                case "speed_lines" -> layers.add(new SpeedLinesEffect()); ///
                case "nebula" -> layers.add(new NebulaEffect());
                case "spiral" -> layers.add(new TimeSpiralEffect());
                case "white_dust" -> layers.add(new WhiteDustEffect());
                case "metal_shining" -> layers.add(new MetalShiningEffect());
                case "rim_light" -> layers.add(new RimLightEffect(0x88154c79, 0x0)); ///
                case "ripples" -> layers.add(new RipplesEffect()); ///
                case "sonar" -> layers.add(new SonarEffect());
                case "stars" -> layers.add(new StarsEffect());
            }

        }

    }

}
