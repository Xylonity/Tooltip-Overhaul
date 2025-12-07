package dev.xylonity.tooltipoverhaul.client.frame;

import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.ColorUtils;
import dev.xylonity.tooltipoverhaul.client.util.Palette;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.config.parser.ConfigColorParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record CustomFrameData(
        List<String> items,
        List<String> tags,
        Optional<String> namespace,
        Optional<String> texture,
        Optional<Integer> backgroundColor,
        Optional<String> borderType,
        Optional<GradientType> gradientType,
        Optional<List<String>> gradientColors,
        Optional<String> itemRating,
        Optional<String> colorItemRating,
        Optional<String> ratingAlignment,
        Optional<String> titleAlignment,
        Optional<Integer> tooltipPositionX,
        Optional<Integer> tooltipPositionY,
        Optional<Integer> mainPanelPaddingX,
        Optional<Integer> mainPanelPaddingY,
        Optional<Integer> dividerLineTopPadding,
        Optional<Integer> dividerLineBottomPadding,
        Optional<Float> iconSize,
        Optional<Float> iconRotatingSpeed,
        Optional<String> iconAppearAnimation,
        Optional<Integer> secondPanelX,
        Optional<Integer> secondPanelY,
        Optional<Integer> secondPanelSizeX,
        Optional<Integer> secondPanelSizeY,
        Optional<Float> secondPanelRendererSpeed,
        Optional<String> dividerLineType,
        Optional<String> dividerLineColor,
        Optional<String> particles,
        Optional<String> specialEffect,
        List<String> vignettes,
        Optional<String> iconBackgroundType,
        Optional<Boolean> showSecondPanel,
        Optional<Boolean> showRating,
        Optional<Boolean> showShadow,
        Optional<Boolean> disableIcon,
        Optional<Boolean> disableScrolling,
        Optional<Boolean> disableTooltip,
        Optional<Boolean> disableDividerLine
) {

    public String getTextureLocation() {
        return texture.filter(t -> !t.trim().isEmpty()).orElse(TooltipsConfig.GLOBAL_FRAME_OVERLAY_LOCATION);
    }

    public String getBorderType() {
        return borderType.orElse(TooltipsConfig.DEFAULT_INNER_OVERLAY_TYPE);
    }

    public GradientType getGradientType() {
        return gradientType.orElse(GradientType.COMMON);
    }

    public boolean hasGradientColors() {
        return gradientColors.isPresent();
    }

    public int[] getGradientColors(TooltipContext context) {
        return gradientColors
                .map(list -> {
                    int length = Math.min(3, list.size());
                    if (length == 0) {
                        return new int[0];
                    }

                    int[] array = new int[3];
                    int last = 0;
                    for (int i = 0; i < length; i++) {
                        String key = list.get(i);
                        if (key == null || key.isBlank()) {
                            continue;
                        }

                        last = ConfigColorParser.parseColor(key.trim());
                        array[i] = last;
                    }

                    for (int i = length; i < 3; i++) {
                        array[i] = last;
                    }

                    return array;
                })
                .orElseGet(() -> Arrays.copyOf(ColorUtils.getColorsPerRarity(context), 3));
    }

    public String getItemRating(ItemStack stack) {
        return itemRating.filter(rating -> !rating.trim().isEmpty()).orElse("W");
    }

    public int getItemRatingColor(TooltipContext context) {
        return colorItemRating.map(ConfigColorParser::parseColor).orElseGet(() -> ColorUtils.getFirstColorOfRarity(context));
    }

    public boolean shouldDisableDividerLine() {
        return disableDividerLine.orElse(TooltipsConfig.DISABLE_DIVIDER_LINE);
    }

    public boolean shouldDisableTooltip() {
        return disableTooltip.orElse(false);
    }

    public String getIconAppearAnimation() {
        return iconAppearAnimation.orElse(TooltipsConfig.ICON_APPEAR_ANIMATION);
    }

    public String getIconBackground() {
        return iconBackgroundType.orElse(TooltipsConfig.ICON_BACKGROUND_TYPE);
    }

    public boolean hasVignette() {
        return !vignettes.isEmpty();
    }

    public float getIconRotatingSpeed() {
        return iconRotatingSpeed.orElse(TooltipsConfig.ICON_ROTATING_SPEED);
    }

    public String getDividerLineType() {
        return dividerLineType.orElse(TooltipsConfig.DIVIDER_LINE_TYPE);
    }

    public int getDividerLineTopPadding() {
        return dividerLineTopPadding.orElse(TooltipsConfig.DIVIDER_LINE_TOP_PADDING);
    }

    public int getDividerLineBottomPadding() {
        return dividerLineBottomPadding.orElse(TooltipsConfig.DIVIDER_LINE_BOTTOM_PADDING);
    }

    public int getTooltipPositionX() {
        return tooltipPositionX.orElse(TooltipsConfig.TOOLTIP_POSITION_X);
    }

    public int getTooltipPositionY() {
        return tooltipPositionY.orElse(TooltipsConfig.TOOLTIP_POSITION_Y);
    }

    public int getBackgroundColor() {
        return backgroundColor.orElse(Palette.PANEL_BG);
    }

    public boolean shouldDisableScrolling() {
        return disableScrolling.orElse(TooltipsConfig.DISABLE_TOOLTIP_SCROLLING);
    }

    public boolean shouldShowShadow() {
        return showShadow.orElse(TooltipsConfig.SHOW_TOOLTIP_SHADOW);
    }

    public int getSecondPanelX() {
        return secondPanelX.orElse(TooltipsConfig.SECOND_PANEL_X);
    }

    public int getSecondPanelY() {
        return secondPanelY.orElse(TooltipsConfig.SECOND_PANEL_Y);
    }

    public int getSecondPanelSizeX() {
        return secondPanelSizeX.orElse(TooltipsConfig.SECOND_PANEL_SIZE_X);
    }

    public int getSecondPanelSizeY() {
        return secondPanelSizeY.orElse(TooltipsConfig.SECOND_PANEL_SIZE_Y);
    }

    public String getRatingAlignment() {
        return ratingAlignment.orElse(TooltipsConfig.RATING_X_ALIGNMENT);
    }

    public String getTitleAlignment() {
        return titleAlignment.orElse(TooltipsConfig.TITLE_X_ALIGNMENT);
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

    public boolean shouldShowSecondPanel(TooltipContext context) {
        if (context.getStack().getItem() instanceof TieredItem && showSecondPanel.orElse(TooltipsConfig.TIERED_ITEMS_RENDERER)) {
            return true;
        }
        else if (context.getStack().getItem() instanceof ArmorItem && showSecondPanel.orElse(TooltipsConfig.ARMOR_ITEMS_RENDERER)) {
            return true;
        }

        return false;
    }

    public boolean hasCustomColorItemRating() {
        return colorItemRating.isPresent();
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

}
