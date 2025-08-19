package dev.xylonity.tooltipoverhaul.client.style.renderer;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipArmorStand;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class DefaultArmorStand implements ITooltipArmorStand {

    public static @Nullable ArmorStand stand;

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        equipArmor(ctx.stack());
        if (Minecraft.getInstance().level == null) return;
        if (stand == null) {
            stand = new ArmorStand(EntityType.ARMOR_STAND, Minecraft.getInstance().level);
            stand.setInvisible(false);
            stand.setNoBasePlate(true);
            stand.setShowArms(true);
            stand.setNoBasePlate(false);
        }

        ctx.push(() -> {
            ctx.translate(pos.x, pos.y, depth.getZ());
            ctx.scale(-30, -30, 30);

            // Default isometric rotation
            ctx.multiply(Axis.XP, 30);
            ctx.multiply(Axis.YP, -45);

            // Continuous horizontal rotation (yaw)
            ctx.multiply(Axis.YP, Util.calcRotY(8000 / TooltipsConfig.ARMOR_PREVIEW_ROTATING_SPEED));

            Lighting.setupForEntityInInventory();
            EntityRenderDispatcher renderer = Minecraft.getInstance().getEntityRenderDispatcher();
            renderer.setRenderShadow(false);
            renderer.render(stand, 0, 0, 0, 0, 1, ctx.pose(), ctx.buffer(), 0xF000F0);
            renderer.setRenderShadow(true);
        });

    }

    private static void equipArmor(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem ar)) return;
        if (stand == null) return;

        for (EquipmentSlot s : EquipmentSlot.values()) {
            stand.setItemSlot(s, ItemStack.EMPTY);
        }

        stand.setItemSlot(ar.getEquipmentSlot(), stack.copy());
    }

}
