package dev.xylonity.tooltipoverhaul.client.style.text;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xylonity.tooltipoverhaul.client.layer.impl.TextLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

import java.util.List;

public class DefaultText implements TextLayer {

    @Override
    public void render(TooltipContext context, Vec2 position) {

        List<ClientTooltipComponent> components = context.getComponents();

        Font font = context.getFont();
        PoseStack poseStack = context.getPose();
        GuiGraphics graphics = context.getGraphics();

        boolean hasIcon = context.hasIcon();
        boolean hasDividerLine = context.hasDividerLine();
        boolean hasRating = RenderUtils.hasRating(context);

        int paddingX = context.getPaddingX();
        int paddingY = context.getPaddingY();

        int x = (int) (position.x + paddingX);
        int y = (int) (position.y + paddingY + 1);

        // Some tooltips (Origins recipe badges, info icons, etc.) may provide a single non-text component as the tooltip content.
        // In those cases, treating the first component as a "title" would skip it (because the content loop starts at index 1),
        // causing the tooltip to appear blank. So we only treat the first component as a title if it's actually a text component
        boolean treatFirstAsTitle = !components.isEmpty() && (components.get(0) instanceof ClientTextTooltip);
        int startIndex = treatFirstAsTitle ? 1 : 0;

        // Rendering the title first (along with the rating text if present)
        if (treatFirstAsTitle) {
            final ClientTooltipComponent titleComponent = components.get(0);
            if (titleComponent != null) {
                // If the icon is present, move the content to the side
                int extraX = 0;
                // Alignment to the center of the icon background (if present)
                int extraY = 0;
                // Extra alignment if there is a rating text present
                int titleAlignY = 0;
                int ratingAlignY = 0;
                if (hasIcon) {
                    extraX = Constants.getIconSize(context) + Constants.getIconTitleSeparation(context);
                    extraY = (Constants.getIconSize(context) / 2);
                    titleAlignY = hasRating ? titleComponent.getHeight() : (Constants.getIconSize(context) / 4);
                }
                else {
                    // Don't apply extra rating alignment when the icon is enabled
                    ratingAlignY = titleComponent.getHeight();
                }

                int titleAlignment = computeTitleAlignment(context, titleComponent, x + extraX);

                // Title text
                titleComponent.renderText(font, x + extraX + titleAlignment, y + extraY - titleAlignY, poseStack.last().pose(), graphics.bufferSource());

                // Rating text. If there is no rating but there is an icon present, the padding between the content and the title is the same
                if (hasRating) {
                    Component rating = TextUtils.getRatingText(context);

                    int ratingAlignment = computeRatingAlignment(context, rating, x + extraX);

                    context.getGraphics().drawString(font, TextUtils.getRatingText(context), x + extraX + ratingAlignment, y + extraY + ratingAlignY, 0xEDDE76, false);
                    y += ClientTooltipComponent.create(rating.getVisualOrderText()).getHeight();
                }
                else if (hasIcon) {
                    y += titleComponent.getHeight();
                }

                y += titleComponent.getHeight();
            }

            // Extra space after the icon
            if (hasIcon) {
                y += Constants.getIconTitleSeparation(context);
            }

            if (hasDividerLine && components.size() > 1) {
                if (hasIcon) {
                    y += Constants.getDividerLineFullPadding(context);
                }
                else {
                    y += Constants.getDividerLineFullPadding(context);
                }

            }

        }

        int contentStartY = y;

        // If there is a scrolling state active, enables the GL scissor
        if (TooltipScrollState.isIsActive()) {

            graphics.flush();

            Minecraft minecraft = Minecraft.getInstance();
            int guiScale = (int) minecraft.getWindow().getGuiScale();

            int scissorLeft = (int) position.x + paddingX;
            int scissorRight = (int) position.x + (int) context.getTooltipSize().x - paddingX;
            int scissorBottom = (int) position.y + (int) context.getTooltipSize().y - paddingY;

            int windowHeight = minecraft.getWindow().getHeight();
            int scaledLeft = scissorLeft * guiScale;
            int scaledTop = windowHeight - (scissorBottom * guiScale);
            int scaledWidth = (scissorRight - scissorLeft) * guiScale;
            int scaledHeight = (scissorBottom - contentStartY) * guiScale;

            GlStateManager._enableScissorTest();
            GlStateManager._scissorBox(scaledLeft, scaledTop, scaledWidth, scaledHeight);

            y -= TooltipScrollState.getScroll();
        }

        // Renders the content of the tooltip (scrollable or not)
        for (int i = startIndex; i < components.size(); i++) {
            ClientTooltipComponent component = components.get(i);

            // Renders the lines if they're available in the viewport
            if (!TooltipScrollState.isIsActive() ||
                    (y + component.getHeight() >= contentStartY &&
                            y <= (int)(position.y + context.getTooltipSize().y - paddingY))) {

                component.renderText(font, x, y, poseStack.last().pose(), graphics.bufferSource());

                // Some custom components rely on buffered text being flushed before their image pass, so when the scissor is disabled,
                // a flush may not happen otherwise
                graphics.flush();

                component.renderImage(font, x, y, graphics);
            }

            y += component.getHeight();
        }

        // Disables the GL scissor
        if (TooltipScrollState.isIsActive()) {
            graphics.flush();
            GlStateManager._disableScissorTest();
        }

    }

    private int computeTitleAlignment(TooltipContext context, ClientTooltipComponent component, int startX) {
        return switch (PositionUtils.getTitleTextAlignment(context)) {
            case "middle" -> {
                int tooltipSizeX = (int) context.getTooltipSize().x;
                int tooltipPositionX = (int) context.getTooltipPosition().x;

                int total = tooltipSizeX + tooltipPositionX;

                yield (total - startX - context.getPaddingX()) / 2 - component.getWidth(context.getFont()) / 2;
            }
            case "right" -> {
                int tooltipSizeX = (int) context.getTooltipSize().x;
                int tooltipPositionX = (int) context.getTooltipPosition().x;

                int total = tooltipSizeX + tooltipPositionX;

                yield (total - startX - context.getPaddingX()) - component.getWidth(context.getFont());
            }
            default -> 0;
        };

    }

    private int computeRatingAlignment(TooltipContext context, Component component, int startX) {
        return switch (PositionUtils.getRatingTextAlignment(context)) {
            case "middle" -> {
                int tooltipSizeX = (int) context.getTooltipSize().x;
                int tooltipPositionX = (int) context.getTooltipPosition().x;

                int total = tooltipSizeX + tooltipPositionX;

                yield (total - startX - context.getPaddingX()) / 2 - context.getFont().width(component) / 2;
            }
            case "right" -> {
                int tooltipSizeX = (int) context.getTooltipSize().x;
                int tooltipPositionX = (int) context.getTooltipPosition().x;

                int total = tooltipSizeX + tooltipPositionX;

                yield (total - startX - context.getPaddingX()) - context.getFont().width(component);
            }
            default -> 0;
        };

    }

}