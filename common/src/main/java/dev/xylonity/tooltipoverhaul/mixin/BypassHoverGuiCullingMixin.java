package dev.xylonity.tooltipoverhaul.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xylonity.tooltipoverhaul.compat.modernfix.ModernFixCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BuiltInModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restores the real baked model of the hovered stack in case there is a mod that potentially culls
 * non-visible faces of the current model (such as modernfix)
 */
@Mixin(value = ItemRenderer.class, priority = 1200)
public abstract class BypassHoverGuiCullingMixin {

    @Unique
    private ItemDisplayContext tooltipoverhaul$lastCtx;

    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V", at = @At("HEAD"))
    private void tooltipoverhaul$captureCtx(ItemStack stack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, BakedModel model, CallbackInfo ci) {
        this.tooltipoverhaul$lastCtx = displayContext;
    }

    @ModifyArg(
            method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderModelLists(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemStack;IILcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"
            ),
            index = 0
    )
    private BakedModel tooltipoverhaul$restoreModelIfHovered(BakedModel model, ItemStack stack, int combinedLight, int combinedOverlay, PoseStack poseStack, VertexConsumer buffer) {
        if (ModernFixCompat.SHOULD_RETURN_ORIGINAL_RENDER && this.tooltipoverhaul$lastCtx == ItemDisplayContext.GUI && ModernFixCompat.isEnabled()) {
            Minecraft minecraft = Minecraft.getInstance();
            BakedModel real = minecraft.getItemRenderer().getModel(stack, minecraft.level, minecraft.player, 0);
            if (real instanceof BuiltInModel || !tooltipoverhaul$hasAnyQuads(real)) {
                try {
                    ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    BakedModel inv = minecraft.getModelManager().getModel(new ModelResourceLocation(itemKey, "inventory"));
                    if (!(inv instanceof BuiltInModel) && tooltipoverhaul$hasAnyQuads(inv)) {
                        return inv;
                    }
                }
                catch (Throwable ignored) {
                    ;;
                }

                return model;
            }

            return real;
        }

        return model;
    }

    @Unique
    private boolean tooltipoverhaul$hasAnyQuads(BakedModel model) {
        RandomSource random = RandomSource.create(42L);
        for (Direction direction : Direction.values()) {
            if (!model.getQuads(null, direction, random).isEmpty()){
                return true;
            }
        }

        return !model.getQuads(null, null, random).isEmpty();
    }


}
