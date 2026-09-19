package dev.xylonity.tooltipoverhaul.client.screen.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.*;

final class ConfigOptionButton<T> extends AbstractWidget {

    private final List<T> options;
    private final int accent;
    private final Function<T, String> id;
    private final Function<T, String> label;
    private final Consumer<T> onChange;
    private final Runnable beforeOpen;
    private final ConfigModalHost modals;
    private final int screenWidth;
    private final int screenHeight;
    private int index;
    private float hoverAnimation;

    ConfigOptionButton(int x, int y, int width, int height, List<T> options, T current, int accent, Function<T, String> id, Function<T, String> label, Consumer<T> onChange, Runnable beforeOpen, ConfigModalHost modals, int screenWidth, int screenHeight) {
        super(x, y, width, height, Component.empty());
        if (options.isEmpty()) {
            throw new IllegalArgumentException("A configuration option button needs at least one option");
        }

        this.options = List.copyOf(options);
        this.accent = accent;
        this.id = id;
        this.label = label;
        this.onChange = onChange;
        this.beforeOpen = beforeOpen;
        this.modals = modals;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        setCurrent(current);
    }

    T getCurrent() {
        return options.get(index);
    }

    void setCurrent(Object value) {
        for (int i = 0; i < options.size(); i++) {
            if (Objects.equals(options.get(i), value)) {
                index = i;
                return;
            }

        }

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible || button != 0 || !clicked(mouseX, mouseY)) {
            return false;
        }

        beforeOpen.run();

        final List<SelectionPopup.Option> choices = options.stream()
                .map(value -> new SelectionPopup.Option(id.apply(value), label.apply(value), "", ItemStack.EMPTY))
                .toList();
        modals.open(new SelectionPopup(screenWidth, screenHeight,
                Component.translatable("tooltipoverhaul.config.frames.choose"), choices,
                value -> id.apply(getCurrent()).equals(value), this::select, false, accent));

        playDownSound(Minecraft.getInstance().getSoundManager());

        return true;
    }

    private void select(String selectedId) {
        for (int i = 0; i < options.size(); i++) {
            final T option = options.get(i);
            if (id.apply(option).equals(selectedId)) {
                index = i;
                onChange.accept(option);
                return;
            }

        }

    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        final int x0 = getX();
        final int y0 = getY();
        final int width = getWidth();
        final int height = getHeight();
        final boolean hovering = mouseX >= x0 && mouseX < x0 + width && mouseY >= y0 && mouseY < y0 + height;
        hoverAnimation = hovering ? Math.min(1f, hoverAnimation + 0.15f) : Math.max(0f, hoverAnimation - 0.1f);
        final int border = buttonBorder(accent, hoverAnimation);
        drawCard(graphics, x0, y0, x0 + width, y0 + height, 0xFF18181A, border);

        final Minecraft client = Minecraft.getInstance();
        final T value = getCurrent();
        String text = label.apply(value);
        final int iconGap = 3;
        final int maximumLabelWidth = width - ConfigIconButton.Icon.EXPAND.width - iconGap - 12;
        if (client.font.width(text) > maximumLabelWidth) {
            final int ellipsisWidth = client.font.width("\u2026");
            text = client.font.plainSubstrByWidth(text, Math.max(4, maximumLabelWidth - ellipsisWidth)) + "\u2026";
        }

        final int textColor = "inherit".equals(id.apply(value)) ? 0xFF666666 : 0xFFD0D0D0;
        final int contentWidth = client.font.width(text) + iconGap + ConfigIconButton.Icon.EXPAND.width;
        final int contentX = x0 + (width - contentWidth) / 2;
        graphics.drawString(client.font, text, contentX, y0 + (height - client.font.lineHeight) / 2 + 1, textColor, false);
        ConfigIconButton.drawIcon(graphics, ConfigIconButton.Icon.EXPAND, contentX + client.font.width(text) + iconGap, y0 + (height - ConfigIconButton.Icon.EXPAND.height) / 2, textColor);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        ;;
    }

}
