package dev.xylonity.tooltipoverhaul.client.util;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.render.FadeRenderType;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
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

        if (context.getStack().getItem() instanceof ArmorItem && !(context.getStack().getItem() instanceof AnimalArmorItem)) {
            return Optional.ofNullable(context.getFrameData()).map(data -> data.shouldShowSecondPanel(context)).orElse(TooltipsConfig.ARMOR_ITEMS_RENDERER);
        }

        return false;
    }

    private static boolean isComparisonActive(TooltipContext context) {
        return EquippedContextCalculator.isComparisonRequested() && context.getOtherTooltipContext() != null;
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

    public static String getInnerFrameCornerType(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getInnerFrameCornerType).orElse(TooltipsConfig.INNER_FRAME_CORNER_TYPE);
    }

    public static String getBackgroundCornerType(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getBackgroundCornerType).orElse(TooltipsConfig.BACKGROUND_CORNER_TYPE);
    }

    public static void applyFrameCorners(GuiGraphics graphics, int x0, int y0, int width, int height, int topColor, int bottomColor, int bgColor, String type, boolean cornerCut) {
        final int[][] erase;
        final int[][] add;
        // Which corners the style is applied to, as indices into the corners array below (in order: top left, top right, bottom left, bottom right)
        int[] cornersToApply = {0, 1, 2, 3};

        switch (type) {
            case "rounded" -> {
                erase = new int[][]{{0, 0}};
                add = new int[][]{};
            }
            case "bevel" -> {
                erase = new int[][]{{0, 0}};
                add = new int[][]{{1, 1}};
            }
            case "inner" -> {
                erase = new int[][]{};
                add = new int[][]{{1, 1}};
            }
            case "cut" -> {
                erase = new int[][]{{0, 0}, {1, 0}, {0, 1}};
                add = new int[][]{{1, 1}};
            }
            case "thick" -> {
                erase = new int[][]{};
                add = new int[][]{{1, 1}, {2, 1}, {3, 1}, {1, 2}, {2, 2}, {1, 3}};
                cornersToApply = new int[]{1};
            }
            case "bracket" -> {
                erase = new int[][]{};
                add = new int[][]{{2, 2}, {3, 2}, {2, 3}};
            }
            case "block" -> {
                erase = new int[][]{};
                add = new int[][]{{1, 1}, {2, 1}, {1, 2}, {2, 2}};
            }
            case "notch" -> {
                erase = new int[][]{{0, 0}, {1, 0}, {0, 1}};
                add = new int[][]{{2, 1}, {1, 2}, {2, 2}};
            }
            case "weld" -> {
                erase = new int[][]{};
                add = new int[][]{{1, 1}, {2, 1}, {1, 2}};
            }
            case "gem" -> {
                erase = new int[][]{};
                add = new int[][]{{2, 1}, {4, 1}, {1, 2}, {3, 2}, {2, 3}, {1, 4}};
            }
            default -> {
                return;
            }

        }

        final int xr = x0 + width - 1;
        final int yb = y0 + height - 1;

        // (cornerX, cornerY, inward X step, inward Y step) for each of the four corners
        int[][] corners = {
                {x0, y0, 1, 1},
                {xr, y0, -1, 1},
                {x0, yb, 1, -1},
                {xr, yb, -1, -1}
        };

        for (final int idx : cornersToApply) {
            final int[] corner = corners[idx];
            final int cornerX = corner[0];
            final int cornerY = corner[1];
            final int stepX = corner[2];
            final int stepY = corner[3];
            final int color = cornerY == y0 ? topColor : bottomColor;

            for (final int[] pixel : erase) {
                if (cornerCut && pixel[0] == 0 && pixel[1] == 0) {
                    continue;
                }

                fillPixel(graphics, cornerX + pixel[0] * stepX, cornerY + pixel[1] * stepY, bgColor);
            }
            for (final int[] pixel : add) {
                fillPixel(graphics, cornerX + pixel[0] * stepX, cornerY + pixel[1] * stepY, color);
            }

        }

    }

    private static void fillPixel(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x, y, x + 1, y + 1, color);
    }

    public static String getOverlayLocation(TooltipContext context) {
        return Optional.ofNullable(context.getFrameData()).map(CustomFrameData::getTextureLocation).orElse(TooltipsConfig.GLOBAL_FRAME_OVERLAY_LOCATION);
    }

    public static void renderItem(TooltipContext context, @Nullable LivingEntity entity, @Nullable Level level, ItemStack stack, int seed) {
        if (!stack.isEmpty()) {
            BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(stack, level, entity, seed);
            context.getPose().pushPose();

            try {
                context.getPose().scale(16.0F, -16.0F, 16.0F);
                boolean flag = !bakedmodel.usesBlockLight();
                if (flag) {
                    Lighting.setupForFlatItems();
                }

                // Cutout/solid item render types don't blend, so the fade of the in/out animation (applied
                // through the shader color alpha) has no visible effect on them
                MultiBufferSource bufferSource = context.getBuffer();
                if (RenderSystem.getShaderColor()[3] < 1.0f) {
                    final MultiBufferSource delegate = bufferSource;
                    bufferSource = type -> delegate.getBuffer(FadeRenderType.remap(type));
                }

                Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.GUI, false, context.getPose(), bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, bakedmodel);
                context.flush();
                if (flag) {
                    Lighting.setupFor3DItems();
                }
            }
            catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
                crashreportcategory.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
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
        graphics.fillGradient(x, y + mid, x + 1, y + height, c2, c3);

        // Right border (top and bottom sections)
        graphics.fillGradient(x + width - 1, y, x + width, y + mid, c1, c2);
        graphics.fillGradient(x + width - 1, y + mid, x + width, y + height, c2, c3);
    }

}
