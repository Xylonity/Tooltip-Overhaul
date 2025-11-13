package dev.xylonity.tooltipoverhaul.client.style.text;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xylonity.tooltipoverhaul.client.layer.impl.TextLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextUtils;
import dev.xylonity.tooltipoverhaul.client.util.TooltipScrollState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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

        // Rendering the title first (along with the rating text if present)
        ClientTooltipComponent titleComponent = components.get(0);
        if (titleComponent != null) {
            // If the icon is present, move the content to the side
            int extraX = 0;
            // Alignment to the center of the icon background (if present)
            int extraY = 0;
            // Extra alignment if there is a rating text present
            int titleAlignY = 0;
            int ratingAlignY = 0;
            if (hasIcon) {
                extraX = Constants.ICON_SIZE + Constants.SEPARATION_TITLE_ICON;
                extraY = (Constants.ICON_SIZE / 2);
                titleAlignY = hasRating ? titleComponent.getHeight() : (Constants.ICON_SIZE / 4);
            }
            else {
                // Don't apply extra rating alignment when the icon is enabled
                ratingAlignY = titleComponent.getHeight();
            }

            // Title text
            titleComponent.renderText(font, x + extraX, y + extraY - titleAlignY, poseStack.last().pose(), graphics.bufferSource());

            // Rating text. If there is no rating but there is an icon present, the padding between the content and the title is the same
            if (hasRating) {
                Component rating = TextUtils.computeRatingText(context);
                context.getGraphics().drawString(font, TextUtils.computeRatingText(context), x + extraX, y + extraY + ratingAlignY, 0xEDDE76, false);
                y += ClientTooltipComponent.create(rating.getVisualOrderText()).getHeight();
            }
            else if (hasIcon) {
                y += titleComponent.getHeight();
            }

            y += titleComponent.getHeight();
        }

        // Extra space after the icon
        if (hasIcon) {
            y += Constants.SEPARATION_TITLE_ICON;
        }

        if (hasDividerLine && components.size() > 1) {
            if (hasIcon) {
                y += Constants.DIVIDER_LINE_FULL_PADDING;
            }
            else {

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
        for (int i = 1; i < components.size(); i++) {
            ClientTooltipComponent component = components.get(i);

            // Renders the lines if they're available in the viewport
            if (!TooltipScrollState.isIsActive() ||
                    (y + component.getHeight() >= contentStartY &&
                            y <= (int)(position.y + context.getTooltipSize().y - paddingY))) {

                component.renderText(font, x, y, poseStack.last().pose(), graphics.bufferSource());
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

}