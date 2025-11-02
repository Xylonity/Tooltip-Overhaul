package dev.xylonity.tooltipoverhaul.client.old.style;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dev.xylonity.tooltipoverhaul.client.old.Palette;
import dev.xylonity.tooltipoverhaul.client.old.style.background.icon.SlotBorderIconBackground;
import dev.xylonity.tooltipoverhaul.client.old.style.background.icon.FocusIconBackground;
import dev.xylonity.tooltipoverhaul.client.old.style.background.icon.SlotIconBackground;
import dev.xylonity.tooltipoverhaul.client.old.style.background.icon.VoidIconBackground;
import dev.xylonity.tooltipoverhaul.client.old.style.effect.*;
import dev.xylonity.tooltipoverhaul.client.old.style.inner.GlintInnerOverlay;
import dev.xylonity.tooltipoverhaul.client.old.style.inner.StaticInnerOverlay;
import dev.xylonity.tooltipoverhaul.client.old.style.inner.GradientInnerOverlay;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class Styles {

    public static final TooltipStyleBuilder COMMON = new TooltipStyleBuilder();
    public static final TooltipStyleBuilder UNCOMMON = new TooltipStyleBuilder();
    public static final TooltipStyleBuilder RARE = new TooltipStyleBuilder();
    public static final TooltipStyleBuilder EPIC = new TooltipStyleBuilder().addEffect(new MetalShiningEffect());
    public static final TooltipStyleBuilder LEGENDARY = new TooltipStyleBuilder().addEffect(new MetalShiningEffect());
    public static final TooltipStyleBuilder CHAOS = new TooltipStyleBuilder().addEffect(new MetalShiningEffect());

    // Static instancing based on the config typedef
    static {
        populateStyles();
    }

    // The DefaultPanel (bg) is already defined in the builder itself
    private static void populateStyles() {
        switch (TooltipsConfig.DEFAULT_INNER_OVERLAY_TYPE.toLowerCase()) {
            case "static" -> {
                COMMON.innerOverlay(new StaticInnerOverlay(Palette.COMMON[0]));
                UNCOMMON.innerOverlay(new StaticInnerOverlay(Palette.UNCOMMON[0]));
                RARE.innerOverlay(new StaticInnerOverlay(Palette.RARE[0]));
                EPIC.innerOverlay(new StaticInnerOverlay(Palette.EPIC[0]));
                LEGENDARY.innerOverlay(new StaticInnerOverlay(Palette.LEGENDARY[0]));
                CHAOS.innerOverlay(new StaticInnerOverlay(Palette.CHAOS[0]));
            }
            case "gradient" -> {
                COMMON.innerOverlay(new GradientInnerOverlay(Palette.COMMON[0], Palette.COMMON[1], Palette.COMMON[2]));
                UNCOMMON.innerOverlay(new GradientInnerOverlay(Palette.UNCOMMON[0], Palette.UNCOMMON[1], Palette.UNCOMMON[2]));
                RARE.innerOverlay(new GradientInnerOverlay(Palette.RARE[0], Palette.RARE[1], Palette.RARE[2]));
                EPIC.innerOverlay(new GradientInnerOverlay(Palette.EPIC[0], Palette.EPIC[1], Palette.EPIC[2]));
                LEGENDARY.innerOverlay(new GradientInnerOverlay(Palette.LEGENDARY[0], Palette.LEGENDARY[1], Palette.LEGENDARY[2]));
                CHAOS.innerOverlay(new GradientInnerOverlay(Palette.CHAOS[0], Palette.CHAOS[1], Palette.CHAOS[2]));
            }
            default -> { // glint
                COMMON.innerOverlay(new GlintInnerOverlay(Palette.COMMON[0], Palette.COMMON[1], 0x00000000, 5, 3));
                UNCOMMON.innerOverlay(new GlintInnerOverlay(Palette.UNCOMMON[0], Palette.UNCOMMON[1],0x00000000, 5, 3));
                RARE.innerOverlay(new GlintInnerOverlay(Palette.RARE[0], Palette.RARE[1], 0x00000000, 4, 2));
                EPIC.innerOverlay(new GlintInnerOverlay(Palette.EPIC[0], Palette.EPIC[1], 0x00000000, 4, 2));
                LEGENDARY.innerOverlay(new GlintInnerOverlay(Palette.LEGENDARY[0], Palette.LEGENDARY[1], 0x00000000, 4, 2));
                CHAOS.innerOverlay(new GlintInnerOverlay(Palette.CHAOS[0], Palette.CHAOS[1], 0x00000000, 4, 2));
            }
        }

        switch (TooltipsConfig.ICON_BACKGROUND_TYPE) {
            case "void" -> {
                COMMON.addIconBackground(new VoidIconBackground());
                UNCOMMON.addIconBackground(new VoidIconBackground());
                RARE.addIconBackground(new VoidIconBackground());
                EPIC.addIconBackground(new VoidIconBackground());
                LEGENDARY.addIconBackground(new VoidIconBackground());
                CHAOS.addIconBackground(new VoidIconBackground());
            }
            case "slot_border" -> {
                COMMON.addIconBackground(new SlotBorderIconBackground());
                UNCOMMON.addIconBackground(new SlotBorderIconBackground());
                RARE.addIconBackground(new SlotBorderIconBackground());
                EPIC.addIconBackground(new SlotBorderIconBackground());
                LEGENDARY.addIconBackground(new SlotBorderIconBackground());
                CHAOS.addIconBackground(new SlotBorderIconBackground());
            }
            case "slot" -> {
                COMMON.addIconBackground(new SlotIconBackground());
                UNCOMMON.addIconBackground(new SlotIconBackground());
                RARE.addIconBackground(new SlotIconBackground());
                EPIC.addIconBackground(new SlotIconBackground());
                LEGENDARY.addIconBackground(new SlotIconBackground());
                CHAOS.addIconBackground(new SlotIconBackground());
            }
            case "focus" -> {
                COMMON.addIconBackground(new FocusIconBackground());
                UNCOMMON.addIconBackground(new FocusIconBackground());
                RARE.addIconBackground(new FocusIconBackground());
                EPIC.addIconBackground(new FocusIconBackground());
                LEGENDARY.addIconBackground(new FocusIconBackground());
                CHAOS.addIconBackground(new FocusIconBackground());
            }
        }
    }

    public static Optional<TooltipStyle> of(ItemStack stack, CustomFrameData data) {
        // Non specified item input in the custom_frames json will be categorized with a default rarity style
        if (data == null) {
            final Rarity r = stack.getRarity();
            if (r == Rarity.COMMON) return Optional.of(COMMON.build());
            if (r == Rarity.UNCOMMON) return Optional.of(UNCOMMON.build());
            if (r == Rarity.RARE) return Optional.of(RARE.build());
            if (r == Rarity.EPIC) return Optional.of(EPIC.build());
            return Optional.of(LEGENDARY.build());
        }
        else {
            TooltipStyleBuilder builder = new TooltipStyleBuilder();

            List<Integer> grads = new ArrayList<>();
            switch (data.getGradientType()) {
                case COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, CHAOS -> {
                    int[] p = Palette.of(data.getGradientType());
                    grads.add(p[0]);
                    grads.add(p[1]);
                    grads.add(p[2]);
                }
                default -> {
                    for (String hex : data.getGradientColors()) {
                        grads.add(color(hex));
                    }

                    // fallback when the color defs are not entirely populated
                    while (grads.size() < 3) {
                        grads.add(0x00000000);
                    }
                }
            }

            // Inner overlay
            //switch (data.getBorderType()) {
            //    case GLINT -> builder.innerOverlay(new GlintInnerOverlay(grads.get(0), grads.get(1), 0x00000000, 4, 2));
            //    case GRADIENT -> builder.innerOverlay(new GradientInnerOverlay(grads.get(0), grads.get(1), grads.get(2)));
            //    case STATIC -> builder.innerOverlay(new StaticInnerOverlay(grads.get(0)));
            //    case AUTO_GRADIENT -> {
            //        if (data.hasCustomTexture()) {
            //            int[] auto = CustomFrameManager.getPalette(data);
            //            if (auto != null && auto.length >= 3) {
            //                builder.innerOverlay(new GradientInnerOverlay(auto[0], auto[1], auto[2]));
            //            } else {
            //                builder.innerOverlay(new GradientInnerOverlay(grads.get(0), grads.get(1), grads.get(2)));
            //            }
            //        } else {
            //            builder.innerOverlay(new GradientInnerOverlay(grads.get(0), grads.get(1), grads.get(2)));
            //        }
            //    }
            //    case AUTO_GLINT -> {
            //        if (data.hasCustomTexture()) {
            //            int[] auto = CustomFrameManager.getPalette(data);
            //            if (auto != null && auto.length >= 3) {
            //                builder.innerOverlay(new GlintInnerOverlay(auto[0], auto[1], 0x00000000, 4, 2));
            //            } else {
            //                builder.innerOverlay(new GlintInnerOverlay(grads.get(0), grads.get(1), 0x00000000, 4, 2));
            //            }
            //        } else {
            //            builder.innerOverlay(new GlintInnerOverlay(grads.get(0), grads.get(1), 0x00000000, 4, 2));
            //        }
            //    }
            //    case AUTO_STATIC -> {
            //        if (data.hasCustomTexture()) {
            //            int[] auto = CustomFrameManager.getPalette(data);
            //            if (auto != null && auto.length >= 3) {
            //                builder.innerOverlay(new StaticInnerOverlay(auto[0]));
            //            } else {
            //                builder.innerOverlay(new StaticInnerOverlay(grads.get(0)));
            //            }
            //        } else {
            //            builder.innerOverlay(new StaticInnerOverlay(grads.get(0)));
            //        }
            //    }
//
            //}

            switch (data.getEffect()) {
                case "metal_shining" -> builder.addEffect(new MetalShiningEffect());
                case "ripples" -> builder.addEffect(new RipplesEffect());
                case "stars" -> builder.addEffect(new StarsEffect());
                case "sonar" -> builder.addEffect(new SonarEffect());
                case "cinder" -> builder.addEffect(new CinderEffect());
                case "rim_light" -> builder.addEffect(new RimLightEffect(0x88154c79, 0x0));
            }

            switch (data.getIconBackground()) {
                case "void" -> builder.addIconBackground(new VoidIconBackground());
                case "slot_border" -> builder.addIconBackground(new SlotBorderIconBackground());
                case "slot" -> builder.addIconBackground(new SlotIconBackground());
                case "focus" -> builder.addIconBackground(new FocusIconBackground());
            }

            return Optional.of(builder.build());
        }
    }

    private static int color(String hex) {
        String s = hex.startsWith("#") ? hex.substring(1) : hex;
        long value = Long.parseLong(s, 16);
        if (s.length() == 6) {
            value |= 0xFF000000L;
        }

        return (int) value;
    }

}