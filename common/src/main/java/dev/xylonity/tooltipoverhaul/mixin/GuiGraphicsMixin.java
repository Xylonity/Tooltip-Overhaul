package dev.xylonity.tooltipoverhaul.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.wrap.TooltipWrapper;
import dev.xylonity.tooltipoverhaul.registry.TooltipOverhaulKeyMappings;
import dev.xylonity.tooltipoverhaul.util.ITooltipOverhaulItemAware;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = GuiGraphics.class, priority = 1)
public class GuiGraphicsMixin {

    /**
     * Main tooltip renderer call. A context is populated with the relevant info needed to render the tooltip. Nothing else
     * is rendered except if explicit specified within the renderer internal logic, thus preventing possible incompats (that
     * shouldn't exist anyways)
     */
    @Inject(method = "renderTooltipInternal", at = @At(value = "HEAD"), cancellable = true)
    private void tooltipoverhaul$coreRenderer(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner tooltipPositioner, CallbackInfo ci) {
        int sw = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int sh = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        ItemStack stack = ((ITooltipOverhaulItemAware) this).tooltipsOverhaul$hoveredItem();

        boolean isKeyDown = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), TooltipOverhaulKeyMappings.COMPARE_TOOLTIP.getDefaultKey().getValue());

        // Meant for comparation (thus matching the equipped stack)
        TooltipContext equippedContext = null;
        if (isKeyDown) {
            equippedContext = tooltipoverhaul$getHoveredContext(font, mouseX, mouseY, stack, sw, sh);
        }

        boolean comparisonActive = equippedContext != null;

        // Prevents double wrapping by error
        List<ClientTooltipComponent> mainText;
        boolean isAtMainHalf = false;
        if (comparisonActive) {
            List<ClientTooltipComponent> rebuilt = tooltipoverhaul$getTooltipFromStack(stack);
            if (tooltipoverhaul$estimateFullWidth(font, rebuilt, !stack.isEmpty()) > ((sw / 2) - 4)) {
                mainText = TooltipWrapper.wrapHalf(font, rebuilt, sw, stack);
                isAtMainHalf = true;
            }
            else {
                mainText = TooltipWrapper.wrap(font, rebuilt, sw, stack);
            }
        }
        else {
            mainText = TooltipWrapper.wrap(font, components, sw, stack);
        }

        // Main context to render
        TooltipContext mainContext = TooltipContext.of((GuiGraphics) (Object) this, mouseX, mouseY, sw, sh, mainText, stack, equippedContext, true);
        mainContext.setHalfWrapped(isAtMainHalf);
        if (equippedContext != null) {
            equippedContext.setOtherTooltipContext(mainContext);
        }

        if (TooltipRenderer.render(mainContext)) {
            if (comparisonActive) {
                TooltipRenderer.render(equippedContext);
            }

            ci.cancel();
        }

    }

    @Unique
    private TooltipContext tooltipoverhaul$getHoveredContext(Font font, int mouseX, int mouseY, ItemStack stack, int sw, int sh) {
        ItemStack toCompare = ItemStack.EMPTY;
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (stack.isEmpty() || player == null) return null;

        if (stack.getItem() instanceof Equipable e) {
            ItemStack armor = player.getInventory().getArmor(e.getEquipmentSlot().getIndex());
            if (!armor.isEmpty()) {
                toCompare = armor;
            }

        }

        if (toCompare.isEmpty() || ItemStack.isSameItemSameTags(toCompare, stack)) return null;

        List<ClientTooltipComponent> compareComponents = tooltipoverhaul$getTooltipFromStack(toCompare);

        boolean needsHalf = tooltipoverhaul$estimateFullWidth(font, compareComponents, true) > ((sw / 2) - 4);
        List<ClientTooltipComponent> wrapped = needsHalf ? TooltipWrapper.wrapHalf(font, compareComponents, sw, toCompare) : TooltipWrapper.wrap(font, compareComponents, sw, toCompare);

        TooltipContext ctx = TooltipContext.of((GuiGraphics) (Object) this, mouseX, mouseY, sw, sh, wrapped, toCompare, null, false);
        ctx.setHalfWrapped(needsHalf);

        return ctx;
    }

    @Unique
    private List<ClientTooltipComponent> tooltipoverhaul$getTooltipFromStack(ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        List<ClientTooltipComponent> components = new ArrayList<>();
        if (player == null || stack.isEmpty()) return components;

        List<Component> lines = stack.getTooltipLines(player, minecraft.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL);
        for (Component line : lines) {
            components.add(ClientTooltipComponent.create(line.getVisualOrderText()));
        }

        stack.getTooltipImage().ifPresent(component -> {
            if (component instanceof BundleTooltip bundle) {
                int idx = lines.size() > 1 ? 1 : components.size();
                components.add(idx, ClientTooltipComponent.create(bundle));
            }
        });

        return components;
    }

    @Unique
    private int tooltipoverhaul$estimateFullWidth(Font font, List<ClientTooltipComponent> components, boolean hasIcon) {
        if (components == null || components.isEmpty()) {
            return 0;
        }

        int width = TooltipRenderer.PADDING_X * 2 + (hasIcon ? 26 : 0) + components.get(0).getWidth(font);
        for (ClientTooltipComponent component : components) {
            width = Math.max(width, TooltipRenderer.PADDING_X * 2 + component.getWidth(font));
        }

        return width;
    }

}
