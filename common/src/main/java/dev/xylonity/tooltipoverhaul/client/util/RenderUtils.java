package dev.xylonity.tooltipoverhaul.client.util;

import com.mojang.blaze3d.platform.Lighting;
import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Optional;

public class RenderUtils {

    public static boolean hasIcon(TooltipContext context) {
        return !context.getStack().isEmpty() && !Optional.ofNullable(context.getFrameData()).map(CustomFrameData::shouldDisableIcon).orElse(TooltipsConfig.DISABLE_ICON);
    }

    public static boolean hasRating(TooltipContext context) {
        return !context.getStack().isEmpty() && Optional.ofNullable(context.getFrameData()).map(CustomFrameData::shouldShowRating).orElse(TooltipsConfig.SHOW_RATING);
    }

    public static boolean hasDividerLine(TooltipContext context) {
        return !context.getStack().isEmpty() && !Optional.ofNullable(context.getFrameData()).map(CustomFrameData::shouldDisableDividerLine).orElse(TooltipsConfig.DISABLE_DIVIDER_LINE);
    }

    public static String getIconAppearAnimation(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getIconAppearAnimation).orElse(TooltipsConfig.ICON_APPEAR_ANIMATION);
    }

    public static float getIconRotatingSpeed(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getIconRotatingSpeed).orElse(TooltipsConfig.ICON_ROTATING_SPEED);
    }

    public static int calculatePadding(TooltipContext context, TextAxis axis) {
        if (axis == TextAxis.X) {
            return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getMainPanelPaddingX).orElse(TooltipsConfig.MAIN_PANEL_PADDING_X);
        }

        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getMainPanelPaddingY).orElse(TooltipsConfig.MAIN_PANEL_PADDING_Y);
    }

    public static String getInnerOverlayType(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getBorderType).orElse(TooltipsConfig.DEFAULT_INNER_OVERLAY_TYPE);
    }

    public static String getOverlayLocation(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getTextureLocation).orElse(TooltipsConfig.GLOBAL_FRAME_OVERLAY_LOCATION);
    }

    public static void renderItem(TooltipContext context, @Nullable LivingEntity entity, @Nullable Level level, ItemStack stack, int seed) {
        if (!stack.isEmpty()) {
            BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(stack, level, entity, seed);
            context.getPose().pushPose();

            try {
                context.getPose().mulPoseMatrix((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
                context.getPose().scale(16.0F, 16.0F, 16.0F);
                boolean flag = !bakedmodel.usesBlockLight();
                if (flag) {
                    Lighting.setupForFlatItems();
                }

                Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.GUI, false, context.getPose(), context.getBuffer(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, bakedmodel);
                context.flush();
                if (flag) {
                    Lighting.setupFor3DItems();
                }
            }
            catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
                crashreportcategory.setDetail("Item Damage", () -> String.valueOf(stack.getDamageValue()));
                crashreportcategory.setDetail("Item NBT", () -> String.valueOf(stack.getTag()));
                crashreportcategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
                throw new ReportedException(crashreport);
            }

            context.getPose().popPose();
        }

    }

}
