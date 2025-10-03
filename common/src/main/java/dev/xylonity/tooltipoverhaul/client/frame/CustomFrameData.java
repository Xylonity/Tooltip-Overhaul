package dev.xylonity.tooltipoverhaul.client.frame;

import dev.xylonity.tooltipoverhaul.client.Palette;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.util.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public record CustomFrameData(
        List<String> items,
        List<String> tags,
        Optional<String> namespace,
        Optional<String> texture,
        Optional<Integer> backgroundColor,
        Optional<InnerBorderType> borderType,
        Optional<GradientType> gradientType,
        Optional<List<String>> gradientColors,
        Optional<String> itemRating,
        Optional<Integer> colorItemRating,
        Optional<String> ratingAlignment,
        Optional<String> titleAlignment,
        Optional<Integer> titlePositionX,
        Optional<Integer> titlePositionY,
        Optional<Integer> ratingPositionX,
        Optional<Integer> ratingPositionY,
        Optional<Integer> tooltipDescriptionPositionX,
        Optional<Integer> tooltipDescriptionPositionY,
        Optional<Integer> mainPanelPaddingX,
        Optional<Integer> mainPanelPaddingY,
        Optional<Float> iconSize,
        Optional<Float> iconRotatingSpeed,
        Optional<String> iconAppearAnimation,
        Optional<Integer> secondPanelX,
        Optional<Integer> secondPanelY,
        Optional<Float> secondPanelRendererSize,
        Optional<Float> secondPanelRendererSpeed,
        Optional<DividerLineType> dividerLineType,//
        Optional<String> dividerLineColor,
        Optional<String> particles,
        Optional<String> specialEffect,
        Optional<Boolean> showSecondPanel,
        Optional<Boolean> showRating,
        Optional<Boolean> disableIcon,
        Optional<Boolean> disableScrolling,
        Optional<Boolean> disableTooltip,
        Optional<Boolean> disableDividerLine
) {

    public String getTexture() {
        return texture.filter(t -> !t.trim().isEmpty()).orElse(TooltipsConfig.GLOBAL_FRAME_OVERLAY_LOCATION);
    }

    public InnerBorderType getBorderType() {
        InnerBorderType type;
        try {
            type = InnerBorderType.valueOf(TooltipsConfig.DEFAULT_INNER_OVERLAY_TYPE.toUpperCase(Locale.ROOT));
        }
        catch (Exception ignore) {
            type = InnerBorderType.GRADIENT;
        }

        return borderType.orElse(type);
    }

    public GradientType getGradientType() {
        return gradientType.orElse(GradientType.COMMON);
    }

    public List<String> getGradientColors() {
        return gradientColors.filter(colors -> colors.size() >= 3).map(colors -> colors.subList(0, 3)).orElse(List.of("#FFFFFFFF", "#FFFFFFFF", "#FFFFFFFF"));
    }

    public String getItemRating(ItemStack stack) {
        return itemRating.filter(rating -> !rating.trim().isEmpty()).orElse(Util.getDefaultRarity(stack).getString());
    }

    public int getItemRatingColor(ItemStack stack) {
        return colorItemRating.orElse(getRarityColor(stack));
    }

    public boolean shouldDisableDividerLine() {
        return disableDividerLine.orElse(TooltipsConfig.DISABLE_DIVIDER_LINE);
    }

    public boolean shouldDisableTooltip() {
        return disableTooltip.orElse(false);
    }

    private int getRarityColor(ItemStack stack) {
        final Rarity r = stack.getRarity();
        // Computes the default color per rarity
        // Defaults to a simulated legendary rarity
        int palette = Palette.LEGENDARY[0];
        if (r == Rarity.COMMON) palette = Palette.COMMON[0];
        if (r == Rarity.UNCOMMON) palette = Palette.UNCOMMON[0];
        if (r == Rarity.RARE) palette = Palette.RARE[0];
        if (r == Rarity.EPIC) palette = Palette.EPIC[0];
        return palette;
    }

    public String getIconAppearAnimation() {
        return iconAppearAnimation.orElse(TooltipsConfig.ICON_APPEAR_ANIMATION);
    }

    public float getIconRotatingSpeed() {
        return iconRotatingSpeed.orElse(TooltipsConfig.ICON_ROTATING_SPEED);
    }

    public int getTitlePositionX() {
        return titlePositionX.orElse(TooltipsConfig.TITLE_POSITION_X);
    }

    public int getTitlePositionY() {
        return titlePositionY.orElse(TooltipsConfig.TITLE_POSITION_Y);
    }

    public int getRatingPositionX() {
        return ratingPositionX.orElse(TooltipsConfig.RATING_POSITION_X);
    }

    public int getRatingPositionY() {
        return ratingPositionY.orElse(TooltipsConfig.RATING_POSITION_Y);
    }

    public int getTooltipDescriptionPositionX() {
        return tooltipDescriptionPositionX.orElse(TooltipsConfig.TOOLTIP_DESCRIPTION_POSITION_X);
    }

    public int getBackgroundColor() {
        return backgroundColor.orElse(Palette.PANEL_BG);
    }

    public boolean shouldDisableScrolling() {
        return disableScrolling.orElse(TooltipsConfig.DISABLE_TOOLTIP_SCROLLING);
    }

    public int getTooltipDescriptionPositionY() {
        return tooltipDescriptionPositionY.orElse(TooltipsConfig.TOOLTIP_DESCRIPTION_POSITION_Y);
    }

    public int getSecondPanelX() {
        return secondPanelX.orElse(TooltipsConfig.SECOND_PANEL_X);
    }

    public int getSecondPanelY() {
        return secondPanelY.orElse(TooltipsConfig.SECOND_PANEL_Y);
    }

    public String getRatingAlignment() {
        return ratingAlignment.orElse(TooltipsConfig.RATING_X_ALIGNMENT);
    }

    public String getTitleAlignment() {
        return titleAlignment.orElse(TooltipsConfig.TITLE_X_ALIGNMENT);
    }

    //public String getParticles() {
    //    return particles.filter(p -> !p.trim().isEmpty()).orElse(Defaults.PARTICLES);
    //}

    public float getIconSize() {
        return iconSize.orElse(TooltipsConfig.ICON_SIZE);
    }

    public float getSecondPanelRendererSize() {
        return secondPanelRendererSize.orElse(TooltipsConfig.SECOND_PANEL_RENDERER_SIZE);
    }

    public float getSecondPanelRendererSpeed() {
        return secondPanelRendererSpeed.orElse(TooltipsConfig.SECOND_PANEL_RENDERER_SPEED);
    }

    public String getEffect() {
        return specialEffect.filter(effect -> !effect.trim().isEmpty()).orElse("none");
    }

    public List<ResourceLocation> getItemLocations() {
        return items.stream().map(ResourceLocation::new).collect(Collectors.toList());
    }

    public List<TagKey<Item>> getTagKeys() {
        return tags.stream().map(name -> TagKey.create(Registries.ITEM, new ResourceLocation(name))).collect(Collectors.toList());
    }

    public boolean matches(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (items.contains(key.toString())) {
            return true;
        }

        if (namespace.isPresent()) {
            String namespace = this.namespace.get().trim();
            if (!namespace.isEmpty()) {
                if (namespace.equals("*") || namespace.equalsIgnoreCase("all")) {
                    return true;
                }
                if (key.getNamespace().equals(namespace)) {
                    return true;
                }
            }

        }

        for (TagKey<Item> tagKey : getTagKeys()) {
            if (stack.is(tagKey)) {
                return true;
            }
        }

        return false;
    }

    public int getMainPanelPaddingX() {
        return mainPanelPaddingX.orElse(TooltipsConfig.MAIN_PANEL_PADDING_X);
    }

    public int getMainPanelPaddingY() {
        return mainPanelPaddingY.orElse(TooltipsConfig.MAIN_PANEL_PADDING_Y);
    }

    public String getDividerLineColor() {
        return dividerLineColor.orElse(TooltipsConfig.DIVIDER_LINE_COLOR);
    }

    public boolean hasCustomTexture() {
        return texture.isPresent() && !texture.get().trim().isEmpty();
    }

    public boolean hasCustomItemRating() {
        return itemRating.isPresent() && !itemRating.get().trim().isEmpty();
    }

    public boolean shouldShowRating() {
        return showRating.orElse(TooltipsConfig.SHOW_RATING);
    }

    public boolean shouldDisableIcon() {
        return disableIcon.isPresent() && disableIcon.get();
    }

    public boolean shouldShowSecondPanel() {
        return showSecondPanel.isPresent() && showSecondPanel.get();
    }

    public boolean hasCustomColorItemRating() {
        return colorItemRating.isPresent();
    }

    public enum InnerBorderType {
        NONE,
        STATIC,
        GLINT,
        GRADIENT,
        AUTO_STATIC,
        AUTO_GLINT,
        AUTO_GRADIENT
    }

    public enum GradientType {
        COMMON,
        UNCOMMON,
        RARE,
        EPIC,
        LEGENDARY,
        CHAOS,
        CUSTOM
    }

    public enum DividerLineType {
        NONE,
        NORMAL
    }

}
