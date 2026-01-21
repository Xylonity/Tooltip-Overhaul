package dev.xylonity.tooltipoverhaul.client.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.mixin.KeyMappingAccessor;
import dev.xylonity.tooltipoverhaul.registry.TooltipOverhaulKeyMappings;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
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

    public static boolean hasShadow(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::shouldShowShadow).orElse(TooltipsConfig.SHOW_TOOLTIP_SHADOW);
    }

    public static boolean hasVignette(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::hasVignette).orElse(TooltipsConfig.VIGNETTES.isBlank());
    }

    public static boolean shouldRender(TooltipContext context) {
        return !Optional.ofNullable(context.getFrameData()).map(CustomFrameData::shouldDisableTooltip).orElse(false);
    }

    public static boolean hasPreviewOfTieredItem(TooltipContext context) {
        if (isComparisonActive(context)) {
            return false;
        }

        if (context.getStack().getItem() instanceof TieredItem) {
            return Optional.ofNullable(context.getFrameData()).map(data -> data.shouldShowSecondPanel(context)).orElse(TooltipsConfig.TIERED_ITEMS_RENDERER);
        }

        return false;
    }

    public static boolean hasPreviewOfArmorItem(TooltipContext context) {
        if (isComparisonActive(context)) {
            return false;
        }

        if (context.getStack().getItem() instanceof ArmorItem) {
            return Optional.ofNullable(context.getFrameData()).map(data -> data.shouldShowSecondPanel(context)).orElse(TooltipsConfig.ARMOR_ITEMS_RENDERER);
        }

        return false;
    }

    private static boolean isComparisonActive(TooltipContext context) {
        InputConstants.Key compareKey = ((KeyMappingAccessor) TooltipOverhaulKeyMappings.COMPARE_TOOLTIP).tooltipoverhaul$key();
        boolean isKeyDown = false;
        if (!compareKey.equals(InputConstants.UNKNOWN)) {
            isKeyDown = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), compareKey.getValue());
        }

        return isKeyDown && context.getOtherTooltipContext() != null;
    }

    public static int calculateSecondPanelSize(TooltipContext context, TextAxis axis) {
        if (axis == TextAxis.X) {
            return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getSecondPanelSizeX).orElse(TooltipsConfig.SECOND_PANEL_SIZE_X);
        }

        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getSecondPanelSizeY).orElse(TooltipsConfig.SECOND_PANEL_SIZE_Y);
    }

    public static String getIconAppearAnimation(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getIconAppearAnimation).orElse(TooltipsConfig.ICON_APPEAR_ANIMATION);
    }

    public static float getIconRotatingSpeed(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getIconRotatingSpeed).orElse(TooltipsConfig.ICON_ROTATING_SPEED);
    }

    public static String getIconBackgroundType(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getIconBackground).orElse(TooltipsConfig.ICON_BACKGROUND_TYPE);
    }

    public static String getDividerLineType(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getDividerLineType).orElse(TooltipsConfig.DIVIDER_LINE_TYPE);
    }

    public static String getEffect(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getEffect).orElse(TooltipsConfig.EFFECTS);
    }

    public static boolean usePlayerSkinInPreview(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getUsePlayerSkinInPreview).orElse(TooltipsConfig.USE_PLAYER_SKIN_IN_PREVIEW);
    }

    public static String getPreviewPanelModel(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getPreviewPanelModel).orElse(TooltipsConfig.PREVIEW_PANEL_MODEL);
    }

    public static int calculatePadding(TooltipContext context, TextAxis axis) {
        if (context.getStack().isEmpty()) {
            if (axis == TextAxis.X) {
                return TooltipsConfig.NO_STACK_TOOLTIP_PADDING_X;
            }

            return TooltipsConfig.NO_STACK_TOOLTIP_PADDING_Y;
        }

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

    public static void renderFrameGradient(GuiGraphics graphics, int x, int y, int width, int height, int c1, int c2, int c3) {
        int mid = height / 2;

        // Left border (top and bottom sections)
        graphics.fillGradient(x, y, x + 1, y + mid, c1, c2);
        graphics.fillGradient(x, y + mid, x + 1, y + height - 1, c2, c3);

        // Right border (top and bottom sections)
        graphics.fillGradient(x + width - 1, y, x + width, y + mid, c1, c2);
        graphics.fillGradient(x + width - 1, y + mid, x + width, y + height - 1, c2, c3);
    }

}
