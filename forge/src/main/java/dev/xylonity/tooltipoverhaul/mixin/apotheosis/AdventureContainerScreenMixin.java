package dev.xylonity.tooltipoverhaul.mixin.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.client.AdventureContainerScreen;
import dev.shadowsoffire.apotheosis.util.DrawsOnLeft;
import dev.shadowsoffire.placebo.screen.PlaceboContainerScreen;
import dev.xylonity.tooltipoverhaul.compat.apotheosis.ApotheosisHook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = AdventureContainerScreen.class, remap = false)
public abstract class AdventureContainerScreenMixin<T extends AbstractContainerMenu> extends PlaceboContainerScreen<T> implements DrawsOnLeft {

    public AdventureContainerScreenMixin(T menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    public void drawOnLeft(GuiGraphics graphics, List<Component> components, int y, int maxWidth) {
        if (components.isEmpty()) return;

        Font font = Minecraft.getInstance().font;
        List<FormattedText> split = new ArrayList<>();
        for (Component c : components) {
            split.addAll(font.getSplitter().splitLines(c, maxWidth, c.getStyle()));
        }

        int x = this.getGuiLeft() - 16 - split.stream().map(font::width).max(Integer::compare).get();

        ApotheosisHook.enter();
        try {
            graphics.renderComponentTooltip(font, split, x, y, ItemStack.EMPTY);
        }
        finally {
            ApotheosisHook.exit();
        }

    }

}
