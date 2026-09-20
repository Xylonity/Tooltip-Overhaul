package dev.xylonity.tooltipoverhaul.client.style.preview.renderer;

import com.mojang.authlib.GameProfile;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;

import java.util.UUID;

public final class PreviewEntityCache {

    private static final GameProfile PROFILE = new GameProfile(new UUID(0L, 0L), "");
    private static final ResourceLocation SKIN = TooltipOverhaul.pathOf("textures/preview/dummy.png");

    private static ArmorStand armorStand;
    private static PreviewPlayer player;

    public static void clear() {
        armorStand = null;
        player = null;
    }

    static ArmorStand armorStand(ClientLevel level) {
        if (armorStand == null || armorStand.level() != level) {
            armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
            armorStand.setNoBasePlate(true);
        }

        return armorStand;
    }

    static RemotePlayer player(ClientLevel level, boolean usePlayerSkin) {
        if (player == null || player.level() != level) {
            player = new PreviewPlayer(level);
        }

        player.usePlayerSkin = usePlayerSkin;

        return player;
    }

    private static final class PreviewPlayer extends RemotePlayer {

        private boolean usePlayerSkin;

        private PreviewPlayer(ClientLevel level) {
            super(level, PROFILE);
        }

        @Override
        public ResourceLocation getSkinTextureLocation() {
            final var local = Minecraft.getInstance().player;
            return usePlayerSkin && local != null ? local.getSkinTextureLocation() : SKIN;
        }

        @Override
        public String getModelName() {
            final var local = Minecraft.getInstance().player;
            return usePlayerSkin && local != null ? local.getModelName() : "default";
        }

        @Override
        public boolean isModelPartShown(PlayerModelPart part) {
            final var local = Minecraft.getInstance().player;
            return usePlayerSkin && local != null ? local.isModelPartShown(part) : super.isModelPartShown(part);
        }

    }

}
