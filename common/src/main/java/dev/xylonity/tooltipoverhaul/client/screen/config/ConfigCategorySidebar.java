package dev.xylonity.tooltipoverhaul.client.screen.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.categoryLabel;
import static dev.xylonity.tooltipoverhaul.client.util.ColorUtils.withAlpha;

/**
 * Configuration navigation rail
 */
final class ConfigCategorySidebar extends ConfigNavigationList<ConfigCategorySidebar.Item> {

    private final List<Item> categories;

    ConfigCategorySidebar(int x, int y, int width, int height, int accent, Map<String, Integer> categoryCounts, int total, Consumer<String> onSelect) {
        this(x, y, width, height, accent, buildItems(categoryCounts, total), onSelect);
    }

    private ConfigCategorySidebar(int x, int y, int width, int height, int accent, List<Item> categories, Consumer<String> onSelect) {
        super(x, y, width, height,
                Component.literal(I18n.get("tooltipoverhaul.config.categories").toUpperCase(Locale.ROOT)),
                accent, categories, new Adapter<>() {
                    @Override
                    public Component label(Item item, int index) {
                        return Component.literal(item.label());
                    }

                    @Override
                    public int rowHeight(Item item, int index, int contentWidth) {
                        return 22;
                    }

                    @Override
                    public int trailingWidth(Item item, int index) {
                        return Minecraft.getInstance().font.width(Integer.toString(item.count())) + 4;
                    }

                    @Override
                    public void renderTrailing(GuiGraphics graphics, Item item, int index, int right, int y, int rowHeight, int alpha, boolean selected, boolean hovered) {
                        final String count = Integer.toString(item.count());
                        final int color = selected ? accent & 0x00FFFFFF : 0x505050;
                        graphics.drawString(Minecraft.getInstance().font, count, right - Minecraft.getInstance().font.width(count),
                                y + (rowHeight - 2 - Minecraft.getInstance().font.lineHeight) / 2 + 1, withAlpha(color, alpha), false);
                    }

                }, index -> onSelect.accept(categories.get(index).key()));

        this.categories = categories;

        setSelectedIndex(0);
    }

    void setSelectedKey(String key) {
        for (int i = 0; i < categories.size(); i++) {
            if (Objects.equals(categories.get(i).key(), key)) {
                setSelectedIndex(i);
                return;
            }

        }

        setSelectedIndex(0);
    }

    private static List<Item> buildItems(Map<String, Integer> categoryCounts, int total) {
        final List<Item> items = new ArrayList<>();

        items.add(new Item(null, I18n.get("tooltipoverhaul.config.category.all"), total));

        for (final Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            items.add(new Item(entry.getKey(), categoryLabel(entry.getKey()), entry.getValue()));
        }

        return items;
    }

    record Item(
            String key,
            String label,
            int count
    ) {
        ;;
    }

}
