package dev.xylonity.tooltipoverhaul.client.frame;

import dev.xylonity.tooltipoverhaul.client.Palette;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record CustomFrameData(List<String> items, List<String> tags, Optional<String> texture, Optional<InnerBorderType> borderType, Optional<GradientType> gradientType, Optional<List<String>> gradientColors, Optional<String> itemRating, Optional<Integer> colorItemRating, Optional<String> particles, Optional<String> specialEffect, Optional<Boolean> disableTooltip) {

    public String getTexture() {
        return texture.filter(t -> !t.trim().isEmpty()).orElse(Defaults.TEXTURE);
    }

    public InnerBorderType getBorderType() {
        return borderType.orElse(Defaults.BORDER_TYPE);
    }

    public GradientType getGradientType() {
        return gradientType.orElse(Defaults.GRADIENT_TYPE);
    }

    public List<String> getGradientColors() {
        return gradientColors.filter(colors -> colors.size() >= 3).map(colors -> colors.subList(0, 3)).orElse(Defaults.GRADIENT_COLORS);
    }

    public String getItemRating(ItemStack stack) {
        return itemRating.filter(rating -> !rating.trim().isEmpty()).orElseGet(() -> formatRarity(stack.getRarity()));
    }

    public int getItemRatingColor(ItemStack stack) {
        return colorItemRating.orElseGet(() -> getRarityColor(stack));
    }

    public boolean shouldDisableTooltip() {
        return disableTooltip.orElse(Defaults.DISABLE_TOOLTIP);
    }

    private static int getRarityColor(ItemStack stack) {
        return switch (stack.getRarity()) {
            case COMMON -> Palette.COMMON[0];
            case UNCOMMON -> Palette.UNCOMMON[0];
            case RARE -> Palette.RARE[0];
            case EPIC -> Palette.EPIC[0];
            // Simulated legendary rarity
            default -> Palette.LEGENDARY[0];
        };
    }

    //public String getParticles() {
    //    return particles.filter(p -> !p.trim().isEmpty()).orElse(Defaults.PARTICLES);
    //}

    public String getEffect() {
        return specialEffect.filter(effect -> !effect.trim().isEmpty()).orElse(Defaults.SPECIAL_EFFECT);
    }

    public List<ResourceLocation> getItemLocations() {
        return items.stream().map(ResourceLocation::parse).collect(Collectors.toList());
    }

    public List<TagKey<Item>> getTagKeys() {
        return tags.stream().map(name -> TagKey.create(Registries.ITEM, ResourceLocation.parse(name))).collect(Collectors.toList());
    }

    public ResourceLocation getTextureLocation() {
        return ResourceLocation.parse(getTexture());
    }

    public List<Color> getGradientColorValues() {
        return getGradientColors().stream().map(hex -> Color.decode(hex.startsWith("#") ? hex : "#" + hex)).collect(Collectors.toList());
    }

    public boolean matches(ItemStack stack) {
        if (items.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
            return true;
        }

        for (TagKey<Item> tagKey : getTagKeys()) {
            if (stack.is(tagKey)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasCustomTexture() {
        return texture.isPresent() && !texture.get().trim().isEmpty();
    }

    public boolean hasCustomBorderType() {
        return borderType.isPresent();
    }

    public boolean hasCustomGradientType() {
        return gradientType.isPresent();
    }

    public boolean hasCustomGradientColors() {
        return gradientColors.isPresent() && gradientColors.get().size() >= 3;
    }

    public boolean hasCustomItemRating() {
        return itemRating.isPresent() && !itemRating.get().trim().isEmpty();
    }

    public boolean hasCustomColorItemRating() {
        return colorItemRating.isPresent();
    }

    public boolean hasCustomParticles() {
        return particles.isPresent() && !particles.get().trim().isEmpty();
    }

    public boolean hasCustomSpecialEffect() {
        return specialEffect.isPresent() && !specialEffect.get().trim().isEmpty();
    }

    public boolean hasCustomDisableTooltip() {
        return disableTooltip.isPresent();
    }

    private static String formatRarity(Rarity rarity) {
        String name = rarity.name();
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    public static final class Defaults {
        public static final String TEXTURE = null;
        public static final InnerBorderType BORDER_TYPE = InnerBorderType.GRADIENT;
        public static final GradientType GRADIENT_TYPE = GradientType.COMMON;
        public static final List<String> GRADIENT_COLORS = List.of("#FFFFFFFF", "#FFFFFFFF", "#FFFFFFFF");
        //public static final String PARTICLES = "none";
        public static final String SPECIAL_EFFECT = "none";
        public static final boolean DISABLE_TOOLTIP = false;
    }

    public enum InnerBorderType {
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

    public static class Builder {
        private List<String> items = List.of();
        private List<String> tags = List.of();
        private Optional<String> texture = Optional.empty();
        private Optional<InnerBorderType> borderType = Optional.empty();
        private Optional<GradientType> gradientType = Optional.empty();
        private Optional<List<String>> gradientColors = Optional.empty();
        private Optional<String> itemRating = Optional.empty();
        private Optional<Integer> colorItemRating = Optional.empty();
        private Optional<String> particles = Optional.empty();
        private Optional<String> specialEffect = Optional.empty();
        private Optional<Boolean> disableTooltip = Optional.empty();

        public Builder items(List<String> items) {
            this.items = items != null ? items : List.of();
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = tags != null ? tags : List.of();
            return this;
        }

        public Builder texture(String texture) {
            this.texture = Optional.ofNullable(texture);
            return this;
        }

        public Builder borderType(InnerBorderType borderType) {
            this.borderType = Optional.ofNullable(borderType);
            return this;
        }

        public Builder gradientType(GradientType gradientType) {
            this.gradientType = Optional.ofNullable(gradientType);
            return this;
        }

        public Builder gradientColors(List<String> gradientColors) {
            this.gradientColors = Optional.ofNullable(gradientColors);
            return this;
        }

        public Builder itemRating(String itemRating) {
            this.itemRating = Optional.ofNullable(itemRating);
            return this;
        }

        public Builder colorItemRating(Integer colorItemRating) {
            this.colorItemRating = Optional.ofNullable(colorItemRating);
            return this;
        }

        public Builder particles(String particles) {
            this.particles = Optional.ofNullable(particles);
            return this;
        }

        public Builder specialEffect(String specialEffect) {
            this.specialEffect = Optional.ofNullable(specialEffect);
            return this;
        }

        public Builder disableTooltip(Boolean disableTooltip) {
            this.disableTooltip = Optional.ofNullable(disableTooltip);
            return this;
        }

        public CustomFrameData build() {
            return new CustomFrameData(items, tags, texture, borderType, gradientType, gradientColors, itemRating, colorItemRating, particles, specialEffect, disableTooltip);
        }

    }

}
