package dev.xylonity.tooltipoverhaul.client.style.preview.renderer;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import dev.xylonity.tooltipoverhaul.client.layer.impl.PreviewRendererLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextAxis;
import dev.xylonity.tooltipoverhaul.compat.modernfix.ModernFixCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

public class DefaultPreviewArmorStand implements PreviewRendererLayer {

    @Override
    public void render(TooltipContext context, Vec2 startPosition, Vec2 endPosition) {

        int sizeX = RenderUtils.calculateSecondPanelSize(context, TextAxis.X);
        int sizeY = RenderUtils.calculateSecondPanelSize(context, TextAxis.Y);
        int y0 = (int) startPosition.y;
        int x1 = (int) endPosition.x;

        context.translate((x1 + sizeX / 2f + 2), (y0 + sizeY / 1.15f) + 2, 0);

        context.multiply(Axis.XP, -30);
        context.multiply(Axis.YP, -45);

        context.multiply(Axis.YP, (((System.currentTimeMillis() - context.getStartTime()) / 20f) % 360) * AnimationUtils.getSecondPanelRendererSpeed(context));

        float scale = Math.min(sizeX, sizeY / 2f) / 1.25f;

        context.scale(-scale, -scale, scale);

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && context.getStack().getItem() instanceof ArmorItem armorItem) {
            ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, minecraft.level);
            armorStand.setNoBasePlate(true);

            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                armorStand.setItemSlot(equipmentSlot, ItemStack.EMPTY);
            }
            armorStand.setItemSlot(armorItem.getEquipmentSlot(), context.getStack());

            Lighting.setupForEntityInInventory();
            EntityRenderDispatcher renderer = Minecraft.getInstance().getEntityRenderDispatcher();
            renderer.setRenderShadow(false);
            renderer.render(armorStand, 0, 0, 0, 0, 1, context.getPose(), context.getBuffer(), 0xF000F0);
            renderer.setRenderShadow(true);
        }

    }

}
