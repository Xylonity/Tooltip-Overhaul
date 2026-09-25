package dev.xylonity.tooltipoverhaul.client.style.text;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.impl.TextLayer;
import dev.xylonity.tooltipoverhaul.client.layout.TooltipLayout;
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

        final List<ClientTooltipComponent> components = context.getComponents();

        Font font = context.getFont();
        final PoseStack poseStack = context.getPose();
        GuiGraphics graphics = context.getGraphics();

        final boolean hasIcon = TooltipLayout.hasHeaderIcon(context);
        final boolean hasDividerLine = context.hasDividerLine();
        final boolean hasRating = RenderUtils.hasRating(context);

        final int paddingX = context.getPaddingX();
        final int paddingY = context.getPaddingY();

        int x = (int) (position.x + paddingX);
        int y = (int) (position.y + paddingY + 1);

        // Some tooltips (Origins recipe badges, info icons, etc.) may provide a single non-text component as the tooltip content
        // In those cases, treating the first component as a "title" would skip it (because the content loop starts at index 1),
        // causing the tooltip to appear blank. So I only treat the first component as a title if it's actually a text component
        final boolean treatFirstAsTitle = !components.isEmpty() && (components.get(0) instanceof ClientTextTooltip);
        final int startIndex = treatFirstAsTitle ? 1 : 0;

        // Rendering the title first (along with the rating text if present)
        if (treatFirstAsTitle && context.getLayoutStyle() == TooltipLayout.Style.COMPACT) {
            final ClientTooltipComponent title = components.get(0);
            final int titleX = x + TooltipLayout.titleInset(context);
            final int titleY = y + (TooltipLayout.headerHeight(context) - title.getHeight()) / 2;
            title.renderText(font, titleX + computeTitleAlignment(context, title, titleX), titleY, poseStack.last().pose(), graphics.bufferSource());
            renderCompactFooter(context, position);
            y = (int) position.y + TooltipLayout.compactBodyTop(context);
        }
        else if (treatFirstAsTitle) {
            final ClientTooltipComponent titleComponent = components.get(0);
            if (titleComponent != null) {
                // If the icon is present, move the content to the side
                final int extraX = hasIcon ? TooltipLayout.titleInset(context) : 0;
                final int textBlockHeight = titleComponent.getHeight() + TooltipLayout.ratingHeight(context);

                // Title and rating are centered together next to the icon, whatever size it has
                final int titleY = hasIcon ? y + (TooltipLayout.headerHeight(context) - textBlockHeight) / 2 : y;

                // Title text
                titleComponent.renderText(font, x + extraX + computeTitleAlignment(context, titleComponent, x + extraX), titleY, poseStack.last().pose(), graphics.bufferSource());

                if (hasRating) {
                    final Component rating = TextUtils.getRatingText(context);
                    context.getGraphics().drawString(font, rating, x + extraX + computeRatingAlignment(context, rating, x + extraX), titleY + titleComponent.getHeight(), 0xEDDE76, false);
                }

                // The header already starts one pixel down, so the body lands right where it did
                y += hasIcon ? TooltipLayout.headerHeight(context) - 2 : textBlockHeight;
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

        final int contentStartY = y;
        final int contentBottom = (int) (position.y + context.getTooltipSize().y) - paddingY - TooltipLayout.footerHeight(context);

        // If there is a scrolling state active, enables the GL scissor
        if (TooltipScrollState.isIsActive()) {

            graphics.flush();

            final Minecraft minecraft = Minecraft.getInstance();
            final int guiScale = (int) minecraft.getWindow().getGuiScale();

            final int scissorLeft = x;
            final int scissorRight = (int) position.x + (int) context.getTooltipSize().x - paddingX;
            final int scissorBottom = contentBottom;

            final int windowHeight = minecraft.getWindow().getHeight();
            final int scaledLeft = scissorLeft * guiScale;
            final int scaledTop = windowHeight - (scissorBottom * guiScale);
            final int scaledWidth = (scissorRight - scissorLeft) * guiScale;
            final int scaledHeight = (scissorBottom - contentStartY) * guiScale;

            GlStateManager._enableScissorTest();
            GlStateManager._scissorBox(scaledLeft, scaledTop, scaledWidth, scaledHeight);

            y -= TooltipScrollState.getScroll();
        }

        // Renders the content of the tooltip (scrollable or not)
        for (int i = startIndex; i < components.size(); i++) {
            final ClientTooltipComponent component = components.get(i);

            // Renders the lines if they're available in the viewport
            if (!TooltipScrollState.isIsActive() ||
                    (y + component.getHeight() >= contentStartY &&
                            y <= contentBottom)) {

                // Some extra components (like celestisynth's weapons) may extract drawings out of the tooltip margins
                final boolean custom = !(component instanceof ClientTextTooltip);
                if (custom) {
                    graphics.flush();
                    poseStack.pushPose();
                    poseStack.translate(0, 0, LayerDepth.CUSTOM_COMPONENT.getZ() - getLayerDepth().getZ());
                }

                component.renderText(font, x, y, poseStack.last().pose(), graphics.bufferSource());

                // Some custom components rely on buffered text being flushed before their image pass, so when the scissor is disabled,
                // a flush may not happen otherwise
                graphics.flush();

                component.renderImage(font, x, y, graphics);

                if (custom) {
                    graphics.flush();
                    poseStack.popPose();
                }

            }

            y += component.getHeight();
        }

        // Disables the GL scissor
        if (TooltipScrollState.isIsActive()) {
            graphics.flush();
            GlStateManager._disableScissorTest();
        }

    }

    private void renderCompactFooter(TooltipContext context, Vec2 position) {
        if (TooltipLayout.footerHeight(context) == 0) {
            return;
        }

        final Font font = context.getFont();
        final GuiGraphics graphics = context.getGraphics();
        final int x = (int) (position.x + context.getPaddingX());
        final int right = (int) (position.x + context.getTooltipSize().x) - context.getPaddingX();
        int y = (int) (position.y + context.getTooltipSize().y) - context.getPaddingY() - TooltipLayout.footerTextHeight(context) + 2;
        int availableWidth = right - x;
        if (RenderUtils.hasRating(context)) {
            final Component rating = TextUtils.getRatingText(context);
            final int ratingX = right - font.width(rating);
            graphics.drawString(font, rating, ratingX, y, 0xEDDE76, false);
            availableWidth = ratingX - x - TooltipLayout.footerTextGap();
        }

        String name = context.getCompactModName();
        if (name.isEmpty() || availableWidth <= 0) {
            return;
        }

        if (font.width(name) > availableWidth) {
            final String ellipsis = "\u2026";
            final int ellipsisWidth = font.width(ellipsis);
            if (availableWidth < ellipsisWidth) {
                return;
            }

            name = font.plainSubstrByWidth(name, availableWidth - ellipsisWidth) + ellipsis;
        }

        final int color = ColorUtils.getCompactModNameColor(context);
        graphics.drawString(font, Component.literal(name), x, y, color, false);
    }

    private int computeTitleAlignment(TooltipContext context, ClientTooltipComponent component, int startX) {
        return computeAlignment(context, PositionUtils.getTitleTextAlignment(context), component.getWidth(context.getFont()), startX);
    }

    private int computeRatingAlignment(TooltipContext context, Component component, int startX) {
        return computeAlignment(context, PositionUtils.getRatingTextAlignment(context), context.getFont().width(component), startX);
    }

    private int computeAlignment(TooltipContext context, String alignment, int textWidth, int startX) {
        final int left = (int) context.getTooltipPosition().x + context.getPaddingX();
        final int right = (int) (context.getTooltipPosition().x + context.getTooltipSize().x) - context.getPaddingX();
        return switch (alignment) {
            case "middle" -> {
                if (PositionUtils.alignmentIgnoresIcon(context)) {
                    yield Math.max(0, (left + right) / 2 - textWidth / 2 - startX);
                }

                yield (right - startX) / 2 - textWidth / 2;
            }
            case "right" -> right - startX - textWidth;
            default -> 0;
        };

    }

}