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
    private void enhancedtooltips$coreRenderer(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner tooltipPositioner, CallbackInfo ci) {
        int sw = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int sh = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        ItemStack stack = ((ITooltipOverhaulItemAware) this).tooltipsOverhaul$hoveredItem();
        List<ClientTooltipComponent> text = TooltipWrapper.wrap(font, components, sw, stack);

        boolean isKeyDown = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), TooltipOverhaulKeyMappings.COMPARE_TOOLTIP.getDefaultKey().getValue());

        // Meant for comparation (thus matching the equipped stack)
        TooltipContext equippedContext = null;
        if (isKeyDown) {
            equippedContext = tooltipOverhaul$getHoveredContext(font, mouseX, mouseY, stack, sw, sh);
        }

        // Main context to render
        TooltipContext mainContext = TooltipContext.of((GuiGraphics) (Object) this, mouseX, mouseY, sw, sh, text, stack, equippedContext, true);
        if (equippedContext != null) {
            equippedContext.setOtherTooltipContext(mainContext);
        }

        if (TooltipRenderer.render(mainContext)) {
            if (isKeyDown && equippedContext != null) {
                TooltipRenderer.render(equippedContext);
            }

            ci.cancel();
        }

    }

    @Unique
    private TooltipContext tooltipOverhaul$getHoveredContext(Font font, int mouseX, int mouseY, ItemStack stack, int sw, int sh) {
        ItemStack toCompare = ItemStack.EMPTY;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (stack.getItem() instanceof Equipable e) {
            if (player != null) {
                ItemStack armor = player.getInventory().getArmor(e.getEquipmentSlot().getIndex());
                if (!armor.isEmpty()) {
                    toCompare = armor;
                }

                if (!toCompare.isEmpty()) {
                    List<ClientTooltipComponent> compareComponents = new ArrayList<>();
                    TooltipFlag flag = mc.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;

                    List<Component> lines = toCompare.getTooltipLines(player, flag);
                    for (Component line : lines) {
                        compareComponents.add(ClientTooltipComponent.create(line.getVisualOrderText()));
                    }

                    toCompare.getTooltipImage().ifPresent(tc -> {
                        if (tc instanceof BundleTooltip bundle) {
                            int idx = lines.size() > 1 ? 1 : compareComponents.size();
                            compareComponents.add(idx, ClientTooltipComponent.create(bundle));
                        }

                    });

                    return TooltipContext.of((GuiGraphics) (Object) this, mouseX, mouseY, sw, sh, TooltipWrapper.wrap(font, compareComponents, sw, toCompare), toCompare, null, false);
                }
            }

        }

        return null;
    }

}
