package dev.xylonity.tooltipoverhaul.client.style.preview.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.layer.impl.PreviewRendererLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.AnimationUtils;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextAxis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DefaultPreviewPlayerRenderer implements PreviewRendererLayer {

    private static final GameProfile DEFAULT_PROFILE = new GameProfile(new UUID(0L, 0L), "");
    private static final ResourceLocation DEFAULT_SKIN = TooltipOverhaul.pathOf("textures/preview/dummy.png");

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
            RemotePlayer player = getRemotePlayer(minecraft, context);

            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                player.setItemSlot(equipmentSlot, ItemStack.EMPTY);
            }
            player.setItemSlot(armorItem.getEquipmentSlot(), context.getStack());

            player.setYRot(0);
            player.setXRot(0);
            player.yBodyRot = 0;
            player.yHeadRot = 0;

            Lighting.setupForEntityInInventory();
            EntityRenderDispatcher renderer = minecraft.getEntityRenderDispatcher();
            renderer.setRenderShadow(false);
            renderer.render(player, 0, 0, 0, 0, 1, context.getPose(), context.getBuffer(), 0xF000F0);
            renderer.setRenderShadow(true);
        }

    }

    private static @NotNull RemotePlayer getRemotePlayer(Minecraft minecraft, TooltipContext context) {
        LocalPlayer localPlayer = minecraft.player;
        boolean usePlayerSkin = RenderUtils.usePlayerSkinInPreview(context);
        return new RemotePlayer(minecraft.level, DEFAULT_PROFILE) {
            @Override
            public PlayerSkin getSkin() {
                if (localPlayer != null && usePlayerSkin) {
                    return localPlayer.getSkin();
                }

                return new PlayerSkin(DEFAULT_SKIN, null, null, null, PlayerSkin.Model.WIDE, true);
            }

        };

    }

}