package dev.xylonity.tooltipoverhaul.client.style;

import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.impl.EffectLayer;
import dev.xylonity.tooltipoverhaul.client.layer.impl.IconBackgroundLayer;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipLayout;
import dev.xylonity.tooltipoverhaul.client.style.background.DefaultBackground;
import dev.xylonity.tooltipoverhaul.client.style.background.CompactFooter;
import dev.xylonity.tooltipoverhaul.client.style.badge.DefaultEquippedBadge;
import dev.xylonity.tooltipoverhaul.client.style.divider.*;
import dev.xylonity.tooltipoverhaul.client.style.effect.*;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectCatalog;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectRuntime;
import dev.xylonity.tooltipoverhaul.client.style.effect.internal.EffectSettings;
import dev.xylonity.tooltipoverhaul.client.style.preview.background.DefaultPreviewBackground;
import dev.xylonity.tooltipoverhaul.client.style.icon.DefaultIcon;
import dev.xylonity.tooltipoverhaul.client.style.icon.background.*;
import dev.xylonity.tooltipoverhaul.client.style.inner.GradientInnerOverlay;
import dev.xylonity.tooltipoverhaul.client.style.inner.StaticInnerOverlay;
import dev.xylonity.tooltipoverhaul.client.style.overlay.DefaultOverlay;
import dev.xylonity.tooltipoverhaul.client.style.preview.inner.DefaultPreviewGradientInnerOverlay;
import dev.xylonity.tooltipoverhaul.client.style.preview.renderer.DefaultPreviewArmorStand;
import dev.xylonity.tooltipoverhaul.client.style.preview.renderer.DefaultPreviewPlayerRenderer;
import dev.xylonity.tooltipoverhaul.client.style.preview.renderer.DefaultPreviewStackRenderer;
import dev.xylonity.tooltipoverhaul.client.style.shadow.DefaultPreviewShadow;
import dev.xylonity.tooltipoverhaul.client.style.shadow.DefaultShadow;
import dev.xylonity.tooltipoverhaul.client.style.text.DefaultText;
import dev.xylonity.tooltipoverhaul.client.style.vignette.CircularHoleVignette;
import dev.xylonity.tooltipoverhaul.client.style.vignette.CircularVignette;
import dev.xylonity.tooltipoverhaul.client.style.vignette.LinearVignette;
import dev.xylonity.tooltipoverhaul.client.style.vignette.ShapedVignette;
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
            final List<String> rawVignetteEntries = context.getFrameData().vignettes();
            if (!rawVignetteEntries.isEmpty()) {
                final StringBuilder rawKeys = new StringBuilder();
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

        if (!RenderUtils.effectsBehindText(context)) {
            assignEffects(context, layers, LayerDepth.EFFECT);
        }

        return layers;
    }

    private List<ITooltipLayer> createForRarity(TooltipContext context) {

        final List<ITooltipLayer> layers = new ArrayList<>();

        final int[] colors = ColorUtils.getColorsPerRarity(context);
        assignDefaultLayers(context, layers, colors);

        // Vignette effects
        final boolean hasVignette = RenderUtils.hasVignette(context);
        if (hasVignette) {
            parseVignetteEntries(TooltipsConfig.VIGNETTES, layers);
        }

        layers.add(new DefaultOverlay());

        if (!RenderUtils.effectsBehindText(context)) {
            assignEffects(context, layers, LayerDepth.EFFECT);
        }

        return layers;
    }

    private void assignDefaultLayers(TooltipContext context, List<ITooltipLayer> layers, int[] colors) {

        layers.add(new DefaultBackground());
        if (RenderUtils.effectsBehindText(context)) {
            assignEffects(context, layers, LayerDepth.BACKGROUND_EFFECT);
        }

        if (TooltipLayout.footerHeight(context) > 0) {
            layers.add(new CompactFooter());
        }

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
            final String iconBackgroundType = RenderUtils.getIconBackgroundType(context);
            IconBackgroundLayer background = switch (iconBackgroundType) {
                case "focus" -> new FocusIconBackground();
                case "void" -> new VoidIconBackground();
                case "slot" -> new SlotIconBackground();
                case "slot_border" -> new SlotBorderIconBackground();
                case "glow" -> new GlowingIconBackground();
                default -> null;
            };
            if (TooltipLayout.hasExternalIcon(context)) {
                layers.add(new DetachedIconPlate(background));
            }
            else if (background != null) {
                layers.add(background);
            }

            layers.add(new DefaultIcon());
        }

        // If the divider line is enabled
        if (context.hasDividerLine() && context.getComponents().size() > 1) {
            final String dividerLineType = RenderUtils.getDividerLineType(context);
            switch (dividerLineType) {
                case "gradient" -> layers.add(new GradientDividerLine());
                case "static" -> layers.add(new StaticDividerLine());
                case "linear" -> layers.add(new LinearDividerLine());
                case "dashed" -> layers.add(new PatternDividerLine(4, 3));
                case "dotted" -> layers.add(new PatternDividerLine(1, 2));
                case "ornament" -> layers.add(new OrnamentDividerLine());
                case "gradient_ornament" -> layers.add(new GradientOrnamentDividerLine());
            }

        }

        final boolean hasPreviewOfStack = RenderUtils.hasPreviewOfStack(context);
        final boolean hasPreviewOfArmorItem = RenderUtils.hasPreviewOfArmorItem(context);
        if (hasPreviewOfStack || hasPreviewOfArmorItem) {
            if (RenderUtils.hasShadow(context)) {
                layers.add(new DefaultPreviewShadow());
            }

            layers.add(new DefaultPreviewBackground());
            layers.add(new DefaultPreviewGradientInnerOverlay(colors[0], colors[1], colors[2]));

            if (hasPreviewOfStack) {
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

        if (!context.isMainTooltip() && !context.isPinned()) {
            layers.add(new DefaultEquippedBadge(colors[0], colors[1], colors[2]));
        }

    }

    private void parseVignetteEntries(String key, List<ITooltipLayer> layers) {
        final List<VignetteEntry> vignetteEntries = VignetteEntry.Parser.from(key);
        for (VignetteEntry entry : vignetteEntries) {
            switch (entry.type()) {
                case "circular" -> layers.add(new CircularVignette(entry));
                case "hole" -> layers.add(new CircularHoleVignette(entry));
                case "ellipse", "diamond", "ring" -> layers.add(new ShapedVignette(entry));
                case "linear" -> layers.add(new LinearVignette(entry));
            }

        }

    }

    private void assignEffects(TooltipContext context, List<ITooltipLayer> layers, LayerDepth depth) {
        final EffectSettings settings = context.getFrameData() == null ? EffectSettings.INHERIT : context.getFrameData().effectSettings();
        for (final String part : RenderUtils.getEffect(context).split("\\s*[;,]\\s*")) {
            final EffectLayer effect = EffectCatalog.create(part);
            if (effect != null) {
                layers.add(EffectRuntime.wrap(part, effect, settings, depth));
            }

        }

    }

}